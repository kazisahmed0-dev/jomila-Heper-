package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReminderItem
import com.example.ui.theme.ReminderOrange
import com.example.util.AlarmScheduler

@Composable
fun RemindersScreen(
    reminders: List<ReminderItem>,
    onToggleReminder: (ReminderItem) -> Unit,
    onDeleteReminder: (ReminderItem) -> Unit,
    onAddReminder: (String, Int, Int, String) -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Next scheduled alert banner
            item {
                val nextActive = reminders.firstOrNull { it.isActive && it.timeMillis > System.currentTimeMillis() }
                    ?: reminders.firstOrNull { it.isActive }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(ReminderOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AlarmOn,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "পরবর্তী সক্রিয় রিমাইন্ডার",
                                style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFFC2410C))
                            )
                            Text(
                                text = nextActive?.title ?: "সকাল ৭:০০ - স্কুলে যাওয়ার অ্যালার্ম",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            if (nextActive != null) {
                                Text(
                                    text = AlarmScheduler.formatBanglaTime(nextActive.timeMillis),
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                                )
                            }
                        }
                    }
                }
            }

            // Quick preset chips
            item {
                Text(
                    text = "এক ট্যাপে অ্যালার্ম সেট করুন:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onAddReminder("সকাল ৭:০০ - স্কুলে যাওয়া", 7, 0, "স্কুল")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ReminderOrange),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("quick_school_alarm_button")
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("৭:০০ স্কুল", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onAddReminder("দুপুর ২:০০ - দুপুরের খাবার ও পড়া", 14, 0, "দৈনন্দিন")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("২:০০ লাঞ্চ", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onAddReminder("রাত ১০:০০ - ওষুধ ও ঘুমানোর প্রস্তুতি", 22, 0, "স্বাস্থ্য")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("১০:০০ ঘুম", fontSize = 12.sp)
                    }
                }
            }

            // Reminders List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "সকল রিমাইন্ডার (${reminders.size}টি)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    TextButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("নতুন রিমাইন্ডার")
                    }
                }
            }

            if (reminders.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("এখনও কোনো রিমাইন্ডার সেট করা হয়নি")
                            Text("জমিলাকে চ্যাটে বলুন অথবা ওপরের বাটনে চাপুন।", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            } else {
                items(reminders, key = { it.id }) { reminder ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (reminder.isActive) Color(0xFFFFEDD5) else Color(0xFFE2E8F0)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = if (reminder.isActive) ReminderOrange else Color.Gray,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = reminder.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (reminder.isActive) MaterialTheme.colorScheme.onSurface else Color.Gray
                                    )
                                )
                                Text(
                                    text = AlarmScheduler.formatBanglaTime(reminder.timeMillis),
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                                )
                            }

                            Switch(
                                checked = reminder.isActive,
                                onCheckedChange = { onToggleReminder(reminder) },
                                colors = SwitchDefaults.colors(checkedThumbColor = ReminderOrange)
                            )

                            IconButton(onClick = { onDeleteReminder(reminder) }) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = "মুছুন",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = ReminderOrange,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_reminder_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "নতুন রিমাইন্ডার")
        }
    }

    if (showAddDialog) {
        var titleInput by remember { mutableStateOf("৭ টায় স্কুলে যাওয়া") }
        var hourInput by remember { mutableStateOf("7") }
        var minuteInput by remember { mutableStateOf("0") }
        var categoryInput by remember { mutableStateOf("স্কুল") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("নতুন রিমাইন্ডার তৈরি করুন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("কাজের নাম বা শিরোনাম") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = hourInput,
                            onValueChange = { hourInput = it },
                            label = { Text("ঘণ্টা (১-১২ বা ০-২৩)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = minuteInput,
                            onValueChange = { minuteInput = it },
                            label = { Text("মিনিট (০-৫৯)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = categoryInput == "স্কুল",
                            onClick = { categoryInput = "স্কুল" },
                            label = { Text("স্কুল") }
                        )
                        FilterChip(
                            selected = categoryInput == "পড়াশোনা",
                            onClick = { categoryInput = "পড়াশোনা" },
                            label = { Text("পড়াশোনা") }
                        )
                        FilterChip(
                            selected = categoryInput == "দৈনন্দিন",
                            onClick = { categoryInput = "দৈনন্দিন" },
                            label = { Text("দৈনন্দিন") }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val h = hourInput.toIntOrNull() ?: 7
                        val m = minuteInput.toIntOrNull() ?: 0
                        onAddReminder(titleInput.trim(), h, m, categoryInput)
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ReminderOrange)
                ) {
                    Text("অ্যালার্ম সেট করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}
