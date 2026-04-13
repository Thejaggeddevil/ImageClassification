package com.hybridclassifier.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

object PastelColors {
    val AccentPurple     = Color(0xFF9B6DD4)
    val AccentBlue       = Color(0xFF5BB8F5)
    val AccentPink       = Color(0xFFE87DC8)
    val AccentGreen      = Color(0xFF4ECBA0)
    val AccentOrange     = Color(0xFFF5A342)
    val AccentYellow     = Color(0xFFE8C832)
    val BackgroundLight  = Color(0xFFFFFBF5)
    val SurfaceLight     = Color(0xFFFFFFFF)
    val CardLight        = Color(0xFFF8F4FF)
    val TextPrimaryLight   = Color(0xFF2D2D3A)
    val TextSecondaryLight = Color(0xFF7A7A8C)
    val BackgroundDark   = Color(0xFF1A1A2E)
    val SurfaceDark      = Color(0xFF24243E)
    val CardDark         = Color(0xFF2E2E4A)
    val TextPrimaryDark    = Color(0xFFF0EEFF)
    val TextSecondaryDark  = Color(0xFFADADC8)
    val DarkAccentPurple = Color(0xFFC4A0F0)
    val DarkAccentBlue   = Color(0xFF90CCF4)
    val DarkAccentPink   = Color(0xFFF4AEDD)
    val DarkAccentGreen  = Color(0xFF86E8C8)
    val DarkAccentOrange = Color(0xFFF5C080)
}

data class AppColorScheme(
    val background: Color, val surface: Color, val card: Color,
    val primary: Color, val secondary: Color, val tertiary: Color,
    val accent1: Color, val accent2: Color, val accent3: Color,
    val textPrimary: Color, val textSecondary: Color, val border: Color,
    val success: Color, val error: Color,
    val chipAnimal: Color, val chipFood: Color, val chipFlower: Color,
    val chipVehicle: Color, val chipWeather: Color, val chipMonument: Color,
    val chipSports: Color, val chipFruits: Color, val chipFlags: Color,
)

val LightAppColors = AppColorScheme(
    background  = PastelColors.BackgroundLight,
    surface     = PastelColors.SurfaceLight,
    card        = PastelColors.CardLight,
    primary     = PastelColors.AccentPurple,
    secondary   = PastelColors.AccentBlue,
    tertiary    = PastelColors.AccentPink,
    accent1     = PastelColors.AccentOrange,
    accent2     = PastelColors.AccentGreen,
    accent3     = PastelColors.AccentYellow,
    textPrimary    = PastelColors.TextPrimaryLight,
    textSecondary  = PastelColors.TextSecondaryLight,
    border      = Color(0xFFE8E0F5),
    success     = Color(0xFF4ECBA0),
    error       = Color(0xFFE87878),
    chipAnimal   = Color(0xFFFFD9B8),
    chipFood     = Color(0xFFFFB8C8),
    chipFlower   = Color(0xFFF5C6E8),
    chipVehicle  = Color(0xFFB8E4F9),
    chipWeather  = Color(0xFFB8D4F9),
    chipMonument = Color(0xFFDDB8F5),
    chipSports   = Color(0xFFB8F0D8),
    chipFruits   = Color(0xFFFFF0B3),
    chipFlags    = Color(0xFFFFD4B8),
)

val DarkAppColors = AppColorScheme(
    background  = PastelColors.BackgroundDark,
    surface     = PastelColors.SurfaceDark,
    card        = PastelColors.CardDark,
    primary     = PastelColors.DarkAccentPurple,
    secondary   = PastelColors.DarkAccentBlue,
    tertiary    = PastelColors.DarkAccentPink,
    accent1     = PastelColors.DarkAccentOrange,
    accent2     = PastelColors.DarkAccentGreen,
    accent3     = Color(0xFFF5E090),
    textPrimary    = PastelColors.TextPrimaryDark,
    textSecondary  = PastelColors.TextSecondaryDark,
    border      = Color(0xFF3A3A5C),
    success     = Color(0xFF86E8C8),
    error       = Color(0xFFF4AEAD),
    chipAnimal   = Color(0xFF4A3020),
    chipFood     = Color(0xFF4A2030),
    chipFlower   = Color(0xFF3A2040),
    chipVehicle  = Color(0xFF1A3050),
    chipWeather  = Color(0xFF1A2850),
    chipMonument = Color(0xFF2A1A40),
    chipSports   = Color(0xFF1A3028),
    chipFruits   = Color(0xFF3A3010),
    chipFlags    = Color(0xFF3A2010),
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

// ── Typography — FontFamily.Default works on all Android devices ──
// FontFamily.Rounded does NOT exist on Android (it's iOS only)
val KidsTypography = Typography(
    displayLarge  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold,      fontSize = 28.sp),
    headlineMedium= TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold,      fontSize = 22.sp),
    titleLarge    = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold,  fontSize = 18.sp),
    titleMedium   = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold,  fontSize = 16.sp),
    bodyLarge     = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,    fontSize = 16.sp),
    bodyMedium    = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,    fontSize = 14.sp),
    bodySmall     = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,    fontSize = 12.sp),
    labelLarge    = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold,      fontSize = 14.sp),
    labelMedium   = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,    fontSize = 12.sp),
)

