package com.wupa.keyboardpanelbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wupa.keyboardpanel.KeyboardPanelBox
import com.wupa.keyboardpanel.KeyboardPanelState
import com.wupa.keyboardpanel.keyboardPanelInput
import com.wupa.keyboardpanel.rememberKeyboardPanelState
import com.wupa.keyboardpanelbox.ui.theme.KeyboardPanelBoxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KeyboardPanelBoxTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    KeyboardPanelDemo()
                }
            }
        }
    }
}

@Composable
private fun KeyboardPanelDemo() {
    val state = rememberKeyboardPanelState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
    ) {
        DemoContent(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            onHideInput = { state.hide() },
        )
        KeyboardPanelBox(
            state = state,
            modifier = Modifier.fillMaxWidth(),
            inputBar = {
                DemoInputBar(state = state)
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
}

@Composable
private fun DemoContent(
    modifier: Modifier = Modifier,
    onHideInput: () -> Unit,
) {
    val messages = remember {
        List(28) { index ->
            if (index % 2 == 0) {
                "Message #$index from the left side"
            } else {
                "Message #$index from the right side"
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .background(Color(0xFFF4F6F8))
            .padding(horizontal = 16.dp),
        reverseLayout = true,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Hide input",
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFE5E7EB))
                        .clickable(onClick = onHideInput)
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    color = Color(0xFF374151),
                    fontSize = 13.sp,
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
        items(messages.reversed()) { message ->
            val isMine = message.contains("right")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
            ) {
                Text(
                    text = message,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isMine) Color(0xFFD8F3DC) else Color.White)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    color = Color(0xFF1F2933),
                    fontSize = 15.sp,
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun DemoInputBar(state: KeyboardPanelState) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = text,
            onValueChange = { text = it },
            textStyle = TextStyle(
                color = Color(0xFF111827),
                fontSize = 16.sp,
            ),
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF3F4F6))
                .keyboardPanelInput(state)
                .padding(horizontal = 12.dp, vertical = 9.dp),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = "Type a message",
                            color = Color(0xFF9CA3AF),
                            fontSize = 16.sp,
                        )
                    }
                    innerTextField()
                }
            },
        )
        Spacer(modifier = Modifier.width(8.dp))
        DemoAction(text = "⌨") { state.showKeyboard() }
        Spacer(modifier = Modifier.width(6.dp))
        DemoAction(text = "☺") { state.showPanel("emoji") }
        Spacer(modifier = Modifier.width(6.dp))
        DemoAction(text = "+") { state.showPanel("menu") }
        Spacer(modifier = Modifier.width(6.dp))
        DemoAction(text = "⋯") { state.showPanel("more") }
    }
}

@Composable
private fun DemoAction(
    text: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Color(0xFFE5E7EB))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Color(0xFF111827),
            fontSize = 17.sp,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmojiPanel() {
    val emojis = listOf(
        "😀", "😄", "😂", "😊", "😍", "😎", "😭", "😡",
        "👍", "👏", "🙏", "💪", "🎉", "🔥", "❤️", "✨",
        "🍎", "🍔", "☕", "⚽", "🚗", "🌙", "⭐", "✅",
    )
    FlowRow(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBEB))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        emojis.forEach { emoji ->
            Text(
                text = emoji,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .padding(8.dp),
                fontSize = 22.sp,
            )
        }
    }
}

@Composable
private fun MenuPanel() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEFF6FF))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        listOf("Photo", "Camera", "File", "Location").forEach { label ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(label.take(1), color = Color(0xFF2563EB), fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(label, color = Color(0xFF374151), fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun MorePanel() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0FDF4))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("More panel", color = Color(0xFF166534), fontSize = 18.sp)
        Text("This panel has its own height and content.", color = Color(0xFF4B5563))
        Text("Switch between keyboard, emoji, menu, and more.", color = Color(0xFF4B5563))
    }
}

@Preview(showBackground = true)
@Composable
private fun KeyboardPanelDemoPreview() {
    KeyboardPanelBoxTheme {
        KeyboardPanelDemo()
    }
}
