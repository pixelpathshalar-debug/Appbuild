package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.util.AppLanguage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    viewModel: MainViewModel,
    language: AppLanguage,
    onAuthSuccess: () -> Unit
) {
    val isBn = language == AppLanguage.BENGALI
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Login, 1: Register
    var identifierType by remember { mutableStateOf("PHONE") } // PHONE or EMAIL
    var identifier by remember { mutableStateOf("+8801712345678") }
    var name by remember { mutableStateOf("Tariqul Islam") }
    var companionId by remember { mutableStateOf("+8801798765432") }
    var companionRole by remember { mutableStateOf("WIFE") }

    var otpCode by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var generatedOtpNotice by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var timerSeconds by remember { mutableIntStateOf(60) }

    // Discrete Admin Login Dialog state
    var showAdminLoginDialog by remember { mutableStateOf(false) }
    var adminIdInput by remember { mutableStateOf("admin@kitchen.com") }
    var adminPasswordInput by remember { mutableStateOf("admin123") }
    var adminPasswordVisible by remember { mutableStateOf(false) }
    var adminLoginError by remember { mutableStateOf<String?>(null) }
    var isAdminVerifying by remember { mutableStateOf(false) }

    LaunchedEffect(isOtpSent) {
        if (isOtpSent) {
            timerSeconds = 60
            while (timerSeconds > 0) {
                delay(1000)
                timerSeconds -= 1
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // App Brand Logo & Cultural Branding
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(76.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            ) {
                Icon(
                    Icons.Default.Kitchen,
                    contentDescription = "Smart Ginni",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isBn) "স্মার্ট গিন্নি • Smart Ginni" else "Smart Ginni (স্মার্ট গিন্নি)",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (isBn) "ডিজিটাল গৃহিণী ও সংসার পরিচালিকা"
                else "Smart Household & Kitchen Companion",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (isBn) "পরিকল্পনা ও রূপায়নে: সুপর্ণা কান্তি দত্ত (৯৭৭৫৭০৮৭২২)"
                else "Handcrafted by Suparna Kanti Datta (9775708722)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Login / Register Tab (Note: Register is strictly for normal users)
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.testTag("auth_tab_row")
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                isOtpSent = false
                                errorMessage = null
                            },
                            text = { Text(if (isBn) "ব্যবহারকারী লগইন" else "User Login", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("login_tab_button")
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                isOtpSent = false
                                errorMessage = null
                            },
                            text = { Text(if (isBn) "ব্যবহারকারী নিবন্ধন" else "User Register", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("register_tab_button")
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    if (!isOtpSent) {
                        // Phone or Email Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = identifierType == "PHONE",
                                onClick = {
                                    identifierType = "PHONE"
                                    identifier = "+8801712345678"
                                },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, Modifier.size(16.dp)) },
                                label = { Text(if (isBn) "মোবাইল নম্বর" else "Phone") },
                                modifier = Modifier.testTag("phone_type_chip")
                            )
                            FilterChip(
                                selected = identifierType == "EMAIL",
                                onClick = {
                                    identifierType = "EMAIL"
                                    identifier = "user@kitchen.com"
                                },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, Modifier.size(16.dp)) },
                                label = { Text(if (isBn) "ইমেইল" else "Email") },
                                modifier = Modifier.testTag("email_type_chip")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (selectedTab == 1) {
                            // Register Name (strictly creates user)
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text(if (isBn) "আপনার নাম" else "Your Full Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_name_input")
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Optional Companion (Husband / Wife) setup
                            Text(
                                text = if (isBn) "সহচর ডিভাইস (স্বামী / স্ত্রী) লিংক করুন:" else "Link Companion (Husband / Wife):",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = companionId,
                                    onValueChange = { companionId = it },
                                    label = { Text(if (isBn) "সঙ্গীর ফোন/ইমেইল (ঐচ্ছিক)" else "Spouse Phone/Email (Optional)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1.3f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                FilterChip(
                                    selected = companionRole == "WIFE",
                                    onClick = { companionRole = if (companionRole == "WIFE") "HUSBAND" else "WIFE" },
                                    label = { Text(if (companionRole == "WIFE") "স্ত্রী 👩" else "স্বামী 👨", fontSize = 11.sp) },
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Mobile or Email field
                        OutlinedTextField(
                            value = identifier,
                            onValueChange = { identifier = it },
                            label = {
                                Text(
                                    if (identifierType == "PHONE") {
                                        if (isBn) "মোবাইল নম্বর" else "Mobile Number"
                                    } else {
                                        if (isBn) "ইমেইল ঠিকানা" else "Email Address"
                                    }
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    if (identifierType == "PHONE") Icons.Default.Phone else Icons.Default.Email,
                                    contentDescription = null
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = if (identifierType == "PHONE") KeyboardType.Phone else KeyboardType.Email
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_identifier_input")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (identifier.isNotBlank()) {
                                    val otp = viewModel.sendOtp(identifier)
                                    otpCode = otp
                                    isOtpSent = true
                                    generatedOtpNotice = if (isBn)
                                        "যাচাইকরণ ওটিপি: $otp (প্রবেশ করান)" else "OTP Code: $otp (Enter to verify)"
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("send_otp_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text = if (selectedTab == 1) {
                                    if (isBn) "নিবন্ধনের জন্য OTP পাঠান" else "Send OTP to Register"
                                } else {
                                    if (isBn) "লগইন OTP পাঠান" else "Send Login OTP"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    } else {
                        // OTP Screen
                        Text(
                            text = if (isBn) "ওটিপি (OTP) কোড লিখুন" else "Enter OTP Code",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isBn) "$identifier এ প্রেরিত ৬ সংখ্যার কোড দিন" else "Code sent to $identifier",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (generatedOtpNotice != null) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Security,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = generatedOtpNotice ?: "",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { if (it.length <= 6) otpCode = it },
                            label = { Text("6-Digit OTP") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("otp_code_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (timerSeconds > 0) "${timerSeconds}s" else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )

                            TextButton(
                                onClick = {
                                    val otp = viewModel.sendOtp(identifier)
                                    otpCode = otp
                                    timerSeconds = 60
                                    generatedOtpNotice = "New OTP: $otp"
                                },
                                enabled = timerSeconds == 0,
                                modifier = Modifier.testTag("resend_otp_button")
                            ) {
                                Text(if (isBn) "পুনরায় পাঠান" else "Resend")
                            }
                        }

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        Button(
                            onClick = {
                                if (selectedTab == 1) {
                                    // Complete Registration
                                    viewModel.registerUser(identifier, name, companionId, companionRole)
                                    onAuthSuccess()
                                } else {
                                    // User Login
                                    val (success, err) = viewModel.loginWithOtp(identifier, otpCode)
                                    if (success) {
                                        onAuthSuccess()
                                    } else {
                                        errorMessage = err
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("verify_otp_button")
                        ) {
                            Text(
                                text = if (selectedTab == 1) {
                                    if (isBn) "সেভ" else "Save"
                                } else {
                                    if (isBn) "যাচাই করে প্রবেশ করুন" else "Verify & Enter"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        TextButton(onClick = { isOtpSent = false }) {
                            Text(if (isBn) "নম্বর পরিবর্তন" else "Change Contact")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(14.dp))

                    // Auto login using linked Google account (Requirement 4)
                    Button(
                        onClick = {
                            viewModel.autoLoginWithGoogleAccount()
                            onAuthSuccess()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("google_auto_login_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "গুগল অ্যাকাউন্ট দিয়ে সরাসরি প্রবেশ" else "Auto Login with Linked Google Account",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 1-Click Quick Demo User login
                    OutlinedButton(
                        onClick = {
                            viewModel.loginWithOtp("+8801712345678", "123456")
                            onAuthSuccess()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("demo_user_login_button")
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBn) "ডেমো ব্যবহারকারী হিসেবে প্রবেশ" else "Quick 1-Click User Demo Login")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Separate Discrete Admin Portal Access (Admin login is NOT for all!)
                    TextButton(
                        onClick = {
                            showAdminLoginDialog = true
                            adminLoginError = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_portal_access_button")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, Modifier.size(15.dp), tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "🔐 গোপন এডমিন পোর্টাল প্রবেশ (প্রশাসক মাত্র)" else "🔐 Admin Portal Secure Login",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }

    // Protected Admin Login Dialog
    if (showAdminLoginDialog) {
        AlertDialog(
            onDismissRequest = { showAdminLoginDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "এডমিন লগইন (অনুমোদিত ব্যক্তিদের জন্য)" else "Admin Secure Portal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isBn) "গুগল শিট ও ডাটাবেস নিয়ন্ত্রণের জন্য এডমিন আইডি ও পাসওয়ার্ড লিখুন:"
                        else "Enter Admin User ID and Password saved in Google Sheets / Database:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    OutlinedTextField(
                        value = adminIdInput,
                        onValueChange = { adminIdInput = it },
                        label = { Text("Admin User ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_id_input")
                    )

                    OutlinedTextField(
                        value = adminPasswordInput,
                        onValueChange = { adminPasswordInput = it },
                        label = { Text("Admin Password") },
                        singleLine = true,
                        visualTransformation = if (adminPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { adminPasswordVisible = !adminPasswordVisible }) {
                                Icon(
                                    if (adminPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("admin_password_input")
                    )

                    if (adminLoginError != null) {
                        Text(
                            text = adminLoginError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isAdminVerifying = true
                            val isValid = viewModel.verifyAdminCredentials(adminIdInput, adminPasswordInput)
                            isAdminVerifying = false
                            if (isValid) {
                                showAdminLoginDialog = false
                                onAuthSuccess()
                            } else {
                                adminLoginError = if (isBn) "ভুল এডমিন আইডি বা পাসওয়ার্ড!" else "Invalid Admin ID or Password!"
                            }
                        }
                    },
                    modifier = Modifier.testTag("admin_login_submit_button")
                ) {
                    if (isAdminVerifying) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text(if (isBn) "এডমিন প্রবেশ" else "Login as Admin")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminLoginDialog = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        )
    }
}
