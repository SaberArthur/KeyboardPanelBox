# KeyboardPanelBox

[![](https://jitpack.io/v/SaberArthur/KeyboardPanelBox.svg)](https://jitpack.io/#SaberArthur/KeyboardPanelBox)

[中文文档](README_CN.md)

<p align="center">
  <img src="image/demo.gif" width="320" alt="KeyboardPanelBox demo" />
</p>

KeyboardPanelBox is a Jetpack Compose bottom container for coordinating the
soft keyboard and custom panels.

It is designed for chat pages, comment inputs, editors, and any screen where an
input bar needs to switch smoothly between the IME and panels such as emoji,
attachments, or more actions.

## Features

- Compose-first API
- Multiple custom panels registered by key
- Panel heights declared by the caller
- Smooth keyboard to panel switching
- Smooth panel to keyboard switching
- Built-in input focus and keyboard control
- Soft keyboard height tracking with `WindowInsets.ime`
- Last keyboard height stored with Android `SharedPreferences`
- No assumption about the main content above the input bar

## Module

The library module is:

```text
:keyboardpanel
```

The sample app module is:

```text
:app
```

## Install

### Local module

If you use this repository directly, add the module dependency:

```kotlin
dependencies {
    implementation(project(":keyboardpanel"))
}
```

### JitPack

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

```kotlin
dependencies {
    implementation("com.github.SaberArthur.KeyboardPanelBox:keyboardpanel:0.1.0")
}
```

## Basic Usage

`KeyboardPanelBox` only owns the bottom input area. The main content is managed
by the caller.

```kotlin
val state = rememberKeyboardPanelState()

Column(Modifier.fillMaxSize()) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
    ) {
        MainContent()
    }

    KeyboardPanelBox(
        state = state,
        inputBar = {
            InputBar(
                modifier = Modifier.keyboardPanelInput(state),
                onKeyboardClick = { state.showKeyboard() },
                onEmojiClick = { state.showPanel("emoji") },
                onMenuClick = { state.showPanel("menu") },
                onMoreClick = { state.showPanel("more") },
            )
        },
    ) {
        panel("emoji", height = 260.dp) {
            EmojiPanel()
        }

        panel("menu", height = 200.dp) {
            MenuPanel()
        }

        panel("more", height = 240.dp) {
            MorePanel()
        }
    }
}
```

## Input Binding

Attach `Modifier.keyboardPanelInput(state)` to the text input that should own
the keyboard focus.

```kotlin
BasicTextField(
    value = text,
    onValueChange = { text = it },
    modifier = Modifier.keyboardPanelInput(state),
)
```

This lets `KeyboardPanelState.showKeyboard()` request focus and show the IME.
It also lets the state update when the user taps the input directly.

## State API

```kotlin
val state = rememberKeyboardPanelState(
    defaultKeyboardHeight = 300.dp,
)
```

Available operations:

```kotlin
state.showKeyboard()
state.showPanel("emoji")
state.togglePanel("emoji")
state.hide()
```

Useful state:

```kotlin
state.mode
state.currentPanelKey
state.isKeyboardMode
state.isPanelMode
state.isVisible
state.bottomHeight
```

## Panel DSL

Panels are registered by string keys:

```kotlin
panel("emoji", height = 260.dp) {
    EmojiPanel()
}
```

The library does not know what `emoji`, `menu`, or `more` mean. It only matches
the key passed to `showPanel(key)` with the registered panel and uses that
panel's declared height.

## Important Notes

Do not add `Modifier.imePadding()` to `KeyboardPanelBox`.

`KeyboardPanelBox` already uses the IME height as its bottom panel height. If
you add `imePadding()` around it, the keyboard height will be applied twice.

Recommended:

```kotlin
KeyboardPanelBox(
    state = state,
    modifier = Modifier.fillMaxWidth(),
    inputBar = { ... },
) {
    panel("emoji", height = 260.dp) { ... }
}
```

Avoid:

```kotlin
KeyboardPanelBox(
    state = state,
    modifier = Modifier
        .fillMaxWidth()
        .imePadding(),
    inputBar = { ... },
) {
    panel("emoji", height = 260.dp) { ... }
}
```

## Behavior

### Keyboard to Panel

When a panel is opened while the keyboard is visible, the bottom height moves
from the current keyboard height to the panel's declared height.

### Panel to Keyboard

When the keyboard is requested while a panel is visible, the current panel
height is held until the keyboard height catches up. This avoids the common
"drop then jump" transition.

### Panel to Panel

Switching from one panel key to another animates the bottom height from the
previous panel height to the next panel height.

### Hide

`state.hide()` hides the keyboard, clears input focus, and collapses the bottom
area.

## Sample

Run the sample app:

```bash
./gradlew :app:installDebug
```

Or compile it:

```bash
./gradlew :app:compileDebugKotlin
```

The sample demonstrates:

- Text input focus binding
- Keyboard button
- Emoji panel
- Menu panel
- More panel
- Caller-owned main content

## Minimum SDK

The current module is configured with:

```kotlin
minSdk = 21
```

The library uses Compose `WindowInsets` APIs instead of platform-only API 30
calls. Keyboard animation behavior can still vary across Android versions,
devices, ROMs, and input methods, so test the transition on the devices you
support.

## License

No license has been added yet. Add a license before publishing the repository.
