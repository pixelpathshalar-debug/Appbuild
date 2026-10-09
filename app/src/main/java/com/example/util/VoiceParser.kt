package com.example.util

data class KnownKitchenItem(
    val englishName: String,
    val bengaliName: String,
    val aliases: List<String>,
    val category: String,
    val defaultBuyQty: Double,
    val unit: String
)

sealed class VoiceCommandResult {
    data class MarkFinished(
        val itemEn: String,
        val itemBn: String,
        val category: String,
        val suggestedBuyQty: Double,
        val unit: String
    ) : VoiceCommandResult()

    data class AddBought(
        val itemEn: String,
        val itemBn: String,
        val category: String,
        val quantity: Double,
        val unit: String
    ) : VoiceCommandResult()

    data class AddToBuy(
        val itemEn: String,
        val itemBn: String,
        val category: String,
        val quantity: Double,
        val unit: String
    ) : VoiceCommandResult()

    data class Unrecognized(
        val rawSpeech: String,
        val tip: String
    ) : VoiceCommandResult()
}

object VoiceParser {
    val KNOWN_ITEMS = listOf(
        KnownKitchenItem("Potato", "আলু", listOf("potato", "potatoes", "alu", "aloo", "আলু"), "VEGETABLES", 5.0, "kg"),
        KnownKitchenItem("Onion", "পেঁয়াজ", listOf("onion", "onions", "piyaj", "peyaj", "পেঁয়াজ", "পিয়াজ"), "VEGETABLES", 2.0, "kg"),
        KnownKitchenItem("Green Chili", "কাঁচা মরিচ", listOf("chili", "chilli", "chilies", "morich", "marich", "lonka", "কাঁচা মরিচ", "মরিচ", "লঙ্কা"), "VEGETABLES", 0.5, "kg"),
        KnownKitchenItem("Tomato", "টমেটো", listOf("tomato", "tomatoes", "tometo", "টমেটো", "টমেটোস"), "VEGETABLES", 1.0, "kg"),
        KnownKitchenItem("Garlic", "রসুন", listOf("garlic", "roshun", "roson", "রসুন"), "SPICES", 250.0, "g"),
        KnownKitchenItem("Ginger", "আদা", listOf("ginger", "ada", "আদা"), "SPICES", 200.0, "g"),
        KnownKitchenItem("Cumin", "জিরা", listOf("cumin", "jeera", "jira", "জিরা", "জিরে"), "SPICES", 100.0, "g"),
        KnownKitchenItem("Turmeric", "হলুদ", listOf("turmeric", "holud", "haldi", "হলুদ"), "SPICES", 200.0, "g"),
        KnownKitchenItem("Coriander", "ধনে", listOf("coriander", "dhone", "dhoniya", "ধনে", "ধনিয়া"), "SPICES", 100.0, "g"),
        KnownKitchenItem("Cardamom", "এলাচ", listOf("cardamom", "elach", "elachi", "এলাচ"), "SPICES", 50.0, "g"),
        KnownKitchenItem("Clove", "লবঙ্গ", listOf("clove", "cloves", "lobongo", "লবঙ্গ"), "SPICES", 50.0, "g"),
        KnownKitchenItem("Cinnamon", "দারুচিনি", listOf("cinnamon", "daruchini", "দারুচিনি"), "SPICES", 50.0, "g"),
        KnownKitchenItem("Bay Leaf", "তেজপাতা", listOf("bay leaf", "tejpatta", "tej pata", "তেজপাতা"), "SPICES", 50.0, "g"),
        KnownKitchenItem("Eggplant", "বেগুন", listOf("eggplant", "brinjal", "begun", "বেগুন"), "VEGETABLES", 1.0, "kg"),
        KnownKitchenItem("Cauliflower", "ফুলকপি", listOf("cauliflower", "fulkopi", "ful kopi", "ফুলকপি"), "VEGETABLES", 2.0, "pcs"),
        KnownKitchenItem("Cabbage", "বাঁধাকপি", listOf("cabbage", "badhakopi", "বাঁধাকপি"), "VEGETABLES", 1.0, "pcs"),
        KnownKitchenItem("Carrot", "গাজর", listOf("carrot", "gajor", "গাজর"), "VEGETABLES", 1.0, "kg"),
        KnownKitchenItem("Spinach", "পালং শাক", listOf("spinach", "palong", "palong shak", "পালং শাক", "শাক"), "VEGETABLES", 1.0, "bundle"),
        KnownKitchenItem("Lentils", "মসুর ডাল", listOf("lentil", "lentils", "dal", "daal", "ডাল", "মসুর ডাল"), "STAPLES", 1.0, "kg"),
        KnownKitchenItem("Cooking Oil", "সয়াবিন তেল", listOf("oil", "cooking oil", "tel", "সয়াবিন তেল", "তেল"), "STAPLES", 2.0, "L"),
        KnownKitchenItem("Salt", "লবণ", listOf("salt", "lobon", "noon", "লবণ", "নুন"), "STAPLES", 1.0, "kg"),
        KnownKitchenItem("Paracetamol", "প্যারাসিটামল", listOf("paracetamol", "napa", "ace", "fever medicine", "প্যারাসিটামল", "নাপা", "জ্বরের ওষুধ"), "MEDICINE", 10.0, "tablet"),
        KnownKitchenItem("Omeprazole", "ওমিপ্রাজল", listOf("omeprazole", "seclo", "gastric medicine", "ওমিপ্রাজল", "সেকলো", "গ্যাস্ট্রিক"), "MEDICINE", 2.0, "strip"),
        KnownKitchenItem("Antacid", "এন্টাসিড", listOf("antacid", "এন্টাসিড"), "MEDICINE", 1.0, "strip"),
        KnownKitchenItem("Cough Syrup", "কাশি সিরাপ", listOf("cough syrup", "tusk", "কাশি সিরাপ", "সিরাপ"), "MEDICINE", 1.0, "bottle"),
        KnownKitchenItem("Milk", "দুধ", listOf("milk", "dudh", "দুধ", "গরুর দুধ"), "STAPLES", 1.0, "L"),
        KnownKitchenItem("Detergent Powder", "ডিটারজেন্ট সার্ফ", listOf("detergent", "surf excel", "surf", "ডিটারজেন্ট", "সার্ফ এক্সেল"), "HOUSEHOLD", 1.0, "kg"),
        KnownKitchenItem("Dish Soap Bar", "ভিম বার", listOf("dish soap", "vim", "vim bar", "ভিম বার", "থালা মাজার সাবান"), "HOUSEHOLD", 1.0, "pcs"),
        KnownKitchenItem("LED Bulb", "এলইডি লাইট", listOf("bulb", "led bulb", "light", "বাল্ব", "এলইডি লাইট"), "HARDWARE", 2.0, "pcs"),
        KnownKitchenItem("Battery", "পেন্সিল ব্যাটারি", listOf("battery", "batteries", "ব্যাটারি", "পেন্সিল ব্যাটারি"), "HARDWARE", 4.0, "pcs"),
        KnownKitchenItem("Traditional Shari", "সুতি শাড়ি", listOf("saree", "shari", "শাড়ি", "সুতি শাড়ি"), "GARMENTS", 1.0, "pcs"),
        KnownKitchenItem("Cotton Towel", "গামছা", listOf("towel", "gamcha", "গামছা", "তোয়ালে"), "GARMENTS", 2.0, "pcs")
    )

