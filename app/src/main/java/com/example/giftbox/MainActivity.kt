package com.example.giftbox

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.giftbox.ui.theme.GiftBoxTheme
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.giftbox.model.Gift
import com.example.giftbox.ui.add.AddGifticon
import com.example.giftbox.ui.detail.DetailScreen
import com.example.giftbox.ui.home.HomeScreen
import com.example.giftbox.ui.home.HomeViewModel
import com.example.giftbox.ui.list.ListScreen
import com.example.giftbox.ui.login.LoginScreen
import com.example.giftbox.ui.login.LoginViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GiftBoxTheme {
                if (!loginViewModel.isLoginState.value) {
                    LoginScreen(
                        modifier = Modifier,
                        mainViewModel = loginViewModel
                    )
                } else {
                    BottomNavigationBar(onLogout = {loginViewModel.logout()})
                }

            }
        }
    }
}

@Composable
fun BottomNavigationBar(onLogout: () -> Unit) {
    val homeViewModel = viewModel<HomeViewModel>()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination
    val bottomScreens = listOf(
        Screen.List,
        Screen.Home,
        Screen.Setting
    )
    val showBottomBar = navController
        .currentBackStackEntryAsState().value?.destination?.route in bottomScreens.map { it.route }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomScreens.forEach { bottomNavigationItem ->
                        NavigationBarItem(
                            selected = currentRoute?.hierarchy?.any { it.route == bottomNavigationItem.route } == true,
                            alwaysShowLabel = false,
                            onClick = {
                                navController.navigate(route = bottomNavigationItem.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = bottomNavigationItem.icon,
                                    contentDescription = stringResource(id = bottomNavigationItem.label)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(id = bottomNavigationItem.label)
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(viewModel = homeViewModel) {
                    navController.navigate(route = Screen.Add.route)
                }
            }
            composable(Screen.List.route) { ListScreen { gift ->
                val gifJson = Uri.encode(Gson().toJson(gift))
                navController.navigate(route = "${Screen.Detail.route}/${gifJson}")
            } }
            composable(Screen.Setting.route) { SettingScreen(onLogout) }
            composable(
                Screen.Add.route,
                enterTransition = {
                    fadeIn(animationSpec = tween(300, easing = LinearEasing)) +
                            slideIntoContainer(
                                animationSpec = tween(300, easing = EaseIn),
                                towards = AnimatedContentTransitionScope.SlideDirection.Start
                            )
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(300, easing = LinearEasing)) +
                            slideOutOfContainer(
                                animationSpec = tween(300, easing = EaseOut),
                                towards = AnimatedContentTransitionScope.SlideDirection.End
                            )
                }
            ) {
                AddGifticon(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(
                route = "${Screen.Detail.route}/{gift}",
                arguments = listOf(
                    navArgument("gift") {
                        type = NavType.StringType
                    }
                )
            ) { navBackStackEntry ->
                val giftJson = navBackStackEntry.arguments?.getString("gift")
                val gift = Gson().fromJson(giftJson, Gift::class.java)
                DetailScreen(gift) { navController.popBackStack() }
            }
        }
    }
}

@Composable
fun SettingScreen(onLogout: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.setting),
            )
            Button(
                onClick = {
                    onLogout()
                }
            ) {
                Text(text = "로그아웃")
            }
        }

    }
}

sealed class Screen(val route: String, val icon: ImageVector, @StringRes val label: Int) {
    data object Home : Screen("home", Icons.Filled.Home, R.string.home)
    data object List : Screen("list", Icons.AutoMirrored.Filled.List, R.string.list)
    data object Setting : Screen("setting", Icons.Filled.Settings, R.string.setting)

    data object Add : Screen("add", Icons.Filled.Add, R.string.setting)
    data object Detail : Screen("detail", Icons.Filled.Search, R.string.detail)
}