package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.local.ScannedImageEntity
import com.example.data.local.ToBuyItemEntity
import com.example.ui.MainViewModel
import com.example.ui.components.CameraScanDialog
import com.example.ui.components.VoiceCommandDialog
import com.example.util.AppLanguage
import com.example.util.NotificationHelper
import java.io.File
import java.util.Locale

@Composable
fun ToBuyListScreen(
    viewModel: MainViewModel,
    language: AppLanguage,
    onNavigateToMarketGuide: () -> Unit
) {
    val context = LocalContext.current
    val isBn = language == AppLanguage.BENGALI
    val toBuyItems by viewModel.toBuyItems.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val scannedImages by viewModel.scannedImages.collectAsState()

    var isMarketMode by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showCameraScanDialog by remember { mutableStateOf(false) }
    var showVoiceCreateDialog by remember { mutableStateOf(false) }
    var itemForReminder by remember { mutableStateOf<ToBuyItemEntity?>(null) }
    var previewImageEntity by remember { mutableStateOf<ScannedImageEntity?>(null) }

    // Search & Filter State
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    // Speech Recognizer Launcher for Voice Search
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

    // Filter items based on category and search query
    val filteredToBuyItems = toBuyItems.filter { item ->
        val matchesCategory = when (selectedCategoryFilter) {
            "ALL" -> true
            "GARMENTS" -> item.category == "GARMENTS"
            "MEDICINE" -> item.category == "MEDICINE"
            "VEGETABLES" -> item.category == "VEGETABLES"
            "SPICES" -> item.category == "SPICES"
            "HOUSEHOLD" -> item.category == "HOUSEHOLD"
            "HARDWARE" -> item.category == "HARDWARE"
            "STAPLES" -> item.category == "STAPLES"
            "OTHERS" -> item.category == "OTHERS" || item.category.isBlank()
            else -> true
        }

        val matchesSearch = searchQuery.isBlank() ||
                item.nameEn.contains(searchQuery, ignoreCase = true) ||
                item.nameBn.contains(searchQuery, ignoreCase = true) ||
                item.notes.contains(searchQuery, ignoreCase = true)

        matchesCategory && matchesSearch
    }

    val totalCount = toBuyItems.size
    val boughtCount = toBuyItems.count { it.isBought }
    val progress = if (totalCount > 0) boughtCount.toFloat() / totalCount else 0f
    val totalEstPrice = toBuyItems.sumOf {
        if (it.estimatedPrice > 0) it.estimatedPrice else (it.rate * it.quantity)
    }

    fun shareScannedImage(image: ScannedImageEntity) {
        try {
            val file = File(image.imagePath)
            if (file.exists()) {
                val authority = "${context.packageName}.fileprovider"
                val uri = FileProvider.getUriForFile(context, authority, file)
                val caption = if (isBn)
                    "স্মার্ট গিন্নি • স্ক্যান করা বাজার ফর্দ ও চিরকুট\nতৈরি করেছেন: সুপর্ণা কান্তি দত্ত (৯৭৭৫৭০৮৭২২)"
                else
                    "Smart Ginni • Scanned Shopping List & Prescription\nHandcrafted by Suparna Kanti Datta (9775708722)"
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, caption)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share Image"))
            }
        } catch (_: Exception) {}
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("to_buy_list"),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
        ) {
            // Header Card & Summary
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (isMarketMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isBn) "দরকারী বাজার ফর্দ" else "Market Shopping List",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isBn) "স্বয়ংক্রিয় ঘাটতি ফর্দ ও স্বামী-স্ত্রী সিঙ্ক"
                                    else "Auto-generated & Household partner sync",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            // Market Mode Switch
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isBn) "বাজার মুড" else "Market Mode",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Switch(
                                    checked = isMarketMode,
                                    onCheckedChange = { isMarketMode = it },
                                    modifier = Modifier.testTag("market_mode_toggle")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Stats & Progress
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBn) "কেনা হয়েছে: $boughtCount / $totalCount টি" else "Bought: $boughtCount of $totalCount",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            if (totalEstPrice > 0) {
                                Text(
                                    text = if (isBn) "মোট প্রাক্কলন: $currency${totalEstPrice.toInt()}" else "Est: $currency${totalEstPrice.toInt()}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            // Middle Branding Badge Card
            item {
                Spacer(modifier = Modifier.height(10.dp))
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("middle_branding_tobuy")
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
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Search Bar with Voice Search Button
            item {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(if (isBn) "ফর্দে পণ্য বা ঔষধ খুঁজুন..." else "Search items or medicine in list...")
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
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
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (isBn) "bn-BD" else Locale.getDefault().toString())
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, if (isBn) "পণ্য খুঁজতে বলুন..." else "Speak to search items...")
                                    }
                                    try {
                                        voiceSearchLauncher.launch(intent)
                                    } catch (_: Exception) {}
                                },
                                modifier = Modifier.testTag("voice_search_tobuy_btn")
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
                    modifier = Modifier.fillMaxWidth().testTag("tobuy_search_field")
                )
            }

            // Category Filter Chips (Garments, Medicine, Vegetables, Spices, Household, Hardware, Staples, Others)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedCategoryFilter == "ALL",
                        onClick = { selectedCategoryFilter = "ALL" },
                        label = { Text(if (isBn) "সব ফর্দ (${toBuyItems.size})" else "All (${toBuyItems.size})", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_all_tobuy")
                    )
                    FilterChip(
                        selected = selectedCategoryFilter == "GARMENTS",
                        onClick = { selectedCategoryFilter = "GARMENTS" },
                        label = { Text(if (isBn) "👕 পোশাক" else "👕 Garments", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_garments_tobuy")
                    )
                    FilterChip(
                        selected = selectedCategoryFilter == "MEDICINE",
                        onClick = { selectedCategoryFilter = "MEDICINE" },
                        label = { Text(if (isBn) "💊 ঔষধ" else "💊 Medicine", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_medicine_tobuy")
                    )
                    FilterChip(
                        selected = selectedCategoryFilter == "VEGETABLES",
                        onClick = { selectedCategoryFilter = "VEGETABLES" },
                        label = { Text(if (isBn) "🥬 শাকসবজি" else "🥬 Veg", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_veg_tobuy")
                    )
                    FilterChip(
                        selected = selectedCategoryFilter == "SPICES",
                        onClick = { selectedCategoryFilter = "SPICES" },
                        label = { Text(if (isBn) "🧂 মসলা" else "🧂 Spice", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_spice_tobuy")
                    )
                    FilterChip(
                        selected = selectedCategoryFilter == "HOUSEHOLD",
                        onClick = { selectedCategoryFilter = "HOUSEHOLD" },
                        label = { Text(if (isBn) "🏠 গৃহস্থালি" else "🏠 Household", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_household_tobuy")
                    )
                    FilterChip(
                        selected = selectedCategoryFilter == "HARDWARE",
                        onClick = { selectedCategoryFilter = "HARDWARE" },
                        label = { Text(if (isBn) "🔧 হার্ডওয়্যার" else "🔧 Hardware", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_hardware_tobuy")
                    )
                    FilterChip(
                        selected = selectedCategoryFilter == "STAPLES",
                        onClick = { selectedCategoryFilter = "STAPLES" },
                        label = { Text(if (isBn) "🍚 রসদ" else "🍚 Staples", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_staples_tobuy")
                    )
                    FilterChip(
                        selected = selectedCategoryFilter == "OTHERS",
                        onClick = { selectedCategoryFilter = "OTHERS" },
                        label = { Text(if (isBn) "📦 অন্যান্য" else "📦 Others", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_others_tobuy")
                    )
                }
            }

            // Action Row: Camera OCR Scan & Voice Command Create
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Camera scan option (Requirement 1 & 2)
                    Button(
                        onClick = { showCameraScanDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("btn_camera_scan_slip")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "প্রেসক্রিপশন/চিরকুট স্ক্যান 📷" else "Scan Slip/Rx 📷",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Voice Command for create items (Requirement 3)
                    Button(
                        onClick = { showVoiceCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("btn_voice_create_item")
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "ভয়েস কমান্ডে ফর্দ 🎙️" else "Voice Create 🎙️",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Secondary Action Row: Auto Low Stock & PDF Share
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.autoGenerateToBuyFromLowStock() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("auto_generate_list_button")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBn) "ঘাটতি থেকে ফর্দ" else "From Low Stock",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.exportAndShareShoppingListPdf(context) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_pdf_button")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBn) "পিডিএফ শেয়ার" else "Share PDF",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Scanned Images Gallery & Share Card (Requirement 2: Save and Share Image Option in To Buy List)
            if (scannedImages.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().testTag("scanned_images_gallery_card")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isBn) "সংরক্ষিত প্রেসক্রিপশন ও চিরকুট (${scannedImages.size})"
                                        else "Saved Prescriptions & Slips (${scannedImages.size})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(scannedImages, key = { it.id }) { img ->
                                    val file = File(img.imagePath)
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        modifier = Modifier.width(160.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(90.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                                    .clickable { previewImageEntity = img }
                                            ) {
                                                if (file.exists()) {
                                                    AsyncImage(
                                                        model = file,
                                                        contentDescription = img.title,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                } else {
                                                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.align(Alignment.Center))
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = img.title,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                IconButton(
                                                    onClick = { shareScannedImage(img) },
                                                    modifier = Modifier.size(28.dp).testTag("share_image_${img.id}")
                                                ) {
                                                    Icon(Icons.Default.Share, contentDescription = "Share", Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                                }

                                                IconButton(
                                                    onClick = { viewModel.deleteScannedImage(img.id) },
                                                    modifier = Modifier.size(28.dp).testTag("delete_image_${img.id}")
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Transfer bought items button (if any bought)
            if (boughtCount > 0) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.transferBoughtItemsToPantry() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transfer_bought_to_pantry_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Inventory, contentDescription = null, Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "কেনা সম্পন্ন! স্টকে যোগ করুন ($boughtCount টি)"
                            else "Done Buying! Transfer to Pantry ($boughtCount)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Empty state
            if (filteredToBuyItems.isEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(30.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(50.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isBn) "কোনো পণ্য পাওয়া যায়নি" else "No items found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isBn) "প্রেসক্রিপশন বা চিরকুট স্ক্যান করুন অথবা ভয়েস বাটনে বলুন!"
                            else "Scan a handwritten slip or prescription, or use voice commands!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Items List (With in-line quantity adjustment)
            items(filteredToBuyItems, key = { it.id }) { item ->
                Spacer(modifier = Modifier.height(8.dp))
                ToBuyItemRow(
                    item = item,
                    language = language,
                    currencySymbol = currency,
                    isMarketMode = isMarketMode,
                    onToggleBought = { viewModel.toggleToBuyBought(item) },
                    onIncreaseQuantity = {
                        val delta = if (item.unit == "kg") 0.5 else if (item.unit == "strip") 1.0 else 1.0
                        viewModel.updateToBuyQuantity(item, item.quantity + delta)
                    },
                    onDecreaseQuantity = {
                        val delta = if (item.unit == "kg") 0.5 else if (item.unit == "strip") 1.0 else 1.0
                        viewModel.updateToBuyQuantity(item, (item.quantity - delta).coerceAtLeast(0.1))
                    },
                    onSetReminder = { itemForReminder = item },
                    onDelete = { viewModel.deleteToBuyItem(item) }
                )
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_add_to_buy"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add to buy")
        }
    }

    // Camera Scan Dialog (AI OCR extraction + Save & Share Image)
    if (showCameraScanDialog) {
        CameraScanDialog(
            language = language,
            onDismiss = { showCameraScanDialog = false },
            onItemsExtractedAndConfirmed = { items, savedFile ->
                if (savedFile != null) {
                    viewModel.saveScannedImage(
                        title = "স্ক্যান_${items.firstOrNull()?.nameBn ?: "প্রেসক্রিপশন"}",
                        imagePath = savedFile.absolutePath,
                        summary = "${items.size} items",
                        itemsCount = items.size
                    )
                }
                viewModel.addBatchScannedItems(items, savedFile?.absolutePath)
                showCameraScanDialog = false
            }
        )
    }

    // Voice Command Create Items Dialog
    if (showVoiceCreateDialog) {
        VoiceCommandDialog(
            language = language,
            onDismiss = { showVoiceCreateDialog = false },
            onCommandSpoken = { spoken ->
                viewModel.processVoiceCommand(spoken)
                showVoiceCreateDialog = false
            }
        )
    }

    // Full screen image preview dialog
    if (previewImageEntity != null) {
        val target = previewImageEntity!!
        val file = File(target.imagePath)
        AlertDialog(
            onDismissRequest = { previewImageEntity = null },
            title = {
                Text(text = target.title, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        if (file.exists()) {
                            AsyncImage(
                                model = file,
                                contentDescription = target.title,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    shareScannedImage(target)
                }) {
                    Icon(Icons.Default.Share, contentDescription = null, Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBn) "শেয়ার করুন" else "Share")
                }
            },
            dismissButton = {
                TextButton(onClick = { previewImageEntity = null }) {
                    Text(if (isBn) "বন্ধ করুন" else "Close")
                }
            }
        )
    }

    // Add To-Buy Item Dialog
    if (showAddDialog) {
        AddToBuyCustomDialog(
            language = language,
            currencySymbol = currency,
            onDismiss = { showAddDialog = false },
            onConfirm = { nameEn, nameBn, cat, qty, unit, rate, price, notes, isPrivate ->
                viewModel.addToBuyItem(nameEn, nameBn, cat, qty, unit, rate, price, notes, isPrivate)
            }
        )
    }

    // Set Reminder Dialog
    if (itemForReminder != null) {
        AlertDialog(
            onDismissRequest = { itemForReminder = null },
            title = {
                Text(
                    text = if (isBn) "⏰ কেনাকাটার রিমাইন্ডার" else "⏰ Shopping Reminder",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isBn)
                        "${itemForReminder?.nameBn} কেনার জন্য ডিভাইসে তাৎক্ষণিক রিমাইন্ডার অ্যালার্ট সেট করা হবে।"
                    else
                        "Set a reminder push alert for buying ${itemForReminder?.nameEn}."
                )
            },
            confirmButton = {
                Button(onClick = {
                    val notifTitle = if (isBn) "⏰ বাজার রিমাইন্ডার!" else "⏰ Shopping Reminder!"
                    val notifMsg = if (isBn)
                        "বাজার থেকে ${itemForReminder?.nameBn} ${itemForReminder?.quantity} ${itemForReminder?.unit} কিনতে ভুলবেন না!"
                    else
                        "Don't forget to buy ${itemForReminder?.nameEn} (${itemForReminder?.quantity} ${itemForReminder?.unit})!"
                    NotificationHelper.showLowStockAlert(context, notifTitle, notifMsg, 4444)
                    itemForReminder = null
                }) {
                    Text(if (isBn) "রিমাইন্ডার চালু করুন" else "Set Reminder")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemForReminder = null }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun ToBuyItemRow(
    item: ToBuyItemEntity,
    language: AppLanguage,
    currencySymbol: String,
    isMarketMode: Boolean,
    onToggleBought: () -> Unit,
    onIncreaseQuantity: () -> Unit,
    onDecreaseQuantity: () -> Unit,
    onSetReminder: () -> Unit,
    onDelete: () -> Unit
) {
    val isBn = language == AppLanguage.BENGALI

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tobuy_row_${item.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isBought)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isBought) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(if (isMarketMode) 14.dp else 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox
                Checkbox(
                    checked = item.isBought,
                    onCheckedChange = { onToggleBought() },
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.size(if (isMarketMode) 30.dp else 24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isBn) item.nameBn else item.nameEn,
                            fontSize = if (isMarketMode) 17.sp else 15.sp,
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (item.isBought) TextDecoration.LineThrough else TextDecoration.None,
                            color = if (item.isBought) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                        )

                        if (item.isPrivate) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("🔒", fontSize = 11.sp)
                        }

                        // Category Badge
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            color = when (item.category) {
                                "MEDICINE" -> MaterialTheme.colorScheme.tertiaryContainer
                                "GARMENTS" -> MaterialTheme.colorScheme.secondaryContainer
                                "HARDWARE" -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = when (item.category) {
                                    "MEDICINE" -> if (isBn) "ঔষধ" else "Med"
                                    "GARMENTS" -> if (isBn) "পোশাক" else "Garments"
                                    "HARDWARE" -> if (isBn) "হার্ডওয়্যার" else "Hardware"
                                    "HOUSEHOLD" -> if (isBn) "গৃহস্থালি" else "Household"
                                    "SPICES" -> if (isBn) "মসলা" else "Spice"
                                    "VEGETABLES" -> if (isBn) "কাঁচাবাজার" else "Veg"
                                    "STAPLES" -> if (isBn) "রসদ" else "Staples"
                                    else -> if (isBn) "অন্যান্য" else "Other"
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    // Price & rate info
                    val est = if (item.estimatedPrice > 0) item.estimatedPrice else (item.rate * item.quantity)
                    if (est > 0) {
                        Text(
                            text = if (item.rate > 0)
                                "দর: $currencySymbol${item.rate} • মোট: ≈ $currencySymbol${est.toInt()}"
                            else
                                "≈ $currencySymbol${est.toInt()}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    if (item.notes.isNotBlank()) {
                        Text(
                            text = "📝 ${item.notes}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                }

                // In-line Quantity Adjustment Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                        .padding(horizontal = 2.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = onDecreaseQuantity,
                        modifier = Modifier.size(28.dp).testTag("inline_qty_minus_${item.id}")
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(14.dp))
                    }

                    Text(
                        text = "${item.quantity} ${item.unit}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    IconButton(
                        onClick = onIncreaseQuantity,
                        modifier = Modifier.size(28.dp).testTag("inline_qty_plus_${item.id}")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(14.dp))
                    }
                }

                // Reminder button
                IconButton(
                    onClick = onSetReminder,
                    modifier = Modifier.size(28.dp).testTag("reminder_btn_${item.id}")
                ) {
                    Icon(
                        Icons.Default.Alarm,
                        contentDescription = "Reminder",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp).testTag("delete_tobuy_${item.id}")
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddToBuyCustomDialog(
    language: AppLanguage,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (
        nameEn: String,
        nameBn: String,
        category: String,
        quantity: Double,
        unit: String,
        rate: Double,
        estimatedPrice: Double,
        notes: String,
        isPrivate: Boolean
    ) -> Unit
) {
    val isBn = language == AppLanguage.BENGALI

    var nameEn by remember { mutableStateOf("") }
    var nameBn by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("VEGETABLES") }
    var quantityStr by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("kg") }
    var rateStr by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun recalculatePrice(q: String, r: String) {
        val qVal = q.toDoubleOrNull() ?: 0.0
        val rVal = r.toDoubleOrNull() ?: 0.0
        if (qVal > 0 && rVal > 0) {
            priceStr = (qVal * rVal).toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBn) "🛒 বাজার ফর্দে নতুন পণ্য" else "🛒 Add to Shopping List",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == "VEGETABLES",
                        onClick = { selectedCategory = "VEGETABLES"; unit = "kg" },
                        label = { Text(if (isBn) "🥬 শাকসবজি" else "🥬 Veg", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedCategory == "SPICES",
                        onClick = { selectedCategory = "SPICES"; unit = "g" },
                        label = { Text(if (isBn) "🧂 মসলা" else "🧂 Spice", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedCategory == "MEDICINE",
                        onClick = {
                            selectedCategory = "MEDICINE"
                            nameEn = "Paracetamol 500mg"
                            nameBn = "প্যারাসিটামল"
                            unit = "tablet"
                            quantityStr = "10"
                            rateStr = "1.5"
                            priceStr = "15.0"
                        },
                        label = { Text(if (isBn) "💊 ঔষধ" else "💊 Medicine", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedCategory == "GARMENTS",
                        onClick = {
                            selectedCategory = "GARMENTS"
                            nameEn = "Cotton Saree"
                            nameBn = "সুতি শাড়ি"
                            unit = "pcs"
                            quantityStr = "1"
                            rateStr = "850.0"
                            priceStr = "850.0"
                        },
                        label = { Text(if (isBn) "👕 পোশাক" else "👕 Garments", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedCategory == "HARDWARE",
                        onClick = {
                            selectedCategory = "HARDWARE"
                            nameEn = "LED Light Bulb"
                            nameBn = "এলইডি লাইট বাল্ব"
                            unit = "pcs"
                            quantityStr = "2"
                            rateStr = "120.0"
                            priceStr = "240.0"
                        },
                        label = { Text(if (isBn) "🔧 হার্ডওয়্যার" else "🔧 Hardware", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedCategory == "HOUSEHOLD",
                        onClick = {
                            selectedCategory = "HOUSEHOLD"
                            nameEn = "Detergent Powder"
                            nameBn = "ডিটারজেন্ট সার্ফ"
                            unit = "kg"
                            quantityStr = "1"
                            rateStr = "140.0"
                            priceStr = "140.0"
                        },
                        label = { Text(if (isBn) "🏠 গৃহস্থালি" else "🏠 Household", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedCategory == "STAPLES",
                        onClick = { selectedCategory = "STAPLES" },
                        label = { Text(if (isBn) "🍚 রসদ" else "🍚 Staples", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedCategory == "OTHERS",
                        onClick = { selectedCategory = "OTHERS"; unit = "pcs" },
                        label = { Text(if (isBn) "📦 অন্যান্য" else "📦 Others", fontSize = 11.sp) }
                    )
                }

                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text(if (isBn) "ইংরেজি নাম" else "English Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("tobuy_name_en_input")
                )
                OutlinedTextField(
                    value = nameBn,
                    onValueChange = { nameBn = it },
                    label = { Text(if (isBn) "বাংলা নাম" else "Bengali Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("tobuy_name_bn_input")
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = {
                            quantityStr = it
                            recalculatePrice(it, rateStr)
                        },
                        label = { Text(if (isBn) "পরিমাণ" else "Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("tobuy_quantity_input")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text(if (isBn) "একক" else "Unit") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = rateStr,
                        onValueChange = {
                            rateStr = it
                            recalculatePrice(quantityStr, it)
                        },
                        label = { Text(if (isBn) "দর প্রতি একক ($currencySymbol)" else "Rate ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text(if (isBn) "মোট প্রাক্কলন ($currencySymbol)" else "Est Price ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isBn) "নোট / ব্র্যান্ড / দোকান" else "Notes / Brand / Shop") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBn) "গোপন ফর্দ (স্বামী-স্ত্রী প্রাইভেট)" else "Private between Couple",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Switch(checked = isPrivate, onCheckedChange = { isPrivate = it })
                }

                if (errorMessage != null) {
                    Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameEn.isBlank() && nameBn.isBlank()) {
                        errorMessage = if (isBn) "পণ্যের নাম লিখুন" else "Enter item name"
                        return@Button
                    }
                    val qty = quantityStr.toDoubleOrNull() ?: 1.0
                    val rate = rateStr.toDoubleOrNull() ?: 0.0
                    val est = priceStr.toDoubleOrNull() ?: (rate * qty)

                    onConfirm(
                        nameEn.ifBlank { nameBn },
                        nameBn.ifBlank { nameEn },
                        selectedCategory,
                        qty,
                        unit.ifBlank { "kg" },
                        rate,
                        est,
                        notes,
                        isPrivate
                    )
                    onDismiss()
                },
                modifier = Modifier.testTag("confirm_add_tobuy_button")
            ) {
                Text(if (isBn) "যোগ করুন" else "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBn) "বাতিল" else "Cancel")
            }
        }
    )
}
