package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.MainViewModel
import com.example.ui.components.VoiceCommandDialog
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.GinnirHishabScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.MarketFinderScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ShariKeysScreen
import com.example.ui.screens.ToBuyListScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.AppLanguage
import com.example.util.NotificationHelper
import com.example.util.Strings

enum class AppScreen {
    INVENTORY,
    TO_BUY,
    HISHAB,
    MARKET_FINDER,
    PROFILE,
    ADMIN
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Notification Channels
        NotificationHelper.createNotificationChannel(this)

        setContent {
            MyApplicationTheme {
                val language by viewModel.currentLanguage.collectAsState()
                val currentUser by viewModel.currentUser.collectAsState()
                val pantryItems by viewModel.pantryItems.collectAsState()
                val toBuyItems by viewModel.toBuyItems.collectAsState()

                var currentScreen by remember { mutableStateOf(AppScreen.INVENTORY) }
                var showVoiceDialog by remember { mutableStateOf(false) }

                // Ask for Notification permission on Android 13+
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { /* Permission granted or denied */ }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val hasNotificationPerm = ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                        if (!hasNotificationPerm) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                // If not logged in, show AuthScreen
                if (currentUser == null) {
                    AuthScreen(
                        viewModel = viewModel,
                        language = language,
                        onAuthSuccess = {
                            currentScreen = AppScreen.INVENTORY
                        }
                    )
                } else {
                    // Handle back press
                    BackHandler(enabled = currentScreen != AppScreen.INVENTORY) {
                        currentScreen = AppScreen.INVENTORY
                    }

                val isBn = language == AppLanguage.BENGALI
                val isAdmin = currentUser?.role == "ADMIN"
                val lowStockCount = pantryItems.count { it.isLowStock || it.isFinished }
                val toBuyCount = toBuyItems.count { !it.isBought }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Kitchen,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isBn) Strings.APP_TITLE_BN else Strings.APP_TITLE_EN,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            },
                            actions = {
                                // Language Toggle Chip
                                FilterChip(
                                    selected = false,
                                    onClick = { viewModel.toggleLanguage() },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Language,
                                            contentDescription = "Language",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = if (isBn) "EN" else "বাং",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    },
                                    modifier = Modifier
                                        .padding(end = 4.dp)
                                        .testTag("language_toggle_button")
                                )

                                // Direct Voice Action in TopBar
                                IconButton(
                                    onClick = { showVoiceDialog = true },
                                    modifier = Modifier.testTag("top_bar_mic_button")
                                ) {
                                    Icon(
                                        Icons.Default.Mic,
                                        contentDescription = "Voice",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            if (isAdmin) {
                                // Admin Only Navigation View
                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.ADMIN,
                                    onClick = { currentScreen = AppScreen.ADMIN },
                                    icon = {
                                        Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin")
                                    },
                                    label = {
                                        Text(
                                            text = if (isBn) Strings.TAB_ADMIN_BN else Strings.TAB_ADMIN_EN,
                                            fontSize = 11.sp
                                        )
                                    },
                                    modifier = Modifier.testTag("nav_tab_admin")
                                )
                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.INVENTORY,
                                    onClick = { currentScreen = AppScreen.INVENTORY },
                                    icon = {
                                        Icon(Icons.Default.Kitchen, contentDescription = "Pantry Preview")
                                    },
                                    label = {
                                        Text(
                                            text = if (isBn) Strings.TAB_VAULT_BN else Strings.TAB_VAULT_EN,
                                            fontSize = 11.sp
                                        )
                                    },
                                    modifier = Modifier.testTag("nav_tab_inventory")
                                )
                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.PROFILE,
                                    onClick = { currentScreen = AppScreen.PROFILE },
                                    icon = {
                                        Icon(Icons.Default.Person, contentDescription = "Profile")
                                    },
                                    label = {
                                        Text(
                                            text = if (isBn) Strings.TAB_PROFILE_BN else Strings.TAB_PROFILE_EN,
                                            fontSize = 11.sp
                                        )
                                    },
                                    modifier = Modifier.testTag("nav_tab_profile")
                                )
                            } else {
                                // NORMAL USER NAVIGATION: Strictly NO Admin Option inside user login!
                                // 1. Ginnir Vault (Inventory)
                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.INVENTORY,
                                    onClick = { currentScreen = AppScreen.INVENTORY },
                                    icon = {
                                        if (lowStockCount > 0) {
                                            BadgedBox(badge = {
                                                Badge { Text("$lowStockCount") }
                                            }) {
                                                Icon(Icons.Default.Kitchen, contentDescription = "Ginnir Vault")
                                            }
                                        } else {
                                            Icon(Icons.Default.Kitchen, contentDescription = "Ginnir Vault")
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = if (isBn) Strings.TAB_VAULT_BN else Strings.TAB_VAULT_EN,
                                            fontSize = 11.sp
                                        )
                                    },
                                    modifier = Modifier.testTag("nav_tab_inventory")
                                )

