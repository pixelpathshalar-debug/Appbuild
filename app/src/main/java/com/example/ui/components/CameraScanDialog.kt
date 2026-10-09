package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.util.AppLanguage
import com.example.util.GeminiVisionScanner
import com.example.util.ScannedItemResult
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CameraScanDialog(
    language: AppLanguage,
    currencySymbol: String = "₹",
    onDismiss: () -> Unit,
    onItemsExtractedAndConfirmed: (List<ScannedItemResult>, File?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isBn = language == AppLanguage.BENGALI

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var loadedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var savedFile by remember { mutableStateOf<File?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var scanFeedback by remember { mutableStateOf<String?>(null) }
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    val extractedItems = remember { mutableStateListOf<ScannedItemResult>() }
    var itemBeingEditedIndex by remember { mutableStateOf<Int?>(null) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            loadedBitmap = bitmap
            val tempFile = GeminiVisionScanner.saveBitmapToStorage(context, bitmap, "camera_capture")
            savedFile = tempFile
            selectedImageUri = tempFile?.let { Uri.fromFile(it) }
            scanFeedback = if (isBn) "ছবি তোলা হয়েছে! এখন 'এআই বিশ্লেষণ' বাটনে চাপুন।" else "Photo captured! Tap 'Analyze with AI'."
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            cameraLauncher.launch(null)
        }
    }

    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            val bmp = GeminiVisionScanner.loadAndResizeBitmap(context, uri)
            loadedBitmap = bmp
            if (bmp != null) {
                savedFile = GeminiVisionScanner.saveBitmapToStorage(context, bmp, "gallery_pick")
            }
            scanFeedback = if (isBn) "ছবি নির্বাচন হয়েছে! এখন 'এআই বিশ্লেষণ' বাটনে চাপুন।" else "Image selected! Tap 'Analyze with AI'."
        }
    }

    fun analyzeImage() {
        val uri = selectedImageUri ?: return
        isAnalyzing = true
        scanFeedback = if (isBn) "স্মার্ট গিন্নি এআই চিরকুট/প্রেসক্রিপশন পড়ছে..." else "Smart Ginni AI is scanning document..."
        scope.launch {
            val result = GeminiVisionScanner.analyzeHandwrittenOrPrescriptionImage(context, uri)
            isAnalyzing = false
            result.onSuccess { items ->
                extractedItems.clear()
                extractedItems.addAll(items)
                scanFeedback = if (isBn)
                    "✅ ${items.size}টি পণ্য/ঔষধ শনাক্ত হয়েছে! প্রয়োজন মতো এডিট করে যোগ করুন।"
                else
                    "✅ Extracted ${items.size} items! Edit or tap confirm to add."
            }.onFailure { err ->
                scanFeedback = if (isBn) "⚠️ স্ক্যান ব্যর্থ: ${err.message}" else "⚠️ Scan failed: ${err.message}"
            }
        }
    }

    fun shareImage() {
        val file = savedFile ?: run {
            val bmp = loadedBitmap
            if (bmp != null) GeminiVisionScanner.saveBitmapToStorage(context, bmp, "share_slip") else null
        }
        if (file != null) {
            val caption = if (isBn)
                "স্মার্ট গিন্নি (Smart Ginni) - বাজার ফর্দ ও প্রেসক্রিপশন চিরকুট\nতৈরি করেছেন: সুপর্ণা কান্তি দত্ত (৯৭৭৫৭০৮৭২২)"
            else
                "Smart Ginni - Shopping List & Prescription Slip\nHandcrafted by Suparna Kanti Datta (9775708722)"
            GeminiVisionScanner.shareImage(context, file, caption)
        }
    }

    fun saveImagePermanently() {
        val bmp = loadedBitmap
        if (bmp != null) {
            val file = GeminiVisionScanner.saveBitmapToStorage(context, bmp, "saved_prescription")
            savedFile = file
            scanFeedback = if (isBn) "💾 ছবি মেমোরিতে সফলভাবে সেভ হয়েছে!" else "💾 Image saved to device storage!"
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isBn) "প্রেসক্রিপশন ও চিরকুট স্ক্যানার" else "Prescription & Slip Scanner",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isBn) "ক্যামেরা ও এআই ওসিআর দিয়ে স্বয়ংক্রিয় ফর্দ" else "AI OCR & Auto Shopping List Generator",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_camera_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // Camera / Gallery Trigger Section
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (selectedImageUri == null && loadedBitmap == null) {
                                    Icon(
                                        Icons.Default.Image,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (isBn) "হাতে লেখা ফর্দ বা ডাক্তারের প্রেসক্রিপশনের ছবি তুলুন"
                                        else "Take photo of handwritten grocery list or prescription",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                } else {
                                    // Image Preview
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    ) {
                                        AsyncImage(
                                            model = selectedImageUri ?: loadedBitmap,
                                            contentDescription = "Scanned Slip",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                // Photo action buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (hasCameraPermission) {
                                                cameraLauncher.launch(null)
                                            } else {
                                                permissionLauncher.launch(Manifest.permission.CAMERA)
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("btn_capture_photo"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (isBn) "ক্যামেরা" else "Camera", fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = { galleryLauncher.launch("image/*") },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("btn_pick_gallery"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (isBn) "গ্যালারি" else "Gallery", fontSize = 12.sp)
                                    }
                                }

                                // Save & Share options for scanned image
                                if (selectedImageUri != null || loadedBitmap != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { saveImagePermanently() },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("btn_save_scanned_image"),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.Save, contentDescription = null, Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isBn) "ছবি সেভ" else "Save Image", fontSize = 11.sp)
                                        }

                                        OutlinedButton(
                                            onClick = { shareImage() },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("btn_share_scanned_image"),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = null, Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isBn) "ছবি শেয়ার" else "Share Image", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Scan / Analyze Action Button
                    if (selectedImageUri != null || loadedBitmap != null) {
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { analyzeImage() },
                                enabled = !isAnalyzing,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_analyze_ai_ocr"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isAnalyzing) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onSecondary,
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (isBn) "এআই চিরকুট বিশ্লেষণ করছে..." else "Scanning with AI & OCR...", fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isBn) "এআই ও ওসিআর দিয়ে ফর্দ তৈরি করুন" else "Extract Items with AI & OCR",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Notice / Feedback
                    if (scanFeedback != null) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = scanFeedback ?: "",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { scanFeedback = null }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Close, contentDescription = null, Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Extracted Items Section Header & Add Item Manually
                    if (extractedItems.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isBn) "শনাক্তকৃত পণ্য (${extractedItems.size}টি):" else "Extracted Items (${extractedItems.size}):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                TextButton(onClick = {
                                    extractedItems.add(
                                        ScannedItemResult(
                                            nameEn = "New Item",
                                            nameBn = "নতুন পণ্য",
                                            category = "OTHERS",
                                            quantity = 1.0,
                                            unit = "pcs"
                                        )
                                    )
                                }) {
                                    Icon(Icons.Default.Add, contentDescription = null, Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isBn) "পণ্য যোগ করুন" else "Add Item", fontSize = 12.sp)
                                }
                            }
                        }

                        // List of Extracted Items (With Inline Edit and Delete)
                        itemsIndexed(extractedItems) { index, item ->
                            Spacer(modifier = Modifier.height(6.dp))
                            ElevatedCard(
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = if (isBn) item.nameBn else item.nameEn,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = when (item.category) {
                                                        "MEDICINE" -> MaterialTheme.colorScheme.tertiaryContainer
                                                        "GARMENTS" -> MaterialTheme.colorScheme.secondaryContainer
                                                        "HARDWARE" -> MaterialTheme.colorScheme.errorContainer
                                                        else -> MaterialTheme.colorScheme.primaryContainer
                                                    }
                                                ) {
                                                    Text(
                                                        text = when (item.category) {
                                                            "MEDICINE" -> if (isBn) "ঔষধ" else "Medicine"
                                                            "GARMENTS" -> if (isBn) "পোশাক" else "Garments"
                                                            "HARDWARE" -> if (isBn) "হার্ডওয়্যার" else "Hardware"
                                                            "HOUSEHOLD" -> if (isBn) "গৃহস্থালি" else "Household"
                                                            "SPICES" -> if (isBn) "মসলা" else "Spice"
                                                            "VEGETABLES" -> if (isBn) "কাঁচাবাজার" else "Veg"
                                                            "STAPLES" -> if (isBn) "রসদ" else "Staples"
                                                            else -> if (isBn) "অন্যান্য" else "Others"
                                                        },
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "${item.quantity} ${item.unit}" + (if (item.estimatedPrice > 0) " • ≈ $currencySymbol${item.estimatedPrice.toInt()}" else "") + (if (item.notes.isNotBlank()) " • ${item.notes}" else ""),
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }

                                        // Edit Button
                                        IconButton(onClick = { itemBeingEditedIndex = index }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                        }

                                        // Delete Button
                                        IconButton(onClick = { extractedItems.removeAt(index) }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Confirmation Action Row
                if (extractedItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            onItemsExtractedAndConfirmed(extractedItems.toList(), savedFile)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_confirm_add_scanned_to_buy"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "সবগুলো (${extractedItems.size}টি) বাজার ফর্দে যোগ করুন 🛒"
                            else "Add All (${extractedItems.size}) to Shopping List 🛒",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    // Edit Item Dialog
    if (itemBeingEditedIndex != null && itemBeingEditedIndex!! in extractedItems.indices) {
        val editingIndex = itemBeingEditedIndex!!
        val currentItem = extractedItems[editingIndex]

        var editNameBn by remember { mutableStateOf(currentItem.nameBn) }
        var editNameEn by remember { mutableStateOf(currentItem.nameEn) }
        var editCategory by remember { mutableStateOf(currentItem.category) }
        var editQty by remember { mutableStateOf(currentItem.quantity.toString()) }
        var editUnit by remember { mutableStateOf(currentItem.unit) }
        var editPrice by remember { mutableStateOf(if (currentItem.estimatedPrice > 0) currentItem.estimatedPrice.toString() else "") }
        var editNotes by remember { mutableStateOf(currentItem.notes) }

        val categories = listOf(
            "VEGETABLES" to (if (isBn) "🥬 কাঁচাবাজার" else "🥬 Veg"),
            "SPICES" to (if (isBn) "🧂 মসলা" else "🧂 Spice"),
            "STAPLES" to (if (isBn) "🍚 রসদ" else "🍚 Staples"),
            "MEDICINE" to (if (isBn) "💊 ঔষধ" else "💊 Medicine"),
            "GARMENTS" to (if (isBn) "👕 পোশাক" else "👕 Garments"),
            "HARDWARE" to (if (isBn) "🔧 হার্ডওয়্যার" else "🔧 Hardware"),
            "HOUSEHOLD" to (if (isBn) "🏠 গৃহস্থালি" else "🏠 Household"),
            "OTHERS" to (if (isBn) "📦 অন্যান্য" else "📦 Others")
        )

        AlertDialog(
            onDismissRequest = { itemBeingEditedIndex = null },
            title = {
                Text(
                    text = if (isBn) "পণ্য সম্পাদনা (Edit Item)" else "Edit Item",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = editNameBn,
                        onValueChange = { editNameBn = it },
                        label = { Text(if (isBn) "বাংলা নাম" else "Bengali Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = editNameEn,
                        onValueChange = { editNameEn = it },
                        label = { Text(if (isBn) "ইংরেজি নাম" else "English Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Category selection
                    Text(text = if (isBn) "ক্যাটাগরি নির্বাচন:" else "Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categories.forEach { (catKey, catLabel) ->
                            FilterChip(
                                selected = editCategory == catKey,
                                onClick = { editCategory = catKey },
                                label = { Text(catLabel, fontSize = 10.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editQty,
                            onValueChange = { editQty = it },
                            label = { Text(if (isBn) "পরিমাণ" else "Qty") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedTextField(
                            value = editUnit,
                            onValueChange = { editUnit = it },
                            label = { Text(if (isBn) "একক" else "Unit") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = editPrice,
                        onValueChange = { editPrice = it },
                        label = { Text(if (isBn) "আনুমানিক মূল্য ($currencySymbol)" else "Est Price ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    extractedItems[editingIndex] = currentItem.copy(
                        nameBn = editNameBn.trim(),
                        nameEn = editNameEn.trim(),
                        category = editCategory,
                        quantity = editQty.toDoubleOrNull() ?: currentItem.quantity,
                        unit = editUnit.trim().ifBlank { currentItem.unit },
                        estimatedPrice = editPrice.toDoubleOrNull() ?: 0.0,
                        notes = editNotes.trim()
                    )
                    itemBeingEditedIndex = null
                }) {
                    Text(if (isBn) "সেভ" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemBeingEditedIndex = null }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        )
    }
}