@Composable
fun KidsLearningTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    val materialColors = if (darkTheme) darkColorScheme(
        primary      = appColors.primary,
        secondary    = appColors.secondary,
        tertiary     = appColors.tertiary,
        background   = appColors.background,
        surface      = appColors.surface,
        onPrimary    = Color.White,
        onBackground = appColors.textPrimary,
        onSurface    = appColors.textPrimary,
        error        = appColors.error
    ) else lightColorScheme(
        primary      = appColors.primary,
        secondary    = appColors.secondary,
        tertiary     = appColors.tertiary,
        background   = appColors.background,
        surface      = appColors.surface,
        onPrimary    = Color.White,
        onBackground = appColors.textPrimary,
        onSurface    = appColors.textPrimary,
        error        = appColors.error
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        val activity = view.context as? Activity
        activity?.let {
            SideEffect {
                it.window.statusBarColor = appColors.background.toArgb()
                WindowCompat.getInsetsController(it.window, view)
                    .isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = materialColors,
            typography  = KidsTypography,
            content     = content
        )
    }
}

val MaterialTheme.appColors: AppColorScheme
    @Composable get() = LocalAppColors.current

@Composable
fun categoryChipColor(category: String): Color {
    val c = MaterialTheme.appColors
    return when (category.lowercase()) {
        "animal", "animals"                              -> c.chipAnimal
        "food"                                           -> c.chipFood
        "flower"                                         -> c.chipFlower
        "vehicle", "vechile"                             -> c.chipVehicle
        "weather"                                        -> c.chipWeather
        "monument"                                       -> c.chipMonument
        "sports_equipments", "sports equipments"         -> c.chipSports
        "fruits_and_vegetables", "fruits and vegetables" -> c.chipFruits
        "flags"                                          -> c.chipFlags
        else                                             -> c.card
    }
}

fun categoryEmoji(category: String): String = when (category.lowercase()) {
    "animal", "animals"                              -> "🐾"
    "food"                                           -> "🍕"
    "flower"                                         -> "🌸"
    "vehicle", "vechile"                             -> "🚗"
    "weather"                                        -> "⛅"
    "monument"                                       -> "🏛️"
    "sports_equipments", "sports equipments"         -> "⚽"
    "fruits_and_vegetables", "fruits and vegetables" -> "🍎"
    "flags"                                          -> "🏳️"
    else                                             -> "✨"
}

// ── Shared TextField Colors ────────────────────
// Used across LoginScreen, SignupScreen, AdminScreen
@Composable
fun kidsTextFieldColors(colors: AppColorScheme) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = colors.primary,
    unfocusedBorderColor = colors.border,
    focusedLabelColor    = colors.primary,
    unfocusedLabelColor  = colors.textSecondary,
    cursorColor          = colors.primary,
    focusedTextColor     = colors.textPrimary,
    unfocusedTextColor   = colors.textPrimary,
    focusedContainerColor   = colors.surface,
    unfocusedContainerColor = colors.surface,
)

// ── Shared KidsTextField ────────────────────────
// Used in SettingsScreen
@Composable
fun KidsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    emoji: String,
    keyboardType: androidx.compose.ui.text.input.KeyboardType
) {
    val colors = MaterialTheme.appColors
    androidx.compose.material3.OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { androidx.compose.material3.Text("$emoji  $label") },
        singleLine = true,
        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
        colors = kidsTextFieldColors(colors)
    )
}