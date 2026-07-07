package com.wupa.keyboardpanel

import android.content.Context
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val KEYBOARD_PANEL_PREFS = "keyboard_panel_box"
private const val KEY_LAST_KEYBOARD_HEIGHT_PX = "last_keyboard_height_px"

data class KeyboardHeightState(
    val current: Dp,
    val currentPx: Int,
    val last: Dp,
    val lastPx: Int,
) {
    val isVisible: Boolean = currentPx > 0
}

@Composable
fun rememberKeyboardHeightState(defaultHeight: Dp = 300.dp): KeyboardHeightState {
    val context = LocalContext.current
    val density = LocalDensity.current
    val prefs = remember(context) {
        context.applicationContext.getSharedPreferences(
            KEYBOARD_PANEL_PREFS,
            Context.MODE_PRIVATE,
        )
    }

    val imeBottomPx = WindowInsets.ime.getBottom(density)
    val navBottomPx = WindowInsets.navigationBars.getBottom(density)

    var stableNavBottomPx by remember { mutableIntStateOf(navBottomPx) }
    if (imeBottomPx == 0 && navBottomPx > 0) {
        stableNavBottomPx = navBottomPx
    }

    val currentPx = (imeBottomPx - stableNavBottomPx).coerceAtLeast(0)
    val defaultHeightPx = with(density) { defaultHeight.roundToPx() }
    var lastPx by remember {
        mutableIntStateOf(prefs.getInt(KEY_LAST_KEYBOARD_HEIGHT_PX, defaultHeightPx))
    }
    var openMaxPx by remember { mutableIntStateOf(0) }

    LaunchedEffect(currentPx) {
        if (currentPx > 0) {
            if (currentPx > openMaxPx) {
                openMaxPx = currentPx
            }
        } else {
            if (openMaxPx > 0 && openMaxPx != lastPx) {
                prefs.edit().putInt(KEY_LAST_KEYBOARD_HEIGHT_PX, openMaxPx).apply()
                lastPx = openMaxPx
            }
            openMaxPx = 0
        }
    }

    return KeyboardHeightState(
        current = with(density) { currentPx.toDp() },
        currentPx = currentPx,
        last = with(density) { lastPx.toDp() },
        lastPx = lastPx,
    )
}