    private val FINISH_KEYWORDS = listOf(
        "finish", "finished", "end", "ended", "empty", "over", "ran out", "run out", "no more", "done",
        "শেষ", "ফুরিয়ে", "নেই", "ফুরিয়ে", "নাই", "শেষ হয়ে গেছে", "শেষ হয়ে গেছে", "sesh", "furiye"
    )

    private val BOUGHT_KEYWORDS = listOf(
        "bought", "buy", "added", "add", "purchase", "got", "brought",
        "কিনলাম", "কিনেছি", "কেনা হলো", "যোগ কর", "যোগ করো", "kinlam", "kinechi"
    )

    fun parse(rawText: String): VoiceCommandResult {
        val text = rawText.trim().lowercase()

        // 1. Detect if it's a finish/depletion command
        val isFinishCommand = FINISH_KEYWORDS.any { text.contains(it) }

        // Find matching item from text
        val matchedItem = KNOWN_ITEMS.firstOrNull { item ->
            item.aliases.any { alias ->
                text.contains(alias.lowercase())
            }
        }

        if (matchedItem != null) {
            if (isFinishCommand) {
                return VoiceCommandResult.MarkFinished(
                    itemEn = matchedItem.englishName,
                    itemBn = matchedItem.bengaliName,
                    category = matchedItem.category,
                    suggestedBuyQty = matchedItem.defaultBuyQty,
                    unit = matchedItem.unit
                )
            }

            // Check if bought / quantity specified e.g. "bought 5kg potato" or "5 kg potato"
            val qty = extractQuantity(text) ?: matchedItem.defaultBuyQty
            val unit = extractUnit(text) ?: matchedItem.unit

            val isBought = BOUGHT_KEYWORDS.any { text.contains(it) }
            if (isBought || text.contains("kg") || text.contains("কেজি") || text.contains("gm") || text.contains("গ্রাম")) {
                return VoiceCommandResult.AddBought(
                    itemEn = matchedItem.englishName,
                    itemBn = matchedItem.bengaliName,
                    category = matchedItem.category,
                    quantity = qty,
                    unit = unit
                )
            } else {
                // If just "potato" or "buy potato", suggest adding to buy list
                return VoiceCommandResult.AddToBuy(
                    itemEn = matchedItem.englishName,
                    itemBn = matchedItem.bengaliName,
                    category = matchedItem.category,
                    quantity = qty,
                    unit = unit
                )
            }
        }

        // Fallback: If contains "finish" but unknown item
        if (isFinishCommand) {
            val words = text.split(" ")
            val nonKeyword = words.firstOrNull { w -> !FINISH_KEYWORDS.contains(w) && w.length > 2 }
            if (nonKeyword != null) {
                val capitalized = nonKeyword.replaceFirstChar { it.uppercase() }
                return VoiceCommandResult.MarkFinished(
                    itemEn = capitalized,
                    itemBn = capitalized,
                    category = inferCategory(capitalized),
                    suggestedBuyQty = 1.0,
                    unit = "kg"
                )
            }
        }

        // Generic voice command to create items: e.g. "দুধ ২ লিটার", "buy 2kg rice", "paracetamol 10 pcs"
        val qty = extractQuantity(text) ?: 1.0
        val unit = extractUnit(text) ?: "pcs"
        val cleanedName = cleanSpokenItemName(rawText)

        if (cleanedName.isNotBlank() && cleanedName.length >= 2) {
            val cat = inferCategory(cleanedName)
            return VoiceCommandResult.AddToBuy(
                itemEn = cleanedName,
                itemBn = cleanedName,
                category = cat,
                quantity = qty,
                unit = unit
            )
        }

        return VoiceCommandResult.Unrecognized(
            rawSpeech = rawText,
            tip = "Try saying: 'Potato finish' (আলু শেষ), 'Milk 2 Liter' (দুধ ২ লিটার) or 'Buy 1kg Onion' (১ কেজি পেঁয়াজ)"
        )
    }

