package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = KitchenPrimaryDark,
    onPrimary = KitchenOnPrimaryDark,
    primaryContainer = KitchenPrimaryContainerDark,
    onPrimaryContainer = KitchenOnPrimaryContainerDark,
    secondary = KitchenSecondaryDark,
    onSecondary = KitchenOnSecondaryDark,
    secondaryContainer = KitchenSecondaryContainerDark,
    onSecondaryContainer = KitchenOnSecondaryContainerDark,
    tertiary = KitchenTertiaryDark,
    onTertiary = KitchenOnTertiaryDark,
    background = KitchenBackgroundDark,
    onBackground = KitchenOnBackgroundDark,
    surface = KitchenSurfaceDark,
    onSurface = KitchenOnSurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = KitchenPrimary,
    onPrimary = KitchenOnPrimary,
    primaryContainer = KitchenPrimaryContainer,
    onPrimaryContainer = KitchenOnPrimaryContainer,
    secondary = KitchenSecondary,
    onSecondary = KitchenOnSecondary,
    secondaryContainer = KitchenSecondaryContainer,
    onSecondaryContainer = KitchenOnSecondaryContainer,
    tertiary = KitchenTertiary,
    onTertiary = KitchenOnTertiary,
    background = KitchenBackground,
    onBackground = KitchenOnBackground,
    surface = KitchenSurface,
    onSurface = KitchenOnSurface,
    surfaceVariant = KitchenSurfaceVariant,
    onSurfaceVariant = KitchenOnSurfaceVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our handcrafted warm kitchen palette
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
