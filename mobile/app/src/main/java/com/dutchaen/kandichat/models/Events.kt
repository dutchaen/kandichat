package com.dutchaen.kandichat.models

import com.google.gson.Gson

interface Event {
    val event: String
}

data class ErrorEvent(
    override val event: String = "error",
    val error: String
) : Event

data class CreateEvent(
    override val event: String = "create",
    val alias: String = "Anonymous"
) : Event

data class JoinEvent(
    override val event: String = "join",
    val id: String
) : Event

data class MessageSendEvent(
    val id: String,
    override val event: String = "message_send",
    val message: String,
    val chatroom_id: String,
) : Event

data class MessageReceivedEvent(
    override val event: String = "message_receive",
    val message: String,
) : Event

data class DisconnectEvent(
    val id: String,
    override val event: String = "disconnect",
    val chatroom_id: String,
) : Event

fun Event.asJsonPayload(): String {
    val gson = Gson()
    return gson.toJson(this);
}