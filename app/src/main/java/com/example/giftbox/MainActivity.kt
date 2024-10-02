package com.example.giftbox

import android.os.Bundle
import android.util.Log
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
import androidx.compose.animation.slideIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key.Companion.Home
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.giftbox.ui.theme.GiftBoxTheme
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getString
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination
    val bottomScreens = BottomNavigationItem().bottomNavigationItems()
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
                HomeScreen {
                    navController.navigate(route = Screen.Add.route)
                }
            }
            composable(Screen.List.route) { ListScreen() }
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
                AddGifticon { navController.popBackStack() }
            }
        }
    }
}

@Composable
fun ListScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = stringResource(id = R.string.list),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.Black,
            modifier = Modifier.align(Alignment.Center)
        )
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

sealed class Screen(val route: String, @StringRes val resourceId: Int) {
    object Home : Screen("home", R.string.home)
    object List : Screen("list", R.string.list)
    object Setting : Screen("setting", R.string.setting)
    object Add : Screen("add", R.string.setting)
}

data class BottomNavigationItem(
    val label: Int = 0,
    val icon: ImageVector = Icons.Filled.Home,
    val route: String = ""
) {
    fun bottomNavigationItems(): List<BottomNavigationItem> {
        return listOf(
            BottomNavigationItem(
                label = R.string.list,
                icon = Icons.AutoMirrored.Filled.List,
                route = "list"
            ),
            BottomNavigationItem(
                label = R.string.home,
                icon = Icons.Filled.Home,
                route = "home"
            ),
            BottomNavigationItem(
                label = R.string.setting,
                icon = Icons.Filled.Settings,
                route = "setting"
            )
        )
    }
}