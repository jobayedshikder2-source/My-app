package com.example.ui.screens

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.repository.LoginResult
import com.example.ui.viewmodel.CommunityViewModel
import com.example.ui.viewmodel.ScreenState
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun LoginScreen(
    viewModel: CommunityViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var phoneNumber by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf<String?>(null) }
    var showPendingNotice by remember { mutableStateOf(false) }
    var isGoogleSigningIn by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6))
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Community App Logo & Header
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(Color(0xFF008069)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_icon_community_1791464050571),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "কমিউনিটি হাব (Community Hub)",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF008069)
        )

        Text(
            text = "সার্বজনীন যোগাযোগ ও নোটিশ প্ল্যাটফর্ম",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1F2937),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Login Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "সদস্য লগইন / Member Login",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF000000)
                )

                Text(
                    text = "গুগল অ্যাকাউন্ট অথবা ফোন নম্বর দিয়ে লগইন করুন",
                    fontSize = 12.sp,
                    color = Color(0xFF374151),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 1. Google Sign-In Button (Mandatory Interactive Button)
                Button(
                    onClick = {
                        isGoogleSigningIn = true
                        loginError = null
                        showPendingNotice = false

                        val clientId = try {
                            context.getString(R.string.default_web_client_id)
                        } catch (e: Exception) {
                            loginError = "Google Client ID configuration error"
                            isGoogleSigningIn = false
                            return@Button
                        }

                        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
                        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

                        coroutineScope.launch {
                            try {
                                val result = credentialManager.getCredential(context as Activity, request)
                                val credential = result.credential
                                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                                    val authResult = Firebase.auth.signInWithCredential(authCredential).await()
                                    val fbUser = authResult.user
                                    if (fbUser != null) {
                                        viewModel.handleGoogleUser(fbUser)
                                    } else {
                                        loginError = "গুগল অথেন্টিকেশন ব্যর্থ হয়েছে।"
                                    }
                                } else {
                                    loginError = "অপ্রত্যাশিত ক্রেডেনশিয়াল রেসপন্স।"
                                }
                            } catch (e: GetCredentialCancellationException) {
                                Log.w("Auth", "Google Sign-In cancelled: ${e.message}")
                            } catch (e: Exception) {
                                Log.e("Auth", "Google Sign-In failed", e)
                                loginError = e.localizedMessage ?: "সাইন ইন ব্যর্থ হয়েছে"
                            } finally {
                                isGoogleSigningIn = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("google_sign_in_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1F2937),
                        contentColor = Color.White
                    ),
                    enabled = !isGoogleSigningIn
                ) {
                    if (isGoogleSigningIn) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "G ",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF4285F4)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Google দিয়ে সাইন ইন করুন",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
                    Text(
                        text = " অথবা ফোন নম্বর দিয়ে ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4B5563),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Phone Input Field
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        phoneNumber = it
                        loginError = null
                        showPendingNotice = false
                    },
                    label = { Text("ফোন নম্বর (Phone Number)", color = Color(0xFF111827), fontWeight = FontWeight.Medium) },
                    placeholder = { Text("01XXXXXXXXX", color = Color(0xFF6B7280)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Phone Icon",
                            tint = Color(0xFF008069)
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF000000),
                        unfocusedTextColor = Color(0xFF000000),
                        focusedBorderColor = Color(0xFF008069),
                        unfocusedBorderColor = Color(0xFF9CA3AF),
                        focusedLabelColor = Color(0xFF008069),
                        unfocusedLabelColor = Color(0xFF1F2937)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("phone_input")
                )

                if (showPendingNotice) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFF3E0),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "আপনার নিবন্ধন এখনো পেন্ডিং রয়েছে। অ্যাডমিন অনুমোদনের পর লগইন সচল হবে।",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF000000),
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                if (loginError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = loginError ?: "",
                        color = Color(0xFFDC2626),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (phoneNumber.isBlank()) {
                            loginError = "ফোন নম্বর প্রবেশ করান"
                            return@Button
                        }
                        viewModel.login(phoneNumber) { result ->
                            when (result) {
                                is LoginResult.Success -> {
                                    // Navigate to dashboard
                                }
                                is LoginResult.PendingApproval -> {
                                    showPendingNotice = true
                                    loginError = null
                                }
                                is LoginResult.Rejected -> {
                                    loginError = "আবেদনটি অ্যাডমিন কর্তৃক বাতিল করা হয়েছে।"
                                }
                                is LoginResult.NotFound -> {
                                    loginError = "এই ফোন নম্বরে কোনো অ্যাকাউন্ট নেই। অনুগ্রহ করে নিবন্ধন করুন।"
                                }
                                is LoginResult.NeedsRegistration -> {
                                    // Handled in viewModel
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("login_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008069))
                ) {
                    Text(
                        text = "ফোন দিয়ে লগইন করুন",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = { viewModel.navigateTo(ScreenState.Register) },
                    modifier = Modifier.testTag("goto_register_button")
                ) {
                    Text(
                        text = "নতুন সদস্য? এখানে নিবন্ধন করুন (Register)",
                        color = Color(0xFF008069),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Admin Access Row
        OutlinedButton(
            onClick = { viewModel.navigateTo(ScreenState.AdminLogin) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("admin_panel_button"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1565C0))
        ) {
            Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = "Admin Icon",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "অ্যাডমিন প্যানেল প্রবেশ (Admin Panel)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
