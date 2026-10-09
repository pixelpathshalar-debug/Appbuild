package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.util.AppLanguage

data class MarketGuideItem(
    val nameEn: String,
    val nameBn: String,
    val sectionEn: String,
    val sectionBn: String,
    val locationTipEn: String,
    val locationTipBn: String,
    val buyingTipEn: String,
    val buyingTipBn: String,
    val priceRangeEn: String,
    val priceRangeBn: String,
    val defaultUnit: String,
    val defaultQty: Double
)

@Composable
fun MarketFinderScreen(
    viewModel: MainViewModel,
    language: AppLanguage,
    onNavigateToToBuy: () -> Unit
) {
    val isBn = language == AppLanguage.BENGALI
    var selectedSection by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var addedNotice by remember { mutableStateOf<String?>(null) }

    val guideItems = remember {
        listOf(
            MarketGuideItem(
                nameEn = "Potato",
                nameBn = "আলু",
                sectionEn = "Vegetable Bazaar",
                sectionBn = "কাঁচাবাজার",
                locationTipEn = "Front row vegetable wholesalers; sold in 5kg/10kg bags or loose",
                locationTipBn = "কাঁচাবাজারের প্রধান পাইকারি ও খুচরা চালানে বস্তাভর্তি বা পাল্লা ধরে পাওয়া যায়",
                buyingTipEn = "Select smooth, firm potatoes without green tint or soft sprouts",
                buyingTipBn = "সবুজ ছোপ বা চোখ ওঠা ছাড়া মসৃণ, শক্ত ডায়মন্ড বা লাল গোল আলু বাছুন",
                priceRangeEn = "৳30 - ৳45 / kg",
                priceRangeBn = "৳৩০ - ৳৪৫ / কেজি",
                defaultUnit = "kg",
                defaultQty = 5.0
            ),
            MarketGuideItem(
                nameEn = "Onion",
                nameBn = "পেঁয়াজ",
                sectionEn = "Vegetable Bazaar",
                sectionBn = "কাঁচাবাজার",
                locationTipEn = "Near potato stalls, sold in 1kg to 5kg palla",
                locationTipBn = "আলুর পাশেই বড় ঝুঁড়িতে থাকে, দেশি ও আমদানিকৃত আলাদা থাকে",
                buyingTipEn = "Choose dry, tight skin with no damp neck or mold",
                buyingTipBn = "শুকনো পাতলা খোসাযুক্ত মাঝারি দেশি পেঁয়াজ সবচেয়ে ঝাঁজালো",
                priceRangeEn = "৳70 - ৳90 / kg",
                priceRangeBn = "৳৭০ - ৳৯০ / কেজি",
                defaultUnit = "kg",
                defaultQty = 2.0
            ),
            MarketGuideItem(
                nameEn = "Green Chili",
                nameBn = "কাঁচা মরিচ",
                sectionEn = "Vegetable Bazaar",
                sectionBn = "কাঁচাবাজার",
                locationTipEn = "Fresh herb & greens row (beside coriander & ginger)",
                locationTipBn = "ধনেপাতা ও লেবুর সারিতে ছোট ঝুড়িতে তাজা মরিচ বিক্রি হয়",
                buyingTipEn = "Look for deep green color, intact green stems, and crisp snap",
                buyingTipBn = "গাঢ় সবুজ রঙ ও তাজা সবুজ বোঁটাযুক্ত মরিচ বাছাই করুন",
                priceRangeEn = "৳120 - ৳180 / kg",
                priceRangeBn = "৳১২০ - ৳১৮০ / কেজি",
                defaultUnit = "kg",
                defaultQty = 0.5
            ),
            MarketGuideItem(
                nameEn = "Tomato",
                nameBn = "টমেটো",
                sectionEn = "Vegetable Bazaar",
                sectionBn = "কাঁচাবাজার",
                locationTipEn = "Produce center displays, arranged in crates",
                locationTipBn = "কাঁচাবাজারের মাঝের সারিতে কাঠের বা প্লাস্টিকের ক্রেটে সাজানো থাকে",
                buyingTipEn = "Even red tone, glossy skin, slightly firm to touch without bruises",
                buyingTipBn = "চকচকে লাল ও দাগহীন শক্ত টমেটো কিনুন, বেশি নরম এড়িয়ে চলুন",
                priceRangeEn = "৳60 - ৳90 / kg",
                priceRangeBn = "৳৬০ - ৳৯০ / কেজি",
                defaultUnit = "kg",
                defaultQty = 1.0
            ),
            MarketGuideItem(
                nameEn = "Garlic",
                nameBn = "রসুন",
                sectionEn = "Spice Market",
                sectionBn = "মসলা ও পসারি দোকান",
                locationTipEn = "Spice & dry produce stalls, hung in mesh bags",
                locationTipBn = "মসলার দোকানে ঝুলিয়ে রাখা জালিতে বা স্তূপ করে রাখা থাকে",
                buyingTipEn = "Plump, tight bulbs that don't yield when pressed gently",
                buyingTipBn = "দেশি ছোট কোয়া রসুন বেশি সুবাসযুক্ত, শুকনো শক্ত খোসা দেখে নিন",
                priceRangeEn = "৳180 - ৳240 / kg",
                priceRangeBn = "৳১৮০ - ৳২৪০ / কেজি",
                defaultUnit = "g",
                defaultQty = 250.0
            ),
            MarketGuideItem(
                nameEn = "Ginger",
                nameBn = "আদা",
                sectionEn = "Spice Market",
                sectionBn = "মসলা ও পসারি দোকান",
                locationTipEn = "Beside garlic and whole spice heaps",
                locationTipBn = "রসুনের পাশেই আদার স্তূপ থাকে, দেশি ও বার্মিজ জাত পাওয়া যায়",
                buyingTipEn = "Smooth skin, spicy puncture aroma, snappy fiber",
                buyingTipBn = "মসৃণ ত্বক এবং নখ দিয়ে চাপলে সুবাসিত রস বের হয় এমন আদা নিন",
                priceRangeEn = "৳200 - ৳260 / kg",
                priceRangeBn = "৳২০০ - ৳২৬০ / কেজি",
                defaultUnit = "g",
                defaultQty = 250.0
            ),
            MarketGuideItem(
                nameEn = "Cumin (Jeera)",
                nameBn = "জিরা",
                sectionEn = "Spice Market",
                sectionBn = "মসলা ও পসারি দোকান",
                locationTipEn = "Traditional spice merchants (Poshari shops) with glass jars or sacks",
                locationTipBn = "পসারি ও পাইকারি মসলার দোকানে বস্তা বা কাঁচের বয়ামে থাকে",
                buyingTipEn = "Small uniform brown seeds, warm fragrant aroma when crushed",
                buyingTipBn = "হাতে ঘষলে তীব্র খাঁটি সুবাস আসবে, ধুলাবালিমুক্ত জিরা কিনুন",
                priceRangeEn = "৳750 - ৳950 / kg",
                priceRangeBn = "৳৭৫০ - ৳৯৫০ / কেজি",
                defaultUnit = "g",
                defaultQty = 100.0
            ),
            MarketGuideItem(
                nameEn = "Turmeric",
                nameBn = "হলুদ",
                sectionEn = "Spice Market",
                sectionBn = "মসলা ও পসারি দোকান",
                locationTipEn = "Whole dried roots or freshly ground at spice mill counter",
                locationTipBn = "শুকনো হলুদ গাট বা মসলা ভাঙ্গানোর কলের সামনে খাঁটি গুঁড়া পাওয়া যায়",
                buyingTipEn = "Deep golden orange color, avoid adulterated yellowish powder",
                buyingTipBn = "গাঢ় সোনালী রঙ ও প্রাকৃতিকভাবে ভাঙানো হলুদ স্বাস্থ্যসম্মত",
                priceRangeEn = "৳320 - ৳400 / kg",
                priceRangeBn = "৳৩২০ - ৳৪০০ / কেজি",
                defaultUnit = "g",
                defaultQty = 200.0
            ),
            MarketGuideItem(
                nameEn = "Cardamom (Elachi)",
                nameBn = "সবুজ এলাচ",
                sectionEn = "Spice Market",
                sectionBn = "মসলা ও পসারি দোকান",
                locationTipEn = "Aromatic spices shelf in grocer/spice shops",
                locationTipBn = "দামী মসলার তাকে কাঁচের পাত্রে সিল করা থাকে",
                buyingTipEn = "Fat green pods with fragrant seeds inside, not bleached white",
                buyingTipBn = "বড় সবুজ দানাযুক্ত এলাচ কিনুন, ফ্যাকাশে সাদা বা শুকনো দানা বাদ দিন",
                priceRangeEn = "৳3,000 - ৳3,800 / kg",
                priceRangeBn = "৳৩,০০০ - ৳৩,৮০০ / কেজি",
                defaultUnit = "g",
                defaultQty = 50.0
            ),
            MarketGuideItem(
                nameEn = "Paracetamol (Napa/Ace)",
                nameBn = "প্যারাসিটামল (নাপা/এইস)",
                sectionEn = "Pharmacy",
                sectionBn = "ঔষধের দোকান / ফার্মেসি",
                locationTipEn = "Licensed chemist shops and market pharmacy counters",
                locationTipBn = "বাজারের প্রবেশমুখে বা প্রধান গলির ফার্মেসিতে পাওয়া যায়",
                buyingTipEn = "Check expiry date and strip seal integrity",
                buyingTipBn = "মেয়াদোত্তীর্ণ তারিখ ও অক্ষত ফয়েল স্ট্রিপ দেখে নিন",
                priceRangeEn = "৳15 - ৳25 / strip",
                priceRangeBn = "৳১৫ - ৳২৫ / পাতা",
                defaultUnit = "strip",
                defaultQty = 2.0
            ),
            MarketGuideItem(
                nameEn = "Cotton Saree (Shari)",
                nameBn = "সুতি শাড়ি",
                sectionEn = "Garments",
                sectionBn = "পোশাক ও কাপড়ের দোকান",
                locationTipEn = "Textile corner, handloom sarees and cloth merchants",
                locationTipBn = "বাজারের কাপড় পট্টি বা তাঁতের কাপড়ের দোকান",
                buyingTipEn = "Check cotton yarn thread count and border print",
                buyingTipBn = "সুতির মান, সুতার বুনন ও আঁচলের পাড় পরীক্ষা করে নিন",
                priceRangeEn = "৳750 - ৳1,500 / pcs",
                priceRangeBn = "৳৭৫০ - ৳১,৫০০ / টি",
                defaultUnit = "pcs",
                defaultQty = 1.0
            ),
            MarketGuideItem(
                nameEn = "LED Bulb & Socket",
                nameBn = "এলইডি বাল্ব ও সুইচ",
                sectionEn = "Hardware",
                sectionBn = "হার্ডওয়্যার ও ইলেকট্রিক দোকান",
                locationTipEn = "Electric & hardware corner near bazaar entrance",
                locationTipBn = "বাজারের ইলেকট্রিক ও হার্ডওয়্যার দোকানে ওয়ারেন্টি সহ মেলে",
                buyingTipEn = "Ensure brand warranty card & lumens rating (9W/12W/15W)",
                buyingTipBn = "ওয়ারেন্টি সীল ও ৯ ওয়াট/১২ ওয়াট আলো নিশ্চিত করুন",
                priceRangeEn = "৳110 - ৳180 / pcs",
                priceRangeBn = "৳১১০ - ৳১৮০ / টি",
                defaultUnit = "pcs",
                defaultQty = 2.0
            ),
            MarketGuideItem(
                nameEn = "Detergent & Dish Soap",
                nameBn = "ডিটারজেন্ট ও ভিম সাবান",
                sectionEn = "Household",
                sectionBn = "গৃহস্থালি ও মুদি দোকান",
                locationTipEn = "General store & FMCG wholesale counter",
                locationTipBn = "মুদি দোকান বা ডিপার্টমেন্টাল স্টোরের সাবানের তাক",
                buyingTipEn = "Look for value refill packs to save money",
                buyingTipBn = "সাশ্রয়ী রিফিল প্যাক কিনলে খরচ কম হয়",
                priceRangeEn = "৳60 - ৳160 / pack",
                priceRangeBn = "৳৬০ - ৳১৬০ / প্যাক",
                defaultUnit = "kg",
                defaultQty = 1.0
            )
        )
    }

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
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                                    text = if (isBn) "কোথায় কোন সবজি ও মসলা পাবেন এবং দরদাম নির্দেশিকা"
                                    else "Find where to buy in bazaar & quality selection guide",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
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
                        label = { Text(if (isBn) "সব বাজার (${guideItems.size})" else "All Bazaar") },
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

            // Items List
            items(filteredItems, key = { it.nameEn }) { item ->
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("guide_card_${item.nameEn.lowercase()}"),
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
                            Column {
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
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Location in market
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

                        Spacer(modifier = Modifier.height(4.dp))

                        // Selection Quality Tip
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

                        Spacer(modifier = Modifier.height(8.dp))

                        // Price and Action Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBn) "গড় দর: ${item.priceRangeBn}" else "Est: ${item.priceRangeEn}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Button(
                                onClick = {
                                    viewModel.addToBuyItem(
                                        nameEn = item.nameEn,
                                        nameBn = item.nameBn,
                                        category = if (item.sectionEn == "Vegetable Bazaar") "VEGETABLES" else "SPICES",
                                        quantity = item.defaultQty,
                                        unit = item.defaultUnit,
                                        rate = if (item.defaultUnit == "kg") 35.0 else 0.5,
                                        estimatedPrice = if (item.defaultUnit == "kg") 100.0 else 50.0,
                                        notes = "Added from Market Directory",
                                        isPrivate = false
                                    )
                                    addedNotice = if (isBn)
                                        "✅ '${item.nameBn}' বাজার ফর্দে যোগ করা হয়েছে!"
                                    else
                                        "✅ Added '${item.nameEn}' to your Shopping List!"
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("add_guide_item_${item.nameEn.lowercase()}")
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
    }
}
