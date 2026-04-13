package com.hybridclassifier.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.hybridclassifier.app.data.remote.*
import com.hybridclassifier.app.ui.theme.*
import com.hybridclassifier.app.ui.viewmodel.AdminUiState
import com.hybridclassifier.app.ui.viewmodel.AdminViewModel

// ═══════════════════════════════════════════════════════
//  ADMIN SCREEN — root composable
// ═══════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val state        by viewModel.state.collectAsState()
    val colors       = MaterialTheme.appColors
    var selectedTab  by remember { mutableStateOf(0) }
    val snackbar     = remember { SnackbarHostState() }

    val tabs = listOf(
        "Dashboard"    to Icons.Default.Dashboard,
        "Approvals"    to Icons.Default.HowToReg,
        "Users"        to Icons.Default.People,
        "Predictions"  to Icons.Default.History,
        "Custom Facts" to Icons.Default.Edit,
        "Classes"      to Icons.Default.Tune,
        "Training"     to Icons.Default.ModelTraining,
        "Logs"         to Icons.Default.List
    )

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }
    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showSnackbar("Error: $it"); viewModel.clearMessage() }
    }

    Scaffold(
        containerColor = colors.background,
        snackbarHost   = { SnackbarHost(snackbar) },
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text("Admin Console",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            Text("KidsLearn Management",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, null, tint = colors.textPrimary)
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.loadAll() }) {
                            Icon(Icons.Default.Refresh, null, tint = colors.primary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
                )
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor   = colors.surface,
                    contentColor     = colors.primary,
                    edgePadding      = 0.dp,
                    divider          = { HorizontalDivider(color = colors.border) }
                ) {
                    tabs.forEachIndexed { i, (label, icon) ->
                        Tab(
                            selected = selectedTab == i,
                            onClick  = { selectedTab = i },
                            modifier = Modifier.height(52.dp)
                        ) {
                            Row(
                                verticalAlignment    = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.padding(horizontal = 10.dp)
                            ) {
                                Icon(icon, null, modifier = Modifier.size(15.dp),
                                    tint = if (selectedTab == i) colors.primary else colors.textSecondary)
                                Text(label,
                                    style      = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedTab == i) FontWeight.Bold else FontWeight.Normal,
                                    color      = if (selectedTab == i) colors.primary else colors.textSecondary)
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CircularProgressIndicator(color = colors.primary, strokeWidth = 3.dp)
                    Text("Loading...", color = colors.textSecondary,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
            return@Scaffold
        }
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> AdminDashboardTab(state, colors)
                1 -> AdminApprovalsTab(state, colors, viewModel)
                2 -> AdminUsersTab(state, colors, viewModel)
                3 -> AdminPredictionsTab(state, colors)
                4 -> AdminCustomFactsTab(state, colors, viewModel)
                5 -> AdminClassSettingsTab(state, colors, viewModel)
                6 -> AdminTrainingTab(state, colors, viewModel)
                7 -> AdminLogsTab(state, colors)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
//  TAB 0 — Dashboard
// ═══════════════════════════════════════════════════════

@Composable
private fun AdminDashboardTab(state: AdminUiState, colors: AppColorScheme) {
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement  = Arrangement.spacedBy(14.dp),
        contentPadding       = PaddingValues(vertical = 16.dp)
    ) {
        // Pending approvals alert
        item {
            val pending = state.stats?.pendingApprovals ?: state.pendingUsers.size
            if (pending > 0) {
                Row(
                    Modifier.fillMaxWidth()
                        .background(colors.error.copy(.08f), RoundedCornerShape(12.dp))
                        .border(1.dp, colors.error.copy(.2f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.HowToReg, null,
                        tint = colors.error, modifier = Modifier.size(22.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "$pending user${if (pending > 1) "s" else ""} waiting for approval",
                            fontWeight = FontWeight.Bold, color = colors.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text("Go to the Approvals tab to review",
                            style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    }
                }
            }
        }

        // Metric row 1
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminMetricCard(Icons.Default.People,
                    state.stats?.totalUsers?.toString() ?: "—",
                    "Total Users", colors.chipVehicle, Modifier.weight(1f))
                AdminMetricCard(Icons.Default.ImageSearch,
                    state.stats?.totalPredictions?.toString() ?: "—",
                    "Total Scans", colors.chipFlower, Modifier.weight(1f))
            }
        }

        // Metric row 2
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminMetricCard(Icons.Default.TrendingUp,
                    state.stats?.activeUsersToday?.toString() ?: "—",
                    "Active Today", colors.chipSports, Modifier.weight(1f))
                AdminMetricCard(Icons.Default.Memory,
                    state.stats?.modelsLoaded?.toString() ?: "—",
                    "Models Ready", colors.chipFruits, Modifier.weight(1f))
            }
        }

        // Top predictions
        if (!state.stats?.topPredictions.isNullOrEmpty()) {
            item {
                AdminSectionCard("Top 10 Classifications", Icons.Default.BarChart, colors) {
                    state.stats!!.topPredictions.forEachIndexed { i, item ->
                        val cls = item["final_class"]?.toString() ?: ""
                        val cnt = item["cnt"]?.toString() ?: "0"
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier.size(24.dp).background(colors.primary.copy(.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${i + 1}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colors.primary, fontWeight = FontWeight.Bold)
                                }
                                Text(cls, style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textPrimary)
                            }
                            Surface(shape = RoundedCornerShape(50.dp), color = colors.primary.copy(.1f)) {
                                Text("$cnt scans",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelMedium, color = colors.primary)
                            }
                        }
                        if (i < 9) HorizontalDivider(color = colors.border, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
//  TAB 1 — Approvals
// ═══════════════════════════════════════════════════════

@Composable
private fun AdminApprovalsTab(state: AdminUiState, colors: AppColorScheme, vm: AdminViewModel) {
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding      = PaddingValues(vertical = 16.dp)
    ) {
        item {
            AdminInfoBanner(
                "Every new account requires your approval before login is allowed. Review and approve or reject each request.",
                Icons.Default.HowToReg, colors
            )
        }

        if (state.pendingUsers.isEmpty()) {
            item {
                AdminEmptyPlaceholder(
                    "No pending approvals",
                    "New signups will appear here for your review",
                    Icons.Default.CheckCircle, colors
                )
            }
        } else {
            item {
                Text(
                    "${state.pendingUsers.size} pending approval${if (state.pendingUsers.size > 1) "s" else ""}",
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = colors.error
                )
            }
            items(state.pendingUsers) { user ->
                AdminApprovalCard(user, colors, vm)
            }
        }
    }
}

@Composable
private fun AdminApprovalCard(user: Map<String, Any>, colors: AppColorScheme, vm: AdminViewModel) {
    val name      = user["name"]?.toString() ?: ""
    val username  = user["username"]?.toString() ?: ""
    val email     = user["email"]?.toString() ?: ""
    val phone     = user["phone"]?.toString() ?: ""
    val childName = user["child_name"]?.toString() ?: ""
    val age       = user["age"]?.toString() ?: ""
    val createdAt = (user["created_at"]?.toString() ?: "").take(16)

    Card(
        Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = colors.surface),
        border    = BorderStroke(1.dp, colors.accent1.copy(.3f)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Text(name, fontWeight = FontWeight.Bold,
                        color = colors.textPrimary, style = MaterialTheme.typography.titleMedium)
                    Text("@$username", style = MaterialTheme.typography.bodySmall, color = colors.primary)
                }
                Surface(shape = RoundedCornerShape(50.dp), color = colors.accent1.copy(.12f)) {
                    Text("Pending",
                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        style      = MaterialTheme.typography.labelMedium,
                        color      = colors.accent1, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = colors.border, thickness = 0.5.dp)

            // Details
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AdminApprovalRow(Icons.Default.Email, "Email", email, colors)
                if (phone.isNotEmpty())
                    AdminApprovalRow(Icons.Default.Phone, "Phone", phone, colors)
                if (childName.isNotEmpty())
                    AdminApprovalRow(Icons.Default.Face, "Child",
                        "$childName${if (age.isNotEmpty() && age != "0") ", Age $age" else ""}", colors)
                AdminApprovalRow(Icons.Default.Schedule, "Requested", createdAt, colors)
            }

            HorizontalDivider(color = colors.border, thickness = 0.5.dp)

            // Buttons
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick   = { vm.rejectUser(username) },
                    modifier  = Modifier.weight(1f).height(44.dp),
                    shape     = RoundedCornerShape(10.dp),
                    border    = BorderStroke(1.dp, colors.error.copy(.4f)),
                    colors    = ButtonDefaults.outlinedButtonColors(contentColor = colors.error)
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Reject", fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium)
                }
                Button(
                    onClick  = { vm.approveUser(username) },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = colors.success)
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Approve", fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun AdminApprovalRow(icon: ImageVector, label: String, value: String, colors: AppColorScheme) {
    if (value.isBlank()) return
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = colors.textSecondary, modifier = Modifier.size(15.dp))
        Text("$label: ", style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary, fontWeight = FontWeight.SemiBold)
        Text(value, style = MaterialTheme.typography.bodySmall, color = colors.textPrimary)
    }
}

// ═══════════════════════════════════════════════════════
//  TAB 2 — Users
// ═══════════════════════════════════════════════════════

@Composable
private fun AdminUsersTab(state: AdminUiState, colors: AppColorScheme, vm: AdminViewModel) {
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding      = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text("${state.users.size} registered accounts",
                style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
        }
        items(state.users) { user ->
            Card(
                Modifier.fillMaxWidth(),
                shape  = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (user.isActive == 0) colors.error.copy(.05f) else colors.surface),
                border    = if (user.isActive == 0) BorderStroke(1.dp, colors.error.copy(.2f)) else null,
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Row(
                        Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(42.dp).background(colors.primary.copy(.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                user.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.primary
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Text(user.name, fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary, style = MaterialTheme.typography.bodyMedium)
                                if (user.role == "admin") {
                                    Surface(shape = RoundedCornerShape(4.dp), color = colors.primary.copy(.12f)) {
                                        Text("Admin",
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                            style = MaterialTheme.typography.labelMedium, color = colors.primary)
                                    }
                                }
                            }
                            Text("@${user.username}  ·  ${user.predictionCount} scans",
                                style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                            Text(user.email, style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    if (user.role != "admin") {
                        Column(
                            horizontalAlignment  = Alignment.CenterHorizontally,
                            verticalArrangement  = Arrangement.spacedBy(2.dp)
                        ) {
                            Switch(
                                checked         = user.isActive == 1,
                                onCheckedChange = { vm.toggleUser(user.username) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor   = Color.White,
                                    checkedTrackColor   = colors.success,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = colors.error.copy(.5f))
                            )
                            Text(
                                if (user.isActive == 1) "Active" else "Blocked",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (user.isActive == 1) colors.success else colors.error
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
//  TAB 3 — Predictions
// ═══════════════════════════════════════════════════════

@Composable
private fun AdminPredictionsTab(state: AdminUiState, colors: AppColorScheme) {
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
        contentPadding      = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text("Last ${state.predictions.size} predictions",
                style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
        }
        items(state.predictions) { pred ->
            Card(
                Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(pred.finalClass, fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary, style = MaterialTheme.typography.bodyMedium)
                        Text("${pred.category}  ·  @${pred.username}",
                            style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                        Text(pred.createdAt.take(16),
                            style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    }
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("${(pred.clsConfidence * 100).toInt()}%",
                            fontWeight = FontWeight.Bold, color = colors.primary,
                            style = MaterialTheme.typography.bodyMedium)
                        Text("${pred.inferenceMs.toInt()} ms",
                            style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
//  TAB 4 — Custom Facts
// ═══════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminCustomFactsTab(state: AdminUiState, colors: AppColorScheme, vm: AdminViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<Map<String, Any>?>(null) }

    if (showDialog || editTarget != null) {
        AdminFactsDialog(
            existing  = editTarget,
            colors    = colors,
            onDismiss = { showDialog = false; editTarget = null },
            onSave    = { vm.saveCustomFacts(it); showDialog = false; editTarget = null }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = colors.primary) {
                Icon(Icons.Default.Add, null, tint = Color.White)
            }
        }
    ) { inner ->
        LazyColumn(
            Modifier.fillMaxSize().padding(inner).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding      = PaddingValues(vertical = 16.dp)
        ) {
            item {
                AdminInfoBanner(
                    "Custom facts override AI-generated content. They take priority over Ollama for the specified class.",
                    Icons.Default.Info, colors
                )
            }
            if (state.customFacts.isEmpty()) {
                item { AdminEmptyPlaceholder("No custom facts yet", "Tap + to write facts for any class", Icons.Default.Edit, colors) }
            }
            items(state.customFacts) { fact ->
                val cn    = fact["class_name"]?.toString() ?: ""
                val title = fact["title"]?.toString() ?: ""
                Card(
                    Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(cn, fontWeight = FontWeight.Bold, color = colors.primary,
                                style = MaterialTheme.typography.bodyMedium)
                            if (title.isNotEmpty())
                                Text(title, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                            Text(
                                "By ${fact["updated_by"]}  ·  ${(fact["updated_at"]?.toString() ?: "").take(10)}",
                                style = MaterialTheme.typography.bodySmall, color = colors.textSecondary
                            )
                        }
                        Row {
                            IconButton(onClick = { editTarget = fact }) {
                                Icon(Icons.Default.Edit, null, tint = colors.secondary, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { vm.deleteCustomFacts(cn) }) {
                                Icon(Icons.Default.Delete, null, tint = colors.error, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

// ═══════════════════════════════════════════════════════
//  TAB 5 — Class Settings
// ═══════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminClassSettingsTab(state: AdminUiState, colors: AppColorScheme, vm: AdminViewModel) {
    var showDialog  by remember { mutableStateOf(false) }
    var targetClass by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var isHidden    by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor   = colors.surface,
            title = { Text("Class: $targetClass", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value          = displayName,
                        onValueChange  = { displayName = it },
                        label          = { Text("Display Name") },
                        placeholder    = { Text("Leave blank to use original") },
                        modifier       = Modifier.fillMaxWidth(),
                        shape          = RoundedCornerShape(12.dp),
                        colors         = kidsTextFieldColors(colors)
                    )
                    Row(
                        Modifier.fillMaxWidth()
                            .background(colors.card, RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Hide from predictions", fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary, style = MaterialTheme.typography.bodyMedium)
                            Text("Class will not appear in any results",
                                style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                        }
                        Switch(
                            checked         = isHidden,
                            onCheckedChange = { isHidden = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White, checkedTrackColor = colors.error)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.updateClassSetting(ClassSettingRequest(targetClass, displayName, isHidden))
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) { Text("Save", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        )
    }

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding      = PaddingValues(vertical = 16.dp)
    ) {
        item {
            AdminInfoBanner(
                "Tap any row to rename a class or hide it. Hidden classes are excluded from all predictions.",
                Icons.Default.Tune, colors
            )
        }
        if (state.classSettings.isEmpty()) {
            item { AdminEmptyPlaceholder("No class settings", "All classes visible by default", Icons.Default.Tune, colors) }
        }
        items(state.classSettings) { s ->
            val cn     = s["class_name"]?.toString() ?: ""
            val dn     = s["display_name"]?.toString() ?: ""
            val hidden = s["is_hidden"].toString() == "1"
            Card(
                Modifier.fillMaxWidth().clickable {
                    targetClass = cn; displayName = dn; isHidden = hidden; showDialog = true
                },
                shape  = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (hidden) colors.error.copy(.06f) else colors.surface),
                border    = if (hidden) BorderStroke(1.dp, colors.error.copy(.2f)) else null,
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(cn, fontWeight = FontWeight.SemiBold,
                            color = if (hidden) colors.error else colors.textPrimary,
                            style = MaterialTheme.typography.bodyMedium)
                        if (dn.isNotEmpty())
                            Text("Shown as: $dn",
                                style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        if (hidden) {
                            Surface(shape = RoundedCornerShape(50.dp), color = colors.error.copy(.1f)) {
                                Text("Hidden",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelMedium, color = colors.error)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, null,
                            tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
//  TAB 6 — Training (Add Class / Category)
// ═══════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminTrainingTab(state: AdminUiState, colors: AppColorScheme, vm: AdminViewModel) {
    var subTab by remember { mutableStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = subTab,
            containerColor   = colors.card,
            contentColor     = colors.primary
        ) {
            Tab(selected = subTab == 0, onClick = { subTab = 0 }, modifier = Modifier.height(44.dp)) {
                Text("Add Class",
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = if (subTab == 0) FontWeight.Bold else FontWeight.Normal,
                    color      = if (subTab == 0) colors.primary else colors.textSecondary)
            }
            Tab(selected = subTab == 1, onClick = { subTab = 1 }, modifier = Modifier.height(44.dp)) {
                Text("Add Category",
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = if (subTab == 1) FontWeight.Bold else FontWeight.Normal,
                    color      = if (subTab == 1) colors.primary else colors.textSecondary)
            }
        }
        when (subTab) {
            0 -> AdminAddClassTab(state, colors, vm)
            1 -> AdminAddCategoryTab(state, colors, vm)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminAddClassTab(state: AdminUiState, colors: AppColorScheme, vm: AdminViewModel) {
    val context = LocalContext.current
    var categoryName   by remember { mutableStateOf("") }
    var className      by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<ByteArray>>(emptyList()) }
    var expanded       by remember { mutableStateOf(false) }

    val existingCategories = listOf(
        "Animals", "Food", "Flower", "Vehicle", "Weather",
        "Monument", "sports_equipments", "fruits_and_vegetables", "Flags"
    )
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImages = uris.mapNotNull { uri ->
            try { context.contentResolver.openInputStream(uri)?.readBytes() } catch (e: Exception) { null }
        }
    }

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding      = PaddingValues(vertical = 16.dp)
    ) {
        item {
            AdminInfoBanner(
                "Add a new class to an existing category. Min 5 images required. No retraining needed.",
                Icons.Default.Info, colors
            )
        }
        item {
            AdminSectionCard("Class Details", Icons.Default.Edit, colors) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value         = categoryName,
                        onValueChange = { categoryName = it },
                        label         = { Text("Select Category *") },
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier      = Modifier.fillMaxWidth().menuAnchor(),
                        shape         = RoundedCornerShape(12.dp),
                        colors        = kidsTextFieldColors(colors)
                    )
                    ExposedDropdownMenu(
                        expanded          = expanded,
                        onDismissRequest  = { expanded = false },
                        modifier          = Modifier.background(colors.surface)
                    ) {
                        existingCategories.forEach { cat ->
                            DropdownMenuItem(
                                text    = { Text(cat, color = colors.textPrimary) },
                                onClick = { categoryName = cat; expanded = false }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                AdminInputField("New Class Name *", "e.g. Skateboard, Helicopter",
                    className, { className = it }, colors)
            }
        }
        item { AdminImagePickerSection(selectedImages, galleryLauncher, colors) }
        item {
            Button(
                onClick = {
                    if (categoryName.isNotBlank() && className.isNotBlank() && selectedImages.size >= 5) {
                        vm.addNewClass(categoryName, className, selectedImages)
                        categoryName = ""; className = ""; selectedImages = emptyList()
                    }
                },
                enabled  = categoryName.isNotBlank() && className.isNotBlank() &&
                        selectedImages.size >= 5 && !state.isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = colors.primary,
                    disabledContainerColor = colors.primary.copy(.4f))
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Adding...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add Class", fontWeight = FontWeight.Bold)
                }
            }
        }
        val classEntries = state.addedEntries.filter { it["entry_type"]?.toString() == "new_class" }
        if (classEntries.isNotEmpty()) {
            item {
                Text("Recent Additions", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = colors.textPrimary)
            }
            items(classEntries.take(5)) { entry -> AdminEntryCard(entry, colors) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminAddCategoryTab(state: AdminUiState, colors: AppColorScheme, vm: AdminViewModel) {
    val context = LocalContext.current
    var categoryName   by remember { mutableStateOf("") }
    var firstClassName by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<ByteArray>>(emptyList()) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImages = uris.mapNotNull { uri ->
            try { context.contentResolver.openInputStream(uri)?.readBytes() } catch (e: Exception) { null }
        }
    }

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding      = PaddingValues(vertical = 16.dp)
    ) {
        item {
            AdminInfoBanner(
                "Add a completely new category with its first class. Min 5 images required.",
                Icons.Default.Info, colors
            )
        }
        item {
            AdminSectionCard("Category Details", Icons.Default.Category, colors) {
                AdminInputField("New Category Name *", "e.g. Insects, Birds, Musical Instruments",
                    categoryName, { categoryName = it }, colors)
                Spacer(Modifier.height(12.dp))
                AdminInputField("First Class Name *", "e.g. Butterfly",
                    firstClassName, { firstClassName = it }, colors)
            }
        }
        item { AdminImagePickerSection(selectedImages, galleryLauncher, colors) }
        item {
            Button(
                onClick = {
                    if (categoryName.isNotBlank() && firstClassName.isNotBlank() && selectedImages.size >= 5) {
                        vm.addNewCategory(categoryName, firstClassName, selectedImages)
                        categoryName = ""; firstClassName = ""; selectedImages = emptyList()
                    }
                },
                enabled  = categoryName.isNotBlank() && firstClassName.isNotBlank() &&
                        selectedImages.size >= 5 && !state.isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = colors.primary,
                    disabledContainerColor = colors.primary.copy(.4f))
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Creating...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add New Category", fontWeight = FontWeight.Bold)
                }
            }
        }
        val catEntries = state.addedEntries.filter { it["entry_type"]?.toString() == "new_category" }
        if (catEntries.isNotEmpty()) {
            item {
                Text("Recently Added Categories", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = colors.textPrimary)
            }
            items(catEntries.take(5)) { entry -> AdminEntryCard(entry, colors) }
        }
    }
}

@Composable
private fun AdminImagePickerSection(
    selectedImages: List<ByteArray>,
    launcher: androidx.activity.result.ActivityResultLauncher<String>,
    colors: AppColorScheme
) {
    AdminSectionCard("Upload Images (min 5)", Icons.Default.Image, colors) {
        if (selectedImages.isNotEmpty()) {
            Row(
                Modifier.fillMaxWidth()
                    .background(colors.success.copy(.1f), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = colors.success, modifier = Modifier.size(20.dp))
                Column {
                    Text("${selectedImages.size} images selected",
                        fontWeight = FontWeight.Bold, color = colors.success,
                        style = MaterialTheme.typography.bodyMedium)
                    Text(
                        if (selectedImages.size >= 5) "Ready to upload"
                        else "Need ${5 - selectedImages.size} more images",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selectedImages.size >= 5) colors.success else colors.error
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        OutlinedButton(
            onClick  = { launcher.launch("image/*") },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape    = RoundedCornerShape(12.dp),
            border   = BorderStroke(1.dp, colors.primary.copy(.4f)),
            colors   = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary)
        ) {
            Icon(Icons.Default.AddPhotoAlternate, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                if (selectedImages.isEmpty()) "Select Images from Gallery" else "Change Selected Images",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
//  TAB 7 — Logs
// ═══════════════════════════════════════════════════════

@Composable
private fun AdminLogsTab(state: AdminUiState, colors: AppColorScheme) {
    val actionColors = mapOf(
        "LOGIN"          to colors.success,
        "SIGNUP"         to colors.secondary,
        "APPROVE_USER"   to colors.success,
        "REJECT_USER"    to colors.error,
        "TOGGLE_USER"    to colors.accent1,
        "CUSTOM_FACTS"   to colors.primary,
        "DELETE_FACTS"   to colors.error,
        "HIDE_CLASS"     to colors.error,
        "SHOW_CLASS"     to colors.success,
        "ADD_CLASS"      to colors.secondary,
        "ADD_CATEGORY"   to colors.tertiary
    )
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        contentPadding      = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text("${state.logs.size} recent activities",
                style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
        }
        if (state.logs.isEmpty()) {
            item { AdminEmptyPlaceholder("No activity yet", "Actions will appear here", Icons.Default.List, colors) }
        }
        items(state.logs) { log ->
            val action = log["action"]?.toString() ?: ""
            val aColor = actionColors[action] ?: colors.textSecondary
            Card(
                Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(7.dp).background(aColor, CircleShape))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text("@${log["username"] ?: ""}",
                                fontWeight = FontWeight.SemiBold, color = colors.textPrimary,
                                style = MaterialTheme.typography.bodySmall)
                            Surface(shape = RoundedCornerShape(4.dp), color = aColor.copy(.1f)) {
                                Text(action,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    style = MaterialTheme.typography.labelMedium, color = aColor)
                            }
                        }
                        val detail = log["detail"]?.toString() ?: ""
                        if (detail.isNotEmpty())
                            Text(detail, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    }
                    Text(
                        (log["created_at"]?.toString() ?: "").take(16),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary, textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
//  DIALOGS
// ═══════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminFactsDialog(
    existing: Map<String, Any>?,
    colors: AppColorScheme,
    onDismiss: () -> Unit,
    onSave: (CustomFactsRequest) -> Unit
) {
    var className   by remember { mutableStateOf(existing?.get("class_name")?.toString() ?: "") }
    var category    by remember { mutableStateOf(existing?.get("category")?.toString() ?: "") }
    var title       by remember { mutableStateOf(existing?.get("title")?.toString() ?: "") }
    var shortDesc   by remember { mutableStateOf(existing?.get("short_desc")?.toString() ?: "") }
    var factsRaw    by remember {
        mutableStateOf(
            try {
                val r = existing?.get("facts")?.toString() ?: ""
                if (r.startsWith("[")) r.trim('[', ']').split("\",\"").joinToString("\n") { it.trim('"') } else r
            } catch (e: Exception) { "" }
        )
    }
    var quiz        by remember { mutableStateOf(existing?.get("quiz")?.toString() ?: "") }
    var safetyNote  by remember { mutableStateOf(existing?.get("safety_note")?.toString() ?: "") }
    var didYouKnow  by remember { mutableStateOf(existing?.get("did_you_know")?.toString() ?: "") }
    var whereFound  by remember { mutableStateOf(existing?.get("where_found")?.toString() ?: "") }
    var coolAbility by remember { mutableStateOf(existing?.get("cool_ability")?.toString() ?: "") }
    var comparison  by remember { mutableStateOf(existing?.get("fun_comparison")?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = colors.surface,
        modifier         = Modifier.fillMaxWidth(),
        title = {
            Text(if (existing != null) "Edit Custom Facts" else "Add Custom Facts",
                color = colors.textPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminInputField("Class Name *", "", className, { className = it }, colors, enabled = existing == null)
                AdminInputField("Category", "", category, { category = it }, colors)
                AdminInputField("Title", "", title, { title = it }, colors)
                AdminInputField("Introduction (2-3 sentences)", "", shortDesc, { shortDesc = it }, colors, lines = 3)
                AdminInputField("Fun Facts (one per line, min 5)", "", factsRaw, { factsRaw = it }, colors, lines = 5)
                AdminInputField("Quiz Question", "", quiz, { quiz = it }, colors)
                AdminInputField("Safety Note", "", safetyNote, { safetyNote = it }, colors)
                AdminInputField("Did You Know", "", didYouKnow, { didYouKnow = it }, colors)
                AdminInputField("Where to Find It", "", whereFound, { whereFound = it }, colors)
                AdminInputField("Special Ability", "", coolAbility, { coolAbility = it }, colors)
                AdminInputField("Fun Comparison", "", comparison, { comparison = it }, colors)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(CustomFactsRequest(
                        className      = className, category = category, title = title,
                        shortDesc      = shortDesc,
                        facts          = factsRaw.lines().map { it.trim() }.filter { it.isNotEmpty() },
                        quiz           = quiz, safetyNote = safetyNote, didYouKnow = didYouKnow,
                        whereFound     = whereFound, coolAbility = coolAbility, funComparison = comparison
                    ))
                },
                enabled = className.isNotEmpty(),
                colors  = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) { Text("Save", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = colors.textSecondary) }
        }
    )
}

// ═══════════════════════════════════════════════════════
//  SHARED HELPER COMPOSABLES
// ═══════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminInputField(
    label: String, placeholder: String, value: String,
    onChange: (String) -> Unit, colors: AppColorScheme,
    lines: Int = 1, enabled: Boolean = true
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onChange,
        label         = { Text(label) },
        placeholder   = if (placeholder.isNotEmpty()) ({ Text(placeholder, style = MaterialTheme.typography.bodySmall) }) else null,
        modifier      = Modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(12.dp),
        minLines      = lines,
        maxLines      = lines + 2,
        enabled       = enabled,
        colors        = kidsTextFieldColors(colors)
    )
}

@Composable
private fun AdminSectionCard(
    title: String, icon: ImageVector, colors: AppColorScheme,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(icon, null, tint = colors.primary, modifier = Modifier.size(20.dp))
                Text(title, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = colors.textPrimary)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colors.border)
            content()
        }
    }
}

@Composable
private fun AdminMetricCard(icon: ImageVector, value: String, label: String, bg: Color, modifier: Modifier) {
    val colors = MaterialTheme.appColors
    Card(
        modifier, shape = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = bg.copy(.3f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(icon, null, tint = colors.primary, modifier = Modifier.size(22.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold, color = colors.textPrimary)
            Text(label, style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun AdminInfoBanner(text: String, icon: ImageVector, colors: AppColorScheme) {
    Row(
        Modifier.fillMaxWidth()
            .background(colors.secondary.copy(.08f), RoundedCornerShape(10.dp))
            .border(1.dp, colors.secondary.copy(.2f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, null, tint = colors.secondary, modifier = Modifier.size(16.dp))
        Text(text, style = MaterialTheme.typography.bodySmall,
            color = colors.textPrimary, lineHeight = 18.sp)
    }
}

@Composable
private fun AdminEmptyPlaceholder(title: String, subtitle: String, icon: ImageVector, colors: AppColorScheme) {
    Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, null, tint = colors.textSecondary.copy(.4f), modifier = Modifier.size(44.dp))
            Text(title, style = MaterialTheme.typography.titleMedium,
                color = colors.textSecondary, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun AdminEntryCard(entry: Map<String, Any>, colors: AppColorScheme) {
    val status = entry["status"]?.toString() ?: ""
    val (statusColor, statusLabel) = when (status) {
        "success"    -> colors.success   to "Added"
        "processing" -> colors.secondary to "Processing"
        "failed"     -> colors.error     to "Failed"
        else         -> colors.textSecondary to status
    }
    Card(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = colors.surface),
        border    = BorderStroke(1.dp, statusColor.copy(.2f)),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                val cat  = entry["category_name"]?.toString() ?: ""
                val cls  = entry["class_name"]?.toString() ?: ""
                val imgs = entry["image_count"]?.toString() ?: "0"
                val date = (entry["created_at"]?.toString() ?: "").take(16)
                Text(if (cls.isNotEmpty()) "$cat  →  $cls" else cat,
                    fontWeight = FontWeight.SemiBold, color = colors.textPrimary,
                    style = MaterialTheme.typography.bodyMedium)
                Text("$imgs images  ·  $date",
                    style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                val error = entry["error_message"]?.toString() ?: ""
                if (error.isNotEmpty() && status == "failed")
                    Text(error, style = MaterialTheme.typography.bodySmall, color = colors.error)
            }
            Surface(shape = RoundedCornerShape(50.dp), color = statusColor.copy(.12f)) {
                Text(statusLabel,
                    modifier   = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    style      = MaterialTheme.typography.labelMedium,
                    color      = statusColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}