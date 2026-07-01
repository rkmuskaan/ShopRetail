package com.example

import androidx.compose.runtime.compositionLocalOf

val LocalAppLanguage = compositionLocalOf { "en" }

val englishToSwahiliDictionary = mapOf(
    "Login" to "Ingia",
    "Log In" to "Ingia",
    "Register" to "Sajili",
    "Welcome to ExpresSoko" to "Karibu ExpresSoko",
    "Select Your Experience" to "Chagua Uzoefu Wako",
    "Customer" to "Mteja",
    "Retailer Merchant" to "Muuzaji",
    "Join our revolutionary marketplace" to "Jiunge na soko letu la mapinduzi",
    "Create your Merchant account" to "Pata akaunti ya mfanyabiashara",
    "Let's get started" to "Tuanze",
    "Your Full Name" to "Jina lako Kamili",
    "Mobile Phone Number" to "Nambari ya Simu Kamili",
    "07XX XXX XXX" to "07XX XXX XXX",
    "National ID / Passport" to "Kitambulisho / Pasipoti",
    "Secure Password" to "Nenosiri",
    "Enable 2FA Security Login" to "Anzisha ulinzi wa njia 2 (2FA)",
    "Create Secure Profile" to "Tengeneza Akaunti yako kwa umakini",
    "Already have an account? Login here" to "Tayari una akaunti? Ingia hapa",
    "Welcome Back" to "Karibu Tena",
    "Enter Password" to "Andika Nenosiri lako",
    "Verify Login" to "Thibitisha kuingia",
    "Don't have an account? Register" to "Huna Akaunti? Jisajili Sasa",
    "Digital OTP Verification" to "Uthibitisho wa OTP wa kidijitali",
    "Enter the 4-digit token sent to your handset" to "Weka Nambari 4 za siri ulizotumiwa kwenye nambari yako ya simu",
    "Verify Authenticity" to "Thibitisha",
    "Search ExpresSoko..." to "Tafuta ExpresSoko...",
    "Explore Assortments" to "Gundua vitu",
    "Filter by maximum price: KES " to "chuja kwa bei ya juu: KES ",
    "Sort Items: " to "Panga Bidhaa: ",
    "Newest First" to "Mpya kwanza",
    "Classic Default" to "Za Zamani kwanza",
    "Layout: " to "Muonekano: ",
    "Cart Space" to "Kikapu",
    "Orders" to "Maagizo",
    "Profile" to "Wasifu",
    "Dashboard" to "Dasibodi",
    "Inventory" to "Mali",
    "Reports" to "Ripoti",
    "AI Brief" to "Maelezo ya AI",
    "Purchases" to "Ununuzi",
    "Messages" to "Ujumbe",
    "Settings" to "Mipangilio",
    "Logout" to "Toka",
    "Log Out" to "Ondoka",
    "Sign Out" to "Toka Akounti",
    "Retailer Payment Details" to "Maelezo ya Malipo ya Muuzaji",
    "Payment Mode" to "Njia ya Kulipa",
    "Business Details" to "Maelezo ya Biashara",
    "App Colour" to "Rangi ya App",
    "Language" to "Lugha",
    "English" to "Kiingereza",
    "Kiswahili" to "Kiswahili",
    "Location Settings" to "Mipangilio ya Eneo",
    "Save Preferences" to "Hifadhi Mapendeleo",
    "Synthesize Day's Brief" to "Tengeneza Ripoti ya Siku",
    "Add to Cart" to "Weka kwa Kikapu",
    "Checkout" to "Lipa Sasa",
    "Total" to "Jumla",
    "Price" to "Bei",
    "Category" to "Kitengo",
    "Stock" to "Mali iliyopo",
    "Update" to "Sasisha",
    "Delete" to "Futa"
)

fun String.toSwahili(): String {
    if (this.isBlank()) return this
    // Try precise match
    englishToSwahiliDictionary[this]?.let { return it }
    
    // For parameterized strings or partial matches it's more complex, 
    // but we check if the string contains key words:
    for ((english, swahili) in englishToSwahiliDictionary) {
        if (this.equals(english, ignoreCase = true)) return swahili
    }
    
    return this
}
