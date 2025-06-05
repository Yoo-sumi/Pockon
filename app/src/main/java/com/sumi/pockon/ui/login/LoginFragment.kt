package com.sumi.pockon.ui.login

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.sumi.pockon.R
import com.sumi.pockon.databinding.FragmentLoginBinding
import com.sumi.pockon.ui.loading.LoadingScreen
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()
    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            loginViewModel.loginForApiLower(account?.idToken, account?.email, account?.displayName, account?.photoUrl)
        } catch (e: ApiException) {
            loginViewModel.loginForApiLower(null, null, null, null)
        } catch (e: Exception) {
            loginViewModel.loginForApiLower(null, null, null, null)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        val btnGoogle: TextView = binding.btnGoogleLogin.getChildAt(0) as TextView
        btnGoogle.text = getString(R.string.btn_goggle_login)

        binding.btnGuestLogin.setOnClickListener {
            loginViewModel.loginAsGuest() // 게스트로 로그인
        }

        btnGoogle.setOnClickListener {
            // 동의 팝업 띄우고 동의하면 로그인 시작
            showPrivacyConsentDialog {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    loginViewModel.loginForApiHigher()
                } else {
                    loginViewModel.getSignInIntent { signInIntent ->
                        signInLauncher.launch(signInIntent)
                    }
                }
            }
        }

        loginViewModel.isFirstLogin.observe(viewLifecycleOwner) {
            binding.lyLogin.visibility = if (it) View.VISIBLE else View.INVISIBLE
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

    private fun showPrivacyConsentDialog(onAgree: () -> Unit) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.privacy_title))
            .setMessage(getString(R.string.privacy_content))
            .setPositiveButton(getString(R.string.btn_agree)) { dialog, _ ->
                dialog.dismiss()
                onAgree()
            }
            .setNegativeButton(getString(R.string.btn_disagree)) { dialog, _ ->
                dialog.dismiss()
                Snackbar.make(binding.root, getString(R.string.privacy_consent_required), Snackbar.LENGTH_SHORT).show()
            }
            .setCancelable(false)
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.gray)
        )
    }
}