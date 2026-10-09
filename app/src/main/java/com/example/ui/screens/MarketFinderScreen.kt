package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MarketGuideEntity
import com.example.ui.MainViewModel
import com.example.util.AppLanguage

private fun Number.toBnDigits(): String {
    val enStr = this.toString()
    val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    return enStr.map { ch ->
        if (ch in '0'..'9') bnDigits[ch - '0'] else ch
    }.joinToString("")
}

private fun toBnUnit(unit: String): String {
    return when (unit.lowercase().trim()) {
        "kg" -> "কেজি"
        "g" -> "গ্রাম"
        "strip" -> "পাতা"
        "pcs" -> "টি"
        "pack" -> "প্যাক"
        "tablet" -> "ট্যাবলেট"
        "bottle" -> "বোতল"
        "bundle" -> "আঁটি"
        "l" -> "লিটার"
        else -> unit
    }
}

private fun formatPriceRange(item: MarketGuideEntity, currencySymbol: String, isBn: Boolean): String {
    val min = item.minPrice.toInt()
    val max = item.maxPrice.toInt()
    return if (isBn) {
        val minBn = min.toBnDigits()
        val maxBn = max.toBnDigits()
        val unitBn = toBnUnit(item.priceUnit)
        "$currencySymbol$minBn - $currencySymbol$maxBn / $unitBn"
    } else {
        "$currencySymbol$min - $currencySymbol$max / ${item.priceUnit}"
    }
}

