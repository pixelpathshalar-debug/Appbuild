package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.AppLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    language: AppLanguage,
    currencySymbol: String = "₹",
    onDismiss: () -> Unit,
    onConfirm: (
        nameEn: String,
        nameBn: String,
        category: String,
        quantity: Double,
        unit: String,
        minThreshold: Double,
        rate: Double,
        price: Double,
        notes: String,
        isPrivate: Boolean
    ) -> Unit
) {
    var nameEn by remember { mutableStateOf("") }
    var nameBn by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("VEGETABLES") }
    var quantityStr by remember { mutableStateOf("1.0") }
    var unit by remember { mutableStateOf("kg") }
    var minThresholdStr by remember { mutableStateOf("0.5") }
    var rateStr by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(false) }

    var unitDropdownExpanded by remember { mutableStateOf(false) }
    val units = listOf("kg", "g", "pcs", "bundle", "packet", "strip", "tablet", "bottle", "L")

    val isBn = language == AppLanguage.BENGALI

    fun recalculatePrice(qStr: String, rStr: String) {
        val q = qStr.toDoubleOrNull() ?: 0.0
        val r = rStr.toDoubleOrNull() ?: 0.0
        if (q > 0 && r > 0) {
            priceStr = (q * r).toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = if (isBn) "🏛️ ভল্টে নতুন পণ্য তৈরি করুন" else "🏛️ Create New Item in Vault",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = if (isBn) "ভল্টের স্থায়ী মজুদ হিসেবে যুক্ত হবে এবং বাজার ফর্দে যোগ করার জন্য প্রস্তুত থাকবে"
                    else "Item will be created in Vault and available for shopping list",
                    fontSize = 11.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.outline
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Category selector
                Text(
                    text = if (isBn) "বিভাগ:" else "Category:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == "VEGETABLES",
                        onClick = {
                            selectedCategory = "VEGETABLES"
                            unit = "kg"
                        },
                        label = { Text(if (isBn) "শাকসবজি" else "Veg", fontSize = 11.sp) },
                        modifier = Modifier.testTag("category_veg_chip")
                    )
                    FilterChip(
                        selected = selectedCategory == "SPICES",
                        onClick = {
                            selectedCategory = "SPICES"
                            unit = "g"
                        },
                        label = { Text(if (isBn) "মসলা" else "Spice", fontSize = 11.sp) },
                        modifier = Modifier.testTag("category_spice_chip")
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
                        label = { Text(if (isBn) "💊 ঔষধ" else "💊 Medicine", fontSize = 11.sp) },
                        modifier = Modifier.testTag("category_medicine_chip")
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
                        label = { Text(if (isBn) "👕 পোশাক" else "👕 Garments", fontSize = 11.sp) },
                        modifier = Modifier.testTag("category_garments_chip")
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
                        label = { Text(if (isBn) "🔧 হার্ডওয়্যার" else "🔧 Hardware", fontSize = 11.sp) },
                        modifier = Modifier.testTag("category_hardware_chip")
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
                        label = { Text(if (isBn) "🏠 গৃহস্থালি" else "🏠 Household", fontSize = 11.sp) },
                        modifier = Modifier.testTag("category_household_chip")
                    )
                    FilterChip(
                        selected = selectedCategory == "STAPLES",
                        onClick = { selectedCategory = "STAPLES" },
                        label = { Text(if (isBn) "রসদ" else "Staple", fontSize = 11.sp) },
                        modifier = Modifier.testTag("category_staple_chip")
                    )
                    FilterChip(
                        selected = selectedCategory == "OTHERS",
                        onClick = { selectedCategory = "OTHERS"; unit = "pcs" },
                        label = { Text(if (isBn) "📦 অন্যান্য" else "📦 Others", fontSize = 11.sp) },
                        modifier = Modifier.testTag("category_others_chip")
                    )
                }

                // Name English & Bengali
                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text(if (isBn) "ইংরেজি নাম (যেমন: Potato, Napa)" else "English Name (e.g. Potato, Napa)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_item_name_en")
                )

                OutlinedTextField(
                    value = nameBn,
                    onValueChange = { nameBn = it },
                    label = { Text(if (isBn) "বাংলা নাম (যেমন: আলু, নাপা)" else "Bengali Name (e.g. আলু, নাপা)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_item_name_bn")
                )

                // Quantity & Unit
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
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_item_quantity")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = unitDropdownExpanded,
                        onExpandedChange = { unitDropdownExpanded = it },
                        modifier = Modifier.weight(0.9f)
                    ) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (isBn) "একক" else "Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .testTag("input_item_unit_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = unitDropdownExpanded,
                            onDismissRequest = { unitDropdownExpanded = false }
                        ) {
                            units.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text(u) },
                                    onClick = {
                                        unit = u
                                        unitDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Rate & Calculated Price
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = rateStr,
                        onValueChange = {
                            rateStr = it
                            recalculatePrice(quantityStr, it)
                        },
                        label = { Text(if (isBn) "দর ($currencySymbol/একক)" else "Rate ($currencySymbol/unit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_item_rate")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text(if (isBn) "মোট মূল্য ($currencySymbol)" else "Total Price ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_item_price")
                    )
                }

                // Low Stock Threshold
                OutlinedTextField(
                    value = minThresholdStr,
                    onValueChange = { minThresholdStr = it },
                    label = { Text(if (isBn) "কম স্টক সতর্কতা সীমা (যেমন: 1.0 kg)" else "Low Stock Threshold Alert") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_item_threshold")
                )

                // Private Toggle (Husband / Wife privacy option)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isPrivate,
                        onCheckedChange = { isPrivate = it },
                        modifier = Modifier.testTag("input_item_private_checkbox")
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = if (isBn) "🔒 একান্ত ব্যক্তিগত (Private)" else "🔒 Keep Private",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (isBn) "সহচর ডিভাইসে (স্বামী/স্ত্রী) এটি দেখা যাবে না।"
                            else "Will not be shared with linked companion device.",
                            fontSize = 11.sp,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isBn) "মন্তব্য / ব্যবহার নির্দেশিকা" else "Notes / Instructions") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_item_notes")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityStr.toDoubleOrNull() ?: 1.0
                    val threshold = minThresholdStr.toDoubleOrNull() ?: 0.5
                    val rate = rateStr.toDoubleOrNull() ?: 0.0
                    val price = priceStr.toDoubleOrNull() ?: (qty * rate)
                    if (nameEn.isNotBlank() || nameBn.isNotBlank()) {
                        onConfirm(
                            nameEn.ifBlank { nameBn },
                            nameBn.ifBlank { nameEn },
                            selectedCategory,
                            qty,
                            unit,
                            threshold,
                            rate,
                            price,
                            notes,
                            isPrivate
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("confirm_add_item_button")
            ) {
                Text(if (isBn) "যোগ করুন" else "Add Item")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_add_item_button")
            ) {
                Text(if (isBn) "বাতিল" else "Cancel")
            }
        }
    )
}
