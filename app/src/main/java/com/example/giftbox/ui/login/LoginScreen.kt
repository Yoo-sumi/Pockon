package com.example.giftbox.ui.login

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getString
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.giftbox.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    mainViewModel: LoginViewModel = viewModel()
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (mainViewModel.isLoginState.value) {
            LogoutWithGoggle(
                modifier = modifier,
                logout = {
                    mainViewModel.logout()
                }
            )
        } else {
            LoginWithGoggle(
                modifier = modifier,
                login = { result ->
                    mainViewModel.login(result)
                },
            )
        }
    }
}

@Composable
fun LoginWithGoggle(
    modifier: Modifier = Modifier,
    login: (GetCredentialResponse) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()


    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.login_symbol_google),
            contentDescription = "Google Logo",
            contentScale = ContentScale.Fit,
            modifier = modifier
                .height(50.dp)
                .width(250.dp)
                .padding(5.dp)
                .clip(CircleShape)
                .clickable {
                    val credentialManager = CredentialManager.create(context)

                    val googleIdOption = GetGoogleIdOption
                        .Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(getString(context, R.string.client_id))
                        .setAutoSelectEnabled(true)
                        .build()

                    val request = GetCredentialRequest
                        .Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    coroutineScope.launch {
                        try {
                            val result = credentialManager.getCredential(
                                request = request,
                                context = context
                            )
                            login(result)
                        } catch (e: GetCredentialException) {
                            Log.d("로그인테스트", "signInWithCredential ${e.errorMessage}")
                        }
                        credentialManager.clearCredentialState(request = ClearCredentialStateRequest())
                    }
                }
        )
    }
}

@Composable
fun LogoutWithGoggle(
    modifier: Modifier = Modifier,
    logout: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                logout()
            }
        ) {
            Text(text = "로그아웃")
        }
    }
}