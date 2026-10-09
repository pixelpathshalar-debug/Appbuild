package com.example.util

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    BENGALI("bn", "Bengali", "বাংলা")
}

object Strings {
    // App Branding & Identity
    const val APP_TITLE_EN = "Smart Ginni"
    const val APP_TITLE_BN = "স্মার্ট গিন্নি"
    const val APP_SUBTITLE_EN = "Smart Household & Kitchen Companion"
    const val APP_SUBTITLE_BN = "ডিজিটাল গৃহিণী ও সংসার পরিচালিকা"
    const val BRANDING_EN = "Handcrafted by Suparna Kanti Datta (9775708722)"
    const val BRANDING_BN = "পরিকল্পনা ও রূপায়নে: সুপর্ণা কান্তি দত্ত (৯৭৭৫৭০৮৭২২)"

    // Cultural Feature Tab Names
    const val TAB_VAULT_EN = "Ginnir Vault"
    const val TAB_VAULT_BN = "গিন্নির ভল্ট"

    const val TAB_FARD_EN = "Bajarer Fardo"
    const val TAB_FARD_BN = "বাজারের ফর্দ"

    const val TAB_HISHAB_EN = "Ginnir Hishab"
    const val TAB_HISHAB_BN = "গিন্নির হিসাব"

    const val TAB_SHARI_KEY_EN = "Shari-Key"
    const val TAB_SHARI_KEY_BN = "চাবির গোছা"

    const val TAB_MARKET_EN = "Bazaar Guide"
    const val TAB_MARKET_BN = "বাজার গাইড"

    const val TAB_PROFILE_EN = "Profile"
    const val TAB_PROFILE_BN = "প্রোফাইল"

    const val TAB_ADMIN_EN = "Admin Portal"
    const val TAB_ADMIN_BN = "এডমিন"

    // Sassy Ginni Alerts
    const val GINNI_ALERT_TITLE_EN = "👑 Ginni Alert!"
    const val GINNI_ALERT_TITLE_BN = "👑 গিন্নি অ্যালার্ট!"

    // Categories
    const val CAT_ALL_EN = "All Items"
    const val CAT_ALL_BN = "সব পণ্য"

    const val CAT_VEGETABLES_EN = "Vegetables"
    const val CAT_VEGETABLES_BN = "শাকসবজি"

    const val CAT_SPICES_EN = "Spices & Pieces"
    const val CAT_SPICES_BN = "মসলা ও দানা"

    const val CAT_STAPLES_EN = "Kitchen Staples"
    const val CAT_STAPLES_BN = "রসদ ও ডাল"

    const val CAT_MEDICINE_EN = "Medicines"
    const val CAT_MEDICINE_BN = "ঔষধ ও ফার্মেসি"

    const val CAT_GARMENTS_EN = "Garments & Clothes"
    const val CAT_GARMENTS_BN = "পোশাক ও বস্ত্র"

    const val CAT_HARDWARE_EN = "Hardware & Electrical"
    const val CAT_HARDWARE_BN = "হার্ডওয়্যার ও লাইট"

    const val CAT_HOUSEHOLD_EN = "Household Needs"
    const val CAT_HOUSEHOLD_BN = "গৃহস্থালি প্রসাধন"

    // Cultural Logout
    const val LOGOUT_EN = "Baper Bari (Logout)"
    const val LOGOUT_BN = "বাপের বাড়ি (লগআউট)"

    // Voice Command
    const val VOICE_PROMPT_EN = "Say: 'Potato finish' or 'Add 5kg potato'"
    const val VOICE_PROMPT_BN = "বলুন: 'আলু শেষ', 'দুধ শেষ' বা '৫ কেজি আলু কিনলাম'"
}
