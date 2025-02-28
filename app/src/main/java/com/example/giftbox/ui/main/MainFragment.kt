package com.example.giftbox.ui.main

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
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