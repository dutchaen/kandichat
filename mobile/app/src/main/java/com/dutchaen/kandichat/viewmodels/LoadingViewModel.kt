package com.dutchaen.kandichat.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.dutchaen.kandichat.Screen
import com.dutchaen.kandichat.WebSocketReader
import com.dutchaen.kandichat.models.ConnectedToChatroomResponse
import com.dutchaen.kandichat.models.JoinEvent
import com.dutchaen.kandichat.models.User
import com.dutchaen.kandichat.models.asJsonPayload
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.WebSocket

class LoadingViewModel(
    val navController: NavController,
    val webSocket: WebSocket?,
    val reader: WebSocketReader?,
    val user: User
): ViewModel() {

    private var _loaded = MutableStateFlow(false);
    val loaded = _loaded.asStateFlow();

    private var loading = false;

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            if (loading) {
                return@launch
            }

            loading = true;

            withContext(Dispatchers.IO) {

                val payload = JoinEvent(
                    id = user!!.id
                ).asJsonPayload()

                webSocket?.send(payload);

                val msg = reader?.message_queue?.receive();
                val gson = Gson()

                val response = gson.fromJson(msg, ConnectedToChatroomResponse::class.java);

                _loaded.value = response.connected;

                delay(3000)

                withContext(Dispatchers.Main) {
                    navController.popBackStack();
                    navController.navigate(Screen.Chat.route + "/${user!!.id}/${response.contact.alias}/${response.chatroom_id}")
                }

            }

        }
    }

    fun cancel() {
        viewModelScope.launch {
            webSocket?.close(1000, "canceled")
        }

    }

}