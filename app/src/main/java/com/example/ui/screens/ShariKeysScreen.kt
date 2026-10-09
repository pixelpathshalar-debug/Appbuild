package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ShariKeyEntity
import com.example.ui.MainViewModel
import com.example.util.AppLanguage

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShariKeysScreen(
    viewModel: MainViewModel,
    language: AppLanguage,
    onNavigateToToBuy: () -> Unit
) {
    val context = LocalContext.current
    val isBn = language == AppLanguage.BENGALI
    val shariKeys by viewModel.shariKeys.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Shari-Keys, 1: Bazaar Guide
    var showAddKeyDialog by remember { mutableStateOf(false) }
    var keyToEdit by remember { mutableStateOf<ShariKeyEntity?>(null) }
    var keyToDelete by remember { mutableStateOf<ShariKeyEntity?>(null) }
    var toastNotice by remember { mutableStateOf<String?>(null) }

    fun copyKeyCode(code: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Smart Ginni Shari-Key", code)
        clipboard.setPrimaryClip(clip)
        toastNotice = if (isBn) "🔑 চাবির কোড ($code) কপি করা হয়েছে!" else "🔑 Key code ($code) copied to clipboard!"
    }

    fun shareKey(key: ShariKeyEntity) {
        val text = if (isBn)
            """
            🔑 স্মার্ট গিন্নি (Smart Ginni) - চাবির গোছা অ্যাক্সেস
            সদস্য: ${key.memberName} (${key.relation})
            গোপন চাবি কোড: ${key.keyCode}
            এই চাবি দিয়ে পরিবারের সদস্যরা দূর থেকে কিচেন স্টক ও বাজার ফর্দ যাচাই করতে পারবেন।
            অ্যাপ নির্মাতা: সুপর্ণা কান্তি দত্ত (৯৭৭৫৭০৮৭২২)
            """.trimIndent()
        else
            """
            🔑 Smart Ginni - Family Shari-Key Access
            Member: ${key.memberName} (${key.relation})
            Secret Key: ${key.keyCode}
            Use this key for remote pantry stock check & shopping sync.
            Handcrafted by Suparna Kanti Datta (9775708722)
            """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share Shari-Key"))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Tab Header: Shari-Keys vs Bazaar Directory
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.testTag("shari_key_tabs")
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Key, contentDescription = null, Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isBn) "চাবির গোছা (${shariKeys.size})" else "Shari-Keys (${shariKeys.size})", fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.testTag("tab_shari_keys")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storefront, contentDescription = null, Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isBn) "বাজার ডিরেক্টরি" else "Bazaar Guide", fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.testTag("tab_bazaar_guide")
                )
            }

            if (selectedTab == 0) {
                // Shari-Key Management Tab
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .testTag("shari_keys_list"),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
                    ) {
                    // Middle / Top Prominent Branding Card
                    item {
                        ElevatedCard(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth().testTag("middle_branding_shari_keys")
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                    ) {
                                        Icon(
                                            Icons.Default.Key,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (isBn) "স্মার্ট গিন্নি • চাবির গোছা" else "Smart Ginni • Shari-Key",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                Icons.Default.Verified,
                                                contentDescription = "Verified",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Text(
                                            text = if (isBn) "পরিবারের দূরবর্তী স্টক অ্যাক্সেস ও ফর্দ সিঙ্ক"
                                            else "Multi-User Remote Family Stock Access",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = if (isBn)
                                            "পরিকল্পনা ও রূপায়নে: সুপর্ণা কান্তি দত্ত (৯৭৭৫৭০৮৭২২)"
                                        else
                                            "Handcrafted by Suparna Kanti Datta (9775708722)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Notice Banner
                    if (toastNotice != null) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = toastNotice ?: "",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { toastNotice = null }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Close, contentDescription = null, Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Explanatory Info Card
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.People, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isBn)
                                        "চাবির গোছা দিয়ে মা, স্বামী, ভাই বা অন্য সদস্যরা দূর থেকে ফ্রিজ বা প্যান্ট্রির স্টক দেখতে ও বাজার ফর্দ আপডেট করতে পারবেন।"
                                    else
                                        "Share family keys so members can remotely verify pantry stocks and fulfill market needs.",
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Empty state
                    if (shariKeys.isEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(40.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isBn) "কোনো চাবি যুক্ত করা হয়নি" else "No Shari-Keys added yet",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isBn) "নিচের '+' বাটন চেপে পরিবারের সদস্যদের জন্য চাবি তৈরি করুন"
                                    else "Tap '+' below to add access keys for family members",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    // Key Items List with Add, Edit, Delete options
                    items(shariKeys, key = { it.id }) { key ->
                        Spacer(modifier = Modifier.height(10.dp))
                        ElevatedCard(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("shari_key_card_${key.id}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(
                                                    if (key.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                                    CircleShape
                                                )
                                        ) {
                                            Text(
                                                text = when (key.relation) {
                                                    "MOTHER" -> "👵"
                                                    "FATHER" -> "👴"
                                                    "SPOUSE" -> "💑"
                                                    "BROTHER" -> "👦"
                                                    "SISTER" -> "👧"
                                                    "CHILD" -> "👶"
                                                    "COOK" -> "👨‍🍳"
                                                    else -> "🗝️"
                                                },
                                                fontSize = 18.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = key.memberName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = "${key.contactIdentifier} • ${key.role}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }

                                    // Action icons: Copy, Share, Edit, Delete
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { copyKeyCode(key.keyCode) }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", Modifier.size(16.dp))
                                        }
                                        IconButton(onClick = { shareKey(key) }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Share, contentDescription = "Share", Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = { keyToEdit = key }, modifier = Modifier.size(32.dp).testTag("edit_key_${key.id}")) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                                        }
                                        IconButton(onClick = { keyToDelete = key }, modifier = Modifier.size(32.dp).testTag("delete_key_${key.id}")) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Key Code Display
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "চাবি কোড: ${key.keyCode}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = if (key.isActive) "সক্রিয় ✅" else "নিষ্ক্রিয় ❌",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Permissions tags
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (key.canCheckStock) {
                                        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(4.dp)) {
                                            Text(if (isBn) "স্টক দেখা ✓" else "View Stock ✓", fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                    if (key.canEditToBuy) {
                                        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(4.dp)) {
                                            Text(if (isBn) "ফর্দ এডিট ✓" else "Edit List ✓", fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                    if (key.canCheckOut) {
                                        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(4.dp)) {
                                            Text(if (isBn) "কেনা সম্পন্ন ✓" else "Checkout ✓", fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // FAB: Add Shari-Key
                FloatingActionButton(
                    onClick = { showAddKeyDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .testTag("fab_add_shari_key"),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Shari Key")
                }
                }
            } else {
                // Bazaar Directory Screen
                MarketFinderScreen(
                    viewModel = viewModel,
                    language = language,
                    onNavigateToToBuy = onNavigateToToBuy
                )
            }
        }
    }

    // Add Key Dialog
    if (showAddKeyDialog) {
        ShariKeyEditorDialog(
            initialKey = null,
            language = language,
            onDismiss = { showAddKeyDialog = false },
            onSave = { name, rel, contact, role, stock, edit, buy, notes ->
                viewModel.addShariKey(name, rel, contact, role, stock, edit, buy, notes)
                showAddKeyDialog = false
                toastNotice = if (isBn) "✅ $name এর জন্য নতুন চাবি যুক্ত হয়েছে!" else "✅ Key added for $name!"
            }
        )
    }

    // Edit Key Dialog
    if (keyToEdit != null) {
        val editing = keyToEdit!!
        ShariKeyEditorDialog(
            initialKey = editing,
            language = language,
            onDismiss = { keyToEdit = null },
            onSave = { name, rel, contact, role, stock, edit, buy, notes ->
                val updated = editing.copy(
                    memberName = name,
                    relation = rel,
                    contactIdentifier = contact,
                    role = role,
                    canCheckStock = stock,
                    canEditToBuy = edit,
                    canCheckOut = buy,
                    notes = notes
                )
                viewModel.updateShariKey(updated)
                keyToEdit = null
                toastNotice = if (isBn) "✅ $name এর চাবি আপডেট হয়েছে!" else "✅ Key updated for $name!"
            }
        )
    }

    // Delete Key Confirmation Dialog
    if (keyToDelete != null) {
        val target = keyToDelete!!
        AlertDialog(
            onDismissRequest = { keyToDelete = null },
            title = {
                Text(if (isBn) "চাবি বাতিল করবেন?" else "Revoke Shari-Key?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    if (isBn)
                        "আপনি কি নিশ্চিত যে '${target.memberName}' এর অ্যাক্সেস চাবি (${target.keyCode}) মুছে ফেলতে চান?"
                    else
                        "Are you sure you want to delete and revoke '${target.memberName}' (${target.keyCode})?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteShariKey(target.id, target.memberName)
                        keyToDelete = null
                        toastNotice = if (isBn) "🗑️ চাবি সফলভাবে মুছে ফেলা হয়েছে!" else "🗑️ Key deleted successfully!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isBn) "মুছে ফেলুন" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { keyToDelete = null }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShariKeyEditorDialog(
    initialKey: ShariKeyEntity?,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (name: String, rel: String, contact: String, role: String, stock: Boolean, edit: Boolean, buy: Boolean, notes: String) -> Unit
) {
    val isBn = language == AppLanguage.BENGALI

    var memberName by remember { mutableStateOf(initialKey?.memberName ?: "") }
    var relation by remember { mutableStateOf(initialKey?.relation ?: "SPOUSE") }
    var contactIdentifier by remember { mutableStateOf(initialKey?.contactIdentifier ?: "") }
    var role by remember { mutableStateOf(initialKey?.role ?: "MEMBER") }
    var canCheckStock by remember { mutableStateOf(initialKey?.canCheckStock ?: true) }
    var canEditToBuy by remember { mutableStateOf(initialKey?.canEditToBuy ?: true) }
    var canCheckOut by remember { mutableStateOf(initialKey?.canCheckOut ?: true) }
    var notes by remember { mutableStateOf(initialKey?.notes ?: "") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val relations = listOf(
        "MOTHER" to (if (isBn) "মা 👵" else "Mother"),
        "FATHER" to (if (isBn) "বাবা 👴" else "Father"),
        "SPOUSE" to (if (isBn) "স্বামী/স্ত্রী 💑" else "Spouse"),
        "BROTHER" to (if (isBn) "ভাই 👦" else "Brother"),
        "SISTER" to (if (isBn) "বোন 👧" else "Sister"),
        "CHILD" to (if (isBn) "সন্তান 👶" else "Child"),
        "COOK" to (if (isBn) "রাঁধুনি 👨‍🍳" else "Cook"),
        "OTHER" to (if (isBn) "অন্যান্য" else "Other")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialKey == null)
                    (if (isBn) "নতুন চাবি যুক্ত করুন 🗝️" else "Add Family Shari-Key 🗝️")
                else
                    (if (isBn) "চাবি সম্পাদনা ✏️" else "Edit Shari-Key ✏️"),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = memberName,
                    onValueChange = { memberName = it },
                    label = { Text(if (isBn) "সদস্যের নাম" else "Member Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("shari_key_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = contactIdentifier,
                    onValueChange = { contactIdentifier = it },
                    label = { Text(if (isBn) "ফোন বা ইমেইল" else "Phone or Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("shari_key_contact_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = if (isBn) "সম্পর্ক নির্বাচন:" else "Relation:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    relations.forEach { (relKey, relLabel) ->
                        FilterChip(
                            selected = relation == relKey,
                            onClick = { relation = relKey },
                            label = { Text(relLabel, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Permissions toggles
                Text(text = if (isBn) "অনুমতিসমূহ (Permissions):" else "Permissions:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isBn) "স্টক যাচাই করার অনুমতি" else "Allow stock checking", fontSize = 12.sp)
                    Switch(checked = canCheckStock, onCheckedChange = { canCheckStock = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isBn) "বাজার ফর্দ এডিট করার অনুমতি" else "Allow editing shopping list", fontSize = 12.sp)
                    Switch(checked = canEditToBuy, onCheckedChange = { canEditToBuy = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isBn) "পণ্য কেনা সম্পন্ন করার অনুমতি" else "Allow checkout / mark bought", fontSize = 12.sp)
                    Switch(checked = canCheckOut, onCheckedChange = { canCheckOut = it })
                }

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(errorMsg ?: "", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (memberName.isBlank()) {
                        errorMsg = if (isBn) "সদস্যের নাম লিখুন" else "Enter member name"
                        return@Button
                    }
                    if (contactIdentifier.isBlank()) {
                        errorMsg = if (isBn) "ফোন নম্বর বা ইমেইল লিখুন" else "Enter contact phone/email"
                        return@Button
                    }
                    onSave(memberName.trim(), relation, contactIdentifier.trim(), role, canCheckStock, canEditToBuy, canCheckOut, notes.trim())
                },
                modifier = Modifier.testTag("save_shari_key_btn")
            ) {
                Text(if (isBn) "সেভ" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBn) "বাতিল" else "Cancel")
            }
        }
    )
}
