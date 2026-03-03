package aether.killergram.neo.ui.theme

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

private val DarkColorScheme = darkColorScheme(
    primary = NeoBlueDark,
    secondary = NeoMintDark,
    tertiary = NeoMintDark,
    background = NeoBackgroundDark,
    surface = NeoSurfaceDark,
    surfaceContainer = NeoSurfaceVariantDark,
    surfaceContainerHigh = NeoSurfaceVariantDark,
    onSurface = NeoOnSurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = NeoBlue,
    secondary = NeoMint,
    tertiary = NeoMint,
    background = NeoBackgroundLight,
    surface = NeoSurfaceLight,
    surfaceContainer = NeoSurfaceVariantLight,
    surfaceContainerHigh = NeoSurfaceVariantLight,
    onSurface = NeoOnSurfaceLight
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
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            window.setBackgroundDrawable(ColorDrawable(colorScheme.background.toArgb()))
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
