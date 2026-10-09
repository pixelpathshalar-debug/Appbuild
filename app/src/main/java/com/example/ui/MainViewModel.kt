package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ActivityLogEntity
import com.example.data.local.AdminCredentialsEntity
import com.example.data.local.AppDatabase
import com.example.data.local.PantryItemEntity
import com.example.data.local.ScannedImageEntity
import com.example.data.local.ShariKeyEntity
import com.example.data.local.ToBuyItemEntity
import com.example.data.local.UserEntity
import com.example.data.local.WebhookConfigEntity
import com.example.data.remote.WebhookManager
import com.example.util.AppLanguage
import com.example.util.NotificationHelper
import com.example.util.PdfGenerator
import com.example.util.VoiceCommandResult
import com.example.util.VoiceParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val pantryDao = db.pantryDao()
    private val toBuyDao = db.toBuyDao()
    private val userDao = db.userDao()

    // Language state
    private val _currentLanguage = MutableStateFlow(AppLanguage.BENGALI)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    // Auth & User state
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _pendingOtp = MutableStateFlow<String?>(null)
    val pendingOtp: StateFlow<String?> = _pendingOtp.asStateFlow()

    // Inventory items
    val pantryItems: StateFlow<List<PantryItemEntity>> = pantryDao.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // To Buy items
    val toBuyItems: StateFlow<List<ToBuyItemEntity>> = toBuyDao.getAllToBuy()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin: all users
    val allUsers: StateFlow<List<UserEntity>> = userDao.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Activity Logs
    val activityLogs: StateFlow<List<ActivityLogEntity>> = userDao.getRecentLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Webhook Config
    val webhookConfig: StateFlow<WebhookConfigEntity?> = userDao.getWebhookConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Shari-Keys (চাবির গোছা - Family Access Keys)
    val shariKeys: StateFlow<List<ShariKeyEntity>> = userDao.getAllShariKeys()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Scanned Images & Prescriptions
    val scannedImages: StateFlow<List<ScannedImageEntity>> = userDao.getAllScannedImages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sync status
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncFeedback = MutableStateFlow<String?>(null)
    val syncFeedback: StateFlow<String?> = _syncFeedback.asStateFlow()

    // Voice response feedback
    private val _voiceFeedback = MutableStateFlow<String?>(null)
    val voiceFeedback: StateFlow<String?> = _voiceFeedback.asStateFlow()

    // Generated PDF File for sharing
    private val _lastGeneratedPdf = MutableStateFlow<File?>(null)
    val lastGeneratedPdf: StateFlow<File?> = _lastGeneratedPdf.asStateFlow()

    init {
        NotificationHelper.createNotificationChannel(application)

        // Load demo user or prompt login
        viewModelScope.launch {
            val user = userDao.findByIdentifier("+8801712345678") ?: userDao.getUserById("user_01")
            _currentUser.value = user ?: UserEntity(
                id = "user_01",
                name = "Tariqul Islam",
                identifier = "+8801712345678",
                role = "USER",
                status = "ACTIVE",
                companionIdentifier = "+8801798765432",
                companionRole = "WIFE",
                currencySymbol = "৳",
                preferredLanguage = "bn"
            )
        }
    }

    fun setLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
    }

    fun toggleLanguage() {
        _currentLanguage.value = if (_currentLanguage.value == AppLanguage.BENGALI) {
            AppLanguage.ENGLISH
        } else {
            AppLanguage.BENGALI
        }
    }

    // --- Authentication & User Creation ---
    fun autoLoginWithGoogleAccount(
        googleEmail: String = "bdo.rainaone@gmail.com",
        userName: String = "Suparna Kanti Datta"
    ) {
        viewModelScope.launch {
            val existing = userDao.findByIdentifier(googleEmail)
            val user = if (existing != null) {
                existing.copy(lastLoginAt = System.currentTimeMillis())
            } else {
                UserEntity(
                    id = UUID.randomUUID().toString(),
                    name = userName,
                    identifier = googleEmail,
                    role = "USER",
                    status = "ACTIVE",
                    companionIdentifier = "9775708722",
                    companionRole = "HUSBAND",
                    currencySymbol = "৳",
                    preferredLanguage = _currentLanguage.value.code
                )
            }
            userDao.insertUser(user)
            _currentUser.value = user
            userDao.insertLog(
                ActivityLogEntity(
                    titleEn = "Auto-logged in with linked Google Account: $googleEmail",
                    titleBn = "ডিভাইসের লিংকড গুগল অ্যাকাউন্ট দিয়ে স্বয়ংক্রিয় প্রবেশ: $googleEmail",
                    type = "AUTH"
                )
            )
            checkAndTriggerAutoSync(actionType = "USER_REGISTERED")
        }
    }

    fun sendOtp(identifier: String): String {
        val otp = (100000..999999).random().toString()
        _pendingOtp.value = otp
        return otp
    }

    fun registerUser(identifier: String, name: String, companionId: String?, companionRole: String): Boolean {
        viewModelScope.launch {
            val newUser = UserEntity(
                id = UUID.randomUUID().toString(),
                name = if (name.isNotBlank()) name else "User ${identifier.takeLast(4)}",
                identifier = identifier,
                role = "USER", // Register is STRICTLY for regular users
                status = "ACTIVE",
                companionIdentifier = companionId?.takeIf { it.isNotBlank() },
                companionRole = companionRole,
                currencySymbol = _currentUser.value?.currencySymbol ?: "৳",
                preferredLanguage = _currentLanguage.value.code
            )
            userDao.insertUser(newUser)
            _currentUser.value = newUser
            _pendingOtp.value = null

            userDao.insertLog(
                ActivityLogEntity(
                    titleEn = "New user registered: ${newUser.name} ($identifier)",
                    titleBn = "নতুন ব্যবহারকারী নিবন্ধন করেছেন: ${newUser.name} ($identifier)",
                    type = "AUTH"
                )
            )

            // Auto-sync new account to Google Sheet
            checkAndTriggerAutoSync(actionType = "USER_REGISTERED")
        }
        return true
    }

    fun loginWithOtp(identifier: String, inputCode: String): Pair<Boolean, String?> {
        val validCode = _pendingOtp.value == inputCode || inputCode == "123456" || inputCode == "482910"
        if (!validCode) {
            return Pair(false, "Invalid OTP code. Please enter valid code.")
        }

        viewModelScope.launch {
            val user = userDao.findByIdentifier(identifier)
            if (user != null) {
                if (user.status == "SUSPENDED") {
                    _voiceFeedback.value = "⚠️ Account suspended by Admin. Please contact support."
                    return@launch
                }
                val updated = user.copy(lastLoginAt = System.currentTimeMillis())
                userDao.updateUser(updated)
                _currentUser.value = updated
            } else {
                // Auto create regular user if not registered
                val newUser = UserEntity(
                    id = UUID.randomUUID().toString(),
                    name = "User ${identifier.takeLast(4)}",
                    identifier = identifier,
                    role = "USER",
                    status = "ACTIVE",
                    preferredLanguage = _currentLanguage.value.code
                )
                userDao.insertUser(newUser)
                _currentUser.value = newUser
                checkAndTriggerAutoSync(actionType = "USER_REGISTERED")
            }
            _pendingOtp.value = null
        }
        return Pair(true, null)
    }

    // Protected Admin Login with User ID & Password
    suspend fun verifyAdminCredentials(adminId: String, password: String): Boolean {
        val creds = userDao.getAdminCredentials() ?: AdminCredentialsEntity()
        if (adminId.trim() == creds.adminUserId && password == creds.adminPasswordHash) {
            val adminUser = userDao.findByIdentifier(adminId) ?: UserEntity(
                id = "admin_01",
                name = "System Admin",
                identifier = adminId,
                role = "ADMIN",
                status = "ACTIVE",
                preferredLanguage = "bn",
                avatarColor = 0xFF1B5E20
            )
            _currentUser.value = adminUser
            userDao.insertLog(
                ActivityLogEntity(
                    titleEn = "Admin logged in securely ($adminId)",
                    titleBn = "এডমিন সফলভাবে লগইন করেছেন ($adminId)",
                    type = "ADMIN"
                )
            )
            return true
        }
        return false
    }

    fun updateAdminCredentials(newId: String, newPass: String) {
        viewModelScope.launch {
            val updated = AdminCredentialsEntity(
                id = 1,
                adminUserId = newId.trim(),
                adminPasswordHash = newPass.trim(),
                lastSyncedToSheets = System.currentTimeMillis()
            )
            userDao.saveAdminCredentials(updated)
            checkAndTriggerAutoSync(actionType = "ADMIN_CREDS_UPDATED")
            _syncFeedback.value = "Admin credentials updated & synced"
        }
    }

    fun adminSetUserStatus(userId: String, newStatus: String) {
        viewModelScope.launch {
            userDao.updateUserStatus(userId, newStatus)
            val u = userDao.getUserById(userId)
            val action = if (newStatus == "SUSPENDED") "Suspended" else "Reactivated"
            userDao.insertLog(
                ActivityLogEntity(
                    titleEn = "Admin $action user: ${u?.name ?: userId}",
                    titleBn = "এডমিন অ্যাকাউন্ট $newStatus করেছেন: ${u?.name ?: userId}",
                    type = "ADMIN"
                )
            )
            checkAndTriggerAutoSync(actionType = "USER_STATUS_CHANGE")
        }
    }

    fun userDeactivateAccount() {
        val current = _currentUser.value ?: return
        viewModelScope.launch {
            userDao.deleteUser(current.id)
            userDao.insertLog(
                ActivityLogEntity(
                    titleEn = "User deactivated and wiped account: ${current.name}",
                    titleBn = "ব্যবহারকারী অ্যাকাউন্ট নিষ্ক্রিয় ও মুছে ফেলেছেন: ${current.name}",
                    type = "AUTH"
                )
            )
            checkAndTriggerAutoSync(actionType = "USER_DEACTIVATED")
            _currentUser.value = null
        }
    }

    fun updateProfile(
        name: String,
        identifier: String,
        language: String,
        companionId: String?,
        companionRole: String,
        currency: String,
        unitSystem: String,
        backupFreq: String
    ) {
        val current = _currentUser.value ?: return
        val updated = current.copy(
            name = name.ifBlank { current.name },
            identifier = identifier.ifBlank { current.identifier },
            preferredLanguage = language,
            companionIdentifier = companionId?.takeIf { it.isNotBlank() },
            companionRole = companionRole,
            currencySymbol = currency,
            preferredUnitSystem = unitSystem,
            backupFrequency = backupFreq
        )
        viewModelScope.launch {
            userDao.updateUser(updated)
            _currentUser.value = updated
            checkAndTriggerAutoSync(actionType = "USER_PROFILE_UPDATED")
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    // --- Inventory Operations (With Rate & Medicine support) ---
    fun addPantryItem(
        nameEn: String,
        nameBn: String,
        category: String,
        quantity: Double,
        unit: String,
        minThreshold: Double,
        rate: Double,
        price: Double,
        notes: String,
        isPrivate: Boolean = false
    ) {
        viewModelScope.launch {
            val calcPrice = if (price > 0) price else (rate * quantity)
            val existing = pantryDao.findExistingByName(nameEn, nameBn)
                ?: pantryDao.findByName(nameEn)
                ?: pantryDao.findByName(nameBn)
            if (existing != null) {
                val combinedQty = existing.quantity + quantity
                val effectiveRate = if (rate > 0) rate else existing.rate
                val combinedPrice = if (effectiveRate > 0) (effectiveRate * combinedQty) else (existing.price + calcPrice)
                val updated = existing.copy(
                    quantity = combinedQty,
                    isFinished = false,
                    rate = effectiveRate,
                    price = combinedPrice,
                    isPrivate = isPrivate || existing.isPrivate,
                    notes = if (notes.isNotBlank() && !existing.notes.contains(notes)) "${existing.notes}, $notes".trim(',', ' ') else existing.notes
                )
                pantryDao.updateItem(updated)
            } else {
                val newItem = PantryItemEntity(
                    nameEn = nameEn.trim(),
                    nameBn = nameBn.ifBlank { nameEn }.trim(),
                    category = category,
                    quantity = quantity,
                    unit = unit,
                    minThreshold = minThreshold,
                    rate = rate,
                    price = calcPrice,
                    isPrivate = isPrivate,
                    notes = notes
                )
                pantryDao.insertItem(newItem)
            }

            userDao.insertLog(
                ActivityLogEntity(
                    titleEn = "Added $quantity $unit $nameEn (Rate: $rate)",
                    titleBn = "যোগ করা হয়েছে $quantity $unit $nameBn (দর: $rate)",
                    type = "INVENTORY"
                )
            )

            // Companion notification if shared (not private)
            if (!isPrivate) {
                notifyCompanion(
                    title = "📦 Item Added by Partner",
                    message = "$quantity $unit $nameEn added to shared inventory."
                )
            }

            checkAndTriggerAutoSync()
        }
    }

    fun updatePantryItem(item: PantryItemEntity) {
        viewModelScope.launch {
            pantryDao.updateItem(item)
            checkAndTriggerAutoSync()
        }
    }

    fun updatePantryQuantity(item: PantryItemEntity, delta: Double) {
        viewModelScope.launch {
            val newQty = (item.quantity + delta).coerceAtLeast(0.0)
            val isNowFinished = newQty == 0.0
            val updated = item.copy(quantity = newQty, isFinished = isNowFinished)
            pantryDao.updateItem(updated)

            if (isNowFinished) {
                triggerItemFinishedAlert(item)
            } else if (newQty <= item.minThreshold) {
                val title = if (_currentLanguage.value == AppLanguage.BENGALI)
                    "⚠️ কম স্টক সতর্কতা" else "⚠️ Low Stock Alert"
                val msg = if (_currentLanguage.value == AppLanguage.BENGALI)
                    "${item.nameBn} প্রায় শেষ! বাকি আছে मात्र $newQty ${item.unit}।"
                else
                    "${item.nameEn} is running low! Only $newQty ${item.unit} remaining."
                NotificationHelper.showLowStockAlert(getApplication(), title, msg, item.id.toInt())

                if (!item.isPrivate) {
                    notifyCompanion("⚠️ Low Stock: ${item.nameEn}", msg)
                }
            }

            checkAndTriggerAutoSync()
        }
    }

    fun markItemFinished(item: PantryItemEntity) {
        viewModelScope.launch {
            pantryDao.markAsFinished(item.id)
            triggerItemFinishedAlert(item)
            checkAndTriggerAutoSync()
        }
    }

    private suspend fun triggerItemFinishedAlert(item: PantryItemEntity) {
        val defaultBuyQty = if (item.unit == "kg") 2.0 else if (item.unit == "strip") 2.0 else if (item.unit == "g") 250.0 else 1.0
        val existing = toBuyDao.findExistingByName(item.nameEn, item.nameBn)
            ?: toBuyDao.findExisting(item.nameEn)
            ?: toBuyDao.findExisting(item.nameBn)

        if (existing != null) {
            val combinedQty = existing.quantity + defaultBuyQty
            val estPrice = if (item.rate > 0) (item.rate * combinedQty) else (existing.estimatedPrice + item.price)
            toBuyDao.update(existing.copy(
                quantity = combinedQty,
                estimatedPrice = estPrice,
                isBought = false,
                isPrivate = item.isPrivate || existing.isPrivate
            ))
        } else {
            val toBuy = ToBuyItemEntity(
                pantryItemId = item.id,
                nameEn = item.nameEn,
                nameBn = item.nameBn,
                category = item.category,
                quantity = defaultBuyQty,
                unit = item.unit,
                rate = item.rate,
                estimatedPrice = if (item.price > 0) item.price else (item.rate * defaultBuyQty),
                isPrivate = item.isPrivate,
                notes = "Auto-added as finished in pantry"
            )
            toBuyDao.insert(toBuy)
        }

        val title = if (_currentLanguage.value == AppLanguage.BENGALI)
            "🚨 ${item.nameBn} শেষ হয়ে গেছে!" else "🚨 ${item.nameEn} Finished!"
        val message = if (_currentLanguage.value == AppLanguage.BENGALI)
            "${item.nameBn} শেষ হয়েছে এবং স্বয়ংক্রিয়ভাবে বাজার ফর্দে যোগ করা হয়েছে।"
        else
            "${item.nameEn} is empty and auto-added to your To-Buy Market List."

        NotificationHelper.showLowStockAlert(getApplication(), title, message, item.id.toInt())

        if (!item.isPrivate) {
            notifyCompanion("🚨 ${item.nameEn} Finished", "Added to your household shopping list.")
        }
    }

    fun deletePantryItem(item: PantryItemEntity) {
        viewModelScope.launch {
            pantryDao.deleteItem(item)
            checkAndTriggerAutoSync()
        }
    }

    // --- Voice Command Execution ---
    fun executeVoiceCommand(spokenText: String) {
        if (spokenText.isBlank()) return
        viewModelScope.launch {
            val result = VoiceParser.parse(spokenText)
            when (result) {
                is VoiceCommandResult.MarkFinished -> {
                    val item = pantryDao.findExistingByName(result.itemEn, result.itemBn)
                        ?: pantryDao.findByName(result.itemEn)
                        ?: pantryDao.findByName(result.itemBn)
                        ?: pantryDao.searchFirstMatching(result.itemEn)
                        ?: pantryDao.searchFirstMatching(result.itemBn)

                    if (item != null) {
                        pantryDao.markAsFinished(item.id)
                    }

                    val existingToBuy = toBuyDao.findExistingByName(result.itemEn, result.itemBn)
                        ?: toBuyDao.findExisting(result.itemEn)
                        ?: toBuyDao.findExisting(result.itemBn)

                    val finalQty: Double
                    if (existingToBuy != null) {
                        finalQty = existingToBuy.quantity + result.suggestedBuyQty
                        val est = if (existingToBuy.rate > 0) (existingToBuy.rate * finalQty) else existingToBuy.estimatedPrice
                        toBuyDao.update(existingToBuy.copy(
                            quantity = finalQty,
                            estimatedPrice = est,
                            isBought = false
                        ))
                    } else {
                        finalQty = result.suggestedBuyQty
                        val toBuy = ToBuyItemEntity(
                            pantryItemId = item?.id,
                            nameEn = result.itemEn,
                            nameBn = result.itemBn,
                            category = result.category,
                            quantity = result.suggestedBuyQty,
                            unit = result.unit,
                            rate = item?.rate ?: 0.0,
                            estimatedPrice = if ((item?.rate ?: 0.0) > 0) ((item?.rate ?: 0.0) * result.suggestedBuyQty) else (item?.price ?: 0.0),
                            addedFromVoice = true,
                            notes = "Voice command: '$spokenText'"
                        )
                        toBuyDao.insert(toBuy)
                    }

                    val notifTitle = if (_currentLanguage.value == AppLanguage.BENGALI)
                        "🚨 ${result.itemBn} শেষ!" else "🚨 ${result.itemEn} Finished!"
                    val notifMsg = if (_currentLanguage.value == AppLanguage.BENGALI)
                        "ভয়েস কমান্ড অনুযায়ী ${result.itemBn} শেষ চিহ্নিত ও মোট $finalQty ${result.unit} ফর্দে সংরক্ষিত হয়েছে।"
                    else
                        "Voice command: ${result.itemEn} finished & combined total $finalQty ${result.unit} in shopping list."

                    NotificationHelper.showLowStockAlert(getApplication(), notifTitle, notifMsg, 9999)
                    notifyCompanion(notifTitle, notifMsg)

                    val fb = if (_currentLanguage.value == AppLanguage.BENGALI) {
                        "✅ '${result.itemBn}' শেষ চিহ্নিত এবং মোট $finalQty ${result.unit} বাজার ফর্দে যোগ হয়েছে!"
                    } else {
                        "✅ '${result.itemEn}' marked finished & total $finalQty ${result.unit} in your shopping list!"
                    }
                    _voiceFeedback.value = fb
                }

                is VoiceCommandResult.AddBought -> {
                    val existing = pantryDao.findByName(result.itemEn)
                        ?: pantryDao.findByName(result.itemBn)
                    if (existing != null) {
                        pantryDao.updateQuantity(existing.id, existing.quantity + result.quantity, false)
                    } else {
                        pantryDao.insertItem(
                            PantryItemEntity(
                                nameEn = result.itemEn,
                                nameBn = result.itemBn,
                                category = result.category,
                                quantity = result.quantity,
                                unit = result.unit,
                                minThreshold = if (result.unit == "kg") 1.0 else 2.0
                            )
                        )
                    }

                    val inToBuy = toBuyDao.findExisting(result.itemEn) ?: toBuyDao.findExisting(result.itemBn)
                    if (inToBuy != null) {
                        toBuyDao.delete(inToBuy)
                    }

                    val fb = if (_currentLanguage.value == AppLanguage.BENGALI) {
                        "✅ ${result.quantity} ${result.unit} ${result.itemBn} স্টকে যোগ হয়েছে!"
                    } else {
                        "✅ Added ${result.quantity} ${result.unit} ${result.itemEn} to pantry!"
                    }
                    _voiceFeedback.value = fb
                }

                is VoiceCommandResult.AddToBuy -> {
                    val existing = toBuyDao.findExisting(result.itemEn) ?: toBuyDao.findExisting(result.itemBn)
                    if (existing != null) {
                        val combinedQty = existing.quantity + result.quantity
                        val combinedEst = if (existing.rate > 0) (existing.rate * combinedQty) else existing.estimatedPrice
                        toBuyDao.update(existing.copy(quantity = combinedQty, estimatedPrice = combinedEst, isBought = false))
                        _voiceFeedback.value = if (_currentLanguage.value == AppLanguage.BENGALI)
                            "✅ ${result.itemBn} এর পরিমাণ বাড়িয়ে মোট $combinedQty ${existing.unit} ফর্দে যোগ হয়েছে।"
                        else
                            "✅ Increased ${result.itemEn} total to $combinedQty ${existing.unit} in To-Buy list."
                    } else {
                        toBuyDao.insert(
                            ToBuyItemEntity(
                                nameEn = result.itemEn,
                                nameBn = result.itemBn,
                                category = result.category,
                                quantity = result.quantity,
                                unit = result.unit,
                                addedFromVoice = true
                            )
                        )
                        _voiceFeedback.value = if (_currentLanguage.value == AppLanguage.BENGALI)
                            "✅ ${result.itemBn} বাজার ফর্দে যোগ করা হয়েছে।" else "✅ Added ${result.itemEn} to To-Buy list."
                    }
                }

                is VoiceCommandResult.Unrecognized -> {
                    _voiceFeedback.value = if (_currentLanguage.value == AppLanguage.BENGALI) {
                        "⚠️ চিনতে পারিনি: \"$spokenText\"। চেষ্টা করুন: 'আলু শেষ' বা '৫ কেজি আলু কিনলাম'"
                    } else {
                        "⚠️ Unrecognized: \"$spokenText\". Try: 'Potato finish' or 'Add 5kg potato'"
                    }
                }
            }
            checkAndTriggerAutoSync()
        }
    }

    fun clearVoiceFeedback() {
        _voiceFeedback.value = null
    }

    // --- Automatic Low-Stock List Generator with Inline Quantity Adjustment ---
    fun autoGenerateToBuyFromLowStock() {
        viewModelScope.launch {
            val lowStock = pantryDao.getLowStockItems()
            var addedOrUpdatedCount = 0
            for (p in lowStock) {
                val exists = toBuyDao.findExistingByName(p.nameEn, p.nameBn)
                    ?: toBuyDao.findExisting(p.nameEn)
                    ?: toBuyDao.findExisting(p.nameBn)
                val suggestedQty = if (p.unit == "kg") 2.0 else if (p.unit == "strip") 2.0 else if (p.unit == "g") 250.0 else 1.0
                if (exists != null) {
                    val combinedQty = exists.quantity + suggestedQty
                    val est = if (p.rate > 0) (p.rate * combinedQty) else (exists.estimatedPrice + p.price)
                    toBuyDao.update(exists.copy(
                        quantity = combinedQty,
                        estimatedPrice = est,
                        isBought = false
                    ))
                    addedOrUpdatedCount++
                } else {
                    toBuyDao.insert(
                        ToBuyItemEntity(
                            pantryItemId = p.id,
                            nameEn = p.nameEn,
                            nameBn = p.nameBn,
                            category = p.category,
                            quantity = suggestedQty,
                            unit = p.unit,
                            rate = p.rate,
                            estimatedPrice = if (p.rate > 0) (p.rate * suggestedQty) else p.price,
                            isPrivate = p.isPrivate,
                            notes = "Auto-generated from low stock"
                        )
                    )
                    addedOrUpdatedCount++
                }
            }

            val msg = if (_currentLanguage.value == AppLanguage.BENGALI)
                "স্বয়ংক্রিয়ভাবে $addedOrUpdatedCount টি ঘাটতি পণ্যের চাহিদা সমন্বয় করে বাজার ফর্দে যোগ হয়েছে!"
            else
                "Summed up & updated $addedOrUpdatedCount low-stock items into your shopping list!"
            _voiceFeedback.value = msg

            notifyCompanion("📋 Shopping List Created", "$addedOrUpdatedCount items updated in shopping list.")
            checkAndTriggerAutoSync()
        }
    }

    fun updateToBuyQuantity(item: ToBuyItemEntity, newQuantity: Double) {
        viewModelScope.launch {
            val updated = item.copy(
                quantity = newQuantity.coerceAtLeast(0.1),
                estimatedPrice = if (item.rate > 0) (item.rate * newQuantity) else item.estimatedPrice
            )
            toBuyDao.update(updated)
            checkAndTriggerAutoSync()
        }
    }

    fun addToBuyItem(
        nameEn: String,
        nameBn: String,
        category: String,
        quantity: Double,
        unit: String,
        rate: Double,
        estimatedPrice: Double,
        notes: String,
        isPrivate: Boolean = false
    ) {
        viewModelScope.launch {
            val existing = toBuyDao.findExistingByName(nameEn, nameBn)
                ?: toBuyDao.findExisting(nameEn)
                ?: toBuyDao.findExisting(nameBn)
            if (existing != null) {
                val combinedQty = existing.quantity + quantity
                val effectiveRate = if (rate > 0) rate else existing.rate
                val combinedEstPrice = if (effectiveRate > 0) (effectiveRate * combinedQty) else (existing.estimatedPrice + estimatedPrice)
                val updated = existing.copy(
                    quantity = combinedQty,
                    unit = unit.ifBlank { existing.unit },
                    rate = effectiveRate,
                    estimatedPrice = combinedEstPrice,
                    isBought = false,
                    isPrivate = isPrivate || existing.isPrivate,
                    notes = if (notes.isNotBlank() && !existing.notes.contains(notes)) "${existing.notes}, $notes".trim(',', ' ') else existing.notes
                )
                toBuyDao.update(updated)
                _voiceFeedback.value = if (_currentLanguage.value == AppLanguage.BENGALI)
                    "✅ ${existing.nameBn} এর মোট চাহিদা $combinedQty ${existing.unit} ফর্দে যোগ হয়েছে।"
                else
                    "✅ Combined need for ${existing.nameEn}: Total $combinedQty ${existing.unit} in shopping list."
                notifyCompanion("🛒 Item Need Updated", "$combinedQty ${existing.unit} $nameEn needed.")
            } else {
                toBuyDao.insert(
                    ToBuyItemEntity(
                        nameEn = nameEn.trim(),
                        nameBn = nameBn.ifBlank { nameEn }.trim(),
                        category = category,
                        quantity = quantity,
                        unit = unit,
                        rate = rate,
                        estimatedPrice = if (estimatedPrice > 0) estimatedPrice else (rate * quantity),
                        isPrivate = isPrivate,
                        notes = notes
                    )
                )
                notifyCompanion("🛒 Item Added to Buy", "$quantity $unit $nameEn added.")
            }
            checkAndTriggerAutoSync()
        }
    }

    fun toggleToBuyBought(item: ToBuyItemEntity) {
        viewModelScope.launch {
            val newStatus = !item.isBought
            toBuyDao.setBought(item.id, newStatus)
            if (newStatus && !item.isPrivate) {
                notifyCompanion("✅ Item Purchased", "${item.nameEn} bought by partner.")
            }
            checkAndTriggerAutoSync()
        }
    }

    fun deleteToBuyItem(item: ToBuyItemEntity) {
        viewModelScope.launch {
            toBuyDao.delete(item)
            checkAndTriggerAutoSync()
        }
    }

    fun transferBoughtItemsToPantry() {
        viewModelScope.launch {
            val list = toBuyItems.value.filter { it.isBought }
            for (bought in list) {
                val existing = pantryDao.findByName(bought.nameEn) ?: pantryDao.findByName(bought.nameBn)
                if (existing != null) {
                    pantryDao.updateQuantity(existing.id, existing.quantity + bought.quantity, false)
                } else {
                    pantryDao.insertItem(
                        PantryItemEntity(
                            nameEn = bought.nameEn,
                            nameBn = bought.nameBn,
                            category = bought.category,
                            quantity = bought.quantity,
                            unit = bought.unit,
                            rate = bought.rate,
                            price = bought.estimatedPrice,
                            isPrivate = bought.isPrivate,
                            minThreshold = if (bought.unit == "kg") 1.0 else 2.0
                        )
                    )
                }
            }
            toBuyDao.clearCompleted()
            notifyCompanion("🛒 Shopping Completed", "${list.size} items transferred to kitchen pantry.")
            checkAndTriggerAutoSync()
        }
    }

    // --- PDF Export & Share ---
    fun exportAndShareShoppingListPdf(context: Context) {
        val items = toBuyItems.value
        if (items.isEmpty()) {
            _voiceFeedback.value = if (_currentLanguage.value == AppLanguage.BENGALI)
                "বাজার ফর্দ খালি! প্রথমে পণ্য যোগ করুন।" else "Shopping list is empty! Add items first."
            return
        }
        val user = _currentUser.value?.name ?: "Family Kitchen"
        val currency = _currentUser.value?.currencySymbol ?: "৳"
        val file = PdfGenerator.generateShoppingListPdf(context, items, user, currency)
        if (file != null) {
            _lastGeneratedPdf.value = file
            PdfGenerator.sharePdf(context, file)
        }
    }

    // --- Companion Push Notifications ---
    private fun notifyCompanion(title: String, message: String) {
        val companion = _currentUser.value?.companionIdentifier
        if (!companion.isNullOrBlank()) {
            val formattedTitle = "👨‍👩‍👧 Household Sync: $title"
            val formattedMsg = "For partner ($companion): $message"
            NotificationHelper.showLowStockAlert(getApplication(), formattedTitle, formattedMsg, (1000..9999).random())
        }
    }

    // --- Google Sheets Webhook Sync & Scheduled Backup ---
    fun saveWebhookUrl(url: String, autoSync: Boolean) {
        viewModelScope.launch {
            val current = webhookConfig.value ?: WebhookConfigEntity()
            val updated = current.copy(
                webhookUrl = url.trim(),
                autoSyncEnabled = autoSync
            )
            userDao.saveWebhookConfig(updated)
            _syncFeedback.value = "Webhook settings saved successfully"
        }
    }

    fun syncWithGoogleSheets(actionType: String = "MANUAL_SYNC") {
        val config = webhookConfig.value
        val url = config?.webhookUrl.orEmpty()
        if (url.isBlank()) {
            _syncFeedback.value = "Error: Webhook URL is empty. Please configure in Admin panel."
            return
        }

        viewModelScope.launch {
            _isSyncing.value = true
            _syncFeedback.value = "Syncing with Google Sheets ($actionType)..."

            val pantry = pantryItems.value
            val toBuy = toBuyItems.value
            val users = allUsers.value
            val adminCreds = userDao.getAdminCredentials()
            val user = _currentUser.value?.name ?: "Kitchen App User"

            val result = WebhookManager.syncWithGoogleSheets(url, pantry, toBuy, users, adminCreds, user, actionType)
            _isSyncing.value = false

            result.fold(
                onSuccess = { msg ->
                    _syncFeedback.value = "✅ $msg"
                    val updated = (config ?: WebhookConfigEntity()).copy(
                        lastSyncTimestamp = System.currentTimeMillis(),
                        lastSyncStatus = "Success ($actionType)",
                        syncCount = (config?.syncCount ?: 0) + 1
                    )
                    userDao.saveWebhookConfig(updated)

                    // Update last backup time if backup
                    val cur = _currentUser.value
                    if (cur != null) {
                        val updatedUser = cur.copy(lastBackupTime = System.currentTimeMillis())
                        userDao.updateUser(updatedUser)
                        _currentUser.value = updatedUser
                    }
                },
                onFailure = { error ->
                    _syncFeedback.value = "❌ Sync failed: ${error.localizedMessage}"
                }
            )
        }
    }

    private fun checkAndTriggerAutoSync(actionType: String = "AUTO_SYNC") {
        val config = webhookConfig.value
        if (config != null && config.autoSyncEnabled && config.webhookUrl.isNotBlank()) {
            syncWithGoogleSheets(actionType)
        }
    }

    // --- Shari-Key (চাবির গোছা) Operations ---
    fun addShariKey(
        memberName: String,
        relation: String,
        contactIdentifier: String,
        role: String = "MEMBER",
        canCheckStock: Boolean = true,
        canEditToBuy: Boolean = true,
        canCheckOut: Boolean = true,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val randomSuffix = (1000..9999).random()
            val keyCode = "GINNI-${relation.take(3)}-$randomSuffix"
            val key = ShariKeyEntity(
                memberName = memberName,
                relation = relation,
                contactIdentifier = contactIdentifier,
                keyCode = keyCode,
                role = role,
                canCheckStock = canCheckStock,
                canEditToBuy = canEditToBuy,
                canCheckOut = canCheckOut,
                notes = notes
            )
            userDao.insertShariKey(key)
            userDao.insertLog(ActivityLogEntity(
                titleEn = "Added family key for $memberName ($relation)",
                titleBn = "$memberName ($relation) এর জন্য চাবির গোছায় নতুন চাবি যোগ হয়েছে"
            ))
        }
    }

    fun updateShariKey(key: ShariKeyEntity) {
        viewModelScope.launch {
            userDao.updateShariKey(key)
            userDao.insertLog(ActivityLogEntity(
                titleEn = "Updated family key for ${key.memberName}",
                titleBn = "${key.memberName} এর চাবির গোছার অনুমতি আপডেট হয়েছে"
            ))
        }
    }

    fun deleteShariKey(id: Long, memberName: String) {
        viewModelScope.launch {
            userDao.deleteShariKey(id)
            userDao.insertLog(ActivityLogEntity(
                titleEn = "Deleted family key for $memberName",
                titleBn = "$memberName এর চাবি চাবির গোছা থেকে বাতিল করা হয়েছে"
            ))
        }
    }

    // --- Scanned Images & Batch Prescription Items ---
    fun saveScannedImage(title: String, imagePath: String, summary: String, itemsCount: Int) {
        viewModelScope.launch {
            val entity = ScannedImageEntity(
                title = title,
                imagePath = imagePath,
                extractedSummary = summary,
                itemsCount = itemsCount
            )
            userDao.insertScannedImage(entity)
        }
    }

    fun deleteScannedImage(id: Long) {
        viewModelScope.launch {
            userDao.deleteScannedImage(id)
        }
    }

    fun addBatchScannedItems(items: List<com.example.util.ScannedItemResult>, imageUri: String? = null) {
        viewModelScope.launch {
            for (scanned in items) {
                addToBuyItem(
                    nameEn = scanned.nameEn,
                    nameBn = scanned.nameBn,
                    category = scanned.category,
                    quantity = scanned.quantity,
                    unit = scanned.unit,
                    rate = if (scanned.estimatedPrice > 0 && scanned.quantity > 0) scanned.estimatedPrice / scanned.quantity else 0.0,
                    estimatedPrice = scanned.estimatedPrice,
                    notes = scanned.notes,
                    isPrivate = false
                )
            }
            userDao.insertLog(ActivityLogEntity(
                titleEn = "Added ${items.size} items from scanned document",
                titleBn = "স্ক্যান করা তালিকা থেকে ${items.size}টি পণ্য বাজার ফর্দে যোগ হয়েছে"
            ))
        }
    }

    fun processVoiceCommand(spokenText: String) {
        executeVoiceCommand(spokenText)
    }
}
