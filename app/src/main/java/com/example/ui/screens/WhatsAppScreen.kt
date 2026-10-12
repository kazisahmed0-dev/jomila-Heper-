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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
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
import com.example.data.model.AutoReplyRule
import com.example.ui.theme.WhatsAppDarkGreen
import com.example.ui.theme.WhatsAppGreen
import com.example.util.WhatsAppHelper

@Composable
fun WhatsAppScreen(
    rules: List<AutoReplyRule>,
    isAutoReplyActive: Boolean,
    onToggleAutoReplyMaster: () -> Unit,
    onToggleRule: (AutoReplyRule) -> Unit,
    onDeleteRule: (AutoReplyRule) -> Unit,
    onAddNewRule: (String, String) -> Unit,
    onSimulateIncoming: (String, String) -> Unit
) {
    val context = LocalContext.current
    val isAccessGranted = remember { WhatsAppHelper.isNotificationAccessGranted(context) }

    var showAddDialog by remember { mutableStateOf(false) }
    var targetPhone by remember { mutableStateOf("") }
    var customMessage by remember { mutableStateOf("আসসালামু আলাইকুম, আমি জরুরি কারণে কালকে আসতে পারব না।") }

    // Simulator states
    var simSender by remember { mutableStateOf("রহিম / বস") }
    var simMessage by remember { mutableStateOf("তুমি কি কালকে ক্লাসে আসবা?") }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Master Toggle Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isAutoReplyActive) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface
                    ),
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
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(WhatsAppDarkGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "হোয়াটসঅ্যাপ অটো-রিপ্লাই",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isAutoReplyActive) "জমিলা সক্রিয়: মেসেজ আসলে স্বয়ংক্রিয় উত্তর দেবে" else "অটো-রিপ্লাই বন্ধ আছে",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        Switch(
                            checked = isAutoReplyActive,
                            onCheckedChange = { onToggleAutoReplyMaster() },
                            colors = SwitchDefaults.colors(checkedThumbColor = WhatsAppDarkGreen),
                            modifier = Modifier.testTag("whatsapp_master_switch")
                        )
                    }
                }
            }

            // Notification Access Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isAccessGranted) Color(0xFFF1F8E9) else Color(0xFFFFF3E0)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isAccessGranted) Icons.Default.CheckCircle else Icons.Default.Security,
                            contentDescription = null,
                            tint = if (isAccessGranted) WhatsAppDarkGreen else Color(0xFFE65100),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isAccessGranted) "নোটিফিকেশন পারমিশন দেওয়া আছে" else "নোটিফিকেশন লিসেনার অনুমতি প্রয়োজন",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = if (isAccessGranted) "হোয়াটসঅ্যাপ মেসেজ এলে জমিলা পড়তে পারবে।" else "অটো রিপ্লাই কার্যকর করতে সেটিংস থেকে অনুমতি দিন।",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                            )
                        }
                        if (!isAccessGranted) {
                            Button(
                                onClick = { WhatsAppHelper.openNotificationAccessSettings(context) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("অনুমতি দিন", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Direct WhatsApp Quick Composer
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "💬 দ্রুত হোয়াটসঅ্যাপে মেসেজ পাঠান",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = targetPhone,
                            onValueChange = { targetPhone = it },
                            placeholder = { Text("ফোন নম্বর (যেমন: 01700000000 বা খালি রাখুন)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = customMessage,
                            onValueChange = { customMessage = it },
                            placeholder = { Text("মেসেজ লিখুন...") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Preset chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FilterChip(
                                selected = customMessage.contains("কালকে আসতে পারব না"),
                                onClick = { customMessage = "আসসালামু আলাইকুম, আমি জরুরি কারণে কালকে আসতে পারব না।" },
                                label = { Text("কাল আসবো না", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = customMessage.contains("মিটিংয়ে আছি"),
                                onClick = { customMessage = "আমি এখন একটি জরুরি মিটিংয়ে আছি, পরে কথা বলছি।" },
                                label = { Text("মিটিংয়ে আছি", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = customMessage.contains("ব্যস্ত"),
                                onClick = { customMessage = "ধন্যবাদ মেসেজের জন্য! আমি একটু পর ফ্রি হয়ে নক দিচ্ছি।" },
                                label = { Text("ব্যস্ত আছি", fontSize = 11.sp) }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                WhatsAppHelper.sendWhatsAppMessage(context, targetPhone, customMessage)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppDarkGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("whatsapp_open_direct_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("হোয়াটসঅ্যাপ খুলুন ও পাঠান")
                        }
                    }
                }
            }

            // Auto Reply Rules Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "স্বয়ংক্রিয় রিপ্লাই রুলস (${rules.size}টি)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    TextButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("নতুন রুল")
                    }
                }
            }

            // List of Rules
            items(rules, key = { it.id }) { rule ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "কি-ওয়ার্ড: ${rule.triggerPattern}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "\"${rule.replyText}\"",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }

                        Switch(
                            checked = rule.isEnabled,
                            onCheckedChange = { onToggleRule(rule) },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        IconButton(onClick = { onDeleteRule(rule) }) {
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

            // Simulator Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF7C3AED))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "🧪 রিপ্লাই টেস্ট সিমুলেটর",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5B21B6)
                                )
                            )
                        }
                        Text(
                            text = "বাস্তবে মেসেজ আসার আগে এখানে টেস্ট করে দেখুন জমিলা কীভাবে উত্তর দেবে:",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF4C1D95)),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = simSender,
                                onValueChange = { simSender = it },
                                label = { Text("প্রেরক") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = simMessage,
                                onValueChange = { simMessage = it },
                                label = { Text("মেসেজ") },
                                modifier = Modifier.weight(1.5f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { onSimulateIncoming(simSender, simMessage) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("simulate_whatsapp_button")
                        ) {
                            Text("টেস্ট চালান ও উত্তর দেখুন")
                        }
                    }
                }
            }
        }
    }

    // Add Rule Dialog
    if (showAddDialog) {
        var patternInput by remember { mutableStateOf("") }
        var replyInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("নতুন অটো-রিপ্লাই রুল") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "কোন শব্দ বা কথায় ট্রিগার হবে (যেমন: kal, urgent, any):",
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = patternInput,
                        onValueChange = { patternInput = it },
                        placeholder = { Text("যেমন: kal|কাল|আসবো না") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "জমিলার উত্তর কী হবে:",
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = replyInput,
                        onValueChange = { replyInput = it },
                        placeholder = { Text("যেমন: আমি কালকে যেতে পারব না।") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (patternInput.isNotBlank() && replyInput.isNotBlank()) {
                            onAddNewRule(patternInput.trim(), replyInput.trim())
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("সংরক্ষণ করুন")
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
