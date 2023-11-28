package com.dutchaen.kandichat.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dutchaen.kandichat.MainActivity
import com.dutchaen.kandichat.WebSocketReader
import com.dutchaen.kandichat.models.CreateEvent
import com.dutchaen.kandichat.models.CreatedUserOkResponse
import com.dutchaen.kandichat.models.User
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket

class NavigatorViewModel: ViewModel() {

    private var _httpClient: MutableStateFlow<OkHttpClient?> = MutableStateFlow(null);
    val httpClient = _httpClient.asStateFlow();

    private var _websocket: MutableStateFlow<WebSocket?> = MutableStateFlow(null);
    val websocket = _websocket.asStateFlow();

    private var _listener: MutableStateFlow<WebSocketReader?> = MutableStateFlow(null);
    val listener = _listener.asStateFlow();

    private var _currentAlias: MutableStateFlow<String?> = MutableStateFlow(null);
    val currentAlias = _currentAlias.asStateFlow();

    private var _user: MutableStateFlow<User?> = MutableStateFlow(null);
    val user = _user.asStateFlow();

    private var _updating = MutableStateFlow(false);
    val updating = _updating.asStateFlow();


    fun join(alias: String) {

        viewModelScope.launch {

            if (_updating.value) {
                return@launch
            }

            _updating.value = true;

            if (_httpClient.value == null || _websocket.value == null || alias  != _currentAlias.value) {
                val httpClient = OkHttpClient();

                _httpClient.value = httpClient;

                val request: Request = Request
                    .Builder()
                    .url("ws://${MainActivity.HOST}/chat")
                    .header("Origin", "http://${MainActivity.HOST}")
                    .build();

                val listener = WebSocketReader();
                val ws = httpClient.newWebSocket(request, listener);


                delay(2000)

                val gson = Gson();

                val createEvent = CreateEvent(
                    alias = alias
                )
                val eventRequest = gson.toJson(createEvent);
                val ok = ws.send(eventRequest);


                val response = listener.message_queue.receive();
                val y = gson.fromJson(response, CreatedUserOkResponse::class.java)
                _user.emit(y.user);
                _currentAlias.emit(y.user.alias);
                _listener.emit(listener);
                _websocket.emit(ws);
            }

            _updating.value = false;

        }

    }

}