                                // 2. Bajarer Fard (To-Buy List)
                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.TO_BUY,
                                    onClick = { currentScreen = AppScreen.TO_BUY },
                                    icon = {
                                        if (toBuyCount > 0) {
                                            BadgedBox(badge = {
                                                Badge { Text("$toBuyCount") }
                                            }) {
                                                Icon(Icons.Default.ShoppingCart, contentDescription = "Bajarer Fard")
                                            }
                                        } else {
                                            Icon(Icons.Default.ShoppingCart, contentDescription = "Bajarer Fard")
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = if (isBn) Strings.TAB_FARD_BN else Strings.TAB_FARD_EN,
                                            fontSize = 11.sp
                                        )
                                    },
                                    modifier = Modifier.testTag("nav_tab_to_buy")
                                )

                                // 3. Ginnir Hishab (Budgeting & Expenses)
                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.HISHAB,
                                    onClick = { currentScreen = AppScreen.HISHAB },
                                    icon = {
                                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Ginnir Hishab")
                                    },
                                    label = {
                                        Text(
                                            text = if (isBn) Strings.TAB_HISHAB_BN else Strings.TAB_HISHAB_EN,
                                            fontSize = 11.sp
                                        )
                                    },
                                    modifier = Modifier.testTag("nav_tab_hishab")
                                )

                                // 4. Shari-Key & Bazaar (Remote Sync & Market Finder)
                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.MARKET_FINDER,
                                    onClick = { currentScreen = AppScreen.MARKET_FINDER },
                                    icon = {
                                        Icon(Icons.Default.Storefront, contentDescription = "Shari-Key Bazaar")
                                    },
                                    label = {
                                        Text(
                                            text = if (isBn) Strings.TAB_SHARI_KEY_BN else Strings.TAB_SHARI_KEY_EN,
                                            fontSize = 11.sp
                                        )
                                    },
                                    modifier = Modifier.testTag("nav_tab_market")
                                )

                                // 5. Profile & Companion
                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.PROFILE,
                                    onClick = { currentScreen = AppScreen.PROFILE },
                                    icon = {
                                        Icon(Icons.Default.Person, contentDescription = "Profile")
                                    },
                                    label = {
                                        Text(
                                            text = if (isBn) Strings.TAB_PROFILE_BN else Strings.TAB_PROFILE_EN,
                                            fontSize = 11.sp
                                        )
                                    },
                                    modifier = Modifier.testTag("nav_tab_profile")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Crossfade(targetState = currentScreen, label = "screen_crossfade") { screen ->
                            when (screen) {
                                AppScreen.INVENTORY -> {
                                    InventoryScreen(
                                        viewModel = viewModel,
                                        language = language,
                                        onNavigateToToBuy = { currentScreen = AppScreen.TO_BUY }
                                    )
                                }
                                AppScreen.TO_BUY -> {
                                    ToBuyListScreen(
                                        viewModel = viewModel,
                                        language = language,
                                        onNavigateToMarketGuide = { currentScreen = AppScreen.MARKET_FINDER }
                                    )
                                }
                                AppScreen.HISHAB -> {
                                    GinnirHishabScreen(
                                        viewModel = viewModel,
                                        language = language
                                    )
                                }
                                AppScreen.MARKET_FINDER -> {
                                    ShariKeysScreen(
                                        viewModel = viewModel,
                                        language = language,
                                        onNavigateToToBuy = { currentScreen = AppScreen.TO_BUY }
                                    )
                                }
                                AppScreen.ADMIN -> {
                                    AdminScreen(
                                        viewModel = viewModel,
                                        language = language,
                                        onNavigateToAuth = {
                                            viewModel.logout()
                                        }
                                    )
                                }
                                AppScreen.PROFILE -> {
                                    ProfileScreen(
                                        viewModel = viewModel,
                                        language = language,
                                        onLogout = {
                                            currentScreen = AppScreen.INVENTORY
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                    // Global Voice Command Dialog
                    if (showVoiceDialog) {
                        VoiceCommandDialog(
                            language = language,
                            onDismiss = { showVoiceDialog = false },
                            onCommandSpoken = { spokenText ->
                                viewModel.executeVoiceCommand(spokenText)
                            }
                        )
                    }
                }
            }
        }
    }
}
