package com.sumi.giftbox.ui.pin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.Navigation
import com.sumi.giftbox.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PinFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            PinScreen {
                // 핀 로그인 성공 > 메인 화면 이동
                val navController = Navigation.findNavController(requireView())
                navController.popBackStack()
                navController.navigate(R.id.mainFragment)
            }
        }
    }
}