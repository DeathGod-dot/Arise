package com.example.arise.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.arise.ui.components.*
import com.example.arise.ui.theme.*
import com.example.arise.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

enum class AuthTab {
    SIGN_IN, REGISTER
}

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(AuthTab.SIGN_IN) }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var usernameInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmailInput by remember { mutableStateOf("") }

    val context = LocalContext.current
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                viewModel.signInWithGoogle(idToken)
            } else {
                viewModel.setError("Google Sign-In failed: No authentication token received.")
            }
        } catch (e: ApiException) {
            // Status code 12501 = user cancelled the sign-in flow, don't show error
            if (e.statusCode != 12501) {
                viewModel.setError("Google Sign-In failed (code ${e.statusCode}). Please try again.")
            }
        } catch (e: Exception) {
            viewModel.setError("Google Sign-In failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header: ARISE & Subtitle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val isLight = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() > 0.5f
                val primaryColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
                val tabBgColor = if (isLight) androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHigh else Color(0xFF0F172A)
                val unselectedTabTextColor = if (isLight) androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF64748B)

                GlitchText(
                    text = "ARISE",
                    style = DisplayRank.copy(fontSize = 32.sp, letterSpacing = 6.sp),
                    baseColor = primaryColor
                )
                Text(
                    text = "[ SYSTEM VERIFICATION & HUNTER LOGIN ]",
                    style = SystemLabel.copy(fontSize = 11.sp, letterSpacing = 1.5.sp),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Auth Card Container
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val isLight = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() > 0.5f
                    val primaryColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    val tabBgColor = if (isLight) androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHigh else Color(0xFF0F172A)
                    val unselectedTabTextColor = if (isLight) androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF64748B)

                    // Tab Selector: SIGN IN vs REGISTER
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(tabBgColor)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (activeTab == AuthTab.SIGN_IN) primaryColor.copy(alpha = 0.2f) else Color.Transparent)
                                .border(
                                    width = if (activeTab == AuthTab.SIGN_IN) 1.dp else 0.dp,
                                    color = if (activeTab == AuthTab.SIGN_IN) primaryColor else Color.Transparent,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable {
                                    activeTab = AuthTab.SIGN_IN
                                    viewModel.clearMessages()
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "SYSTEM LOGIN",
                                style = SystemLabel.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                color = if (activeTab == AuthTab.SIGN_IN) primaryColor else unselectedTabTextColor
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (activeTab == AuthTab.REGISTER) primaryColor.copy(alpha = 0.2f) else Color.Transparent)
                                .border(
                                    width = if (activeTab == AuthTab.REGISTER) 1.dp else 0.dp,
                                    color = if (activeTab == AuthTab.REGISTER) primaryColor else Color.Transparent,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable {
                                    activeTab = AuthTab.REGISTER
                                    viewModel.clearMessages()
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "NEW HUNTER",
                                style = SystemLabel.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                color = if (activeTab == AuthTab.REGISTER) primaryColor else unselectedTabTextColor
                            )
                        }
                    }

                    // Status Messages (Error / Success Banners)
                    state.errorMessage?.let { error ->
                        Surface(
                            color = AriseDangerRed.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, AriseDangerRed),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = AriseDangerRed, modifier = Modifier.size(18.dp))
                                Text(error, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, style = AriseTypography.bodyMedium)
                            }
                        }
                    }

                    state.successMessage?.let { msg ->
                        Surface(
                            color = ArisePrimary.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, ArisePrimary),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ArisePrimary, modifier = Modifier.size(18.dp))
                                Text(msg, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, style = AriseTypography.bodyMedium)
                            }
                        }
                    }

                    val tfColors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLowest,
                        focusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLowest,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant,
                        focusedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                        cursorColor = primaryColor
                    )

                    // Optional Username field when registering
                    AnimatedVisibility(visible = activeTab == AuthTab.REGISTER) {
                        OutlinedTextField(
                            value = usernameInput,
                            onValueChange = { usernameInput = it },
                            singleLine = true,
                            label = { Text("Hunter Codename", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = primaryColor) },
                            colors = tfColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Email Field
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        singleLine = true,
                        label = { Text("Email Identifier", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = primaryColor) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = tfColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Password Field
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        singleLine = true,
                        label = { Text("Security Access Key", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = primaryColor) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Password",
                                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = tfColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Forgot Password link (when in Sign-In mode)
                    if (activeTab == AuthTab.SIGN_IN) {
                        Text(
                            text = "RECOVER ACCESS CODE?",
                            style = SystemLabel.copy(fontSize = 10.sp, color = primaryColor),
                            modifier = Modifier
                                .align(Alignment.End)
                                .clickable {
                                    resetEmailInput = emailInput
                                    showResetDialog = true
                                }
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    // Submit Action Button
                    SystemButton(
                        text = if (state.isLoading) "AUTHENTICATING..." else if (activeTab == AuthTab.SIGN_IN) "INITIALIZE ACCESS" else "REGISTER HUNTER",
                        onClick = {
                            if (activeTab == AuthTab.SIGN_IN) {
                                viewModel.signIn(emailInput, passwordInput)
                            } else {
                                viewModel.signUp(emailInput, passwordInput, usernameInput)
                            }
                        },
                        variant = ButtonVariant.PRIMARY,
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Divider & Google Sign-In Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)
                        Text("OR", style = SystemLabel.copy(fontSize = 10.sp), color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                        HorizontalDivider(modifier = Modifier.weight(1f), color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)
                    }

                    OutlinedButton(
                        onClick = {
                            val webClientId = context.getString(com.example.arise.R.string.default_web_client_id)
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(webClientId)
                                .requestEmail()
                                .build()
                            val googleSignInClient = GoogleSignIn.getClient(context, gso)
                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = SciFiCutCornerShape(),
                        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = primaryColor)
                            Text("CONTINUE WITH GOOGLE", style = SystemLabel.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold), color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }

    // Reset Password Dialog
    if (showResetDialog) {
        val isLight = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() > 0.5f
        val primaryColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
        Dialog(onDismissRequest = { showResetDialog = false }) {
            Surface(
                color = if (isLight) androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHigh else Color(0xFF090B10),
                shape = SciFiCutCornerShape(),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "PASSWORD RECOVERY PROTOCOL",
                        style = SystemLabel.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                        color = primaryColor
                    )
                    Text(
                        text = "Enter your registered email address to receive a system access key reset transmission.",
                        style = AriseTypography.bodyMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = resetEmailInput,
                        onValueChange = { resetEmailInput = it },
                        singleLine = true,
                        label = { Text("Registered Email", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLowest,
                            focusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLowest,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant,
                            focusedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                            cursorColor = primaryColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showResetDialog = false }) {
                            Text("CANCEL", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.width(8.dp))
                        SystemButton(
                            text = "TRANSMIT LINK",
                            onClick = {
                                viewModel.sendPasswordReset(resetEmailInput)
                                showResetDialog = false
                            },
                            variant = ButtonVariant.PRIMARY
                        )
                    }
                }
            }
        }
    }
}
