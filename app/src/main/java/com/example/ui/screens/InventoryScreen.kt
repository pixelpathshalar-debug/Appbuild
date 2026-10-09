package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.PantryItemEntity
import com.example.ui.MainViewModel
import com.example.ui.components.AddItemDialog
import com.example.ui.components.EditItemDialog
import com.example.ui.components.VoiceCommandDialog
import com.example.ui.theme.FinishedColor
import com.example.ui.theme.InStockColor
import com.example.ui.theme.LowStockColor
import com.example.util.AppLanguage

@Composable
fun InventoryScreen(
    viewModel: MainViewModel,
    language: AppLanguage,
    onNavigateToToBuy: () -> Unit
) {
    val isBn = language == AppLanguage.BENGALI
    val items by viewModel.pantryItems.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val voiceFeedback by viewModel.voiceFeedback.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<PantryItemEntity?>(null) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    val voiceSearchLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenWords = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = spokenWords?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                searchQuery = spoken
            }
        }
    }

    val currency = currentUser?.currencySymbol ?: "৳"
    val lowStockCount = items.count { it.isLowStock }
    val finishedCount = items.count { it.isFinished }

    val filteredItems = items.filter { item ->
        val matchesCategory = when (selectedCategory) {
            "ALL" -> true
            "VEGETABLES" -> item.category == "VEGETABLES"
            "SPICES" -> item.category == "SPICES"
            "MEDICINE" -> item.category == "MEDICINE"
            "GARMENTS" -> item.category == "GARMENTS"
            "HARDWARE" -> item.category == "HARDWARE"
            "HOUSEHOLD" -> item.category == "HOUSEHOLD"
            "STAPLES" -> item.category == "STAPLES"
            "OTHERS" -> item.category == "OTHERS" || item.category.isBlank()
            "LOW_STOCK" -> item.isLowStock || item.isFinished
            else -> true
        }
        val matchesSearch = searchQuery.isBlank() ||
                item.nameEn.contains(searchQuery, ignoreCase = true) ||
                item.nameBn.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("inventory_list"),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Hero Banner & Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.kitchen_market_hero),
                        contentDescription = "Kitchen Produce",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f))
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = if (isBn) "রান্নাঘর ও পারিবারিক মজুদ" else "Kitchen & Household Pantry",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isBn) "শাকসবজি, মসলা ও ঔষধের স্টক এবং দর ব্যবস্থাপনা"
                            else "Vegetables, spices, medicine stock & rate management",
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Companion Household Sync Info Banner (Husband & Wife)
            if (!currentUser?.companionIdentifier.isNullOrBlank()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.People,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBn)
                                    "👨‍👩‍👧 সহচর ডিভাইস সংযুক্ত (${currentUser?.companionRole ?: "সঙ্গী"}: ${currentUser?.companionIdentifier})"
                                else
                                    "👨‍👩‍👧 Linked with ${currentUser?.companionRole ?: "Spouse"} (${currentUser?.companionIdentifier})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // Voice Command Banner / Trigger
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { showVoiceDialog = true }
                        .testTag("voice_command_banner_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "Voice",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) "🎙️ ভয়েস কমান্ড: 'আলু শেষ' বা 'নাপা শেষ'" else "🎙️ Voice: 'Potato finish' or 'Napa finish'",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isBn) "স্বয়ংক্রিয়ভাবে বাজার ফর্দে যোগ হবে ও নোটিফিকেশন পাঠাবে"
                                else "Auto-adds to buy list & sends companion push notification",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Voice Feedback Snackbar / Banner
            if (voiceFeedback != null) {
                item {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = voiceFeedback ?: "",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            IconButton(onClick = { viewModel.clearVoiceFeedback() }) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Low Stock Alert Banner
            if (lowStockCount > 0 || finishedCount > 0) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("low_stock_warning_banner"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(26.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isBn) "⚠️ $lowStockCount টি পণ্যের স্টক কম, $finishedCount টি শেষ!"
                                    else "⚠️ $lowStockCount low stock, $finishedCount finished!",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 13.sp
                                )
                            }
                            Button(
                                onClick = onNavigateToToBuy,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(if (isBn) "ফর্দ দেখুন" else "View List", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Middle Prominent Branding Card
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("middle_branding_inventory")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (isBn) "স্মার্ট গিন্নি • Smart Ginni" else "Smart Ginni (স্মার্ট গিন্নি)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = "Verified Handcrafted",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = if (isBn)
                                "পরিকল্পনা ও রূপায়নে: সুপর্ণা কান্তি দত্ত (৯৭৭৫৭০৮৭২২)"
                            else
                                "Handcrafted by Suparna Kanti Datta (9775708722)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // Search Bar with Voice Search Button
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(if (isBn) "পণ্য বা ঔষধ খুঁজুন (যেমন: আলু, জিরা, প্যারাসিটামল)..." else "Search items or medicine...")
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                }
                            }
                            IconButton(
                                onClick = {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (isBn) "bn-BD" else java.util.Locale.getDefault().toString())
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, if (isBn) "পণ্য খুঁজতে বলুন..." else "Speak to search pantry items...")
                                    }
                                    try {
                                        voiceSearchLauncher.launch(intent)
                                    } catch (_: Exception) {}
                                },
                                modifier = Modifier.testTag("voice_search_inventory_btn")
                            ) {
                                Icon(
                                    Icons.Default.Mic,
                                    contentDescription = "Voice Search",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("inventory_search_field")
                )
            }

            // Category Filter Chips (Including Garments, Hardware, Household, Medicine & Others)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == "ALL",
                        onClick = { selectedCategory = "ALL" },
                        label = { Text(if (isBn) "সব" else "All", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_all_chip")
                    )
                    FilterChip(
                        selected = selectedCategory == "VEGETABLES",
                        onClick = { selectedCategory = "VEGETABLES" },
                        label = { Text(if (isBn) "🥬 শাকসবজি" else "🥬 Veg", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_veg_chip")
                    )
                    FilterChip(
                        selected = selectedCategory == "SPICES",
                        onClick = { selectedCategory = "SPICES" },
                        label = { Text(if (isBn) "🧂 মসলা" else "🧂 Spice", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_spice_chip")
                    )
                    FilterChip(
                        selected = selectedCategory == "MEDICINE",
                        onClick = { selectedCategory = "MEDICINE" },
                        label = { Text(if (isBn) "💊 ঔষধ" else "💊 Medicine", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_medicine_chip")
                    )
                    FilterChip(
                        selected = selectedCategory == "GARMENTS",
                        onClick = { selectedCategory = "GARMENTS" },
                        label = { Text(if (isBn) "👕 পোশাক" else "👕 Garments", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_garments_chip")
                    )
                    FilterChip(
                        selected = selectedCategory == "HARDWARE",
                        onClick = { selectedCategory = "HARDWARE" },
                        label = { Text(if (isBn) "🔧 হার্ডওয়্যার" else "🔧 Hardware", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_hardware_chip")
                    )
                    FilterChip(
                        selected = selectedCategory == "HOUSEHOLD",
                        onClick = { selectedCategory = "HOUSEHOLD" },
                        label = { Text(if (isBn) "🏠 গৃহস্থালি" else "🏠 Household", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_household_chip")
                    )
                    FilterChip(
                        selected = selectedCategory == "STAPLES",
                        onClick = { selectedCategory = "STAPLES" },
                        label = { Text(if (isBn) "🍚 রসদ" else "🍚 Staples", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_staples_chip")
                    )
                    FilterChip(
                        selected = selectedCategory == "OTHERS",
                        onClick = { selectedCategory = "OTHERS" },
                        label = { Text(if (isBn) "📦 অন্যান্য" else "📦 Others", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_others_chip")
                    )
                    FilterChip(
                        selected = selectedCategory == "LOW_STOCK",
                        onClick = { selectedCategory = "LOW_STOCK" },
                        label = { Text(if (isBn) "ঘাটতি ($lowStockCount)" else "Low ($lowStockCount)", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_low_stock_chip")
                    )
                }
            }

            // Items List
            items(filteredItems, key = { it.id }) { item ->
                PantryItemCard(
                    item = item,
                    language = language,
                    currencySymbol = currency,
                    onIncrease = {
                        val delta = if (item.unit == "kg") 0.5 else if (item.unit == "strip") 1.0 else if (item.unit == "g") 50.0 else 1.0
                        viewModel.updatePantryQuantity(item, delta)
                    },
                    onDecrease = {
                        val delta = if (item.unit == "kg") -0.5 else if (item.unit == "strip") -1.0 else if (item.unit == "g") -50.0 else -1.0
                        viewModel.updatePantryQuantity(item, delta)
                    },
                    onMarkFinished = { viewModel.markItemFinished(item) },
                    onEdit = { itemToEdit = item },
                    onDelete = { viewModel.deletePantryItem(item) }
                )
            }
        }

        // Floating Action Buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SmallFloatingActionButton(
                onClick = { showVoiceDialog = true },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.testTag("fab_voice_command")
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Voice")
            }

            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_item")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    }

    // Add Item Dialog (with Rate & Medicine)
    if (showAddDialog) {
        AddItemDialog(
            language = language,
            currencySymbol = currency,
            onDismiss = { showAddDialog = false },
            onConfirm = { nameEn, nameBn, cat, qty, unit, minT, rate, price, notes, isPrivate ->
                viewModel.addPantryItem(nameEn, nameBn, cat, qty, unit, minT, rate, price, notes, isPrivate)
            }
        )
    }

    // Edit Item Dialog
    if (itemToEdit != null) {
        EditItemDialog(
            item = itemToEdit!!,
            language = language,
            currencySymbol = currency,
            onDismiss = { itemToEdit = null },
            onSave = { updated ->
                viewModel.updatePantryItem(updated)
                itemToEdit = null
            }
        )
    }

    // Voice Dialog
    if (showVoiceDialog) {
        VoiceCommandDialog(
            language = language,
            onDismiss = { showVoiceDialog = false },
            onCommandSpoken = { spoken -> viewModel.executeVoiceCommand(spoken) }
        )
    }
}

@Composable
fun PantryItemCard(
    item: PantryItemEntity,
    language: AppLanguage,
    currencySymbol: String,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onMarkFinished: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isBn = language == AppLanguage.BENGALI

    val statusBadgeColor = when {
        item.isFinished -> FinishedColor
        item.isLowStock -> LowStockColor
        else -> InStockColor
    }

    val statusBadgeText = when {
        item.isFinished -> if (isBn) "ফুরিয়ে গেছে" else "FINISHED"
        item.isLowStock -> if (isBn) "স্টক কম!" else "LOW STOCK"
        else -> if (isBn) "মজুদ আছে" else "IN STOCK"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .testTag("pantry_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Name & Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isBn) item.nameBn else item.nameEn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (item.isPrivate) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "🔒",
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = if (isBn) item.nameEn else item.nameBn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                // Status Badge
                Surface(
                    color = statusBadgeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = statusBadgeText,
                        color = statusBadgeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quantity & Rate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${item.quantity}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (item.isFinished) FinishedColor else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.unit,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    if (item.rate > 0) {
                        Text(
                            text = if (isBn) "দর: $currencySymbol${item.rate}/${item.unit}" else "Rate: $currencySymbol${item.rate}/${item.unit}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (item.price > 0) {
                        Text(
                            text = if (isBn) "মোট: $currencySymbol${item.price.toInt()}" else "Total: $currencySymbol${item.price.toInt()}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row: - / + / Edit / Finish / Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Counter
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                        .padding(horizontal = 2.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = onDecrease,
                        enabled = item.quantity > 0,
                        modifier = Modifier.size(32.dp).testTag("decrease_btn_${item.id}")
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                    }

                    Text(
                        text = if (item.unit == "kg") "0.5kg" else "1",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    IconButton(
                        onClick = onIncrease,
                        modifier = Modifier.size(32.dp).testTag("increase_btn_${item.id}")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                    }
                }

                // Edit Item button
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(34.dp).testTag("edit_btn_${item.id}")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }

                // Finish button
                if (!item.isFinished) {
                    OutlinedButton(
                        onClick = onMarkFinished,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("finish_btn_${item.id}")
                    ) {
                        Text(if (isBn) "শেষ 🛒" else "Done 🛒", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp).testTag("delete_btn_${item.id}")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
