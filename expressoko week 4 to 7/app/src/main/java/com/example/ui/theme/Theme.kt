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

private fun getDynamicLightColorScheme(appColor: String) = lightColorScheme(
    primary = when(appColor) { "Pink" -> PinkPrimaryLight "Gray" -> GrayPrimaryLight "Red" -> RedPrimaryLight "Green" -> GreenPrimaryLight else -> SleekPrimaryLight },
    onPrimary = SleekOnPrimaryLight, // Assuming white text looks good on all these
    primaryContainer = when(appColor) { "Pink" -> PinkContainerLight "Gray" -> GrayContainerLight "Red" -> RedContainerLight "Green" -> GreenContainerLight else -> SleekContainerLight },
    onPrimaryContainer = SleekOnContainerLight,
    secondary = when(appColor) { "Pink" -> PinkPrimaryLight "Gray" -> GrayPrimaryLight "Red" -> RedPrimaryLight "Green" -> GreenPrimaryLight else -> SleekPrimaryLight },
    background = SleekBgLight,
    surface = CleanWhite,
    onBackground = SleekTextLight,
    onSurface = SleekTextLight,
    surfaceVariant = SleekSecondaryBgLight,
    onSurfaceVariant = SleekSecondaryTextLight,
    outline = SleekBorderLight
)

private fun getDynamicDarkColorScheme(appColor: String) = darkColorScheme(
    primary = when(appColor) { "Pink" -> PinkPrimaryDark "Gray" -> GrayPrimaryDark "Red" -> RedPrimaryDark "Green" -> GreenPrimaryDark else -> SleekPrimaryDark },
    onPrimary = SleekOnPrimaryDark, // usually dark color for text on light primary in dark mode, or keep white if preferred. M3 dark primary is light.
    primaryContainer = when(appColor) { "Pink" -> PinkContainerDark "Gray" -> GrayContainerDark "Red" -> RedContainerDark "Green" -> GreenContainerDark else -> SleekContainerDark },
    onPrimaryContainer = SleekOnContainerDark,
    secondary = when(appColor) { "Pink" -> PinkPrimaryDark "Gray" -> GrayPrimaryDark "Red" -> RedPrimaryDark "Green" -> GreenPrimaryDark else -> SleekPrimaryDark },
    background = SleekBgDark,
    surface = SleekSecondaryBgDark,
    onBackground = SleekTextDark,
    onSurface = SleekTextDark,
    surfaceVariant = SleekSecondaryBgDark,
    onSurfaceVariant = SleekSecondaryTextDark,
    outline = SleekBorderDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    appColor: String = "Blue",
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Keep branding static for cohesive Kenyan luxury layout
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> getDynamicDarkColorScheme(appColor)
        else -> getDynamicLightColorScheme(appColor)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
