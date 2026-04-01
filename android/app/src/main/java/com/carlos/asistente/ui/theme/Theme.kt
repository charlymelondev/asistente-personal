package com.carlos.asistente.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// Disable dynamic color — we want a consistent branded experience
private val LightColorScheme = lightColorScheme(
    primary = NavyDeep,
    onPrimary = OnNavy,
    primaryContainer = NavyMid,
    onPrimaryContainer = OnNavy,
    secondary = CoralAccent,
    onSecondary = OnNavy,
    secondaryContainer = Color(0xFFFFE8DE),
    onSecondaryContainer = Color(0xFF5A1800),
    tertiary = SuccessGreen,
    onTertiary = OnNavy,
    tertiaryContainer = Color(0xFFDCF5DC),
    onTertiaryContainer = Color(0xFF003908),
    background = SurfaceOffWhite,
    onBackground = NavyDeep,
    surface = SurfaceWhite,
    onSurface = NavyDeep,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = Color(0xFF546E7A),
    outline = Color(0xFFCFD8DC),
    error = PriorityUrgent,
    onError = OnNavy,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF003258),
    primaryContainer = NavyMid,
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = CoralLight,
    onSecondary = Color(0xFF5A1800),
    secondaryContainer = Color(0xFF7B3000),
    onSecondaryContainer = Color(0xFFFFDBC8),
    tertiary = Color(0xFF81C784),
    onTertiary = Color(0xFF003910),
    background = Color(0xFF111820),
    onBackground = Color(0xFFE2E9EF),
    surface = Color(0xFF1C2430),
    onSurface = Color(0xFFE2E9EF),
    surfaceVariant = Color(0xFF2A3441),
    onSurfaceVariant = Color(0xFFB0BEC5),
    outline = Color(0xFF455A64),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    ),
)

@Composable
fun AsistenteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = AppShapes,
        typography = AppTypography,
        content = content
    )
}
