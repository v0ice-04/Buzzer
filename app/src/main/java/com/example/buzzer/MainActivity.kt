package com.example.buzzer // Ensure this matches your exact package name

import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

// --- INTERACTION LOGGER FOR TESTING VARIABLES & STEP ACTION ENGINE ---
class UnsafeLogger {
    var deviceId: String? = null
    fun formatMessage(msg: String): String {
        // 🔬 STEP ACTIONS TARGET: The '!!' operator triggers a NullPointerException if deviceId is null
        return "LOG_VAL: " + deviceId!!.lowercase() + " -> $msg"
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainDebugDashboard()
                }
            }
        }
    }
}

@Composable
fun MainDebugDashboard() {
    var currentTab by remember { mutableStateOf(0) }
    val tabs = listOf("Clicker", "NPE Lab", "Crash Log", "UI State Bug")

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        TabRow(selectedTabIndex = currentTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = currentTab == index,
                    onClick = { currentTab = index },
                    text = { Text(title, fontSize = 12.sp) }
                )
            }
        }

        Box(modifier = Modifier.weight(1f).padding(16.dp)) {
            when (currentTab) {
                0 -> StressClickerScreen()
                1 -> NullPointerScreen()
                2 -> CrashLogScreen()
                3 -> BrokenStateScreen()
            }
        }
    }
}

// --- CORE JETPACK COMPOSE STRESS CLICKER ---
@Composable
fun StressClickerScreen() {
    var clickCount by remember { mutableStateOf(0) }
    var baseColor by remember { mutableStateOf(Color(0xFF81C784)) }

    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "ButtonScale"
    )

    val animatedBgColor by animateColorAsState(
        targetValue = baseColor,
        animationSpec = tween(durationMillis = 500),
        label = "BgColorTransition"
    )

    Column(
        modifier = Modifier.fillMaxSize().background(animatedBgColor.copy(alpha = 0.15f)).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Take a Deep Breath", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text("Tap the button below to release tension", fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
        }

        Box(
            modifier = Modifier.size(200.dp).scale(scale).clip(CircleShape).background(baseColor)
                .clickable(interactionSource = interactionSource, indication = null) {
                    clickCount++
                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                    baseColor = Color(red = Random.nextFloat() * 0.4f + 0.5f, green = Random.nextFloat() * 0.4f + 0.6f, blue = Random.nextFloat() * 0.4f + 0.6f, alpha = 1.0f)
                },
            contentAlignment = Alignment.Center
        ) {
            Text("TAP", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = 2.sp)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 20.dp)) {
            Text("Tension Released", fontSize = 14.sp, color = Color.Gray)
            Text("$clickCount", fontSize = 48.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        }
    }
}

// --- LAB 1: NULL POINTER, STEPPERS, VARIABLES, CALL STACK ---
@Composable
fun NullPointerScreen() {
    var outputText by remember { mutableStateOf("Ready to experiment.") }
    val logger = remember { UnsafeLogger() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Debugging Stepper & NPE Lab", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        Text("1. Place a breakpoint on the line inside this onClick button lambda.\n2. Tap the Debug bug icon at the top of Android Studio.\n3. Execute the action to test your inspection tools.", fontSize = 13.sp)
        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            val intermediateVar = "User Action Triggered"
            // 🎯 INSTRUCTION: Set your breakpoint on the line below!
            outputText = processLogMessage(logger, intermediateVar)
        }) {
            Text("Execute Unsafe Action")
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = outputText, color = MaterialTheme.colorScheme.error)
    }
}

fun processLogMessage(logger: UnsafeLogger, message: String): String {
    return logger.formatMessage(message)
}

// --- LAB 2: LOGCAT INTENTIONAL CRASH EXPLORATION ---
@Composable
fun CrashLogScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Intentional Crash & Logcat Lab", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        Text("Clicking this button will force an unhandled runtime error. Use Quail 3's Logcat panel filtered to 'level:error' to inspect the stack trace layout.", fontSize = 13.sp)
        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                throw IllegalStateException("CRITICAL QUAIL LAB: Simulated App Crash Sequence Triggered.")
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Trigger Fatal Crash")
        }
    }
}

// --- LAB 3: STATE RECOMPOSITION ANOMALY ---
@Composable
fun BrokenStateScreen() {
    // ❌ STATE BUG ANTI-PATTERN: Local runtime primitives reset or fail to alert the draw phase
    var brokenCounter = 0
    var functioningCounter by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("UI & State Recomposition Bug", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        Text("Incrementing the broken local counter updates values in the system memory trace but fails to trigger a Jetpack Compose frame update. Forcing structural recomposition via the standard Compose state wrapper reveals the hidden accumulated data value updates instantly.", fontSize = 13.sp)
        Spacer(modifier = Modifier.height(20.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Broken Primitive State: $brokenCounter")
                Text("Correct Stateful Wrapper: $functioningCounter")
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            brokenCounter++
            println("Quail Console Output -> current value of brokenCounter: $brokenCounter")
        }) {
            Text("Increment Broken Counter")
        }
        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = { functioningCounter++ }) {
            Text("Force Recomposition")
        }
    }
}
