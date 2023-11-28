package com.dutchaen.kandichat.view

import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.dutchaen.kandichat.MainActivity
import com.dutchaen.kandichat.Screen
import com.dutchaen.kandichat.WebSocketReader
import com.dutchaen.kandichat.models.User
import com.dutchaen.kandichat.ui.theme.fontFamily
import com.dutchaen.kandichat.viewmodels.ChatScreenViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import okhttp3.WebSocket


data class Message (
    val text: String,
    val isMine: Boolean,
)



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    id: String,
    chatroom_id: String,
    sender: String,
    webSocket: WebSocket?,
    reader: WebSocketReader?,
    window: Window,
    navController: NavController,
    me: User
) {

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val chatScreenViewModel = viewModel<ChatScreenViewModel>(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChatScreenViewModel(
                    id,
                    chatroom_id,
                    webSocket,
                    reader,
                    listState,
                    scope
                ) as T
            }
        }
    )

    LaunchedEffect(Unit) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    var messageText = remember {mutableStateOf("")};
    var text by messageText;

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val messages by chatScreenViewModel.messages.collectAsState();
    val disconnected by chatScreenViewModel.disconnected.collectAsState();


    val handler = CoroutineExceptionHandler { _, throwable ->
        Log.e("ChatScreen", "there was problem: " , throwable)
    }


    CompositionLocalProvider(
        LocalFontFamilyResolver provides createFontFamilyResolver(LocalContext.current, handler)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null
                            )
                            
                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = if (disconnected) "Disconnected" else sender,
                                fontFamily = fontFamily,
                                color = MainActivity.PRIMARY_COLOR,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }

                    },
                    actions = {

                        if (disconnected) {
                            IconButton(
                                onClick = {
                                    navController.popBackStack()
                                    navController.navigate(Screen.Loading.route + "/${me.alias}")
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Refresh,
                                    contentDescription = null
                                )
                            }
                        }
                        else {
                            IconButton(
                                onClick = {
                                    chatScreenViewModel.disconnect()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = null
                                )
                            }
                        }

                    },
                    colors = TopAppBarDefaults.mediumTopAppBarColors(
                        containerColor = Color.Black
                    ),
                    scrollBehavior = scrollBehavior
                )
            },
            bottomBar = {

                if (disconnected) {
                    DisconnectedBar()
                }
                else {
                    MessageBottomBar(
                        messageText = messageText,
                        onSendMessage = {
                            chatScreenViewModel.sendMessage(text);
                            text = "";
                        }
                    )
                }



            }
        ) { padding ->
            Box (
                modifier = Modifier
                    .background(Color.Black)
                    .padding(padding),
            ) {
                LazyColumn(
                    modifier = Modifier
                        .background(Color.Black)
                        .fillMaxSize()
                        .padding(horizontal = 5.dp),
                    state = listState,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    items(messages) {
                        MessageBubble(
                            text = it.text,
                            isMine = it.isMine,
                        )
                        Spacer(modifier = Modifier.height(7.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageBubble(
    text: String,
    isMine: Boolean,
) {
    val color = if (isMine)
        Color(0, 0xFF, 0xC3)
    else
        Color.LightGray;

    Column(
        modifier = Modifier.fillMaxWidth(.95f),
        horizontalAlignment =  if (isMine) Alignment.End else  Alignment.Start
    ) {
        Row {
            Card(
                onClick = {},
                modifier = Modifier.widthIn(0.dp, 300.dp),
                colors = CardDefaults.cardColors(
                    containerColor = color
                )
            ) {
                Text(
                    text = text,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Light,
                    textAlign = if (isMine) TextAlign.Right else TextAlign.Left,
                    color = Color.Black,
                    overflow = TextOverflow.Visible,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageBottomBar(
    messageText: MutableState<String>,
    onSendMessage: () -> Unit
) {

    var text by messageText;

    Box(
        modifier = Modifier
            .height(80.dp)
            .fillMaxWidth()
            .background(Color.Black)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth()
                .background(Color.Black),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = text,
                onValueChange = { text = it },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = Color.White,
                    cursorColor = MainActivity.PRIMARY_COLOR,
                    focusedLabelColor = MainActivity.PRIMARY_COLOR,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    unfocusedLabelColor = MainActivity.PRIMARY_COLOR,
                    containerColor = Color.DarkGray,
                ),
                shape = RoundedCornerShape(40.dp),
                maxLines = 4,
                textStyle = TextStyle(
                    fontFamily = fontFamily,

                ),
                placeholder = {
                    Text(
                        "Say something...",
                        fontFamily = fontFamily,
                        textAlign = TextAlign.Center,
                    )
                },
                modifier = Modifier.fillMaxWidth(.85f)
            )

            Spacer(modifier = Modifier.width(5.dp))

            IconButton(
                onClick = onSendMessage,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MainActivity.PRIMARY_COLOR
                ),
                modifier = Modifier.size(50.dp),
                enabled = text.isNotEmpty(),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Send,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun DisconnectedBar() {
    Box(
        modifier = Modifier
            .height(80.dp)
            .fillMaxWidth()
            .background(MainActivity.PRIMARY_COLOR)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "You've been disconnected.",
            fontFamily = fontFamily,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Center,
            color = Color.Black,
            overflow = TextOverflow.Visible,
            modifier = Modifier.padding(8.dp)
        )
    }
}