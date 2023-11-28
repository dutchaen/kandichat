package main

type ErrorEvent struct {
	Event string `json:"event"`
	Error string `json:"error"`
}

type CreateEvent struct {
	Event string `json:"event"`
	Alias string `json:"alias"`
}

type JoinEvent struct {
	Event string `json:"event"`
	Id    string `json:"id"`
}

type DisconnectEvent struct {
	Id         string `json:"id"`
	Event      string `json:"event"`
	ChatroomId string `json:"chatroom_id"`
}

type DisconnectedEvent struct {
	Event string `json:"event"`
}

type MessageSendEvent struct {
	Id         string `json:"id"`
	Event      string `json:"event"`
	Message    string `json:"message"`
	ChatroomId string `json:"chatroom_id"`
}

type MessageReceivedEvent struct {
	Event   string `json:"event"`
	Message string `json:"message"`
}
