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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.util.AppLanguage
import com.example.util.Strings

@Composable
fun GinnirHishabScreen(
    viewModel: MainViewModel,
    language: AppLanguage
) {
    val isBn = language == AppLanguage.BENGALI
    val currentUser by viewModel.currentUser.collectAsState()
    val pantryItems by viewModel.pantryItems.collectAsState()
    val toBuyItems by viewModel.toBuyItems.collectAsState()

    val currency = currentUser?.currencySymbol ?: "₹"

    // Financial calculations
    val totalVaultValue = pantryItems.sumOf {
        if (it.price > 0) it.price else (it.rate * it.quantity)
    }
    val totalToBuyEst = toBuyItems.sumOf {
        if (it.estimatedPrice > 0) it.estimatedPrice else (it.rate * it.quantity)
    }

    var monthlyBudget by remember { mutableDoubleStateOf(15000.0) }
    var isEditingBudget by remember { mutableStateOf(false) }
    var budgetInput by remember { mutableStateOf("15000") }

    val remainingBudget = (monthlyBudget - totalToBuyEst).coerceAtLeast(0.0)
    val budgetProgress = if (monthlyBudget > 0) (totalToBuyEst / monthlyBudget).toFloat().coerceIn(0f, 1f) else 0f

    // Category wise expenses
    val vegTotal = toBuyItems.filter { it.category == "VEGETABLES" }.sumOf { if (it.estimatedPrice > 0) it.estimatedPrice else (it.rate * it.quantity) }
    val spiceTotal = toBuyItems.filter { it.category == "SPICES" }.sumOf { if (it.estimatedPrice > 0) it.estimatedPrice else (it.rate * it.quantity) }
    val stapleTotal = toBuyItems.filter { it.category == "STAPLES" }.sumOf { if (it.estimatedPrice > 0) it.estimatedPrice else (it.rate * it.quantity) }
    val medTotal = toBuyItems.filter { it.category == "MEDICINE" }.sumOf { if (it.estimatedPrice > 0) it.estimatedPrice else (it.rate * it.quantity) }
    val garmentTotal = toBuyItems.filter { it.category == "GARMENTS" }.sumOf { if (it.estimatedPrice > 0) it.estimatedPrice else (it.rate * it.quantity) }
    val hardwareTotal = toBuyItems.filter { it.category == "HARDWARE" }.sumOf { if (it.estimatedPrice > 0) it.estimatedPrice else (it.rate * it.quantity) }
    val householdTotal = toBuyItems.filter { it.category == "HOUSEHOLD" }.sumOf { if (it.estimatedPrice > 0) it.estimatedPrice else (it.rate * it.quantity) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("ginnir_hishab_list"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Hero Card
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Savings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isBn) "গিন্নির হিসাব ও বাজেট" else "Ginnir Hishab (Budgeting)",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = if (isBn) "মাসিক সংসার খরচ ও সঞ্চয়ের চাবিকাঠি" else "Household expenses & smart family savings",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isBn) "ভল্টের মজুদ মূল্য" else "Vault Stock Value",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = "$currency${totalVaultValue.toInt()}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isBn) "ফর্দের আনুমানিক খরচ" else "Shopping Est.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = "$currency${totalToBuyEst.toInt()}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Monthly Budget Card
        item {
            Spacer(modifier = Modifier.height(14.dp))
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
                            text = if (isBn) "মাসিক সংসার বাজেট লক্ষ্যমাত্রা" else "Monthly Household Budget Target",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        TextButton(onClick = { isEditingBudget = !isEditingBudget }) {
                            Text(if (isEditingBudget) "Done" else (if (isBn) "বাজেট বদলান" else "Edit Budget"))
                        }
                    }

                    if (isEditingBudget) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = budgetInput,
                                onValueChange = { budgetInput = it },
                                label = { Text("Budget ($currency)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = {
                                val b = budgetInput.toDoubleOrNull() ?: 15000.0
                                monthlyBudget = b
                                isEditingBudget = false
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "Save", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "বাজেট: $currency${monthlyBudget.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "অবশিষ্ট: $currency${remainingBudget.toInt()}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { budgetProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (budgetProgress > 0.85f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        // Category Breakdown Card (Veggies, Spices, Staples, Medicines, Garments, Hardware, Household)
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBn) "খাতওয়ারি খরচের হিসাব (বাজারের ফর্দ অনুযায়ী):" else "Category Spending Breakdown:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CategorySpendRow("🥦 সবজি (Vegetables)", "$currency${vegTotal.toInt()}")
                    CategorySpendRow("🌶️ মসলা ও দানা (Spices)", "$currency${spiceTotal.toInt()}")
                    CategorySpendRow("🌾 রসদ ও দুধ (Staples & Milk)", "$currency${stapleTotal.toInt()}")
                    CategorySpendRow("💊 ঔষধ ও ফার্মেসি (Medicine)", "$currency${medTotal.toInt()}")
                    CategorySpendRow("👗 পোশাক ও বস্ত্র (Garments)", "$currency${garmentTotal.toInt()}")
                    CategorySpendRow("💡 হার্ডওয়্যার ও লাইট (Hardware)", "$currency${hardwareTotal.toInt()}")
                    CategorySpendRow("🧼 গৃহস্থালি প্রসাধন (Household)", "$currency${householdTotal.toInt()}")
                }
            }
        }

        // Smart Ginni's Advice & Savings Tip
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💡", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "স্মার্ট গিন্নির সাশ্রয় টিপস" else "Smart Ginni's Money Saving Tip",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isBn)
                            "• কাঁচাবাজারে আলু ও পেঁয়াজ পাল্লা ধরে (৫ কেজি) কিনলে কেজিতে ৫-১০ টাকা সাশ্রয় হয়।\n• মশলা আস্ত কিনে ঘরে গুঁড়ো করলে খাঁটি স্বাদ ও অর্থ দুটোই বাঁচে।\n• ওষুধের জন্য প্রেসক্রিপশনের জেনেরিক নাম দেখে কিনুন।"
                        else
                            "• Buying potatoes and onions in 5kg lots saves 15-20% compared to loose retail.\n• Purchase whole spices and grind at home for superior purity and cost efficiency.\n• Always verify shopping list before market to prevent impulse buys.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
fun CategorySpendRow(label: String, amount: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp)
        Text(text = amount, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}
