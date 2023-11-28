package main

type CreateUserOKRespond struct {
	OK   bool  `json:"ok"`
	User *User `json:"user"`
}

type ConnectedRespond struct {
	Connected  bool    `json:"connected"`
	ChatroomId string  `json:"chatroom_id"`
	Contact    Contact `json:"contact"`
}

type Contact struct {
	Alias string `json:"alias"`
}
