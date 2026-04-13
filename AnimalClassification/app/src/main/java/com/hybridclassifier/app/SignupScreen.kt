package com.hybridclassifier.app.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.hybridclassifier.app.ui.theme.*
import com.hybridclassifier.app.ui.viewmodel.SignupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onSignupSuccess: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: SignupViewModel = hiltViewModel()
) {
    val state        = viewModel.state.collectAsState().value
    val colors       = MaterialTheme.appColors
    val focusManager = LocalFocusManager.current

    // Parent / account info
    var name            by remember { mutableStateOf("") }
    var username        by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var phone           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passVisible     by remember { mutableStateOf(false) }

    // Child info
    var childName by remember { mutableStateOf("") }
    var childAge  by remember { mutableStateOf("") }

    // Show success screen after signup
    if (state.isSuccess) {
        SignupSuccessScreen(onBackClick)
        return
    }

    Box(Modifier.fillMaxSize().background(colors.background)) {

        // Decoration
        Box(Modifier.size(200.dp).offset((-60).dp, (-40).dp)
            .background(Brush.radialGradient(listOf(colors.secondary.copy(.15f), Color.Transparent)), CircleShape))

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Box(Modifier.fillMaxWidth().padding(8.dp)) {
                IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.Default.ArrowBack, null, tint = colors.textPrimary)
                }
                Text("Create Account",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = colors.textPrimary,
                    modifier = Modifier.align(Alignment.Center))
            }

            Column(
                Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // ── Pending approval notice ──────────────
                Row(Modifier.fillMaxWidth()
                    .background(colors.accent1.copy(.1f), RoundedCornerShape(12.dp))
                    .border(1.dp, colors.accent1.copy(.2f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.HourglassEmpty, null,
                        tint = colors.accent1, modifier = Modifier.size(18.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Admin Approval Required",
                            fontWeight = FontWeight.Bold, color = colors.accent1,
                            style = MaterialTheme.typography.bodyMedium)
                        Text("After creating your account, an admin will review and approve it. You will be able to login once approved.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textPrimary, lineHeight = 16.sp)
                    }
                }

                // ── Section: Parent/Guardian Details ─────
                SectionHeader("Parent / Guardian Details", Icons.Default.Person, colors)

                SignupField("Full Name *", "Your full name",
                    name, { name = it }, Icons.Default.Person,
                    KeyboardType.Text, ImeAction.Next, focusManager, colors)

                // Username field with auto-generate button
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it.trim().lowercase() },
                        label = { Text("Username *") },
                        placeholder = { Text("Choose a unique username") },
                        leadingIcon = { Icon(Icons.Default.AlternateEmail, null, tint = colors.primary) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        colors = kidsTextFieldColors(colors)
                    )
                    // Auto-generate username button
                    OutlinedButton(
                        onClick = {
                            val base = name.trim().lowercase()
                                .replace(" ", "")
                                .filter { it.isLetterOrDigit() }
                                .take(8)
                            val suffix = (100..999).random()
                            username = if (base.isNotEmpty()) "${base}${suffix}" else "user${suffix}"
                        },
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, colors.primary.copy(.4f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(18.dp))
                    }
                }
                if (username.isNotEmpty()) {
                    Text("Username: @$username",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.primary, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp))
                }

                SignupField("Email Address *", "your@email.com",
                    email, { email = it.trim() }, Icons.Default.Email,
                    KeyboardType.Email, ImeAction.Next, focusManager, colors)

                SignupField("Phone Number *", "+91 XXXXX XXXXX",
                    phone, { phone = it }, Icons.Default.Phone,
                    KeyboardType.Phone, ImeAction.Next, focusManager, colors)

                // Password
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Password *") },
                    placeholder = { Text("Min 6 characters") },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = colors.primary) },
                    trailingIcon = {
                        IconButton(onClick = { passVisible = !passVisible }) {
                            Icon(if (passVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                null, tint = colors.textSecondary)
                        }
                    },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors = kidsTextFieldColors(colors)
                )

                // Confirm password
                OutlinedTextField(
                    value = confirmPassword, onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password *") },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = colors.primary) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    isError = confirmPassword.isNotEmpty() && confirmPassword != password,
                    supportingText = {
                        if (confirmPassword.isNotEmpty() && confirmPassword != password)
                            Text("Passwords do not match", color = colors.error)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors = kidsTextFieldColors(colors)
                )

                // ── Section: Child Details ────────────────
                Spacer(Modifier.height(4.dp))
                SectionHeader("Child's Details", Icons.Default.ChildCare, colors)

                SignupField("Child's Name", "Your child's name",
                    childName, { childName = it }, Icons.Default.Face,
                    KeyboardType.Text, ImeAction.Next, focusManager, colors)

                OutlinedTextField(
                    value = childAge, onValueChange = { if (it.length <= 2) childAge = it.filter { c -> c.isDigit() } },
                    label = { Text("Child's Age") },
                    placeholder = { Text("e.g. 7") },
                    leadingIcon = { Icon(Icons.Default.Cake, null, tint = colors.primary) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    colors = kidsTextFieldColors(colors)
                )

                // ── Error message ─────────────────────────
                AnimatedVisibility(state.error != null) {
                    Row(Modifier.fillMaxWidth()
                        .background(colors.error.copy(.1f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ErrorOutline, null,
                            tint = colors.error, modifier = Modifier.size(16.dp))
                        Text(state.error ?: "", color = colors.error,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }

                // ── Create Account button ─────────────────
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (password != confirmPassword) return@Button
                        viewModel.signup(
                            name = name, username = username,
                            email = email, phone = phone,
                            password = password,
                            childName = childName, age = childAge
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !state.isLoading && name.isNotBlank() && username.isNotBlank() &&
                            email.isNotBlank() && phone.isNotBlank() && password.isNotBlank() &&
                            password == confirmPassword,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        disabledContainerColor = colors.primary.copy(.4f))
                ) {
                    if (state.isLoading)
                        CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    else
                        Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// ── Success screen shown after signup ─────────
@Composable
private fun SignupSuccessScreen(onBackClick: () -> Unit) {
    val colors = MaterialTheme.appColors
    Box(Modifier.fillMaxSize().background(colors.background), contentAlignment = Alignment.Center) {
        Column(
            Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.CheckCircle, null,
                tint = colors.success, modifier = Modifier.size(72.dp))
            Text("Account Created!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold, color = colors.textPrimary,
                textAlign = TextAlign.Center)
            Text("Your account is now pending admin approval.\n\nOnce approved, you can login with your username and password.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary, textAlign = TextAlign.Center,
                lineHeight = 22.sp)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                Text("Back to Login", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Reusable field ─────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignupField(
    label: String, placeholder: String, value: String,
    onChange: (String) -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType, imeAction: ImeAction,
    focusManager: androidx.compose.ui.focus.FocusManager,
    colors: AppColorScheme
) {
    OutlinedTextField(
        value = value, onValueChange = onChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodySmall) },
        leadingIcon = { Icon(icon, null, tint = colors.primary) },
        singleLine = true, modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) },
            onDone = { focusManager.clearFocus() }
        ),
        colors = kidsTextFieldColors(colors)
    )
}

@Composable
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    colors: AppColorScheme
) {
    Row(
        Modifier.fillMaxWidth()
            .background(colors.primary.copy(.06f), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, null, tint = colors.primary, modifier = Modifier.size(18.dp))
        Text(title, fontWeight = FontWeight.Bold, color = colors.primary,
            style = MaterialTheme.typography.bodyMedium)
    }
}