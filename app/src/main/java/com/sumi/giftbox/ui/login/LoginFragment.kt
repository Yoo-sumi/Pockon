package com.sumi.giftbox.ui.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.sumi.giftbox.BuildConfig
import com.sumi.giftbox.R
import com.sumi.giftbox.databinding.FragmentLoginBinding
import com.sumi.giftbox.ui.loading.LoadingScreen
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
                    loginViewModel.login(credentialManager, result) // 구글 로그인 이어서 진행
                } catch (e: GetCredentialException) { // 구글 로그인 실패
                    Snackbar.make(
                        binding.root,
                        getString(R.string.msg_login_fail),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
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

        loginViewModel.isFail.observe(viewLifecycleOwner) {
            if (it) {
                // 로그인 실패 스낵바
                Snackbar.make(
                    binding.root,
                    getString(R.string.msg_login_fail),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        loginViewModel.isLoading.observe(viewLifecycleOwner) {
            binding.cvLoadingScreen.setContent {
                if (it) LoadingScreen()
            }
        }

        return binding.root
    }
}