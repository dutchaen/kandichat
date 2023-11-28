package com.dutchaen.kandichat.view

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.dutchaen.kandichat.MainActivity
import com.dutchaen.kandichat.Screen
import com.dutchaen.kandichat.WebSocketReader
import com.dutchaen.kandichat.composables.BackHandler
import com.dutchaen.kandichat.models.User
import com.dutchaen.kandichat.ui.theme.fontFamily
import com.dutchaen.kandichat.viewmodels.LoadingViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import okhttp3.WebSocket

@Composable
fun LoadingView(
    alias: String,
    navController: NavController,
    websocket: WebSocket?,
    reader: WebSocketReader?,
    user: User,
) {
    val strokeWidth = 3.dp

    val handler = CoroutineExceptionHandler { _, throwable ->
        Log.e("LoadingView", "there was problem: " , throwable)
    }

    //val scope  = rememberCoroutineScope();

    val loadingViewModel = viewModel<LoadingViewModel>(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LoadingViewModel(
                    navController,
                    websocket,
                    reader,
                    user
                ) as T
            }
        }
    )

    // throw into viewmodel
    //var loadingText by remember { mutableStateOf("Waiting for someone to talk to...") }

    val loaded by loadingViewModel.loaded.collectAsState();

    loadingViewModel.load();

    /*

     LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            delay(7000) // some websocket waiting stuff
            withContext(Dispatchers.Main) {
                loadingText = "Connecting..."
            }

            delay(2000)
            withContext(Dispatchers.Main) {
                navController.navigate(Screen.Chat.route + "/1/Alexa/1")
            }
        }
    }

     */



    BackHandler {
        navController.popBackStack();
        navController.navigate(Screen.Welcome.route)
    }

    CompositionLocalProvider(
        LocalFontFamilyResolver provides createFontFamilyResolver(LocalContext.current, handler)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {


            CircularProgressIndicator(
                modifier = Modifier
                    .size(85.dp)
                    .drawBehind {
                        drawCircle(
                            MainActivity.PRIMARY_COLOR,
                            radius = size.width / 2 - strokeWidth.toPx() / 2,
                            style = Stroke(strokeWidth.toPx())
                        )
                    },
                color = Color.LightGray,
                strokeWidth = strokeWidth
            )
            
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (loaded) "Connecting..." else "Waiting for someone to talk to...",
                fontWeight = FontWeight.Light,
                fontFamily = fontFamily,
                color = MainActivity.PRIMARY_COLOR
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    loadingViewModel.cancel();
                    navController.popBackStack();
                    navController.navigate(Screen.Welcome.route)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier.fillMaxWidth(0.40f),
                enabled = alias.isNotEmpty(),
                border = BorderStroke(2.dp, Color.DarkGray)
            ) {
                Text(
                    text = "Cancel",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = fontFamily
                )
            }
        }


    }


}