package com.hybridclassifier.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hybridclassifier.app.data.remote.ExplanationData
import com.hybridclassifier.app.data.remote.PredictionResponse
import com.hybridclassifier.app.ui.theme.*
import com.hybridclassifier.app.ui.viewmodel.ClassifierViewModel
import com.hybridclassifier.app.ui.viewmodel.PredictState
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassifierScreen(
    onSettingsClick: () -> Unit,
    // Admin button completely removed from classifier screen
    // Admin only accessible via Login screen
    viewModel: ClassifierViewModel = hiltViewModel()
) {
    val state     by viewModel.state.collectAsState()
    val userName  by viewModel.userName.collectAsState(initial = "")
    val childName by viewModel.childName.collectAsState(initial = "")
    val colors    = MaterialTheme.appColors
    val context   = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedUri = it
            context.contentResolver.openInputStream(it)?.readBytes()
                ?.let { b -> viewModel.selectImage(b) }
        }
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("KidsLearn!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold, color = colors.primary)
                        val greeting = when {
                            childName.isNotEmpty() -> "Hi $childName!"
                            userName.isNotEmpty()  -> "Hi $userName!"
                            else -> "What will we discover today?"
                        }
                        Text(greeting, style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary)
                    }
                },
                actions = {
                    // Only settings button — NO admin button here
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, null, tint = colors.textSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    elevation = CardDefaults.cardElevation(2.dp)) {
                    if (selectedUri != null && state.selectedImageBytes != null) {
                        Box(Modifier.fillMaxWidth()) {
                            AsyncImage(
                                model = selectedUri, contentDescription = null,
                                modifier = Modifier.fillMaxWidth().height(260.dp)
                                    .clip(RoundedCornerShape(24.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Box(Modifier.align(Alignment.TopEnd).padding(12.dp)
                                .background(Color.Black.copy(.55f), CircleShape)
                                .clickable { galleryLauncher.launch("image/*") }
                                .padding(8.dp)) {
                                Icon(Icons.Default.Edit, null,
                                    tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    } else {
                        Box(Modifier.fillMaxWidth().height(220.dp)
                            .clickable { galleryLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(Modifier.size(80.dp).background(
                                    Brush.radialGradient(listOf(
                                        colors.secondary.copy(.35f), colors.primary.copy(.1f))),
                                    CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.AddAPhoto, null,
                                        tint = colors.primary, modifier = Modifier.size(36.dp))
                                }
                                Text("Tap to pick a photo!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                Text("Take any photo and discover what it is!",
                                    color = colors.textSecondary,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, colors.border),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Gallery", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { viewModel.predict() },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = state.selectedImageBytes != null &&
                                state.predictState !is PredictState.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            disabledContainerColor = colors.primary.copy(.4f))
                    ) {
                        AnimatedContent(state.predictState is PredictState.Loading, label = "") { loading ->
                            if (loading)
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    CircularProgressIndicator(Modifier.size(18.dp),
                                        color = Color.White, strokeWidth = 2.dp)
                                    Text("Analysing...", fontWeight = FontWeight.Bold)
                                }
                            else
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp))
                                    Text("Discover!", fontWeight = FontWeight.Bold)
                                }
                        }
                    }
                }
            }

            when (val ps = state.predictState) {
                is PredictState.Error -> item { ErrorCard(ps.message) }
                is PredictState.Success -> {
                    item { ResultCard(ps.result) }
                    val exp = ps.result.explanation
                    if (exp != null && (!exp.facts.isNullOrEmpty() || !exp.short.isNullOrEmpty())) {
                        item { FunFactsCard(exp, ps.result.finalClass) }
                        item { ExtraDetailsCard(exp) }
                    } else {
                        item { OfflineDetailsCard(ps.result.finalClass, ps.result.category) }
                    }
                    item {
                        TextButton(onClick = { viewModel.reset() }, Modifier.fillMaxWidth()) {
                            Text("Try Another Image", color = colors.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                else -> {}
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun ResultCard(result: PredictionResponse) {
    val colors   = MaterialTheme.appColors
    val catColor = categoryChipColor(result.category)
    val catEmoji = categoryEmoji(result.category)
    var showAlts by remember { mutableStateOf(false) }

    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

            Text("Identification Result",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = colors.textPrimary)

            Box(Modifier.background(catColor, RoundedCornerShape(50.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp)) {
                Text("$catEmoji  ${result.category.replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold, color = colors.textPrimary)
            }

            Box(Modifier.fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(
                        colors.primary.copy(.12f), colors.secondary.copy(.08f))),
                    RoundedCornerShape(18.dp)).padding(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Identified as",
                        style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    // Show display name if admin set one, else original class name
                    Text(result.displayName ?: result.finalClass,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold, color = colors.primary)
                    Text("Confidence: ${(result.finalClassConfidence * 100).roundToInt()}%",
                        style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    ConfidenceBar(result.finalClassConfidence, colors.primary)
                }
            }

            TextButton(onClick = { showAlts = !showAlts }, Modifier.fillMaxWidth()) {
                Text(if (showAlts) "Hide alternatives" else "Show other possibilities",
                    color = colors.textSecondary, style = MaterialTheme.typography.bodySmall)
            }
            AnimatedVisibility(showAlts) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    result.finalClassTop3.forEachIndexed { i, item ->
                        Row(Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Text("${i+1}.  ${item.label}",
                                style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                            Text("${(item.confidence * 100).roundToInt()}%",
                                style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                        }
                        ConfidenceBar(item.confidence, colors.primary.copy(.5f))
                    }
                }
            }
        }
    }
}

@Composable
private fun FunFactsCard(exp: ExplanationData, className: String) {
    val colors = MaterialTheme.appColors
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

            Text(exp.title ?: "About $className",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold, color = colors.textPrimary)

            exp.short?.let { short ->
                if (short.isNotEmpty()) {
                    Box(Modifier.fillMaxWidth()
                        .background(colors.secondary.copy(.1f), RoundedCornerShape(14.dp))
                        .padding(14.dp)) {
                        Text(short, style = MaterialTheme.typography.bodyMedium,
                            color = colors.textPrimary, lineHeight = 22.sp)
                    }
                }
            }

            if (!exp.facts.isNullOrEmpty()) {
                Text("Fun Facts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = colors.primary)
                exp.facts.forEachIndexed { i, fact ->
                    if (fact.isNotEmpty()) {
                        Row(Modifier.fillMaxWidth()
                            .background(colors.primary.copy(.06f), RoundedCornerShape(14.dp))
                            .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("${i+1}.", fontWeight = FontWeight.Bold,
                                color = colors.primary, style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.width(24.dp))
                            Text(fact, style = MaterialTheme.typography.bodyMedium,
                                color = colors.textPrimary, lineHeight = 22.sp)
                        }
                    }
                }
            }

            exp.didYouKnow?.let { dyk ->
                if (dyk.isNotEmpty()) {
                    Box(Modifier.fillMaxWidth()
                        .background(colors.accent3.copy(.3f), RoundedCornerShape(16.dp))
                        .border(1.dp, colors.accent3.copy(.5f), RoundedCornerShape(16.dp))
                        .padding(14.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Did You Know?",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            Text(dyk, style = MaterialTheme.typography.bodyMedium,
                                color = colors.textPrimary, lineHeight = 22.sp)
                        }
                    }
                }
            }

            exp.quiz?.let { quiz ->
                if (quiz.isNotEmpty()) {
                    Box(Modifier.fillMaxWidth()
                        .background(colors.tertiary.copy(.15f), RoundedCornerShape(16.dp))
                        .border(1.dp, colors.tertiary.copy(.3f), RoundedCornerShape(16.dp))
                        .padding(14.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Quiz Time!", style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            Text(quiz, style = MaterialTheme.typography.bodyMedium,
                                color = colors.textPrimary)
                        }
                    }
                }
            }

            exp.safetyNote?.let { note ->
                if (note.isNotEmpty()) {
                    Row(Modifier.fillMaxWidth()
                        .background(colors.success.copy(.12f), RoundedCornerShape(14.dp))
                        .border(1.dp, colors.success.copy(.3f), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HealthAndSafety, null,
                            tint = colors.success, modifier = Modifier.size(18.dp))
                        Text(note, style = MaterialTheme.typography.bodySmall,
                            color = colors.textPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExtraDetailsCard(exp: ExplanationData) {
    val colors = MaterialTheme.appColors
    val hasContent = listOf(exp.whereFound, exp.coolAbility, exp.funComparison)
        .any { !it.isNullOrEmpty() }
    if (!hasContent) return

    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("More Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = colors.textPrimary)
            exp.whereFound?.let { if (it.isNotEmpty()) DetailRow(Icons.Default.LocationOn, "Where to find it", it, colors.chipVehicle) }
            exp.coolAbility?.let { if (it.isNotEmpty()) DetailRow(Icons.Default.Star, "Special ability", it, colors.chipFlower) }
            exp.funComparison?.let { if (it.isNotEmpty()) DetailRow(Icons.Default.CompareArrows, "Fun comparison", it, colors.chipFruits) }
        }
    }
}

@Composable
private fun OfflineDetailsCard(className: String, category: String) {
    val colors = MaterialTheme.appColors
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("About $className",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold, color = colors.textPrimary)

            Box(Modifier.fillMaxWidth()
                .background(colors.secondary.copy(.1f), RoundedCornerShape(14.dp))
                .padding(14.dp)) {
                Text("$className belongs to the $category category. " +
                        "This is a great opportunity to learn more about it!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textPrimary, lineHeight = 22.sp)
            }

            Text("Learning Activities",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = colors.primary)

            listOf(
                "Ask your teacher or parent more about $className.",
                "Look up $className in a book or encyclopedia.",
                "Search for $className videos online with a parent.",
                "Try drawing a $className and show your friends.",
                "Find out if $className lives near you!"
            ).forEachIndexed { i, text ->
                Row(Modifier.fillMaxWidth()
                    .background(colors.primary.copy(.05f), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("${i+1}.", fontWeight = FontWeight.Bold, color = colors.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(24.dp))
                    Text(text, style = MaterialTheme.typography.bodyMedium,
                        color = colors.textPrimary, lineHeight = 22.sp)
                }
            }

            Row(Modifier.fillMaxWidth()
                .background(colors.accent1.copy(.12f), RoundedCornerShape(14.dp))
                .border(1.dp, colors.accent1.copy(.3f), RoundedCornerShape(14.dp))
                .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = colors.accent1, modifier = Modifier.size(18.dp))
                Text("Start Ollama on your server to get AI-powered fun facts.",
                    style = MaterialTheme.typography.bodySmall, color = colors.textPrimary)
            }
        }
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, bgColor: Color) {
    val colors = MaterialTheme.appColors
    Row(Modifier.fillMaxWidth()
        .background(bgColor.copy(.3f), RoundedCornerShape(14.dp)).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, tint = colors.primary, modifier = Modifier.size(20.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold, color = colors.textSecondary)
            Spacer(Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium,
                color = colors.textPrimary, lineHeight = 21.sp)
        }
    }
}

@Composable
private fun ConfidenceBar(value: Float, color: Color) {
    val animWidth by animateFloatAsState(value, tween(600, easing = EaseOut), label = "")
    val colors = MaterialTheme.appColors
    Box(Modifier.fillMaxWidth().height(8.dp).background(colors.border, RoundedCornerShape(4.dp))) {
        Box(Modifier.fillMaxWidth(animWidth).height(8.dp).background(color, RoundedCornerShape(4.dp)))
    }
}

@Composable
private fun ErrorCard(message: String) {
    val colors = MaterialTheme.appColors
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.error.copy(.1f)),
        border = BorderStroke(1.dp, colors.error.copy(.3f))) {
        Row(Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ErrorOutline, null,
                tint = colors.error, modifier = Modifier.size(22.dp))
            Text(message, color = colors.error,
                style = MaterialTheme.typography.bodyMedium)
        }
    }
}