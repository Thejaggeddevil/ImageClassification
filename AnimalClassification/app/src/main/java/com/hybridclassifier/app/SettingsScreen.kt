package com.hybridclassifier.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.hybridclassifier.app.ui.theme.*
import com.hybridclassifier.app.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state    by viewModel.state.collectAsState()
    val darkMode by viewModel.darkMode.collectAsState(initial = false)
    val role     by viewModel.role.collectAsState(initial = "user")
    val colors   = MaterialTheme.appColors
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = colors.surface,
            icon = { Text("👋", fontSize = 36.sp) },
            title = { Text("Leaving so soon?", fontWeight = FontWeight.Bold, color = colors.textPrimary) },
            text = { Text("Are you sure you want to log out?", color = colors.textSecondary) },
            confirmButton = {
                Button(onClick = { viewModel.logout(); onLogout() },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.error)) {
                    Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Stay! 🎉", color = colors.primary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("Settings ⚙️", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold, color = colors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // Avatar / profile header
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(2.dp)) {
                Box(Modifier.fillMaxWidth().background(
                    Brush.horizontalGradient(listOf(colors.primary.copy(.15f), colors.secondary.copy(.1f))),
                    RoundedCornerShape(24.dp))) {
                    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(80.dp).background(colors.primary.copy(.2f), CircleShape),
                            contentAlignment = Alignment.Center) {
                            Text(if (state.name.isNotEmpty()) state.name.first().uppercaseChar().toString() else "👤",
                                fontSize = 36.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                        }
                        Text(state.name.ifEmpty { "Your Name" }, style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold, color = colors.textPrimary)
                        Text(state.email.ifEmpty { "email@example.com" }, style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary)
                        if (role == "admin") {
                            Box(Modifier.background(colors.primary.copy(.2f), RoundedCornerShape(50.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)) {
                                Text("🛡️ Admin", style = MaterialTheme.typography.labelMedium,
                                    color = colors.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Profile edit section
            SettingsSection(title = "👤 My Profile") {
                KidsTextField(state.name, { viewModel.updateName(it) }, "Full Name", "🙋", KeyboardType.Text)
                Spacer(Modifier.height(12.dp))
                KidsTextField(state.email, { viewModel.updateEmail(it) }, "Email", "📧", KeyboardType.Email)
                Spacer(Modifier.height(12.dp))
                KidsTextField(state.childName, { viewModel.updateChildName(it) }, "Child's Name", "⭐", KeyboardType.Text)
                Spacer(Modifier.height(12.dp))
                KidsTextField(state.age.let { if (it == 0) "" else it.toString() },
                    { viewModel.updateAge(it) }, "Child's Age", "🎂", KeyboardType.Number)
                Spacer(Modifier.height(16.dp))

                AnimatedVisibility(state.isSaved) {
                    Row(Modifier.fillMaxWidth().background(colors.success.copy(.15f), RoundedCornerShape(12.dp))
                        .padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("✅", fontSize = 16.sp)
                        Text("Profile saved!", color = colors.success, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(8.dp))

                Button(onClick = { viewModel.save() }, modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)) {
                    Text("💾  Save Changes", fontWeight = FontWeight.Bold)
                }
            }

            // Appearance
            SettingsSection(title = "🎨 Appearance") {
                Row(Modifier.fillMaxWidth().background(colors.card, RoundedCornerShape(16.dp)).padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (darkMode) "🌙" else "☀️", fontSize = 24.sp)
                        Column {
                            Text(if (darkMode) "Dark Mode" else "Light Mode",
                                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                                color = colors.textPrimary)
                            Text(if (darkMode) "Easy on the eyes at night" else "Bright and cheerful!",
                                style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                        }
                    }
                    Switch(checked = darkMode, onCheckedChange = { viewModel.setDarkMode(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = colors.primary))
                }

                Spacer(Modifier.height(12.dp))
                // Theme preview row
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("☀️ Light" to LightAppColors.primary, "🌙 Dark" to DarkAppColors.primary).forEach { (label, color) ->
                        val isSelected = if (label.contains("Light")) !darkMode else darkMode
                        Box(Modifier.weight(1f)
                            .border(2.dp, if (isSelected) color else Color.Transparent, RoundedCornerShape(14.dp))
                            .background(color.copy(.12f), RoundedCornerShape(14.dp))
                            .padding(12.dp),
                            contentAlignment = Alignment.Center) {
                            Text(label, style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) color else colors.textSecondary)
                        }
                    }
                }
            }

            // About
            SettingsSection(title = "ℹ️ About") {
                InfoRow("📱 App Version", "1.0.0")
                InfoRow("🎓 Made for", "Little Learners!")
                InfoRow("🤖 AI Model", "EfficientNet + Ollama")
                InfoRow("📊 Categories", "9 categories, 200+ classes")
            }

            // Logout
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.error.copy(.15f))
            ) {
                Text("👋  Log Out", color = colors.error, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.appColors
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                color = colors.textPrimary)
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val colors = MaterialTheme.appColors
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
    }
    HorizontalDivider(color = colors.border, thickness = 0.5.dp)
}
