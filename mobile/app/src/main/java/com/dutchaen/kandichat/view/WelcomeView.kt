package com.dutchaen.kandichat.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dutchaen.kandichat.MainActivity
import com.dutchaen.kandichat.Screen
import com.dutchaen.kandichat.ui.theme.fontFamily
import kotlinx.coroutines.CoroutineExceptionHandler
import java.net.URLEncoder


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun WelcomeView(
    navController: NavController
) {

    var alias by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current;

    val handler = CoroutineExceptionHandler { _, throwable ->
        Log.e("WelcomeView", "there was problem: " , throwable)
    }

    CompositionLocalProvider(
        LocalFontFamilyResolver provides createFontFamilyResolver(LocalContext.current, handler)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(20.dp)

        ) {


            Text("KandiChat", color = MainActivity.PRIMARY_COLOR, fontWeight = FontWeight.SemiBold, fontFamily = fontFamily)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(0.dp, 250.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "haiiiiiiiii! select an alias and start chatting!",
                    color = Color.White,
                    fontWeight = FontWeight.Light,
                    fontFamily = fontFamily
                )
                
                Spacer(modifier = Modifier.height(20.dp))

                TextField(
                    value = alias,
                    onValueChange = { alias = it },
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = MainActivity.PRIMARY_COLOR,
                        cursorColor = MainActivity.PRIMARY_COLOR,
                        focusedLabelColor = MainActivity.PRIMARY_COLOR,
                        focusedIndicatorColor = MainActivity.PRIMARY_COLOR,
                        unfocusedLabelColor = MainActivity.PRIMARY_COLOR,
                        containerColor = Color.Black,
                    ),
                    shape = RoundedCornerShape(3.dp),
                    maxLines = 1,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = fontFamily,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {

                        keyboardController?.hide();
                        navController.popBackStack();
                        val encodedAlias = URLEncoder.encode(alias, "utf-8");
                        navController.navigate(Screen.Loading.route + "/${encodedAlias}")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MainActivity.PRIMARY_COLOR
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(0.80f),
                    enabled = alias.isNotEmpty()
                ) {
                    Text(
                        text = "Chat Now!",
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = fontFamily
                    )
                }


            }
        }
    }
}