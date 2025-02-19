package com.example.giftbox.ui.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.example.giftbox.BuildConfig
import com.example.giftbox.R
import com.example.giftbox.databinding.FragmentLoginBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        val btnGoogle: TextView = binding.btnGoogleLogin.getChildAt(0) as TextView
        btnGoogle.text = getString(R.string.btn_goggle_login)

        val credentialManager = CredentialManager.create(requireContext())

        val googleIdOption = GetGoogleIdOption
            .Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest
            .Builder()
            .addCredentialOption(googleIdOption)
            .build()

        binding.btnGuestLogin.setOnClickListener {
            loginViewModel.loginAsGuest() // 게스트로 로그인
        }

        btnGoogle.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val result = credentialManager.getCredential(
                        request = request,
                        context = requireContext()
                    )
                    loginViewModel.login(result)
                } catch (e: GetCredentialException) {
                    TODO()
                }
//                credentialManager.clearCredentialState(request = ClearCredentialStateRequest()) >> 로그아웃할때 필요
            }
        }

        loginViewModel.isLogin.observe(viewLifecycleOwner) {
            if (it) {
                // 로그인 성공 > 메인 화면으로 이동
                val navController = Navigation.findNavController(requireView())
                navController.popBackStack()
                if (loginViewModel.getIsPinUse()) navController.navigate(R.id.pinFragment)
                else navController.navigate(R.id.mainFragment)
            }
        }

        return binding.root
    }

}