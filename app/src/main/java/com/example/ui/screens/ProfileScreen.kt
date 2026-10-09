package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.MainViewModel
import com.example.util.AppLanguage
import com.example.util.NotificationHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    language: AppLanguage,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val isBn = language == AppLanguage.BENGALI
    val currentUser by viewModel.currentUser.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var editName by remember(currentUser) { mutableStateOf(currentUser?.name ?: "") }
    var editIdentifier by remember(currentUser) { mutableStateOf(currentUser?.identifier ?: "") }
    var editCompanionId by remember(currentUser) { mutableStateOf(currentUser?.companionIdentifier ?: "") }
    var editCompanionRole by remember(currentUser) { mutableStateOf(currentUser?.companionRole ?: "WIFE") }
    var editCurrency by remember(currentUser) { mutableStateOf(currentUser?.currencySymbol ?: "₹") }
    var editUnitSystem by remember(currentUser) { mutableStateOf(currentUser?.preferredUnitSystem ?: "METRIC") }
    var editBackupFreq by remember(currentUser) { mutableStateOf(currentUser?.backupFrequency ?: "DAILY") }

    var saveFeedback by remember { mutableStateOf<String?>(null) }
    var showDeactivateDialog by remember { mutableStateOf(false) }

    val currencyList = listOf(
        Pair("₹", "INR (₹) - India"),
        Pair("৳", "BDT (৳) - Bangladesh"),
        Pair("$", "USD ($) - United States"),
        Pair("€", "EUR (€) - Europe"),
        Pair("£", "GBP (£) - UK"),
        Pair("﷼", "SAR (﷼) - Saudi Arabia"),
        Pair("د.إ", "AED (د.إ) - UAE")
    )
    var currencyDropdownExpanded by remember { mutableStateOf(false) }
    var backupDropdownExpanded by remember { mutableStateOf(false) }
    val backupFrequencies = listOf("DAILY", "WEEKLY", "MONTHLY", "OFF")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("profile_screen_list"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // User Profile Header Card
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(currentUser?.avatarColor ?: 0xFF1B5E20), CircleShape)
                    ) {
                        Text(
                            text = (currentUser?.name ?: "U").take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = currentUser?.name ?: "User",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = currentUser?.identifier ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            color = if (currentUser?.role == "ADMIN") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (currentUser?.role == "ADMIN") "ADMIN" else "USER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentUser?.role == "ADMIN") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Currency: ${currentUser?.currencySymbol ?: "₹"}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // Edit Profile & Companion Device Linking (Husband & Wife)
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "প্রোফাইল ও সহচর ডিভাইস" else "Profile & Companion Device",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        OutlinedButton(
                            onClick = { isEditing = !isEditing },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("toggle_edit_profile_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isEditing) (if (isBn) "বাতিল" else "Cancel") else (if (isBn) "সম্পাদনা" else "Edit"), fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isEditing) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text(if (isBn) "নাম" else "Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = editIdentifier,
                            onValueChange = { editIdentifier = it },
                            label = { Text(if (isBn) "মোবাইল বা ইমেইল" else "Phone / Email") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Companion Device Setup (Husband & Wife)
                        Text(
                            text = if (isBn) "👨‍👩‍👧 সহচর ডিভাইস (স্বামী বা স্ত্রী):" else "👨‍👩‍👧 Companion Device (Husband / Wife):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = editCompanionId,
                                onValueChange = { editCompanionId = it },
                                label = { Text(if (isBn) "সঙ্গীর ফোন বা ইমেইল" else "Spouse Phone/Email") },
                                singleLine = true,
                                modifier = Modifier.weight(1.3f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                                FilterChip(
                                    selected = editCompanionRole == "WIFE",
                                    onClick = { editCompanionRole = if (editCompanionRole == "WIFE") "HUSBAND" else "WIFE" },
                                    label = { Text(if (editCompanionRole == "WIFE") "স্ত্রী 👩" else "স্বামী 👨", fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Currency Selector
                        Text(
                            text = if (isBn) "মুদ্রা নির্বাচন (Currency):" else "Currency Selection:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ExposedDropdownMenuBox(
                            expanded = currencyDropdownExpanded,
                            onExpandedChange = { currencyDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = currencyList.firstOrNull { it.first == editCurrency }?.second ?: editCurrency,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Currency") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyDropdownExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = currencyDropdownExpanded,
                                onDismissRequest = { currencyDropdownExpanded = false }
                            ) {
                                currencyList.forEach { (sym, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            editCurrency = sym
                                            currencyDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Scheduled Google Backup
                        Text(
                            text = if (isBn) "গুগল ক্লাউড ব্যাকআপ সময়সূচী:" else "Google Cloud Backup Schedule:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ExposedDropdownMenuBox(
                            expanded = backupDropdownExpanded,
                            onExpandedChange = { backupDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = editBackupFreq,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Backup Frequency") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = backupDropdownExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = backupDropdownExpanded,
                                onDismissRequest = { backupDropdownExpanded = false }
                            ) {
                                backupFrequencies.forEach { freq ->
                                    DropdownMenuItem(
                                        text = { Text(freq) },
                                        onClick = {
                                            editBackupFreq = freq
                                            backupDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                viewModel.updateProfile(
                                    editName,
                                    editIdentifier,
                                    language.code,
                                    editCompanionId,
                                    editCompanionRole,
                                    editCurrency,
                                    editUnitSystem,
                                    editBackupFreq
                                )
                                isEditing = false
                                saveFeedback = if (isBn) "✅ সফলভাবে সেভ হয়েছে!" else "✅ Saved successfully!"
                            },
                            modifier = Modifier.fillMaxWidth().testTag("save_profile_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isBn) "সেভ" else "Save")
                        }
                    } else {
                        // Display view
                        Text(text = if (isBn) "নাম: ${currentUser?.name}" else "Name: ${currentUser?.name}", fontSize = 13.sp)
                        Text(text = if (isBn) "যোগাযোগ: ${currentUser?.identifier}" else "Contact: ${currentUser?.identifier}", fontSize = 13.sp)
                        val comp = currentUser?.companionIdentifier
                        Text(
                            text = if (!comp.isNullOrBlank())
                                (if (isBn) "সহচর (${currentUser?.companionRole}): $comp" else "Companion (${currentUser?.companionRole}): $comp")
                            else
                                (if (isBn) "সহচর: লিংক করা নেই" else "Companion: None linked"),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(text = "Currency: ${currentUser?.currencySymbol ?: "₹"} • Schedule: ${currentUser?.backupFrequency ?: "DAILY"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }

                    if (saveFeedback != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = saveFeedback ?: "", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Google Backup Upload Card
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
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "গুগল ক্লাউড ব্যাকআপ" else "Google Cloud Backup & Restore",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val lastBackup = currentUser?.lastBackupTime ?: 0L
                    val lastBackupStr = if (lastBackup > 0) {
                        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(lastBackup))
                    } else {
                        if (isBn) "এখনো ব্যাকআপ নেওয়া হয়নি" else "Never backed up"
                    }

                    Text(
                        text = if (isBn)
                            "শিডিউল: ${currentUser?.backupFrequency ?: "DAILY"} • শেষ ব্যাকআপ: $lastBackupStr\nইমেইল (${currentUser?.identifier}) দিয়ে গুগল শিট ডাটাবেজে সুরক্ষিত ব্যাকআপ।"
                        else
                            "Schedule: ${currentUser?.backupFrequency ?: "DAILY"} • Last backup: $lastBackupStr\nSafely stores your inventory and shopping history in Google Sheets.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.syncWithGoogleSheets(actionType = "SCHEDULED_BACKUP") },
                        modifier = Modifier.fillMaxWidth().testTag("backup_now_button")
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null, Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBn) "এখনই গুগলে ব্যাকআপ আপলোড করুন" else "Backup Now to Google")
                    }
                }
            }
        }

        // Language & Preference Settings
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
                        Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "ভাষা পরিবর্তন (Language)" else "Language Toggle",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = language == AppLanguage.BENGALI,
                            onClick = { viewModel.setLanguage(AppLanguage.BENGALI) },
                            label = { Text("বাংলা (Bengali)", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = language == AppLanguage.ENGLISH,
                            onClick = { viewModel.setLanguage(AppLanguage.ENGLISH) },
                            label = { Text("English", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Logout & Deactivate Account (Requirement 6)
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBn) "অ্যাকাউন্ট নিয়ন্ত্রণ ও মুছে ফেলা" else "Account & Data Management",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.logout()
                                onLogout()
                            },
                            modifier = Modifier.weight(1.2f).testTag("baper_bari_logout_button")
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBn) "বাপের বাড়ি 🧳" else "Baper Bari (Logout)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showDeactivateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1.3f).testTag("deactivate_account_button")
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBn) "অ্যাকাউন্ট মুছুন" else "Deactivate & Wipe", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Handcrafted by Suparna Kanti Datta Branding Card
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Smart Ginni • স্মার্ট গিন্নি",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Handcrafted by Suparna Kanti Datta",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "📞 9775708722",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "বাংলা সংস্কৃতি ও আধুনিক সংসার ব্যবস্থাপনার মেলবন্ধন",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }

    // Deactivate & Delete Profile Data Dialog
    if (showDeactivateDialog) {
        AlertDialog(
            onDismissRequest = { showDeactivateDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "অ্যাকাউন্ট নিষ্ক্রিয় ও মুছে ফেলা" else "Deactivate & Delete Account",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text(
                    text = if (isBn)
                        "আপনি কি নিশ্চিত যে আপনার প্রোফাইল নিষ্ক্রিয় করবেন এবং গুগল শিট ডাটাবেজ থেকে সমস্ত তথ্য মুছে ফেলবেন? এই কাজটি অপরিবর্তনীয়।"
                    else
                        "Are you sure you want to deactivate your account and delete all profile data from the Google Sheet database? This cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeactivateDialog = false
                        viewModel.userDeactivateAccount()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isBn) "হ্যাঁ, মুছে ফেলুন" else "Yes, Delete & Wipe")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeactivateDialog = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        )
    }
}
