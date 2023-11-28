package main

import (
	"crypto/md5"
	"encoding/hex"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"math/rand"
	"net/http"
	"sync"
	"time"

	"golang.org/x/net/websocket"
)

type Server struct {
	conns     map[*websocket.Conn]bool
	queue0    *sync.Map
	queue_mtx *sync.Mutex
	users     map[string]*User
	chatrooms map[string]*Chatroom
	mtx       *sync.Mutex
}

type Chatroom struct {
	Id     string
	People []*User
}

func (chatroom *Chatroom) GetOtherUser(user *User) *User {
	for _, person := range chatroom.People {
		if person.Id != user.Id {
			return person
		}
	}
	return nil
}

type User struct {
	Id    string `json:"id"`
	Alias string `json:"alias"`
	conn  *websocket.Conn
	mtx   *sync.Mutex
}

func NewServer() *Server {
	return &Server{
		conns:     make(map[*websocket.Conn]bool),
		queue0:    &sync.Map{},
		queue_mtx: &sync.Mutex{},
		users:     make(map[string]*User),
		chatrooms: make(map[string]*Chatroom),
		mtx:       &sync.Mutex{},
	}
}

func (server *Server) PrintQueue() {
	k := make(map[interface{}]interface{})
	server.queue0.Range(func(key, value any) bool {
		k[key] = value
		return true
	})

	fmt.Println(k)
}

func NewUser(alias string, ws *websocket.Conn) *User {
	return &User{
		Id:    CreateId(),
		Alias: alias,
		conn:  ws,
		mtx:   &sync.Mutex{},
	}
}

func (user *User) ToContact() *Contact {
	return &Contact{
		Alias: user.Alias,
	}
}

func CreateId() string {
	writer := md5.New()

	k := time.Now().UnixNano() & rand.Int63()
	s := "SparkleSpecialist"

	v := fmt.Sprintf("%s-%d", s, k)
	writer.Write([]byte(v))

	return hex.EncodeToString(writer.Sum(nil))
}

func (server *Server) HandleWS(ws *websocket.Conn) {

	fmt.Println("Connection:", ws.RemoteAddr())
	server.mtx.Lock()
	server.conns[ws] = true

	server.mtx.Unlock()
	server.ReadLoop(ws)

}

func (server *Server) SendError(ws *websocket.Conn, message string) {

	error_event := ErrorEvent{
		Event: "error",
		Error: message,
	}
	buffer, _ := json.Marshal(&error_event)
	ws.Write(buffer)

}

func (server *Server) HandleDisconnect(ws *websocket.Conn) {

	user_id := ""
	var u *User = nil

	for id, user := range server.users {
		if user.conn == ws {
			user_id = id
			u = user
			break
		}
	}

	server.queue0.Range(func(key, value any) bool {
		user := key.(*User)
		if user.conn == ws {
			u = user
			return false
		}
		return true

	})

	if u != nil {
		server.queue0.Delete(u)

		for id, chatroom := range server.chatrooms {
			for _, person := range chatroom.People {
				if person.Id == u.Id {
					other_user := chatroom.GetOtherUser(person)

					de := DisconnectedEvent{
						Event: "disconnected",
					}

					b, _ := json.Marshal(&de)

					person.conn.Write(b)
					other_user.conn.Write(b)

					delete(server.chatrooms, id)
					break
				}
			}
		}
	}

	if len(user_id) > 0 {
		delete(server.users, user_id)
	}

	if err := ws.Close(); err != nil {
		log.Println(err)
	}

	k := make(map[interface{}]interface{})
	server.queue0.Range(func(key, value any) bool {
		k[key] = value
		return true
	})
	fmt.Println(k)

}

