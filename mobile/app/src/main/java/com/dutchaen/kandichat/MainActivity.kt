package com.dutchaen.kandichat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.dutchaen.kandichat.ui.theme.KandichatTheme

class MainActivity : ComponentActivity() {

    companion object {
        val PRIMARY_COLOR = Color(0, 0xFF, 0xC3)
        const val HOST = ""
        //val PRIMARY_COLOR = Color.hsl(183f, .28f, .7f)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setContent {
            KandichatTheme {
                // A surface container using the 'background' color from the theme
                Navigator(window)
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KandichatTheme {
        Greeting("Android")
    }
}