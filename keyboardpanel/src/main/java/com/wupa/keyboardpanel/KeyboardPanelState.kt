package com.wupa.keyboardpanel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun rememberKeyboardPanelState(
    defaultKeyboardHeight: Dp = 300.dp,
): KeyboardPanelState {
    return remember(defaultKeyboardHeight) {
        KeyboardPanelState(defaultKeyboardHeight = defaultKeyboardHeight)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Stable
class KeyboardPanelState internal constructor(
    internal val defaultKeyboardHeight: Dp,
) {
    val focusRequester: FocusRequester = FocusRequester()

    var mode: KeyboardPanelMode by mutableStateOf(KeyboardPanelMode.None)
        private set

    var bottomHeight: Dp by mutableStateOf(0.dp)
        internal set

    val currentPanelKey: String?
        get() = (mode as? KeyboardPanelMode.Panel)?.key

    val isKeyboardMode: Boolean
        get() = mode == KeyboardPanelMode.Keyboard

    val isPanelMode: Boolean
        get() = mode is KeyboardPanelMode.Panel

    val isVisible: Boolean
        get() = mode != KeyboardPanelMode.None

    internal var keyboardController: SoftwareKeyboardController? = null
    internal var focusManager: FocusManager? = null
    internal var holdBottomUntilKeyboard: Boolean by mutableStateOf(false)
    internal var holdBottomHeight: Dp by mutableStateOf(0.dp)

    fun showKeyboard() {
        val previousMode = mode
        if (previousMode is KeyboardPanelMode.Panel) {
            holdBottomHeight = bottomHeight.takeIf { it > 0.dp } ?: defaultKeyboardHeight
            holdBottomUntilKeyboard = true
        }
        mode = KeyboardPanelMode.Keyboard
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    fun showPanel(key: String) {
        if (key.isBlank()) return
        holdBottomUntilKeyboard = false
        mode = KeyboardPanelMode.Panel(key)
        keyboardController?.hide()
        focusManager?.clearFocus(force = true)
    }

    fun togglePanel(key: String) {
        if (currentPanelKey == key) {
            showKeyboard()
        } else {
            showPanel(key)
        }
    }

    fun hide() {
        holdBottomUntilKeyboard = false
        mode = KeyboardPanelMode.None
        keyboardController?.hide()
        focusManager?.clearFocus(force = true)
    }

    internal fun notifyInputFocused() {
        if (mode is KeyboardPanelMode.Panel) {
            holdBottomHeight = bottomHeight.takeIf { it > 0.dp } ?: defaultKeyboardHeight
            holdBottomUntilKeyboard = true
        } else if (mode != KeyboardPanelMode.Keyboard) {
            holdBottomUntilKeyboard = false
        }
        mode = KeyboardPanelMode.Keyboard
        keyboardController?.show()
    }

    internal fun completeKeyboardHold() {
        holdBottomUntilKeyboard = false
    }
}
