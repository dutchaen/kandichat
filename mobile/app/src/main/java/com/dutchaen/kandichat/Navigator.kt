package com.dutchaen.kandichat

import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dutchaen.kandichat.view.ChatScreen
import com.dutchaen.kandichat.view.LoadingView
import com.dutchaen.kandichat.view.WelcomeView
import com.dutchaen.kandichat.viewmodels.NavigatorViewModel
import java.net.URLDecoder


@Composable
fun Navigator(
    window: Window
) {

    val navController = rememberNavController();
    val viewModel = viewModel<NavigatorViewModel>();

    val websocket = viewModel.websocket.collectAsState();
    val listener = viewModel.listener.collectAsState();
    val user = viewModel.user.collectAsState();

    
    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route
    ) {
        composable(Screen.Welcome.route) {
            WelcomeView(
                navController = navController
            )
        }

        composable(Screen.Loading.route + "/{alias}") {
            val alias = URLDecoder.decode(
                it.arguments?.getString("alias") ?: "Anonymous",
                "utf-8"
            );

            viewModel.join(alias);

            user.value?.let {
                LoadingView(
                    alias = alias,
                    navController = navController,
                    websocket.value,
                    listener.value,
                    it
                )
            }
        }

        composable(Screen.Chat.route + "/{id}/{sender}/{chatroom_id}") {
            val id = it.arguments?.getString("id") ?: "";

            val sender = URLDecoder.decode(
                it.arguments?.getString("sender") ?: "Anonymous",
                "utf-8"
            );

            val chatroomId = it.arguments?.getString("chatroom_id") ?: "";


            ChatScreen(
                id,
                chatroomId,
                sender,
                websocket.value,
                listener.value,
                window,
                navController,
                user.value!!
            )
        }
    }
}