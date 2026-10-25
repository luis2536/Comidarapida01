package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val StreetFoodDarkColorScheme = darkColorScheme(
    primary = AmberPrimary,
    onPrimary = TextDark,
    primaryContainer = OrangeFlameDark,
    onPrimaryContainer = AmberGlow,
    secondary = OrangeFlame,
    onSecondary = TextWhite,
    secondaryContainer = PosDarkElevated,
    onSecondaryContainer = AmberGlow,
    tertiary = YellowCheese,
    onTertiary = TextDark,
    background = PosDarkBg,
    onBackground = TextWhite,
    surface = PosDarkSurface,
    onSurface = TextWhite,
    surfaceVariant = PosDarkElevated,
    onSurfaceVariant = TextLightGrey,
    outline = PosDarkBorder,
    outlineVariant = PosDarkBorderHighlight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // POS systems look stunning in fixed dark mode
    dynamicColor: Boolean = false, // Keep signature brand styling
    content: @Composable () -> Unit
) {
    val colorScheme = StreetFoodDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = PosDarkBg.toArgb()
                window.navigationBarColor = PosDarkBg.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
