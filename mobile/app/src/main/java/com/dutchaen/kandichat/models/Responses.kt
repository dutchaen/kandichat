package com.dutchaen.kandichat.models

data class User (
    val id: String,
    val alias: String,
)

data class CreatedUserOkResponse(
    val ok: Boolean,
    val user: User
)

data class ConnectedToChatroomResponse(
    val connected: Boolean,
    val chatroom_id: String,
    val contact: Contact
)

data class Contact (
    val alias: String
)