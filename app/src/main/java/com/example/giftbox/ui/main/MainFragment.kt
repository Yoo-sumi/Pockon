package com.example.giftbox.ui.main

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.Navigation
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.giftbox.R
import com.example.giftbox.model.Gift
import com.example.giftbox.ui.add.AddGifticon
import com.example.giftbox.ui.detail.DetailScreen
import com.example.giftbox.ui.home.HomeScreen
import com.example.giftbox.ui.list.ListScreen
import com.example.giftbox.ui.list.ListViewModel
import com.example.giftbox.ui.map.MapScreen
import com.example.giftbox.ui.settings.SettingScreen
import com.example.giftbox.ui.used.UsedScreen
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = ComposeView(requireContext()).apply {
        setContent {
            BottomNavigationBar(
                movePinScreen = {
                    val navController = Navigation.findNavController(requireView())
                    navController.popBackStack()
                    navController.navigate(R.id.pinFragment)
                },
                moveLogInScreen = {
                    val navController = Navigation.findNavController(requireView())
                    navController.popBackStack()
                    navController.navigate(R.id.loginFragment)
                }
            )
        }
    }

}

@Composable
fun BottomNavigationBar(movePinScreen: () -> Unit, moveLogInScreen: () -> Unit) {
    val navController = rememberNavController()

    val listViewModel = hiltViewModel<ListViewModel>()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination
    val bottomScreens = listOf(
        Screen.List,
        Screen.Home,
        Screen.Setting
    )
    val showBottomBar = navController
        .currentBackStackEntryAsState().value?.destination?.route in bottomScreens.map { it.route }

    val context = LocalContext.current

    // check permission
    val launcherPermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val areGranted = permissionsMap.values.reduce { acc, next -> acc && next }
        if (!areGranted) {
            AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.txt_alert))
                .setMessage(context.getString(R.string.msg_no_permission))
                .setPositiveButton("확인") { dialog, which ->
                    // 긍정 버튼 클릭 동작 처리
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    )
                    context.startActivity(intent)
                    val activity = context as? Activity
                    activity?.finish()
                }
                .show()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomScreens.forEach { bottomNavigationItem ->
                        NavigationBarItem(
                            selected = currentRoute?.hierarchy?.any { it.route == bottomNavigationItem.route } == true,
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
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onAdd = {
                        navController.navigate(route = Screen.Add.route)
                    },
                    showMap =  {
                        navController.navigate(route = Screen.Map.route)
                    },
                    onDetail = { gift ->
                        val gifJson = Uri.encode(Gson().toJson(gift))
                        navController.navigate(route = "${Screen.Detail.route}/${gifJson}")
                    }
                )
            }
            composable(Screen.List.route) {
                if (listViewModel.giftList.value.isEmpty()) {
                    EmptyScreen { navController.navigate(route = Screen.Add.route) }
                    return@composable
                }

                ListScreen(
                    listViewModel = listViewModel,
                    onDetail = { gift ->
                        val gifJson = Uri.encode(Gson().toJson(gift))
                        navController.navigate(route = "${Screen.Detail.route}/${gifJson}")
                    },
                    onAdd = {
                        navController.navigate(route = Screen.Add.route)
                    }
                )
            }
            composable(Screen.Setting.route) {
                SettingScreen(
                    onUsedGift = {
                        navController.navigate(route = Screen.Used.route)
                    },
                    movePinScreen = {
                        movePinScreen()
                    },
                    moveLogInScreen = {
                        moveLogInScreen()
                    }
                )
            }
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
                    onBack = { isRefresh ->
                        if (isRefresh) listViewModel.getGiftList()
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
                DetailScreen(gift) {
                    navController.popBackStack()
                }
            }
            composable(Screen.Map.route) {
                MapScreen {
                    navController.popBackStack()
                }
            }
            composable(Screen.Used.route) {
                UsedScreen(
                    onDetail = { gift ->
                        val gifJson = Uri.encode(Gson().toJson(gift))
                        navController.navigate(route = "${Screen.Detail.route}/${gifJson}")
                    }
                )
            }
        }
    }
    CheckPermission(context, launcherPermissions)
}

@Composable
fun EmptyScreen(onAdd: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                fontSize = 18.sp,
                text = stringResource(id = R.string.txt_no_gift),
            )

            Spacer(modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp))

            Image(
                modifier = Modifier
                    .width(80.dp)
                    .height(80.dp),
                painter = painterResource(id = R.drawable.icon_empty_box),
                contentDescription = "add photo",
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RectangleShape,
                onClick = {
                    onAdd()
                }
            ) {
                Text(text = stringResource(id = R.string.btn_register))
            }
        }
    }
}

/** 앨범, 위치 권한 체크 */
@Composable
private fun CheckPermission(
    context: Context,
    launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>
) {
    val permissions = if (Build.VERSION.SDK_INT >= 33) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,  Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    if (!permissions.all {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }) {
        LaunchedEffect(Unit) {
            launcher.launch(permissions)
        }
    }
}

sealed class Screen(val route: String, val icon: ImageVector, @StringRes val label: Int) {
    data object Home : Screen("home", Icons.Filled.Home, R.string.home)
    data object List : Screen("list", Icons.AutoMirrored.Filled.List, R.string.list)
    data object Setting : Screen("setting", Icons.Filled.Settings, R.string.setting)

    data object Add : Screen("add", Icons.Filled.Add, R.string.setting)
    data object Detail : Screen("detail", Icons.Filled.Search, R.string.detail)
    data object Map : Screen("map", Icons.Filled.LocationOn, R.string.map)
    data object Used : Screen("used", Icons.Filled.LocationOn, R.string.map)
}