    private fun inferCategory(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.contains("medicine") || lower.contains("ঔষধ") || lower.contains("ওষুধ") ||
            lower.contains("napa") || lower.contains("নাপা") || lower.contains("paracetamol") ||
            lower.contains("seclo") || lower.contains("সেকলো") || lower.contains("tablet") ||
            lower.contains("ট্যাবলেট") || lower.contains("strip") || lower.contains("পাতা") -> "MEDICINE"

            lower.contains("shirt") || lower.contains("শার্ট") || lower.contains("shari") ||
            lower.contains("saree") || lower.contains("শাড়ি") || lower.contains("towel") ||
            lower.contains("gamcha") || lower.contains("গামছা") || lower.contains("পোশাক") ||
            lower.contains("pant") || lower.contains("প্যান্ট") || lower.contains("পাঞ্জাবি") -> "GARMENTS"

            lower.contains("bulb") || lower.contains("বাল্ব") || lower.contains("light") ||
            lower.contains("লাইট") || lower.contains("battery") || lower.contains("ব্যাটারি") ||
            lower.contains("switch") || lower.contains("সুইচ") || lower.contains("তার") ||
            lower.contains("হার্ডওয়্যার") || lower.contains("hardware") -> "HARDWARE"

            lower.contains("soap") || lower.contains("সাবান") || lower.contains("surf") ||
            lower.contains("detergent") || lower.contains("ডিটারজেন্ট") || lower.contains("vim") ||
            lower.contains("ভিম") || lower.contains("shampoo") || lower.contains("শ্যাম্পু") ||
            lower.contains("গৃহস্থালি") -> "HOUSEHOLD"

            lower.contains("alu") || lower.contains("আলু") || lower.contains("শাক") ||
            lower.contains("মরিচ") || lower.contains("বেগুন") || lower.contains("পেঁয়াজ") ||
            lower.contains("টমেটো") || lower.contains("কপি") || lower.contains("লাউ") -> "VEGETABLES"

            lower.contains("জিরা") || lower.contains("হলুদ") || lower.contains("আদা") ||
            lower.contains("রসুন") || lower.contains("এলাচ") || lower.contains("দারুচিনি") ||
            lower.contains("মসলা") -> "SPICES"

            lower.contains("চাল") || lower.contains("ডাল") || lower.contains("তেল") ||
            lower.contains("চিনি") || lower.contains("লবণ") || lower.contains("দুধ") ||
            lower.contains("ডিম") || lower.contains("আটা") || lower.contains("ময়দা") -> "STAPLES"

            else -> "OTHERS"
        }
    }

    private fun cleanSpokenItemName(raw: String): String {
        var res = raw.trim()
        val removeWords = listOf(
            "buy", "add", "create", "need", "please", "want", "got", "fard",
            "কিনব", "কিনবো", "কিনতে হবে", "যোগ কর", "যোগ করো", "ফর্দে দাও", "বাজার", "দরকার", "চাই"
        )
        for (w in removeWords) {
            res = res.replace("(?i)\\b$w\\b".toRegex(), "").trim()
        }
        // Remove quantities and unit words from name
        res = res.replace("""\d+(\.\d+)?""".toRegex(), "")
        res = res.replace("""[০-৯]+""".toRegex(), "")
        val unitWords = listOf("kg", "কেজি", "গ্রাম", "gm", "g", "লিটার", "liter", "পিস", "pcs", "টা", "টি", "পাতা", "strip", "ট্যাবলেট", "tablet", "বোতল", "bottle", "আঁটি", "bundle")
        for (u in unitWords) {
            res = res.replace("(?i)\\b$u\\b".toRegex(), "").trim()
        }
        return res.trim().replace("\\s+".toRegex(), " ")
    }

    private fun extractQuantity(text: String): Double? {
        // Bengali numbers to English
        val bengaliNums = mapOf(
            '০' to '0', '১' to '1', '২' to '2', '৩' to '3', '৪' to '4',
            '৫' to '5', '৬' to '6', '৭' to '7', '৮' to '8', '৯' to '9'
        )
        val normalized = text.map { bengaliNums[it] ?: it }.joinToString("")
        val regex = Regex("""(\d+(\.\d+)?)""")
        val match = regex.find(normalized)
        return match?.groupValues?.get(1)?.toDoubleOrNull()
    }

    private fun extractUnit(text: String): String? {
        val lower = text.lowercase()
        return when {
            lower.contains("kg") || lower.contains("কেজি") || lower.contains("kilo") -> "kg"
            lower.contains("gram") || lower.contains("গ্রাম") || lower.contains("gm") || lower.contains(" g ") -> "g"
            lower.contains("piece") || lower.contains("পিস") || lower.contains("টা") || lower.contains("টি") -> "pcs"
            lower.contains("bundle") || lower.contains("আঁটি") -> "bundle"
            lower.contains("packet") || lower.contains("প্যাকেট") -> "packet"
            lower.contains("strip") || lower.contains("পাতা") -> "strip"
            lower.contains("tablet") || lower.contains("ট্যাবলেট") -> "tablet"
            lower.contains("bottle") || lower.contains("বোতল") -> "bottle"
            lower.contains("liter") || lower.contains("লিটার") || lower.contains("litre") -> "L"
            else -> null
        }
    }
}
