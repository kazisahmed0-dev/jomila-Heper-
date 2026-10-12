package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.db.AppDatabase
import com.example.data.repository.JomilaRepository
import com.example.ui.components.JomilaTopBar
import com.example.ui.screens.AssistantScreen
import com.example.ui.screens.ReceptionistScreen
import com.example.ui.screens.RemindersScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.WhatsAppScreen
import com.example.ui.theme.JomilaHelperTheme
import com.example.ui.viewmodel.JomilaViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val navigateToTarget = intent?.getStringExtra("navigate_to")

        setContent {
            JomilaHelperTheme {
                val context = LocalContext.current
                val database = remember { AppDatabase.getDatabase(context) }
                val repository = remember { JomilaRepository(database.appDao()) }
                val viewModel: JomilaViewModel = viewModel(
                    factory = JomilaViewModel.Factory(repository, context)
                )

                // Request notification permission on Android 13+
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { /* Permissions handled */ }

                LaunchedEffect(Unit) {
                    val permsToRequest = mutableListOf<String>()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            permsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                    if (permsToRequest.isNotEmpty()) {
                        permissionLauncher.launch(permsToRequest.toTypedArray())
                    }
                }

                JomilaMainContent(
                    viewModel = viewModel,
                    initialTarget = navigateToTarget
                )
            }
        }
    }
}

@Composable
fun JomilaMainContent(
    viewModel: JomilaViewModel,
    initialTarget: String? = null
) {
    val context = LocalContext.current
    var selectedTab by remember {
        mutableIntStateOf(
            when (initialTarget) {
                "reminders" -> 3
                "receptionist" -> 2
                "whatsapp" -> 1
                "tasks" -> 4
                else -> 0
            }
        )
    }

    // Required BackHandler for custom state-switching navigation
    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    val isVoiceEnabled by viewModel.isVoiceOutputEnabled.collectAsStateWithLifecycle()
    val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val rules by viewModel.rules.collectAsStateWithLifecycle()
    val callLogs by viewModel.callLogs.collectAsStateWithLifecycle()
    val isAutoReplyActive by viewModel.isAutoReplyActive.collectAsStateWithLifecycle()
    val isReceptionistActive by viewModel.isReceptionistActive.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            JomilaTopBar(
                isVoiceEnabled = isVoiceEnabled,
                onToggleVoice = { viewModel.toggleVoiceOutput() },
                onClearChat = { viewModel.clearChat() }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.SmartToy, contentDescription = "সহকারী") },
                    label = { Text("সহকারী", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_assistant")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "হোয়াটসঅ্যাপ") },
                    label = { Text("হোয়াটসঅ্যাপ", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_whatsapp")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.PhoneInTalk, contentDescription = "কল সহকারী") },
                    label = { Text("কল সহকারী", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_receptionist")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Alarm, contentDescription = "রিমাইন্ডার") },
                    label = { Text("রিমাইন্ডার", fontSize = 11.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_reminders")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Checklist, contentDescription = "কাজের লিস্ট") },
                    label = { Text("কাজের লিস্ট", fontSize = 11.sp, fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tasks")
                )
            }
        }
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> AssistantScreen(
                    messages = messages,
                    isAiThinking = isAiThinking,
                    onSendMessage = { text -> viewModel.sendMessage(text, context) },
                    onSpeak = { text -> viewModel.speakAloud(text) },
                    onNavigateTab = { tabIndex -> selectedTab = tabIndex }
                )
                1 -> WhatsAppScreen(
                    rules = rules,
                    isAutoReplyActive = isAutoReplyActive,
                    onToggleAutoReplyMaster = { viewModel.toggleAutoReplyMaster() },
                    onToggleRule = { rule -> viewModel.toggleRule(rule) },
                    onDeleteRule = { rule -> viewModel.deleteRule(rule) },
                    onAddNewRule = { pattern, reply -> viewModel.addNewRule(pattern, reply) },
                    onSimulateIncoming = { sender, text -> viewModel.simulateIncomingWhatsApp(sender, text) }
                )
                2 -> ReceptionistScreen(
                    callLogs = callLogs,
                    isReceptionistActive = isReceptionistActive,
                    onToggleReceptionist = { viewModel.toggleReceptionistMaster() }
                )
                3 -> RemindersScreen(
                    reminders = reminders,
                    onToggleReminder = { reminder -> viewModel.toggleReminderActive(reminder, context) },
                    onDeleteReminder = { reminder -> viewModel.deleteReminder(reminder, context) },
                    onAddReminder = { title, hour, minute, cat ->
                        viewModel.addNewReminder(title, hour, minute, cat, context)
                    }
                )
                4 -> TasksScreen(
                    tasks = tasks,
                    onToggleTask = { task -> viewModel.toggleTaskCompleted(task) },
                    onDeleteTask = { task -> viewModel.deleteTask(task) },
                    onRequestAiHelp = { task -> viewModel.requestAiHelpForTask(task) },
                    onAddTask = { title, notes, priority, cat ->
                        viewModel.addNewTask(title, notes, priority, cat)
                    }
                )
            }
        }
    }
}
