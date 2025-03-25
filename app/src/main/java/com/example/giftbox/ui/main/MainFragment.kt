package com.example.giftbox.ui.main

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.navigation.Navigation
import com.example.giftbox.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment() {

    @Inject lateinit var sharedPref: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = ComposeView(requireContext()).apply {
        val isGuestMode = sharedPref.getBoolean("guest_mode", false)
        setContent {
            val lightColorScheme = lightColorScheme(
                primary = colorResource(id = R.color.primary),  // Primary 색상
                onPrimary = colorResource(id = R.color.onPrimary),  // Primary 색상의 텍스트 색
                primaryContainer = colorResource(id = R.color.primaryContainer),  // Primary 색상에 맞춘 컨테이너 색상
                onPrimaryContainer = colorResource(id = R.color.onPrimary),  // Primary 컨테이너의 텍스트 색상
                secondary = colorResource(id = R.color.secondary),  // Secondary 색상 Color(0xFF8AC3E1)
                tertiary = colorResource(id = R.color.tertiary),  // Tertiary 색상
                outline = colorResource(id = R.color.light_gray),  // Tertiary 색상
                error = colorResource(id = R.color.red),  // Error 색상에 맞춘 컨테이너 색상
                errorContainer = colorResource(id = R.color.gray),  // Error 색상에 맞춘 컨테이너 색상
                background = colorResource(id = R.color.background)  // Error 색상에 맞춘 컨테이너 색상
            )
            MaterialTheme(
                colorScheme = lightColorScheme  // Light Mode Color Scheme
            ) {
                BottomNavigationBar(
                    isGuestMode,
                    movePinScreen = {
                        val navController = Navigation.findNavController(requireView())
                        navController.popBackStack()
                        navController.navigate(R.id.pinFragment)
                    },
                    moveLogInScreen = {
                        val navController = Navigation.findNavController(requireView())
                        navController.popBackStack()
                        navController.navigate(R.id.loginFragment)
                    },
                    onFinish = {
                        activity?.supportFragmentManager
                            ?.beginTransaction()
                            ?.remove(this@MainFragment)
                            ?.commit()
                    }
                )
            }
        }
    }

}