func (server *Server) ReadLoop(ws *websocket.Conn) {
	buf := make([]byte, 4096)
	for {

		n, err := ws.Read(buf)
		if err != nil {

			switch err {
			case io.EOF:
				fmt.Println("EOF")
				break
			default:
				fmt.Println("Disconnected")
				server.HandleDisconnect(ws)
				log.Printf("%#v\r\n", err)
				break
			}

		}
		msg := buf[:n]
		if len(msg) == 0 {
			// disconnect
			fmt.Println("Disconnected")
			server.HandleDisconnect(ws)
			break

		}

		var json_object map[string]interface{}
		if err := json.Unmarshal(msg, &json_object); err != nil {
			server.SendError(ws, "cannot parse payload to json")
			log.Println(err)
			continue
		}

		fmt.Println(json_object)

		event, exists := json_object["event"]
		if !exists {
			server.SendError(ws, "invalid event type")
			log.Println("event does not exist", string(msg))
			continue
		}

		switch event.(string) {
		case "create":
			create_event := CreateEvent{}
			if err := json.Unmarshal(msg, &create_event); err != nil {
				server.SendError(ws, "cannot parse payload to json")
				log.Println(err)
				continue
			}

			user := NewUser(create_event.Alias, ws)
			response := &CreateUserOKRespond{
				OK:   true,
				User: user,
			}

			server.users[user.Id] = user

			buffer, err := json.Marshal(response)
			if err != nil {
				server.SendError(ws, "cannot parse payload to json")
				log.Println(err)
				continue
			}
			ws.Write(buffer)

		case "join":
			join_event := JoinEvent{}
			if err := json.Unmarshal(msg, &join_event); err != nil {
				log.Println(err)
				continue
			}

			user, exists := server.users[join_event.Id]
			if !exists {
				log.Println("not exists")
				continue
			}

			server.queue_mtx.Lock()
			server.PrintQueue()

			if _, exists := server.queue0.Load(user); exists {
				server.queue_mtx.Unlock()
				continue
			}

			server.queue0.Store(user, true)

			server.queue_mtx.Unlock()
			//server.in_queue <- user

			go func() {

				found := false
				for !found {

					server.queue0.Range(func(key, value any) bool {

						queued := key.(*User)
						if queued.Id == user.Id {
							time.Sleep(time.Millisecond * 100)
							return true
						}

						if !user.mtx.TryLock() {
							return true
						}
						if !queued.mtx.TryLock() {
							user.mtx.Unlock()
							return true
						}

						fmt.Println("cmp : (queued, user) OK:", queued.Id, user.Id)

						defer user.mtx.Unlock()
						defer queued.mtx.Unlock()

						chatroom := &Chatroom{
							Id: CreateId(),
							People: []*User{
								user, queued,
							},
						}
						server.chatrooms[chatroom.Id] = chatroom

						// for the us
						c0 := ConnectedRespond{
							Connected:  true,
							ChatroomId: chatroom.Id,
							Contact:    *user.ToContact(),
						}

						// for them
						c1 := ConnectedRespond{
							Connected:  true,
							ChatroomId: chatroom.Id,
							Contact:    *queued.ToContact(),
						}

						b, _ := json.Marshal(&c1)
						b0, _ := json.Marshal(&c0)

						// send the data
						user.conn.Write(b)
						queued.conn.Write(b0)

						defer server.queue0.Delete(user)
						defer server.queue0.Delete(queued)

						found = true
						return false
					})

				}
			}()

		case "message_send":
			message_send_event := MessageSendEvent{}
			if err := json.Unmarshal(msg, &message_send_event); err != nil {
				log.Println(err)
				continue
			}

			user, exists := server.users[message_send_event.Id]
			if !exists {
				log.Println("user not exists")
				continue
			}

			chatroom, exists := server.chatrooms[message_send_event.ChatroomId]
			if !exists {
				log.Println("chatroom not exists")
				continue
			}

			other_user := chatroom.GetOtherUser(user)
			message_received_event := MessageReceivedEvent{
				Event:   "message_receive",
				Message: message_send_event.Message,
			}

			b, _ := json.Marshal(&message_received_event)
			if _, err := other_user.conn.Write(b); err != nil {
				// bad
				// if we cant send, disconnect the chatroom
			}

		case "disconnect":
			disconnect_event := DisconnectEvent{}

			if err := json.Unmarshal(msg, &disconnect_event); err != nil {
				log.Println(err)
				continue
			}

			user, exists := server.users[disconnect_event.Id]
			if !exists {
				log.Println("user not exists")
				continue
			}

			chatroom, exists := server.chatrooms[disconnect_event.ChatroomId]
			if !exists {
				log.Println("chatroom not exists")
				continue
			}

			other_user := chatroom.GetOtherUser(user)

			de := DisconnectedEvent{
				Event: "disconnected",
			}

			b, _ := json.Marshal(&de)

			user.conn.Write(b)
			other_user.conn.Write(b)

			delete(server.chatrooms, disconnect_event.ChatroomId)

			server.PrintQueue()
			fmt.Println(server.users)

		}

	}
}

func main() {
	log.SetFlags(log.Flags() | log.Llongfile)
	server := NewServer()

	http.Handle("/chat", websocket.Handler(server.HandleWS))
	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		w.Write([]byte("Hi"))
	})

	fmt.Println("Starting server...")
	http.ListenAndServe(":3000", nil)
}