@Composable
fun MarketFinderScreen(
    viewModel: MainViewModel,
    language: AppLanguage,
    onNavigateToToBuy: () -> Unit
) {
    val isBn = language == AppLanguage.BENGALI
    val guideItems by viewModel.marketGuideItems.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val currencySymbol = currentUser?.currencySymbol ?: "₹"

    var selectedSection by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var addedNotice by remember { mutableStateOf<String?>(null) }

    // Add / Edit Dialog state
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<MarketGuideEntity?>(null) }
    var itemToDelete by remember { mutableStateOf<MarketGuideEntity?>(null) }

    val filteredItems = guideItems.filter { item ->
        val matchesSection = when (selectedSection) {
            "ALL" -> true
            "VEG" -> item.sectionEn == "Vegetable Bazaar"
            "SPICE" -> item.sectionEn == "Spice Market"
            "MED" -> item.sectionEn == "Pharmacy"
            "GARMENTS" -> item.sectionEn == "Garments"
            "HARDWARE" -> item.sectionEn == "Hardware"
            "HOUSEHOLD" -> item.sectionEn == "Household"
            else -> true
        }
        val matchesSearch = searchQuery.isBlank() ||
                item.nameEn.contains(searchQuery, ignoreCase = true) ||
                item.nameBn.contains(searchQuery, ignoreCase = true) ||
                item.locationTipEn.contains(searchQuery, ignoreCase = true) ||
                item.locationTipBn.contains(searchQuery, ignoreCase = true)
        matchesSection && matchesSearch
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("market_finder_list"),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
        ) {
            // Header Card
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                                ) {
                                    Icon(
                                        Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isBn) "বাজার ডিরেক্টরি ও পণ্য খোঁজা" else "Bazaar Directory & Item Finder",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isBn) "মুদ্রা: $currencySymbol • দরদাম নির্দেশিকা ও দোকান তথ্য"
                                        else "Currency: $currencySymbol • Selection guide & bazaar rates",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            // Add Directory Item Button
                            Button(
                                onClick = { showAddDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_add_bazaar_directory_item")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isBn) "নতুন পণ্য" else "Add Item", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Notice Banner
            if (addedNotice != null) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
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
                                text = addedNotice ?: "",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { addedNotice = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Search Bar
            item {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(if (isBn) "পণ্য বা বাজারের সেকশন খুঁজুন..." else "Search items or bazaar locations...")
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("market_search_input")
                )
            }

            // Section Filter Chips
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedSection == "ALL",
                        onClick = { selectedSection = "ALL" },
                        label = { Text(if (isBn) "সব বাজার (${guideItems.size})" else "All (${guideItems.size})") },
                        modifier = Modifier.testTag("market_filter_all")
                    )
                    FilterChip(
                        selected = selectedSection == "VEG",
                        onClick = { selectedSection = "VEG" },
                        label = { Text(if (isBn) "🥬 কাঁচাবাজার" else "🥬 Veggies") },
                        modifier = Modifier.testTag("market_filter_veg")
                    )
                    FilterChip(
                        selected = selectedSection == "SPICE",
                        onClick = { selectedSection = "SPICE" },
                        label = { Text(if (isBn) "🌶️ মসলার দোকান" else "🌶️ Spices") },
                        modifier = Modifier.testTag("market_filter_spice")
                    )
                    FilterChip(
                        selected = selectedSection == "MED",
                        onClick = { selectedSection = "MED" },
                        label = { Text(if (isBn) "💊 ফার্মেসি" else "💊 Pharmacy") },
                        modifier = Modifier.testTag("market_filter_pharmacy")
                    )
                    FilterChip(
                        selected = selectedSection == "GARMENTS",
                        onClick = { selectedSection = "GARMENTS" },
                        label = { Text(if (isBn) "👕 কাপড়ের দোকান" else "👕 Garments") },
                        modifier = Modifier.testTag("market_filter_garments")
                    )
                    FilterChip(
                        selected = selectedSection == "HARDWARE",
                        onClick = { selectedSection = "HARDWARE" },
                        label = { Text(if (isBn) "🔧 হার্ডওয়্যার" else "🔧 Hardware") },
                        modifier = Modifier.testTag("market_filter_hardware")
                    )
                    FilterChip(
                        selected = selectedSection == "HOUSEHOLD",
                        onClick = { selectedSection = "HOUSEHOLD" },
                        label = { Text(if (isBn) "🏠 মুদি দোকান" else "🏠 Household") },
                        modifier = Modifier.testTag("market_filter_household")
                    )
                }
            }

            // Empty state
            if (filteredItems.isEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(30.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isBn) "কোনো বাজার পণ্য পাওয়া যায়নি" else "No directory items found",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isBn) "নতুন বাজার পণ্য যোগ করতে '+' বাটন চাপুন" else "Tap '+' button to add items to bazaar directory",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Items List (With Edit & Delete options)
            items(filteredItems, key = { it.id }) { item ->
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("guide_card_${item.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isBn) item.nameBn else item.nameEn,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isBn) item.nameEn else item.nameBn,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Bazaar Section Pill
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (isBn) item.sectionBn else item.sectionEn,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Edit Button
                                IconButton(
                                    onClick = { itemToEdit = item },
                                    modifier = Modifier.size(32.dp).testTag("edit_guide_${item.id}")
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit Item",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Delete Button
                                IconButton(
                                    onClick = { itemToDelete = item },
                                    modifier = Modifier.size(32.dp).testTag("delete_guide_${item.id}")
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete Item",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Location in market
                        if (item.locationTipBn.isNotBlank() || item.locationTipEn.isNotBlank()) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBn) "অবস্থান: ${item.locationTipBn}" else "Location: ${item.locationTipEn}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Selection Quality Tip
                        if (item.buyingTipBn.isNotBlank() || item.buyingTipEn.isNotBlank()) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBn) "চেনার কৌশল: ${item.buyingTipBn}" else "Quality Tip: ${item.buyingTipEn}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Price and Action Button (USING DYNAMIC CURRENCY SYMBOL)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val priceRangeText = formatPriceRange(item, currencySymbol, isBn)
                            Text(
                                text = if (isBn) "গড় দর: $priceRangeText" else "Est: $priceRangeText",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Button(
                                onClick = {
                                    val avgRate = if (item.minPrice > 0 && item.maxPrice > 0)
                                        (item.minPrice + item.maxPrice) / 2.0
                                    else
                                        item.minPrice.coerceAtLeast(item.maxPrice)

                                    val estTotal = avgRate * item.defaultQty
                                    viewModel.addToBuyItem(
                                        nameEn = item.nameEn,
                                        nameBn = item.nameBn,
                                        category = when (item.sectionEn) {
                                            "Vegetable Bazaar" -> "VEGETABLES"
                                            "Spice Market" -> "SPICES"
                                            "Pharmacy" -> "MEDICINE"
                                            "Garments" -> "GARMENTS"
                                            "Hardware" -> "HARDWARE"
                                            "Household" -> "HOUSEHOLD"
                                            else -> "OTHERS"
                                        },
                                        quantity = item.defaultQty,
                                        unit = item.defaultUnit,
                                        rate = avgRate,
                                        estimatedPrice = estTotal,
                                        notes = "Added from Bazaar Directory",
                                        isPrivate = false
                                    )
                                    addedNotice = if (isBn)
                                        "✅ '${item.nameBn}' বাজার ফর্দে যোগ করা হয়েছে ($currencySymbol${estTotal.toInt()})!"
                                    else
                                        "✅ Added '${item.nameEn}' to Shopping List ($currencySymbol${estTotal.toInt()})!"
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("add_guide_item_${item.id}")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isBn) "ফর্দে যোগ" else "Add to Buy", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button to Add new Bazaar directory item
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_add_market_item"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Bazaar Item")
        }
    }

    // Add Bazaar Item Dialog
    if (showAddDialog) {
        AddEditMarketItemDialog(
            initialItem = null,
            language = language,
            currencySymbol = currencySymbol,
            onDismiss = { showAddDialog = false },
            onSave = { nameEn, nameBn, secEn, secBn, locTipEn, locTipBn, buyTipEn, buyTipBn, minP, maxP, pUnit, dUnit, dQty ->
                viewModel.addMarketGuideItem(
                    nameEn = nameEn,
                    nameBn = nameBn,
                    sectionEn = secEn,
                    sectionBn = secBn,
                    locationTipEn = locTipEn,
                    locationTipBn = locTipBn,
                    buyingTipEn = buyTipEn,
                    buyingTipBn = buyTipBn,
                    minPrice = minP,
                    maxPrice = maxP,
                    priceUnit = pUnit,
                    defaultUnit = dUnit,
                    defaultQty = dQty
                )
                showAddDialog = false
                addedNotice = if (isBn) "✅ নতুন বাজার পণ্য সফলভাবে যোগ করা হয়েছে!" else "✅ Directory item added successfully!"
            }
        )
    }

    // Edit Bazaar Item Dialog
    if (itemToEdit != null) {
        val target = itemToEdit!!
        AddEditMarketItemDialog(
            initialItem = target,
            language = language,
            currencySymbol = currencySymbol,
            onDismiss = { itemToEdit = null },
            onSave = { nameEn, nameBn, secEn, secBn, locTipEn, locTipBn, buyTipEn, buyTipBn, minP, maxP, pUnit, dUnit, dQty ->
                val updated = target.copy(
                    nameEn = nameEn,
                    nameBn = nameBn,
                    sectionEn = secEn,
                    sectionBn = secBn,
                    locationTipEn = locTipEn,
                    locationTipBn = locTipBn,
                    buyingTipEn = buyTipEn,
                    buyingTipBn = buyTipBn,
                    minPrice = minP,
                    maxPrice = maxP,
                    priceUnit = pUnit,
                    defaultUnit = dUnit,
                    defaultQty = dQty
                )
                viewModel.updateMarketGuideItem(updated)
                itemToEdit = null
                addedNotice = if (isBn) "✅ বাজার পণ্য আপডেট করা হয়েছে!" else "✅ Directory item updated!"
            }
        )
    }

    // Delete Confirmation Dialog
    if (itemToDelete != null) {
        val target = itemToDelete!!
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "পণ্য মুছে ফেলবেন?" else "Delete Directory Item?",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text(
                    text = if (isBn)
                        "আপনি কি নিশ্চিত যে '${target.nameBn}' (${target.nameEn}) বাজার ডিরেক্টরি থেকে মুছে ফেলতে চান?"
                    else
                        "Are you sure you want to delete '${target.nameEn}' from the bazaar directory?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMarketGuideItem(target)
                        itemToDelete = null
                        addedNotice = if (isBn) "🗑️ পণ্য মুছে ফেলা হয়েছে!" else "🗑️ Directory item deleted!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isBn) "মুছে ফেলুন" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMarketItemDialog(
    initialItem: MarketGuideEntity?,
    language: AppLanguage,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (
        nameEn: String,
        nameBn: String,
        sectionEn: String,
        sectionBn: String,
        locationTipEn: String,
        locationTipBn: String,
        buyingTipEn: String,
        buyingTipBn: String,
        minPrice: Double,
        maxPrice: Double,
        priceUnit: String,
        defaultUnit: String,
        defaultQty: Double
    ) -> Unit
) {
    val isBn = language == AppLanguage.BENGALI

    var nameEn by remember { mutableStateOf(initialItem?.nameEn ?: "") }
    var nameBn by remember { mutableStateOf(initialItem?.nameBn ?: "") }
    var selectedSectionKey by remember {
        mutableStateOf(
            when (initialItem?.sectionEn) {
                "Vegetable Bazaar" -> "VEG"
                "Spice Market" -> "SPICE"
                "Pharmacy" -> "MED"
                "Garments" -> "GARMENTS"
                "Hardware" -> "HARDWARE"
                "Household" -> "HOUSEHOLD"
                else -> "VEG"
            }
        )
    }
    var locationTipEn by remember { mutableStateOf(initialItem?.locationTipEn ?: "") }
    var locationTipBn by remember { mutableStateOf(initialItem?.locationTipBn ?: "") }
    var buyingTipEn by remember { mutableStateOf(initialItem?.buyingTipEn ?: "") }
    var buyingTipBn by remember { mutableStateOf(initialItem?.buyingTipBn ?: "") }
    var minPriceStr by remember { mutableStateOf(initialItem?.minPrice?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var maxPriceStr by remember { mutableStateOf(initialItem?.maxPrice?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var priceUnit by remember { mutableStateOf(initialItem?.priceUnit ?: "kg") }
    var defaultQtyStr by remember { mutableStateOf(initialItem?.defaultQty?.toString() ?: "1.0") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val sections = listOf(
        Triple("VEG", "Vegetable Bazaar", "কাঁচাবাজার"),
        Triple("SPICE", "Spice Market", "মসলা ও পসারি দোকান"),
        Triple("MED", "Pharmacy", "ঔষধের দোকান / ফার্মেসি"),
        Triple("GARMENTS", "Garments", "পোশাক ও কাপড়ের দোকান"),
        Triple("HARDWARE", "Hardware", "হার্ডওয়্যার ও ইলেকট্রিক দোকান"),
        Triple("HOUSEHOLD", "Household", "গৃহস্থালি ও মুদি দোকান")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialItem == null)
                    (if (isBn) "➕ নতুন বাজার পণ্য ও দর নির্দেশিকা" else "➕ Add Bazaar Directory Item")
                else
                    (if (isBn) "✏️ বাজার পণ্য সম্পাদনা" else "✏️ Edit Bazaar Directory Item"),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Section Picker
                Text(text = if (isBn) "বাজার সেকশন:" else "Bazaar Section:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    sections.forEach { (key, en, bn) ->
                        FilterChip(
                            selected = selectedSectionKey == key,
                            onClick = { selectedSectionKey = key },
                            label = { Text(if (isBn) bn else en, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = nameBn,
                    onValueChange = { nameBn = it },
                    label = { Text(if (isBn) "বাংলা নাম (যেমন: পটল, এলাচ)" else "Bengali Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("bazaar_name_bn_input")
                )

                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text(if (isBn) "ইংরেজি নাম (যেমন: Pointed Gourd)" else "English Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("bazaar_name_en_input")
                )

                // Price Range Inputs with dynamic currency symbol
                Text(
                    text = if (isBn) "গড় দরদাম নির্দেশিকা ($currencySymbol):" else "Price Range ($currencySymbol):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = minPriceStr,
                        onValueChange = { minPriceStr = it },
                        label = { Text(if (isBn) "নূন্যতম ($currencySymbol)" else "Min ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("bazaar_min_price_input")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedTextField(
                        value = maxPriceStr,
                        onValueChange = { maxPriceStr = it },
                        label = { Text(if (isBn) "সর্বোচ্চ ($currencySymbol)" else "Max ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("bazaar_max_price_input")
                    )
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = priceUnit,
                        onValueChange = { priceUnit = it },
                        label = { Text(if (isBn) "দর একক (যেমন: kg, strip, pcs)" else "Unit (kg, strip, pcs)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("bazaar_unit_input")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedTextField(
                        value = defaultQtyStr,
                        onValueChange = { defaultQtyStr = it },
                        label = { Text(if (isBn) "ডিফল্ট কেনার পরিমাণ" else "Default Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Location Tip
                OutlinedTextField(
                    value = locationTipBn,
                    onValueChange = { locationTipBn = it },
                    label = { Text(if (isBn) "বাজারে কোথায় পাবেন (বাংলা)" else "Location Tip (Bengali)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = locationTipEn,
                    onValueChange = { locationTipEn = it },
                    label = { Text(if (isBn) "অবস্থান নির্দেশিকা (ইংরেজি)" else "Location Tip (English)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Buying / Quality Tip
                OutlinedTextField(
                    value = buyingTipBn,
                    onValueChange = { buyingTipBn = it },
                    label = { Text(if (isBn) "ভালো পণ্য চেনার কৌশল (বাংলা)" else "Quality Tip (Bengali)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = buyingTipEn,
                    onValueChange = { buyingTipEn = it },
                    label = { Text(if (isBn) "চেনার কৌশল (ইংরেজি)" else "Quality Tip (English)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameEn.isBlank() && nameBn.isBlank()) {
                        errorMessage = if (isBn) "পণ্যের নাম লিখুন" else "Please enter item name"
                        return@Button
                    }
                    val currentSection = sections.firstOrNull { it.first == selectedSectionKey } ?: sections[0]
                    val minPrice = minPriceStr.toDoubleOrNull() ?: 0.0
                    val maxPrice = maxPriceStr.toDoubleOrNull() ?: minPrice
                    val defaultQty = defaultQtyStr.toDoubleOrNull() ?: 1.0

                    onSave(
                        nameEn.ifBlank { nameBn },
                        nameBn.ifBlank { nameEn },
                        currentSection.second,
                        currentSection.third,
                        locationTipEn.ifBlank { "Available in local bazaar" },
                        locationTipBn.ifBlank { "বাজারের সাধারণ দোকানে মেলে" },
                        buyingTipEn.ifBlank { "Check freshness and seal" },
                        buyingTipBn.ifBlank { "তাজা ও ভালো মান দেখে নিন" },
                        minPrice,
                        maxPrice,
                        priceUnit.ifBlank { "kg" },
                        priceUnit.ifBlank { "kg" },
                        defaultQty
                    )
                },
                modifier = Modifier.testTag("save_bazaar_item_button")
            ) {
                Text(if (isBn) "সংরক্ষণ করুন" else "Save Item")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBn) "বাতিল" else "Cancel")
            }
        }
    )
}
