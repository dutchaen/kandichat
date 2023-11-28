package com.dutchaen.kandichat.viewmodels

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dutchaen.kandichat.WebSocketReader
import com.dutchaen.kandichat.models.DisconnectEvent
import com.dutchaen.kandichat.models.MessageReceivedEvent
import com.dutchaen.kandichat.models.MessageSendEvent
import com.dutchaen.kandichat.models.asJsonPayload
import com.dutchaen.kandichat.view.Message
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.WebSocket

class ChatScreenViewModel(
    val id: String,
    val chatroom_id: String,
    val websocket: WebSocket?,
    val reader: WebSocketReader?,
    val lazyListState: LazyListState,
    val scope: CoroutineScope

): ViewModel() {

    private var _messages = MutableStateFlow(mutableListOf<Message>());
    val messages = _messages.asStateFlow();

    private var _disconnected = MutableStateFlow(false);
    val disconnected = _disconnected.asStateFlow();


    init {
        poll()
    }

    private fun poll() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val gson = Gson()

                while (true) {

                    if (reader != null) {
                        val eventText = reader.message_queue.receive();

                        val element = JsonParser.parseString(eventText);


                        if (element.asJsonObject["event"] == null) {
                            continue
                        }

                        val eventType = element.asJsonObject["event"]
                            .asString;

                        when (eventType) {
                            "disconnected" -> {
                                _disconnected.value = true;
                                break;
                            }
                            "message_receive" -> {

                                val recv_event = gson.fromJson(eventText, MessageReceivedEvent::class.java);

                                addMessageToView(
                                    Message(
                                        recv_event.message,
                                        isMine = false
                                    )
                                );
                            }
                        }
                    }

                }
            }
        }
    }


    fun sendMessage(text: String) {
        viewModelScope.launch {

            val payload = MessageSendEvent(
                id = id,
                message = text,
                chatroom_id = chatroom_id
            ).asJsonPayload()


            websocket?.send(payload)
            addMessageToView(
                Message(
                    text = text,
                    isMine = true
                )
            )
        }
    }

    fun setSenderMessage(text: String) {
        viewModelScope.launch {

            addMessageToView(
                Message(
                    text = text,
                    isMine = false
                )
            )
        }
    }

    fun addMessageToView(msg: Message) {
        viewModelScope.launch {
            val  x = _messages.value.toMutableList();
            x.add(msg);
            _messages.emit(x);

            scope.launch {
                if (!lazyListState.canScrollForward) {
                    lazyListState.animateScrollToItem(_messages.value.lastIndex)
                }
            }


        }
    }

    fun disconnect() {
        viewModelScope.launch {

            val payload = DisconnectEvent(
                id = id,
                chatroom_id = chatroom_id
            ).asJsonPayload();

            websocket?.send(payload)
        }
    }


}