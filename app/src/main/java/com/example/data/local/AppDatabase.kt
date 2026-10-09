package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PantryItemEntity::class,
        ToBuyItemEntity::class,
        UserEntity::class,
        AdminCredentialsEntity::class,
        ActivityLogEntity::class,
        WebhookConfigEntity::class,
        ShariKeyEntity::class,
        ScannedImageEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pantryDao(): PantryDao
    abstract fun toBuyDao(): ToBuyDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kitchen_pantry_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getInstance(context)
                                seedInitialData(database)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(db: AppDatabase) {
            val starterItems = listOf(
                PantryItemEntity(
                    nameEn = "Potato",
                    nameBn = "আলু",
                    category = "VEGETABLES",
                    quantity = 5.0,
                    unit = "kg",
                    minThreshold = 1.0,
                    rate = 30.0,
                    price = 150.0,
                    notes = "Fresh red potatoes / লাল গোল আলু"
                ),
                PantryItemEntity(
                    nameEn = "Onion",
                    nameBn = "পেঁয়াজ",
                    category = "VEGETABLES",
                    quantity = 2.0,
                    unit = "kg",
                    minThreshold = 0.5,
                    rate = 80.0,
                    price = 160.0,
                    notes = "Deshi onion / দেশি পেঁয়াজ"
                ),
                PantryItemEntity(
                    nameEn = "Green Chili",
                    nameBn = "কাঁচা মরিচ",
                    category = "VEGETABLES",
                    quantity = 0.5,
                    unit = "kg",
                    minThreshold = 0.1,
                    rate = 120.0,
                    price = 60.0,
                    notes = "Fresh spicy chilies / তাজা মরিচ"
                ),
                PantryItemEntity(
                    nameEn = "Garlic",
                    nameBn = "রসুন",
                    category = "SPICES",
                    quantity = 250.0,
                    unit = "g",
                    minThreshold = 50.0,
                    rate = 0.22,
                    price = 55.0,
                    notes = "Deshi whole garlic / দেশি রসুন"
                ),
                PantryItemEntity(
                    nameEn = "Ginger",
                    nameBn = "আদা",
                    category = "SPICES",
                    quantity = 200.0,
                    unit = "g",
                    minThreshold = 50.0,
                    rate = 0.225,
                    price = 45.0,
                    notes = "Fresh root / তাজা আদা"
                ),
                PantryItemEntity(
                    nameEn = "Cumin Powder",
                    nameBn = "জিরা গুঁড়া",
                    category = "SPICES",
                    quantity = 100.0,
                    unit = "g",
                    minThreshold = 25.0,
                    rate = 0.8,
                    price = 80.0,
                    notes = "Roasted aroma / ভাজা জিরা"
                ),
                // Medicine items
                PantryItemEntity(
                    nameEn = "Paracetamol 500mg",
                    nameBn = "প্যারাসিটামল ৫০০ মি.গ্রা.",
                    category = "MEDICINE",
                    quantity = 10.0,
                    unit = "tablet",
                    minThreshold = 2.0,
                    rate = 1.2,
                    price = 12.0,
                    notes = "Fever and headache / জ্বর ও ব্যথা"
                ),
                PantryItemEntity(
                    nameEn = "Omeprazole 20mg",
                    nameBn = "ওমিপ্রাজল (গ্যাস্ট্রিকের ঔষধ)",
                    category = "MEDICINE",
                    quantity = 2.0,
                    unit = "strip",
                    minThreshold = 1.0,
                    rate = 50.0,
                    price = 100.0,
                    notes = "Before meal gastric relief / খাওয়ার পূর্বে"
                ),
                // Garments, Hardware & Household items
                PantryItemEntity(
                    nameEn = "Fresh Cow Milk",
                    nameBn = "তাজা গরুর দুধ",
                    category = "STAPLES",
                    quantity = 2.0,
                    unit = "L",
                    minThreshold = 0.5,
                    rate = 85.0,
                    price = 170.0,
                    notes = "Daily tea and breakfast / সকালের চা ও নাস্তা"
                ),
                PantryItemEntity(
                    nameEn = "Detergent Surf Excel",
                    nameBn = "সার্ফ এক্সেল ডিটারজেন্ট",
                    category = "HOUSEHOLD",
                    quantity = 1.0,
                    unit = "kg",
                    minThreshold = 0.25,
                    rate = 140.0,
                    price = 140.0,
                    notes = "Washing clothes / কাপড় ধোয়ার জন্য"
                ),
                PantryItemEntity(
                    nameEn = "LED Bulb 9W",
                    nameBn = "৯ ওয়াট এলইডি বাল্ব",
                    category = "HARDWARE",
                    quantity = 2.0,
                    unit = "pcs",
                    minThreshold = 1.0,
                    rate = 120.0,
                    price = 240.0,
                    notes = "Kitchen and dining spare / খাবার ঘরের অতিরিক্ত বাতি"
                ),
                PantryItemEntity(
                    nameEn = "Cotton Gamchha",
                    nameBn = "দেশি সুতি গামছা",
                    category = "GARMENTS",
                    quantity = 2.0,
                    unit = "pcs",
                    minThreshold = 1.0,
                    rate = 80.0,
                    price = 160.0,
                    notes = "Bathroom daily towel / গোসলের গামছা"
                )
            )
            db.pantryDao().insertItems(starterItems)

            // Seed initial admin credentials
            db.userDao().saveAdminCredentials(
                AdminCredentialsEntity(
                    id = 1,
                    adminUserId = "admin@kitchen.com",
                    adminPasswordHash = "admin123"
                )
            )

            // Seed initial admin & demo user
            val adminUser = UserEntity(
                id = "admin_01",
                name = "System Admin",
                identifier = "admin@kitchen.com",
                role = "ADMIN",
                status = "ACTIVE",
                preferredLanguage = "bn",
                avatarColor = 0xFF1B5E20
            )
            val demoUser = UserEntity(
                id = "user_01",
                name = "Tariqul Islam",
                identifier = "+8801712345678",
                role = "USER",
                status = "ACTIVE",
                companionIdentifier = "+8801798765432",
                companionRole = "WIFE",
                currencySymbol = "৳",
                preferredLanguage = "bn",
                avatarColor = 0xFFD84315
            )
            db.userDao().insertUser(adminUser)
            db.userDao().insertUser(demoUser)

            // Seed initial Webhook Config
            db.userDao().saveWebhookConfig(
                WebhookConfigEntity(
                    id = 1,
                    webhookUrl = "https://script.google.com/macros/s/AKfycbx_DEMO_KITCHEN_APP_WEBHOOK/exec",
                    autoSyncEnabled = true,
                    lastSyncStatus = "Ready for connection"
                )
            )

            // Seed initial Shari-Keys (Family access keys)
            db.userDao().insertShariKey(
                ShariKeyEntity(
                    memberName = "মা (Mother)",
                    relation = "MOTHER",
                    contactIdentifier = "9775708722",
                    keyCode = "GINNI-MOM-8722",
                    role = "MANAGER",
                    canCheckStock = true,
                    canEditToBuy = true,
                    canCheckOut = true,
                    notes = "প্রধান গৃহিণী অ্যাক্সেস"
                )
            )
            db.userDao().insertShariKey(
                ShariKeyEntity(
                    memberName = "স্বামী (Husband)",
                    relation = "SPOUSE",
                    contactIdentifier = "+8801798765432",
                    keyCode = "GINNI-SPOUSE-4421",
                    role = "BUYER",
                    canCheckStock = true,
                    canEditToBuy = true,
                    canCheckOut = true,
                    notes = "বাজার কেনাকাটা অ্যাক্সেস"
                )
            )

            db.userDao().insertLog(
                ActivityLogEntity(
                    titleEn = "System initialized with vegetables, spices, household items & Shari-Keys",
                    titleBn = "শাকসবজি, মসলা, গৃহস্থালি পণ্য ও চাবির গোছা দিয়ে ডাটাবেজ প্রস্তুত হয়েছে",
                    type = "INFO"
                )
            )
        }
    }
}
