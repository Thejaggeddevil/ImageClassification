package com.hybridclassifier.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.hybridclassifier.app.ui.theme.*
import com.hybridclassifier.app.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (role: String) -> Unit,
    onSignupClick: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state        = viewModel.state.collectAsState().value
    val colors       = MaterialTheme.appColors
    val focusManager = LocalFocusManager.current

    // User login fields — always empty, never auto-filled
    var username        by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passVisible     by remember { mutableStateOf(false) }

    // Admin login fields — separate, always empty
    var adminUser       by remember { mutableStateOf("") }
    var adminPass       by remember { mutableStateOf("") }
    var adminPassVisible by remember { mutableStateOf(false) }

    // 3D flip state
    var flipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "flip"
    )
    val isFront = rotation <= 90f

    // Clear error when switching sides
    LaunchedEffect(flipped) { viewModel.clearError() }

    // Mascot bounce
    val inf = rememberInfiniteTransition(label = "")
    val bounce by inf.animateFloat(
        initialValue = 0f, targetValue = -10f,
        animationSpec = infiniteRepeatable(tween(1400, easing = EaseInOutSine), RepeatMode.Reverse),
        label = ""
    )

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) { delay(150); onLoginSuccess(state.role) }
    }

    Box(Modifier.fillMaxSize().background(colors.background)) {

        // Background decoration
        Box(
            Modifier.size(280.dp).offset((-90).dp, (-70).dp)
                .background(Brush.radialGradient(listOf(colors.secondary.copy(.18f), Color.Transparent)), CircleShape)
        )
        Box(
            Modifier.size(220.dp).align(Alignment.BottomEnd).offset(80.dp, 80.dp)
                .background(Brush.radialGradient(listOf(colors.tertiary.copy(.15f), Color.Transparent)), CircleShape)
        )

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(40.dp))

            // App logo + name
            Text("🦁", fontSize = 64.sp, modifier = Modifier.offset(y = bounce.dp))
            Spacer(Modifier.height(6.dp))
            Text("KidsLearn",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold, color = colors.primary)
            Spacer(Modifier.height(4.dp))
            Text(
                if (!flipped) "Tap the shield icon to switch to Admin login"
                else "Tap the back arrow to return to User login",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary, textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            // ── FLIP CARD ──────────────────────────────
            Box(
                Modifier.fillMaxWidth().graphicsLayer {
                    rotationY     = rotation
                    cameraDistance = 14f * density
                }
            ) {
                if (isFront) {
                    // ════════════════════════════
                    //   FRONT — User Login
                    // ════════════════════════════
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        elevation = CardDefaults.cardElevation(3.dp)
                    ) {
                        Column(
                            Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Header row
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Sign In",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                    Text("Enter your credentials to continue",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary)
                                }
                                // Shield icon → flip to admin
                                IconButton(
                                    onClick = {
                                        focusManager.clearFocus()
                                        flipped = true
                                    }
                                ) {
                                    Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin Login",
                                        tint = colors.textSecondary.copy(.6f), modifier = Modifier.size(24.dp))
                                }
                            }

                            // Username
                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("Username") },
                                leadingIcon = { Icon(Icons.Default.Person, null, tint = colors.primary) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                ),
                                colors = kidsTextFieldColors(colors)
                            )

                            // Password
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, null, tint = colors.primary) },
                                trailingIcon = {
                                    IconButton(onClick = { passVisible = !passVisible }) {
                                        Icon(
                                            if (passVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            null, tint = colors.textSecondary
                                        )
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    focusManager.clearFocus()
                                    viewModel.login(username, password)
                                }),
                                colors = kidsTextFieldColors(colors)
                            )

                            // Error message
                            if (state.error != null) LoginError(state.error, colors)

                            // Sign In button
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.login(username, password)
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                enabled = !state.isLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                            ) {
                                if (state.isLoading)
                                    CircularProgressIndicator(
                                        Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp
                                    )
                                else
                                    Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }

                            // Approval notice
                            Row(
                                Modifier.fillMaxWidth()
                                    .background(colors.secondary.copy(.08f), RoundedCornerShape(10.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, null,
                                    tint = colors.secondary, modifier = Modifier.size(14.dp))
                                Text("New accounts need admin approval before first login.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary, lineHeight = 16.sp)
                            }
                        }
                    }

                } else {
                    // ════════════════════════════
                    //   BACK — Admin Login
                    // ════════════════════════════
                    Card(
                        Modifier.fillMaxWidth().graphicsLayer { rotationY = 180f },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.5.dp, colors.primary.copy(.25f)),
                        elevation = CardDefaults.cardElevation(3.dp)
                    ) {
                        Column(
                            Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Header row
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Back arrow → flip to user
                                IconButton(
                                    onClick = {
                                        focusManager.clearFocus()
                                        flipped = false
                                    }
                                ) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back to User Login",
                                        tint = colors.primary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Admin Login",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold, color = colors.primary)
                                    Text("Authorised personnel only",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary)
                                }
                            }

                            // Admin info box
                            Row(
                                Modifier.fillMaxWidth()
                                    .background(colors.primary.copy(.07f), RoundedCornerShape(10.dp))
                                    .border(1.dp, colors.primary.copy(.15f), RoundedCornerShape(10.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Shield, null,
                                    tint = colors.primary, modifier = Modifier.size(16.dp))
                                Text("Admin account manages user approvals, categories, custom facts, and class settings.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textPrimary, lineHeight = 16.sp)
                            }

                            // Admin username — completely separate field, no auto-fill
                            OutlinedTextField(
                                value = adminUser,
                                onValueChange = { adminUser = it },
                                label = { Text("Admin Username") },
                                leadingIcon = {
                                    Icon(Icons.Default.AdminPanelSettings, null, tint = colors.primary)
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                ),
                                colors = kidsTextFieldColors(colors)
                            )

                            // Admin password — completely separate
                            OutlinedTextField(
                                value = adminPass,
                                onValueChange = { adminPass = it },
                                label = { Text("Admin Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, null, tint = colors.primary) },
                                trailingIcon = {
                                    IconButton(onClick = { adminPassVisible = !adminPassVisible }) {
                                        Icon(
                                            if (adminPassVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            null, tint = colors.textSecondary
                                        )
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                visualTransformation = if (adminPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    focusManager.clearFocus()
                                    viewModel.login(adminUser, adminPass)
                                }),
                                colors = kidsTextFieldColors(colors)
                            )

                            // Error
                            if (state.error != null) LoginError(state.error, colors)

                            // Admin Sign In
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.login(adminUser, adminPass)
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                enabled = !state.isLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                            ) {
                                if (state.isLoading)
                                    CircularProgressIndicator(
                                        Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp
                                    )
                                else {
                                    Icon(Icons.Default.AdminPanelSettings, null,
                                        modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Admin Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            }
            // ── END FLIP CARD ──────────────────────────

            Spacer(Modifier.height(16.dp))

            // Signup link — only visible on front
            if (isFront) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("New here? ", color = colors.textSecondary,
                        style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = onSignupClick) {
                        Text("Create Account", color = colors.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

// ── Error message composable ──────────────────
@Composable
private fun LoginError(error: String, colors: AppColorScheme) {
    val isPending  = error.contains("PENDING", ignoreCase = true)
    val isRejected = error.contains("REJECTED", ignoreCase = true)

    val displayText = error
        .removePrefix("PENDING_APPROVAL:")
        .removePrefix("REJECTED:")
        .trim()

    val bgColor   = when { isPending -> colors.accent1.copy(.1f); else -> colors.error.copy(.1f) }
    val textColor = when { isPending -> colors.accent1; else -> colors.error }
    val icon      = when { isPending -> Icons.Default.HourglassEmpty; else -> Icons.Default.ErrorOutline }

    Row(
        Modifier.fillMaxWidth()
            .background(bgColor, RoundedCornerShape(10.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = textColor, modifier = Modifier.size(16.dp))
        Text(displayText, color = textColor,
            style = MaterialTheme.typography.bodySmall, lineHeight = 16.sp)
    }
}