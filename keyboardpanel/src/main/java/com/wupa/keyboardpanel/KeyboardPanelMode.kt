package com.wupa.keyboardpanel

sealed interface KeyboardPanelMode {
    data object None : KeyboardPanelMode
    data object Keyboard : KeyboardPanelMode
    data class Panel(val key: String) : KeyboardPanelMode
}
