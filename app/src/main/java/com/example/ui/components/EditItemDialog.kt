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
import com.example.data.local.PantryItemEntity
import com.example.util.AppLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemDialog(
    item: PantryItemEntity,
    language: AppLanguage,
    currencySymbol: String = "৳",
    onDismiss: () -> Unit,
    onSave: (PantryItemEntity) -> Unit
) {
    val isBn = language == AppLanguage.BENGALI

    var nameEn by remember { mutableStateOf(item.nameEn) }
    var nameBn by remember { mutableStateOf(item.nameBn) }
    var selectedCategory by remember { mutableStateOf(item.category) }
    var quantityStr by remember { mutableStateOf(item.quantity.toString()) }
    var unit by remember { mutableStateOf(item.unit) }
    var minThresholdStr by remember { mutableStateOf(item.minThreshold.toString()) }
    var rateStr by remember { mutableStateOf(if (item.rate > 0) item.rate.toString() else "0") }
    var priceStr by remember { mutableStateOf(if (item.price > 0) item.price.toString() else "0") }
    var notes by remember { mutableStateOf(item.notes) }
    var isPrivate by remember { mutableStateOf(item.isPrivate) }

    var unitDropdownExpanded by remember { mutableStateOf(false) }
    val units = listOf("kg", "g", "pcs", "bundle", "packet", "strip", "tablet", "bottle", "L")

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
            Text(
                text = if (isBn) "✏️ পণ্য ও দর সম্পাদন" else "✏️ Edit Item & Rate",
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Category
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == "VEGETABLES",
                        onClick = { selectedCategory = "VEGETABLES" },
                        label = { Text(if (isBn) "🥬 শাকসবজি" else "🥬 Veg", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedCategory == "SPICES",
                        onClick = { selectedCategory = "SPICES" },
                        label = { Text(if (isBn) "🧂 মসলা" else "🧂 Spice", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedCategory == "MEDICINE",
                        onClick = { selectedCategory = "MEDICINE" },
                        label = { Text(if (isBn) "💊 ঔষধ" else "💊 Med", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedCategory == "GARMENTS",
                        onClick = { selectedCategory = "GARMENTS" },
                        label = { Text(if (isBn) "👕 পোশাক" else "👕 Garments", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedCategory == "HARDWARE",
                        onClick = { selectedCategory = "HARDWARE" },
                        label = { Text(if (isBn) "🔧 হার্ডওয়্যার" else "🔧 Hardware", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedCategory == "HOUSEHOLD",
                        onClick = { selectedCategory = "HOUSEHOLD" },
                        label = { Text(if (isBn) "🏠 গৃহস্থালি" else "🏠 Household", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedCategory == "STAPLES",
                        onClick = { selectedCategory = "STAPLES" },
                        label = { Text(if (isBn) "🍚 রসদ" else "🍚 Staple", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedCategory == "OTHERS",
                        onClick = { selectedCategory = "OTHERS" },
                        label = { Text(if (isBn) "📦 অন্যান্য" else "📦 Others", fontSize = 11.sp) }
                    )
                }

                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text(if (isBn) "ইংরেজি নাম" else "English Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_item_name_en")
                )

                OutlinedTextField(
                    value = nameBn,
                    onValueChange = { nameBn = it },
                    label = { Text(if (isBn) "বাংলা নাম" else "Bengali Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_item_name_bn")
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
                        modifier = Modifier.weight(1f).testTag("edit_item_quantity")
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
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
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

                // Rate & Price
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
                        modifier = Modifier.weight(1f).testTag("edit_item_rate")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text(if (isBn) "মোট মূল্য ($currencySymbol)" else "Total Price ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("edit_item_price")
                    )
                }

                // Threshold
                OutlinedTextField(
                    value = minThresholdStr,
                    onValueChange = { minThresholdStr = it },
                    label = { Text(if (isBn) "সতর্কতা সীমা" else "Low Stock Threshold") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Private toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isPrivate,
                        onCheckedChange = { isPrivate = it }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBn) "🔒 একান্ত ব্যক্তিগত (সহচর ডিভাইসে লুকানো)" else "🔒 Private (Hide from companion device)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isBn) "মন্তব্য" else "Notes") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val q = quantityStr.toDoubleOrNull() ?: item.quantity
                    val r = rateStr.toDoubleOrNull() ?: item.rate
                    val p = priceStr.toDoubleOrNull() ?: (q * r)
                    val t = minThresholdStr.toDoubleOrNull() ?: item.minThreshold
                    val updated = item.copy(
                        nameEn = nameEn.ifBlank { item.nameEn },
                        nameBn = nameBn.ifBlank { item.nameBn },
                        category = selectedCategory,
                        quantity = q,
                        unit = unit,
                        minThreshold = t,
                        rate = r,
                        price = p,
                        notes = notes,
                        isPrivate = isPrivate,
                        isFinished = q <= 0.0
                    )
                    onSave(updated)
                    onDismiss()
                },
                modifier = Modifier.testTag("save_edit_item_button")
            ) {
                Text(if (isBn) "সংরক্ষণ করুন" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBn) "বাতিল" else "Cancel")
            }
        }
    )
}
