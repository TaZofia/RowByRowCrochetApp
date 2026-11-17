package com.example.rowbyrowapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkPurplePrimary,
    primaryContainer = DarkPurpleContainer,
    onPrimaryContainer = DarkOnPurpleContainer,
    background = DarkBackgroundCanvas,
    onBackground = Gray,
    surface = DarkCardSurface,
    onSurface = DarkTextWhite
)

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    primaryContainer = PurpleContainer,
    onPrimaryContainer = OnPurpleContainer,
    background = BackgroundCanvas,
    onBackground = Gray,
    surface = CardWhite,
    onSurface = TextBlack
)

@Composable
fun RowByRowAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // wymuszam moją paletę kolorów
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            DarkColorScheme
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}