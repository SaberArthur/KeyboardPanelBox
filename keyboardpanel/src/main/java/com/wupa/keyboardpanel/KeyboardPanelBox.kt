package com.wupa.keyboardpanel

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun KeyboardPanelBox(
    state: KeyboardPanelState,
    modifier: Modifier = Modifier,
    animationSpec: AnimationSpec<Dp> = tween(
        durationMillis = 260,
        easing = FastOutSlowInEasing,
    ),
    inputBar: @Composable () -> Unit,
    panels: KeyboardPanelScope.() -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val keyboardHeight = rememberKeyboardHeightState(
        defaultHeight = state.defaultKeyboardHeight,
    )

    SideEffect {
        state.keyboardController = keyboardController
        state.focusManager = focusManager
    }

    val scope = KeyboardPanelScopeImpl().apply(panels)
    val currentPanel = scope.items.lastOrNull { item -> item.key == state.currentPanelKey }
    val targetPanelHeight = currentPanel?.height ?: state.defaultKeyboardHeight
    val keyboardHoldHeight = if (state.holdBottomUntilKeyboard) {
        val holdPx = with(density) { state.holdBottomHeight.roundToPx() }
        val targetPx = keyboardHeight.lastPx.coerceAtLeast(1)
        val heightPx = if (holdPx > targetPx && keyboardHeight.currentPx > 0) {
            val progress = (keyboardHeight.currentPx.toFloat() / targetPx).coerceIn(0f, 1f)
            (holdPx - (holdPx - targetPx) * progress).roundToInt()
        } else {
            maxOf(holdPx, keyboardHeight.currentPx)
        }
        with(density) { heightPx.toDp() }
    } else {
        keyboardHeight.current
    }

    val animatedHeight by animateDpAsState(
        targetValue = when (state.mode) {
            KeyboardPanelMode.None -> 0.dp
            KeyboardPanelMode.Keyboard -> {
                if (state.holdBottomUntilKeyboard) {
                    keyboardHoldHeight
                } else {
                    keyboardHeight.current
                }
            }
            is KeyboardPanelMode.Panel -> targetPanelHeight
        },
        animationSpec = animationSpec,
        label = "KeyboardPanelHeight",
    )

    LaunchedEffect(
        state.holdBottomUntilKeyboard,
        keyboardHeight.currentPx,
        keyboardHeight.lastPx,
        state.holdBottomHeight,
    ) {
        if (!state.holdBottomUntilKeyboard) return@LaunchedEffect
        if (keyboardHeight.currentPx <= 0) return@LaunchedEffect

        val holdPx = with(density) { state.holdBottomHeight.roundToPx() }
        val targetPx = maxOf(1, minOf(keyboardHeight.lastPx, holdPx.takeIf { it > 0 } ?: keyboardHeight.lastPx))
        if (keyboardHeight.currentPx >= targetPx) {
            state.completeKeyboardHold()
        }
    }

    val resolvedHeight = when (state.mode) {
        KeyboardPanelMode.Keyboard -> {
            if (state.holdBottomUntilKeyboard) {
                keyboardHoldHeight
            } else {
                keyboardHeight.current
            }
        }

        is KeyboardPanelMode.Panel -> animatedHeight
        KeyboardPanelMode.None -> {
            if (keyboardHeight.isVisible) keyboardHeight.current else animatedHeight
        }
    }

    SideEffect {
        state.bottomHeight = resolvedHeight
    }

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        inputBar()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(resolvedHeight),
        ) {
            Crossfade(
                targetState = currentPanel,
                animationSpec = tween(durationMillis = 180),
                label = "KeyboardPanelContent",
            ) { item ->
                item?.content?.invoke()
            }
        }
    }
}

fun Modifier.keyboardPanelInput(state: KeyboardPanelState): Modifier {
    return this
        .focusRequester(state.focusRequester)
        .onFocusChanged { focusState ->
            if (focusState.isFocused) {
                state.notifyInputFocused()
            }
        }
}
