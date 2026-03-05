package aether.killergram.neo.ui.theme

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

private val DarkColorScheme = darkColorScheme(
    primary = NeoPrimaryDark,
    onPrimary = NeoOnPrimaryDark,
    primaryContainer = NeoPrimaryContainerDark,
    onPrimaryContainer = NeoOnPrimaryContainerDark,
    secondary = NeoSecondaryDark,
    onSecondary = NeoOnSecondaryDark,
    secondaryContainer = NeoSecondaryContainerDark,
    onSecondaryContainer = NeoOnSecondaryContainerDark,
    tertiary = NeoTertiaryDark,
    onTertiary = NeoOnTertiaryDark,
    tertiaryContainer = NeoTertiaryContainerDark,
    onTertiaryContainer = NeoOnTertiaryContainerDark,
    background = NeoBackgroundDark,
    onBackground = NeoOnBackgroundDark,
    surface = NeoSurfaceDark,
    onSurface = NeoOnSurfaceDark,
    surfaceVariant = NeoSurfaceVariantDark,
    onSurfaceVariant = NeoOnSurfaceVariantDark,
    outline = NeoOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = NeoPrimaryLight,
    onPrimary = NeoOnPrimaryLight,
    primaryContainer = NeoPrimaryContainerLight,
    onPrimaryContainer = NeoOnPrimaryContainerLight,
    secondary = NeoSecondaryLight,
    onSecondary = NeoOnSecondaryLight,
    secondaryContainer = NeoSecondaryContainerLight,
    onSecondaryContainer = NeoOnSecondaryContainerLight,
    tertiary = NeoTertiaryLight,
    onTertiary = NeoOnTertiaryLight,
    tertiaryContainer = NeoTertiaryContainerLight,
    onTertiaryContainer = NeoOnTertiaryContainerLight,
    background = NeoBackgroundLight,
    onBackground = NeoOnBackgroundLight,
    surface = NeoSurfaceLight,
    onSurface = NeoOnSurfaceLight,
    surfaceVariant = NeoSurfaceVariantLight,
    onSurfaceVariant = NeoOnSurfaceVariantLight,
    outline = NeoOutlineLight
)

@Composable
fun KillergramNeoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
            window.setBackgroundDrawable(ColorDrawable(colorScheme.background.toArgb()))
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
