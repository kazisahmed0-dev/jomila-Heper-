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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallActivityLog
import com.example.ui.theme.CallBlue
import com.example.ui.theme.WhatsAppDarkGreen
import com.example.util.AlarmScheduler
import com.example.util.PhoneCallHelper
import com.example.util.WhatsAppHelper

@Composable
fun ReceptionistScreen(
    callLogs: List<CallActivityLog>,
    isReceptionistActive: Boolean,
    onToggleReceptionist: () -> Unit
) {
    val context = LocalContext.current
    var inputPhone by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf("সম্মানজনক বাংলা") }
    var responseMessage by remember {
        mutableStateOf("আসসালামু আলাইকুম! আমি এখন কথা বলতে পারছি না, একটু পর কল ব্যাক করছি। - জমিলা হেল্পার")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Master switch
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isReceptionistActive) Color(0xFFE0F2FE) else MaterialTheme.colorScheme.surface
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
                            .background(CallBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PhoneInTalk,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "স্মার্ট কল রিসেপশনিস্ট",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isReceptionistActive) "কল আসলে জমিলা সুন্দর ও সম্মানজনক মেসেজে হ্যান্ডেল করবে" else "কল রিসেপশনিস্ট স্ট্যান্ডবাই আছে",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    Switch(
                        checked = isReceptionistActive,
                        onCheckedChange = { onToggleReceptionist() },
                        colors = SwitchDefaults.colors(checkedThumbColor = CallBlue),
                        modifier = Modifier.testTag("receptionist_switch")
                    )
                }
            }
        }

        // Response Templates & Quick Caller Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📞 কলারকে কী উত্তর দিতে চান?",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedTemplate == "সম্মানজনক বাংলা",
                            onClick = {
                                selectedTemplate = "সম্মানজনক বাংলা"
                                responseMessage = "আসসালামু আলাইকুম! আমি এখন কথা বলতে পারছি না, একটু পর কল ব্যাক করছি। - জমিলা হেল্পার"
                            },
                            label = { Text("সম্মানজনক", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedTemplate == "জরুরি মিটিং",
                            onClick = {
                                selectedTemplate = "জরুরি মিটিং"
                                responseMessage = "আমি একটি জরুরি মিটিংয়ে আছি, অনুগ্রহ করে এসএমএস করুন। - জমিলা"
                            },
                            label = { Text("মিটিং", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedTemplate == "স্কুল মোড",
                            onClick = {
                                selectedTemplate = "স্কুল মোড"
                                responseMessage = "আমি এখন স্কুলে/ক্লাসে আছি, ক্লাস শেষে কথা বলব ইনশাআল্লাহ। - জমিলা"
                            },
                            label = { Text("স্কুল মোড", fontSize = 11.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = responseMessage,
                        onValueChange = { responseMessage = it },
                        label = { Text("রিসেপশনিস্ট বার্তা") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = inputPhone,
                        onValueChange = { inputPhone = it },
                        placeholder = { Text("কলার বা প্রাপকের ফোন নম্বর (যেমন: 017...)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (inputPhone.isNotBlank()) {
                                    PhoneCallHelper.dialPhoneNumber(context, inputPhone)
                                } else {
                                    PhoneCallHelper.dialPhoneNumber(context, "")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CallBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("dial_call_button")
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("কল ডায়াল", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                PhoneCallHelper.sendSms(context, inputPhone, responseMessage)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("এসএমএস", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                WhatsAppHelper.sendWhatsAppMessage(context, inputPhone, responseMessage)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppDarkGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("হোয়াটসঅ্যাপ", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Call Logs Header
        item {
            Text(
                text = "সাম্প্রতিক কল ও জমিলা রেসপন্স লগ (${callLogs.size}টি)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (callLogs.isEmpty()) {
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
                        Text(
                            text = "কোনো সাম্প্রতিক কল লগ নেই",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = "কল আসলে জমিলা স্বয়ংক্রিয়ভাবে এখানে তালিকা তৈরি করবে।",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Color.Gray)
                        )
                    }
                }
            }
        } else {
            items(callLogs, key = { it.id }) { log ->
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
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Call,
                                contentDescription = null,
                                tint = CallBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = log.phoneNumber.ifBlank { "ইনকামিং কলার" },
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = AlarmScheduler.formatBanglaTime(log.timestamp),
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                            )
                        }

                        OutlinedButton(
                            onClick = { PhoneCallHelper.dialPhoneNumber(context, log.phoneNumber) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("কল ব্যাক", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
