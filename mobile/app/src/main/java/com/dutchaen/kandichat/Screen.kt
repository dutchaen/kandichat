package com.dutchaen.kandichat

sealed class Screen(val route: String) {
    object Welcome : Screen("Welcome")
    object Loading : Screen("Loading")
    object Chat : Screen("Chat")
}
