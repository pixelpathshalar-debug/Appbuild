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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.WebhookManager
import com.example.ui.MainViewModel
import com.example.util.AppLanguage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminScreen(
    viewModel: MainViewModel,
    language: AppLanguage,
    onNavigateToAuth: () -> Unit
) {
    val isBn = language == AppLanguage.BENGALI
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val webhookConfig by viewModel.webhookConfig.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncFeedback by viewModel.syncFeedback.collectAsState()
    val activityLogs by viewModel.activityLogs.collectAsState()

    var selectedAdminTab by remember { mutableIntStateOf(0) } // 0: Webhook/Creds, 1: Users, 2: Logs
    var webhookUrlInput by remember(webhookConfig) {
        mutableStateOf(webhookConfig?.webhookUrl ?: "https://script.google.com/macros/s/AKfycbx_DEMO_KITCHEN_APP_WEBHOOK/exec")
    }
    var autoSyncState by remember(webhookConfig) {
        mutableStateOf(webhookConfig?.autoSyncEnabled ?: true)
    }

    var adminUserIdInput by remember { mutableStateOf("admin@kitchen.com") }
    var adminPasswordInput by remember { mutableStateOf("admin123") }
    var showScriptDialog by remember { mutableStateOf(false) }

    val isAdmin = currentUser?.role == "ADMIN"

    if (!isAdmin) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(64.dp)
                            .background(MaterialTheme.colorScheme.errorContainer, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isBn) "প্রশাসক প্রবেশাধিকার সীমাবদ্ধ" else "Admin Access Restricted",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isBn)
                            "এই বিভাগটি শুধুমাত্র এডমিনের জন্য সংরক্ষিত। সাধারণ ব্যবহারকারীদের এখানে প্রবেশাধিকার নেই।"
                        else
                            "This panel is restricted to System Admins only. Regular user accounts cannot access database management.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onNavigateToAuth,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isBn) "এডমিন পোর্টালে লগইন করুন" else "Login via Admin Portal")
                    }
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("admin_dashboard_list"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Admin Header
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(46.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(26.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isBn) "এডমিন কন্ট্রোল প্যানেল" else "Admin Control Dashboard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = if (isBn) "গুগল শিট ক্রেডেনশিয়াল ও ব্যবহারকারী একাউন্ট নিয়ন্ত্রণ"
                            else "Google Sheet Creds & User Account Management",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Admin Tabs
        item {
            Spacer(modifier = Modifier.height(12.dp))
            TabRow(
                selectedTabIndex = selectedAdminTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.testTag("admin_tabs")
            ) {
                Tab(
                    selected = selectedAdminTab == 0,
                    onClick = { selectedAdminTab = 0 },
                    text = { Text(if (isBn) "শিট ও ক্রেডেনশিয়াল" else "Sheets & Creds", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedAdminTab == 1,
                    onClick = { selectedAdminTab = 1 },
                    text = { Text(if (isBn) "ব্যবহারকারী (${allUsers.size})" else "Users (${allUsers.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedAdminTab == 2,
                    onClick = { selectedAdminTab = 2 },
                    text = { Text(if (isBn) "অডিট লগ" else "Audit Logs", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        when (selectedAdminTab) {
            0 -> {
                // Admin Credentials & Google Sheet Config
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBn) "এডমিন আইডি ও পাসওয়ার্ড" else "Admin Credentials",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isBn)
                                    "এখানে সেট করা এডমিন ইউজার আইডি ও পাসওয়ার্ড গুগল শিটের 'Admin_Config' ট্যাবে সংরক্ষিত হবে।"
                                else
                                    "Admin User ID & Password will be synchronized to Google Sheets 'Admin_Config' tab.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = adminUserIdInput,
                                onValueChange = { adminUserIdInput = it },
                                label = { Text("Admin User ID") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = adminPasswordInput,
                                onValueChange = { adminPasswordInput = it },
                                label = { Text("Admin Password") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { viewModel.updateAdminCredentials(adminUserIdInput, adminPasswordInput) },
                                modifier = Modifier.fillMaxWidth().testTag("save_admin_creds_button")
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isBn) "সেভ" else "Save")
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(14.dp))

                            // Webhook URL
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBn) "গুগল শিট Webhook URL" else "Google Sheet Webhook URL",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = webhookUrlInput,
                                onValueChange = { webhookUrlInput = it },
                                label = { Text("Google Apps Script Web App URL") },
                                maxLines = 3,
                                modifier = Modifier.fillMaxWidth().testTag("webhook_url_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(if (isBn) "স্বয়ংক্রিয় সিঙ্ক (Auto-Sync)" else "Auto-Sync Enabled", fontSize = 13.sp)
                                Switch(
                                    checked = autoSyncState,
                                    onCheckedChange = { autoSyncState = it }
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.saveWebhookUrl(webhookUrlInput, autoSyncState) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(if (isBn) "সেভ" else "Save")
                                }

                                Button(
                                    onClick = {
                                        viewModel.saveWebhookUrl(webhookUrlInput, autoSyncState)
                                        viewModel.syncWithGoogleSheets(actionType = "ADMIN_MANUAL_SYNC")
                                    },
                                    enabled = !isSyncing,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.weight(1.2f).testTag("sync_now_button")
                                ) {
                                    if (isSyncing) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.Sync, contentDescription = null, Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isBn) "এখনই সিঙ্ক" else "Sync Now")
                                }
                            }

                            if (syncFeedback != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = syncFeedback ?: "", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedButton(
                                onClick = { showScriptDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Code, contentDescription = null, Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isBn) "গুগল শিট স্ক্রিপ্ট কোড দেখুন" else "View Google Apps Script Code")
                            }
                        }
                    }
                }
            }

            1 -> {
                // User Accounts Management (Suspend / Reactivate)
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isBn) "নিবন্ধিত অ্যাকাউন্ট ও নিয়ন্ত্রণ (Suspend / Reactivate):" else "User Accounts & Moderation:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                items(allUsers, key = { it.id }) { user ->
                    Spacer(modifier = Modifier.height(8.dp))
                    val isSuspended = user.status == "SUSPENDED"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSuspended)
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = user.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = if (isSuspended) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = user.status,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSuspended) Color.White else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(text = user.identifier, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                                    if (!user.companionIdentifier.isNullOrBlank()) {
                                        Text(
                                            text = "Companion (${user.companionRole}): ${user.companionIdentifier}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                // Suspend or Reactivate button (Requirement 6)
                                if (user.role != "ADMIN") {
                                    if (isSuspended) {
                                        Button(
                                            onClick = { viewModel.adminSetUserStatus(user.id, "ACTIVE") },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null, Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isBn) "সক্রিয় করুন" else "Reactivate", fontSize = 11.sp)
                                        }
                                    } else {
                                        OutlinedButton(
                                            onClick = { viewModel.adminSetUserStatus(user.id, "SUSPENDED") },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Block, contentDescription = null, Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isBn) "বরখাস্ত করুন" else "Suspend", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Audit Logs
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isBn) "কার্যকলাপের লগ:" else "Audit Activity Logs:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                items(activityLogs, key = { it.id }) { log ->
                    Spacer(modifier = Modifier.height(6.dp))
                    val dateFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(log.timestamp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = if (isBn) log.titleBn else log.titleEn, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(text = "$dateFormatted • ${log.type}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showScriptDialog) {
        AlertDialog(
            onDismissRequest = { showScriptDialog = false },
            title = { Text(if (isBn) "গুগল শিট Apps Script কোড" else "Google Sheets Script Template", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = if (isBn)
                            "এই কোডটি Google Sheets এ পেস্ট করুন। এটি স্বয়ংক্রিয়ভাবে 'Pantry', 'ToBuy', 'Users', এবং 'Admin_Config' শিট তৈরি করবে।"
                        else
                            "Paste this in Google Sheets -> Extensions -> Apps Script. It handles Pantry, ToBuy, Users, and Admin credentials.",
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = WebhookManager.GOOGLE_APPS_SCRIPT_SAMPLE.take(300) + "\n... [Full script configured]",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showScriptDialog = false }) { Text("Close") }
            }
        )
    }
}
