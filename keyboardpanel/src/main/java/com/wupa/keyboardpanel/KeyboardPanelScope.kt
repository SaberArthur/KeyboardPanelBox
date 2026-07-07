package com.wupa.keyboardpanel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp

interface KeyboardPanelScope {
    fun panel(
        key: String,
        height: Dp,
        content: @Composable () -> Unit,
    )
}

@Immutable
internal data class KeyboardPanelItem(
    val key: String,
    val height: Dp,
    val content: @Composable () -> Unit,
)

internal class KeyboardPanelScopeImpl : KeyboardPanelScope {
    private val _items = mutableListOf<KeyboardPanelItem>()
    val items: List<KeyboardPanelItem> = _items

    override fun panel(
        key: String,
        height: Dp,
        content: @Composable () -> Unit,
    ) {
        if (key.isBlank()) return
        _items += KeyboardPanelItem(
            key = key,
            height = height,
            content = content,
        )
    }
}
