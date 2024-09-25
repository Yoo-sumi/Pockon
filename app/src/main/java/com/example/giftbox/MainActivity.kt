package com.example.giftbox

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Im
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.giftbox.ui.theme.GiftBoxTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getString
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import javax.annotation.Untainted

class MainActivity : ComponentActivity() {
//    private val mainViewModel: MianViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContent {
            GiftBoxTheme {
                HandleSignInWithGoggle()
            }
        }
    }
}

@Composable
fun GoogleLogin(
    modifier: Modifier = Modifier,
    sendSnackbarMessage: (String) -> Unit,
    onGoogleLoginClick: () -> Unit
) {
    // 구글 로그인 버튼 등록
    Image(
        painter = painterResource(
            id = R.drawable.login_symbol_google
        ),
        contentDescription = "Google Login Button",
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onGoogleLoginClick() }
    )
}

@Composable
fun Login(
    modifier: Modifier = Modifier,
) {
    var user by remember {
        mutableStateOf(Firebase.auth.currentUser)
    }

    val token = stringResource(id = R.string.client_id)
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (user == null) {
            Image(
                painter = painterResource(id = R.drawable.login_symbol_google),
                contentDescription = "Google Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(50.dp)
                    .width(250.dp)
                    .padding(5.dp)
                    .clip(CircleShape)
                    .clickable { }
            )
        } else {
            Text(
                text = "Hi ${user!!.displayName}",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(15.dp))

            Button(onClick = {
                Firebase.auth.signOut()
                user = null
            },
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
                    .padding(5.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "Log Out",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    letterSpacing = 0.1.em
                )
            }
        }
    }
}

@Composable
fun rememberFirebaseAuthLauncher(
    onAuthComplete: (AuthResult) -> Unit,
    onAuthError: (ApiException) -> Unit
): ManagedActivityResultLauncher<Intent, ActivityResult> {
    val scope = rememberCoroutineScope()

    return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
    }
}

@Composable
fun HandleSignInWithGoggle() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Image(
        painter = painterResource(id = R.drawable.login_symbol_google),
        contentDescription = "Google Logo",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .height(50.dp)
            .width(250.dp)
            .padding(5.dp)
            .clip(CircleShape)
            .clickable {
                Log.d("로그인테스트", "signInWithCredential")

                val credentialManager = CredentialManager.create(context)

                val googleIdOption = GetGoogleIdOption
                    .Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(getString(context, R.string.client_id))
                    .setAutoSelectEnabled(true)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                coroutineScope.launch {
                    try {
                        val result = credentialManager.getCredential(
                            request = request,
                            context = context
                        )
                        handleSignIn(result)
                    } catch (e: GetCredentialException) {
                        Log.d("로그인테스트", "signInWithCredential ${e.errorMessage}")
                    }
                }
            }
    )
}

private fun handleSignIn(result: GetCredentialResponse) {
    Log.d("로그인테스트", "signInWithCredential 2")

    val auth = Firebase.auth
    Log.d("로그인테스트", "result.credential: ${result.credential.type}")

    when (val credential = result.credential) {
        is CustomCredential -> {
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("로그인테스트", "signInWithCredential: success")
                            Log.d("로그인테스트", "signInWithCredential: ${task.result.user?.displayName}")
                        } else {
                            Log.d("로그인테스트", "signInWithCredential: failure")
                        }
                    }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GiftBoxTheme {
        GoogleLogin(
            sendSnackbarMessage = {

            },
            onGoogleLoginClick = {

            }
        )
    }
}