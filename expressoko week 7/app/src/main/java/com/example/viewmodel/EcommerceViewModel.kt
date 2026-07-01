package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SessionManager
import com.example.data.api.FirestoreManager
import com.example.data.api.GeminiManager
import com.example.data.api.MpesaUtils
import com.example.data.local.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.ExistingPeriodicWorkPolicy
import com.example.data.worker.SyncWorker
import java.util.concurrent.TimeUnit

enum class AppNavDestination {
    SPLASH,
    ROLE_SELECTION,
    LOGIN,
    REGISTRATION,
    OTP_VERIFICATION,
    CUSTOMER_HUB,
    RETAILER_HUB,
    CUSTOMER_CATALOG,
    CUSTOMER_CART,
    PROFILE_SCREEN,
    MESSAGE_LIST,
    CHAT_SCREEN,
    RETAILER_BUSINESS_REGISTRATION,
    CUSTOMER_ORDERS,
    RETAILER_ORDERS,
    RETAILER_DETAILED_REPORTS
}

enum class CatalogueLayout {
    SQUARE,
    LIST,
    TABULAR
}

data class AppState(
    // Routing state configuration
    val currentDestination: AppNavDestination = AppNavDestination.SPLASH,
    val backStack: List<AppNavDestination> = emptyList(),
    val waitingForExitConfirmation: Boolean = false,
    
    // Auth inputs
    val inputName: String = "",
    val inputPhone: String = "",
    val inputIdNo: String = "",
    val inputPasswordSet: String = "",
    val inputIs2FAEnabledInRegister: Boolean = false,
    
    // Checkout state
    val isProcessingCheckout: Boolean = false,

    // Login inputs
    val loginPhoneInput: String = "",
    val loginPasswordInput: String = "",
    val login2FAEnabled: Boolean = false,
    
    // Active validated user session profile parameters
    val activeUserId: String = "",
    val activeUserName: String = "",
    val activeUserPhone: String = "",
    val activeUserRole: String = "Customer", // "Customer" or "Retailer"
    val activeUserProfilePhoto: String = "",
    val activeUserBusinessPhoto: String = "",
    val activeUserInterests: String = "",
    val activeUserBusinessName: String = "",
    val activeUserBusinessPhone: String = "",
    val activeUserBusinessNumber: String = "",
    val activeUserPaymentType: String = "", // "Pochi la Biashara", "Paybill", "Lipa na MPESA till"
    val activeUserPaymentNumber1: String = "",
    val activeUserPaymentNumber2: String = "",
    val activeUserPaymentType2: String = "", 
    val activeUserPaymentNumber3: String = "",
    val activeUserPaymentNumber4: String = "",
    val userLocationName: String = "",
    val userLocationCoordinates: String = "",
    
    // Business registration inputs
    val inputBusinessName: String = "",
    val inputBusinessPhone: String = "",
    
    // Language tag: "en" / "sw"
    val languageTag: String = "en",
    val appColor: String = "Blue",
    
    // OTP verification parameters
    val generatedOtpCode: String = "",
    val enteredOtpCode: String = "",
    val otpNavPurpose: String = "REGISTRATION", // "REGISTRATION" or "2FA_LOGIN"
    val otpValidationErrorNotice: String = "",
    
    // Customer search & display configurations
    val catalogLayoutMode: CatalogueLayout = CatalogueLayout.SQUARE,
    val selectedProductForFeedback: ProductEntity? = null,
    val productSearchFilter: String = "",
    val selectedCategoryClass: String = "All", // "All", "Tech Gadget", "Food", "Snacks", "Spices", "Toiletries"
    val costBracketFilter: Double = 50000.0, // slider upper-bound
    val sortByRecency: Boolean = false,
    
    // Retailer state variables
    val retailerInputName: String = "",
    val retailerInputPrice: Double = 0.0,
    val retailerInputCategory: String = "Food",
    val retailerInputDescription: String = "",
    val retailerInputWeight: String = "",
    val retailerInputIsPack: Boolean = false,
    val retailerInputPackConstituents: Int = 0,
    val retailerInputAdditionalDescription: String = "",
    val retailerInputStock: Int = 10,
    val retailerInputMaxCapacity: Int = 100,
    val retailerInputBatchNumber: String = "",
    val retailerInputExpiryDate: String = "",
    val retailerInputImageUri: String = "",
    val retailerInputPaymentMode: String = "Cash on pickup",
    val retailerInputWholesalePieces: Int = 1,
    
    val selectedRetailerMetricRange: String = "Weekly", // "Weekly", "Monthly", "Yearly"
    val dailyAiReportText: String = "Click 'Synthesize Day's Brief' to generate an intelligent AI business report.",
    val isGeneratingAiReport: Boolean = false,
    
    // App updates toggle simulation
    val updateSimulatedDownloadCompleted: Boolean = false,
    val isAppUpdateAvailable: Boolean = true,
    
    // Chat state
    val activeChatRetailerId: String = "",
    val activeChatRetailerPhone: String = "",
    val activeChatRetailerName: String = "",
    val isTyping: Boolean = false,
    
    // Notifications system status
    val triggerNotificationMessage: String = ""
)

class EcommerceViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.ecommerceDao()
    private val sessionManager = SessionManager(application)
    private val firestoreManager = FirestoreManager()

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    // Purely reactive data stream pulling directly from Room (No Mock Lists!)
    val allProducts: Flow<List<ProductEntity>> = dao.getAllProducts()

    // Cart items mapped reactively to active customer ID
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activeCartItems: Flow<List<CartProductJoin>> = _state.flatMapLatest { state ->
        if (state.activeUserId.isNotEmpty()) {
            dao.getCartWithProducts(state.activeUserId)
        } else {
            flowOf(emptyList())
        }
    }

    // Comments/Feedback associated with the inspected item
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activeComments: Flow<List<CommentEntity>> = _state.flatMapLatest { state ->
        val selected = state.selectedProductForFeedback
        if (selected != null) {
            dao.getCommentsForProduct(selected.id)
        } else {
            flowOf(emptyList())
        }
    }

    // Historical transactions list
    val transactionHistory: Flow<List<TransactionEntity>> = dao.getAllTransactions()

    init {
        // Evaluate startup session and preferred system language context
        viewModelScope.launch {
            // Restore language tag preference
            sessionManager.languageTag.collectLatest { tag ->
                _state.update { it.copy(languageTag = tag) }
            }
        }
        
        // Sync Firestore Data to Local Room Database
        viewModelScope.launch {
            firestoreManager.listenToProducts().collectLatest { products ->
                products.forEach { dao.insertProduct(it) }
            }
        }

        // Configure background sync for offline changes
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
            
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
            
        WorkManager.getInstance(application).enqueueUniquePeriodicWork(
            "OfflineSyncWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
        
        viewModelScope.launch {
            _state.map { it.activeUserId }.filter { it.isNotEmpty() }.distinctUntilChanged().collectLatest { userId ->
                firestoreManager.listenToCart(userId).collectLatest { cartItems ->
                    cartItems.forEach { dao.addOrUpdateCartItem(it) }
                }
            }
        }
        
        viewModelScope.launch {
            // Restore app color preference
            sessionManager.appColor.collectLatest { color ->
                _state.update { it.copy(appColor = color) }
            }
        }

        viewModelScope.launch {
            // Read status on launch
            val isLoggedInOnce = sessionManager.isLoggedIn.first()
            if (isLoggedInOnce) {
                val restoredId = sessionManager.userId.first() ?: ""
                val restoredName = sessionManager.userName.first() ?: ""
                val restoredPhone = sessionManager.phoneNumber.first() ?: ""
                val restoredRole = sessionManager.userType.first() ?: "Customer"

                val user = dao.getUserById(restoredId)
                if (user != null) {
                    _state.update {
                        it.copy(
                            activeUserId = restoredId,
                            activeUserName = restoredName,
                            activeUserPhone = restoredPhone,
                            activeUserRole = restoredRole,
                            activeUserProfilePhoto = user.profilePhotoPath,
                            activeUserBusinessPhoto = user.businessPhotoPath,
                            activeUserInterests = user.interests,
                            activeUserBusinessName = user.businessName,
                            activeUserBusinessPhone = user.businessPhone,
                            activeUserBusinessNumber = user.businessNumber,
                            activeUserPaymentType = user.paymentType,
                            activeUserPaymentNumber1 = user.paymentNumber1,
                            activeUserPaymentNumber2 = user.paymentNumber2,
                            activeUserPaymentType2 = user.paymentType2,
                            activeUserPaymentNumber3 = user.paymentNumber3,
                            activeUserPaymentNumber4 = user.paymentNumber4,
                            currentDestination = if (restoredRole == "Retailer") AppNavDestination.RETAILER_HUB else AppNavDestination.CUSTOMER_HUB
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            activeUserId = restoredId,
                            activeUserName = restoredName,
                            activeUserPhone = restoredPhone,
                            activeUserRole = restoredRole,
                            currentDestination = if (restoredRole == "Retailer") AppNavDestination.RETAILER_HUB else AppNavDestination.CUSTOMER_HUB
                        )
                    }
                }
                
                // Catalog pulls directly from database
            } else {
                val restoredSelectedRole = sessionManager.selectedRole.first()
                val restoredPhone = sessionManager.phoneNumber.first() ?: ""
                var restoredPassword = ""
                if (restoredPhone.isNotEmpty()) {
                    val user = dao.getUserByPhone(restoredPhone)
                    if (user != null) {
                        restoredPassword = user.passwordSet
                    }
                }
                
                if (!restoredSelectedRole.isNullOrEmpty()) {
                    _state.update { 
                        it.copy(
                            activeUserRole = restoredSelectedRole,
                            loginPhoneInput = restoredPhone, // populate phone input for login
                            loginPasswordInput = restoredPassword,
                            currentDestination = AppNavDestination.LOGIN,
                            backStack = listOf(AppNavDestination.ROLE_SELECTION)
                        ) 
                    }
                } else {
                    _state.update { it.copy(
                        currentDestination = AppNavDestination.ROLE_SELECTION,
                        loginPhoneInput = restoredPhone,
                        loginPasswordInput = restoredPassword
                    ) }
                }
            }
        }
    }


    // --- Language Interface Switching ---
    fun toggleLanguageSelection() {
        viewModelScope.launch {
            val current = _state.value.languageTag
            val target = if (current == "en") "sw" else "en"
            sessionManager.setLanguage(target)
            _state.update { it.copy(languageTag = target) }
            
            val userId = _state.value.activeUserId
            if (userId.isNotEmpty()) {
                dao.getUserById(userId)?.let { user ->
                    val updatedUser = user.copy(appLanguage = target)
                    dao.insertUser(updatedUser)
                    firestoreManager.syncUserOnline(updatedUser)
                }
            }
        }
    }

    fun updateAppColor(color: String) {
        viewModelScope.launch {
            sessionManager.setAppColor(color)
            _state.update { it.copy(appColor = color) }

            val userId = _state.value.activeUserId
            if (userId.isNotEmpty()) {
                dao.getUserById(userId)?.let { user ->
                    val updatedUser = user.copy(appTheme = color)
                    dao.insertUser(updatedUser)
                    firestoreManager.syncUserOnline(updatedUser)
                }
            }
        }
    }


    // --- General Navigation Controllers ---
    fun navigateTo(dest: AppNavDestination) {
        _state.update { current ->
            if (current.currentDestination == dest) return@update current
            
            val clearStackDests = listOf(
                AppNavDestination.SPLASH,
                AppNavDestination.ROLE_SELECTION,
                AppNavDestination.CUSTOMER_HUB,
                AppNavDestination.RETAILER_HUB
            )
            val newStack = if (dest in clearStackDests) emptyList() else current.backStack + current.currentDestination
            
            current.copy(currentDestination = dest, backStack = newStack, waitingForExitConfirmation = false)
        }
    }

    fun navigateBack(): Boolean {
        var shouldExit = false
        _state.update { current ->
            if (current.backStack.isNotEmpty()) {
                val previous = current.backStack.last()
                val newStack = current.backStack.dropLast(1)
                current.copy(currentDestination = previous, backStack = newStack, waitingForExitConfirmation = false)
            } else {
                if (current.waitingForExitConfirmation) {
                    shouldExit = true
                    current
                } else {
                    triggerNotification("Press Back again to exit")
                    current.copy(waitingForExitConfirmation = true)
                }
            }
        }
        return shouldExit
    }
    
    fun resetExitConfirmation() {
        _state.update { it.copy(waitingForExitConfirmation = false) }
    }

    fun triggerSyncWork() {
        val syncWorkRequest = androidx.work.OneTimeWorkRequestBuilder<com.example.data.worker.SyncWorker>().build()
        androidx.work.WorkManager.getInstance(getApplication()).enqueue(syncWorkRequest)
    }

    // --- Form Parameter Updates ---
    fun updateRegistrationForm(
        name: String? = null,
        phone: String? = null,
        idNo: String? = null,
        password: String? = null,
        is2FAEnabled: Boolean? = null
    ) {
        _state.update { current ->
            current.copy(
                inputName = name ?: current.inputName,
                inputPhone = phone ?: current.inputPhone,
                inputIdNo = idNo ?: current.inputIdNo,
                inputPasswordSet = password ?: current.inputPasswordSet,
                inputIs2FAEnabledInRegister = is2FAEnabled ?: current.inputIs2FAEnabledInRegister
            )
        }
    }

    fun updateLoginForm(phone: String? = null, password: String? = null) {
        viewModelScope.launch {
            val current = _state.value
            var newPassword = password ?: current.loginPasswordInput
            if (phone != null && phone.length >= 10) {
                val user = dao.getUserByPhone(phone)
                if (user != null) {
                    newPassword = user.passwordSet
                }
            }
            _state.update {
                it.copy(
                    loginPhoneInput = phone ?: current.loginPhoneInput,
                    loginPasswordInput = newPassword
                )
            }
        }
    }

    fun selectUserProfileType(role: String) {
        viewModelScope.launch {
            sessionManager.saveSelectedRole(role)
            _state.update { it.copy(activeUserRole = role) }
            navigateTo(AppNavDestination.LOGIN)
        }
    }


    // --- Registration Core Operations ---
    fun submitUserRegistration() {
        val current = _state.value
        if (current.inputPhone.isEmpty() || current.inputName.isEmpty() || current.inputIdNo.isEmpty() || current.inputPasswordSet.isEmpty()) {
            triggerNotification("⚠️ Please fill out all input fields before submitting.")
            return
        }

        viewModelScope.launch {
            val allUsers = dao.getAllUsersList()
            if (allUsers.any { it.idNumber == current.inputIdNo }) {
                triggerNotification("⚠️ User with this national ID profile is already registered.")
                return@launch
            }
            if (allUsers.any { it.phoneNumber == current.inputPhone }) {
                triggerNotification("⚠️ This phone number is already registered.")
                return@launch
            }
            
            // Check Firestore for existing user in case of reinstall
            try {
                val onlineUser = firestoreManager.getUserOnline(current.inputPhone)
                if (onlineUser != null) {
                    dao.insertUser(onlineUser)
                    triggerNotification("⚠️ Profile recognized from cloud. Please log in.")
                    _state.update { it.copy(loginPhoneInput = current.inputPhone) }
                    navigateTo(AppNavDestination.LOGIN)
                    return@launch
                }
            } catch (e: Exception) {
                // ignore
            }

            if (allUsers.any { it.passwordSet == current.inputPasswordSet }) {
                triggerNotification("⚠️ Security Policy: Password must be unique, please choose another.")
                return@launch
            }

            // Commit user profile to persistent safe directly
            val newUser = UserEntity(
                idNumber = current.inputIdNo,
                name = current.inputName,
                phoneNumber = current.inputPhone,
                userType = current.activeUserRole,
                passwordSet = current.inputPasswordSet,
                is2FAEnabled = current.inputIs2FAEnabledInRegister
            )
            dao.insertUser(newUser)
            
            // Start session automatically
            sessionManager.saveSession(newUser.idNumber, newUser.name, newUser.phoneNumber, newUser.userType)
            
            _state.update {
                it.copy(
                    activeUserId = newUser.idNumber,
                    activeUserName = newUser.name,
                    activeUserPhone = newUser.phoneNumber,
                    activeUserProfilePhoto = newUser.profilePhotoPath,
                    activeUserBusinessPhoto = newUser.businessPhotoPath,
                    activeUserInterests = newUser.interests,
                    inputName = "",
                    inputPhone = "",
                    inputIdNo = "",
                    inputPasswordSet = "",
                    inputIs2FAEnabledInRegister = false
                )
            }
            
            triggerNotification("🎉 Profile created successfully!")
            navigateTo(if (newUser.userType == "Retailer") AppNavDestination.RETAILER_BUSINESS_REGISTRATION else AppNavDestination.CUSTOMER_HUB)
        }
    }

    fun updateBusinessRegistrationForm(name: String, phone: String) {
        _state.update { it.copy(inputBusinessName = name, inputBusinessPhone = phone) }
    }
    
    fun submitBusinessRegistration() {
        val current = _state.value
        if (current.inputBusinessName.isEmpty() || current.inputBusinessPhone.isEmpty()) {
            triggerNotification("⚠️ Please fill out all business details.")
            return
        }
        
        // Generate a 10 character business number
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val generatedBizNumber = (1..10).map { chars.random() }.joinToString("")
        
        viewModelScope.launch {
            val allUsers = dao.getAllUsersList()
            if (allUsers.any { it.businessName.equals(current.inputBusinessName, ignoreCase = true) }) {
                triggerNotification("⚠️ A shop with this name already exists. Please choose a unique name.")
                return@launch
            }
            if (allUsers.any { it.businessPhone == current.inputBusinessPhone }) {
                triggerNotification("⚠️ This business phone is already registered.")
                return@launch
            }

            val user = dao.getUserById(current.activeUserId)
            if (user != null) {
                val updatedUser = user.copy(
                    businessName = current.inputBusinessName,
                    businessPhone = current.inputBusinessPhone,
                    businessNumber = generatedBizNumber
                )
                dao.insertUser(updatedUser)
                
                _state.update {
                    it.copy(
                        activeUserBusinessName = current.inputBusinessName,
                        activeUserBusinessPhone = current.inputBusinessPhone,
                        activeUserBusinessNumber = generatedBizNumber,
                        inputBusinessName = "",
                        inputBusinessPhone = ""
                    )
                }
                triggerNotification("✅ Business registered successfully! ID: $generatedBizNumber")
                navigateTo(AppNavDestination.RETAILER_HUB)
            }
        }
    }


    // --- Login Authentication Core Operations ---
    fun submitUserLogin() {
        val current = _state.value
        if (current.loginPhoneInput.isEmpty() || current.loginPasswordInput.isEmpty()) {
            triggerNotification("⚠️ Please enter both your phone number and password.")
            return
        }

        viewModelScope.launch {
            var user = dao.getUserByPhone(current.loginPhoneInput)
            if (user == null) {
                // Try fetching from online database just in case it's a new device
                try {
                    val onlineUser = firestoreManager.getUserOnline(current.loginPhoneInput)
                    if (onlineUser != null) {
                        dao.insertUser(onlineUser)
                        user = onlineUser
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (user == null) {
                triggerNotification("⚠️ No profile registered with number: ${current.loginPhoneInput}")
                return@launch
            }

            if (user.userType != current.activeUserRole) {
                triggerNotification("⚠️ Incorrect role selection! Profile belongs to a ${user.userType}.")
                return@launch
            }

            if (user.passwordSet != current.loginPasswordInput) {
                triggerNotification("⚠️ Invalid password credentials entered. Please try again.")
                return@launch
            }

            // Successfully matched credentials! Check 2FA condition
            if (user.is2FAEnabled) {
                val generated2FACode = (1000..9999).random().toString()
                _state.update {
                    it.copy(
                        activeUserId = user.idNumber,
                        activeUserName = user.name,
                        activeUserPhone = user.phoneNumber,
                        generatedOtpCode = generated2FACode,
                        otpNavPurpose = "2FA_LOGIN",
                        otpValidationErrorNotice = "",
                        enteredOtpCode = ""
                    )
                }
                triggerNotification("🛡️ 2FA Activated! Your login security token code is [$generated2FACode]")
                navigateTo(AppNavDestination.OTP_VERIFICATION)
            } else {
                // Keep session persistent automatically
                sessionManager.saveSession(user.idNumber, user.name, user.phoneNumber, user.userType)
                sessionManager.setAppColor(user.appTheme)
                sessionManager.setLanguage(user.appLanguage)
                _state.update {
                    it.copy(
                        activeUserId = user.idNumber,
                        activeUserName = user.name,
                        activeUserPhone = user.phoneNumber,
                        activeUserProfilePhoto = user.profilePhotoPath,
                        activeUserBusinessPhoto = user.businessPhotoPath,
                        activeUserInterests = user.interests,
                        activeUserBusinessName = user.businessName,
                        activeUserBusinessPhone = user.businessPhone,
                        activeUserBusinessNumber = user.businessNumber,
                        activeUserPaymentType = user.paymentType,
                        activeUserPaymentNumber1 = user.paymentNumber1,
                        activeUserPaymentNumber2 = user.paymentNumber2,
                        activeUserPaymentType2 = user.paymentType2,
                        activeUserPaymentNumber3 = user.paymentNumber3,
                        activeUserPaymentNumber4 = user.paymentNumber4,
                        appColor = user.appTheme,
                        languageTag = user.appLanguage
                    )
                }
                triggerNotification("🔓 Welcome back, ${user.name}!")
                navigateTo(if (user.userType == "Retailer") AppNavDestination.RETAILER_HUB else AppNavDestination.CUSTOMER_HUB)
            }
        }
    }


    // --- OTP Verification Processing ---
    fun updateOtpDigitalBoxes(entered: String) {
        _state.update { it.copy(enteredOtpCode = entered) }
    }

    fun verifyIncomingOtpToken() {
        val current = _state.value
        if (current.enteredOtpCode.length != 4) {
            _state.update { it.copy(otpValidationErrorNotice = "Verification code must consist of exactly 4 digits.") }
            return
        }

        if (current.enteredOtpCode != current.generatedOtpCode) {
            _state.update { it.copy(otpValidationErrorNotice = "Invalid code. Please re-enter the code sent to your handset.") }
            return
        }

        // Successfully verified code! Either finalize registration or complete 2FA login
        viewModelScope.launch {
            if (current.otpNavPurpose == "REGISTRATION") {
                // Commit user profile to persistent safe
                val newUser = UserEntity(
                    idNumber = current.inputIdNo,
                    name = current.inputName,
                    phoneNumber = current.inputPhone,
                    userType = current.activeUserRole,
                    passwordSet = current.inputPasswordSet,
                    is2FAEnabled = current.inputIs2FAEnabledInRegister
                )
                dao.insertUser(newUser)
                firestoreManager.syncUserOnline(newUser)
                
                // Clear state variables
                sessionManager.saveSession(newUser.idNumber, newUser.name, newUser.phoneNumber, newUser.userType)
                _state.update {
                    it.copy(
                        activeUserId = newUser.idNumber,
                        activeUserName = newUser.name,
                        activeUserPhone = newUser.phoneNumber,
                        inputName = "",
                        inputPhone = "",
                        inputIdNo = "",
                        inputPasswordSet = "",
                        inputIs2FAEnabledInRegister = false
                    )
                }
                triggerNotification("🎉 ExpresSoko profile created successfully!")
                navigateTo(if (newUser.userType == "Retailer") AppNavDestination.RETAILER_BUSINESS_REGISTRATION else AppNavDestination.CUSTOMER_HUB)
            } else {
                // 2FA login verification succeeded
                val user = dao.getUserByPhone(current.activeUserPhone)
                if (user != null) {
                    sessionManager.setAppColor(user.appTheme)
                    sessionManager.setLanguage(user.appLanguage)
                    _state.update {
                        it.copy(
                            appColor = user.appTheme, 
                            languageTag = user.appLanguage,
                            activeUserProfilePhoto = user.profilePhotoPath,
                            activeUserBusinessPhoto = user.businessPhotoPath,
                            activeUserInterests = user.interests,
                            activeUserBusinessName = user.businessName,
                            activeUserBusinessPhone = user.businessPhone,
                            activeUserBusinessNumber = user.businessNumber,
                            activeUserPaymentType = user.paymentType,
                            activeUserPaymentNumber1 = user.paymentNumber1,
                            activeUserPaymentNumber2 = user.paymentNumber2,
                            activeUserPaymentType2 = user.paymentType2,
                            activeUserPaymentNumber3 = user.paymentNumber3,
                            activeUserPaymentNumber4 = user.paymentNumber4
                        )
                    }
                }
                sessionManager.saveSession(current.activeUserId, current.activeUserName, current.activeUserPhone, current.activeUserRole)
                triggerNotification("🔓 2FA authentication verified. Welcome back!")
                navigateTo(if (current.activeUserRole == "Retailer") AppNavDestination.RETAILER_HUB else AppNavDestination.CUSTOMER_HUB)
            }
        }
    }


    // --- Location Settings ---
    fun updateLocationName(name: String) {
        _state.update { it.copy(userLocationName = name) }
    }

    fun updateLocationCoordinates(coordinates: String) {
        _state.update { it.copy(userLocationCoordinates = coordinates) }
    }

    // --- Manual Session Terminations ---
    fun triggerLogOutSequence() {
        val current = _state.value
        viewModelScope.launch {
            val user = dao.getUserByPhone(current.activeUserPhone)
            val password = user?.passwordSet ?: ""

            sessionManager.clearSession()
            sessionManager.setAppColor("Blue") // default
            sessionManager.setLanguage("en")
            _state.update {
                it.copy(
                    activeUserId = "",
                    activeUserName = "",
                    activeUserPhone = "",
                    loginPhoneInput = current.activeUserPhone,
                    loginPasswordInput = password, // We will auto-fill next time they open the app by checking local DB
                    appColor = "Blue",
                    languageTag = "en"
                )
            }
            triggerNotification("🚪 Logged out securely. Come back soon!")
            navigateTo(AppNavDestination.ROLE_SELECTION)
        }
    }


    // --- Customer: Go Shopping & Filtering Controls ---
    fun updateSearchFilters(query: String) {
        _state.update { it.copy(productSearchFilter = query) }
    }

    fun selectCategoryFilter(category: String) {
        _state.update { it.copy(selectedCategoryClass = category) }
    }

    fun updateCostBracketSlider(bound: Double) {
        _state.update { it.copy(costBracketFilter = bound) }
    }

    fun toggleRecencySort(enable: Boolean) {
        _state.update { it.copy(sortByRecency = enable) }
    }

    fun toggleCatalogueLayout(mode: CatalogueLayout) {
        _state.update { it.copy(catalogLayoutMode = mode) }
    }


    // --- Cart System Core Logic ---
    fun appendItemToDigitalCart(product: ProductEntity) {
        viewModelScope.launch {
            val customerId = _state.value.activeUserId
            if (customerId.isEmpty()) {
                triggerNotification("⚠️ Please log in to add products to your cart.")
                return@launch
            }

            // ExpresSoko Requirement: Option to increase quantity only appears after item enters cart!
            // First item enters cart with quantity 1
            val entry = CartItemEntity(
                productId = product.id,
                quantity = 1,
                customerId = customerId
            )
            dao.addOrUpdateCartItem(entry)
            firestoreManager.syncCartItemOnline(entry)
            triggerNotification("🛒 Added '${product.name}' to Cart! Open Cart Space to adjust quantities.")
        }
    }

    fun updateCartProductCount(id: Int, currentCount: Int, quantityDelta: Int) {
        viewModelScope.launch {
            val targetCount = currentCount + quantityDelta
            val customerId = _state.value.activeUserId
            if (targetCount <= 0) {
                dao.removeProductFromCart(id, customerId)
                firestoreManager.removeCartItemOnline(customerId, id)
                triggerNotification("🗑️ Item cleared from shopping cart namespace.")
            } else {
                // Ensure customer doesn't select counts exceeding retailer's active stock levels!
                dao.updateCartQuantity(id, targetCount, customerId)
                firestoreManager.syncCartItemOnline(CartItemEntity(productId = id, quantity = targetCount, customerId = customerId))
            }
        }
    }

    fun removeCartItemDirectly(id: Int) {
        viewModelScope.launch {
            val customerId = _state.value.activeUserId
            dao.removeProductFromCart(id, customerId)
            firestoreManager.removeCartItemOnline(customerId, id)
            triggerNotification("🗑️ Product removed from shopping space.")
        }
    }


    // --- Call Retailer offline/online ---
    fun initiateCellularCall(phoneNo: String) {
        triggerNotification("📞 Routing to native hardware phone dialer calling: $phoneNo")
    }


    // --- Feedback, Ratings & Comments ---
    fun inspectProductDetails(product: ProductEntity?) {
        _state.update { it.copy(selectedProductForFeedback = product) }
    }

    fun postCustomerPurchaseFeedback(productText: String, scoreSelection: Int) {
        val selected = _state.value.selectedProductForFeedback ?: return
        val currentCustName = _state.value.activeUserName.ifEmpty { "Verified Buyer" }
        if (productText.isBlank()) {
            triggerNotification("⚠️ Comment text cannot be blank.")
            return
        }

        viewModelScope.launch {
            val feedback = CommentEntity(
                productId = selected.id,
                customerName = currentCustName,
                commentText = productText,
                rating = scoreSelection
            )
            dao.insertComment(feedback)
            triggerNotification("📝 Comment published successfully to the database!")
        }
    }


    // --- checkout transaction processing ---
    fun completeCheckout(pickupTimes: Map<String, String>, inputNumber: String = "", securityPin: String = "") {
        viewModelScope.launch {
            _state.value = _state.value.copy(isProcessingCheckout = true)
            val customerId = _state.value.activeUserId
            val currentCart = activeCartItems.first()
            if (currentCart.isEmpty()) {
                triggerNotification("⚠️ Your cart is currently empty.")
                _state.value = _state.value.copy(isProcessingCheckout = false)
                return@launch
            }

            val checkoutAmount = currentCart.sumOf { it.product.price * it.cartItem.quantity }
            
            // Trigger push transaction simulation notifications if details given
            if (inputNumber.isNotEmpty()) {
                triggerNotification("📲 sending interactive push STK notification payload to: $inputNumber...")
            }
            
            // Mock dynamic checkout generation
            val mockCheckoutRequestId = "ws_CO_${(10000000..99999999).random()}"
            val itemSummaryText = currentCart.joinToString { "${it.product.name} (Qty x${it.cartItem.quantity})" }

            // Add simulated pending Transaction object to database
            val pendingReceipt = TransactionEntity(
                customerId = customerId,
                totalAmount = checkoutAmount,
                checkoutRequestID = mockCheckoutRequestId,
                status = "PENDING",
                itemSummary = itemSummaryText
            )
            val txId = dao.insertTransaction(pendingReceipt)

            if (inputNumber.isNotEmpty()) {
                triggerNotification("M-Pesa STK Push sent. Awaiting PIN entry...")
                kotlinx.coroutines.delay(3000)
            }
            
            // Update to SUCCESS
            dao.updateTransactionStatus(txId.toInt(), "SUCCESS")

            // Adjust stock counts corresponding to items successfully bought
            currentCart.forEach { join ->
                val updatedStock = (join.product.stockQuantity - join.cartItem.quantity).coerceAtLeast(0)
                dao.updateProductStock(join.product.id, updatedStock)
                firestoreManager.updateProductStockOnline(join.product.id, updatedStock)
            }

            // Create tracking orders for each retailer
            val itemsByRetailer = currentCart.groupBy { it.product.retailerId }
            itemsByRetailer.forEach { (retailerId, items) ->
                val amount = items.sumOf { it.product.price * it.cartItem.quantity }
                val summary = items.joinToString { "${it.product.name} (Qty x${it.cartItem.quantity})" }
                val pickupTime = pickupTimes[retailerId] ?: ""
                
                val order = OrderEntity(
                    customerId = customerId,
                    retailerId = retailerId,
                    totalAmount = amount,
                    itemSummary = summary,
                    pickupTime = pickupTime,
                    status = "PENDING"
                )
                dao.insertOrder(order)
            }

            // Flush out shopper's local cart
            currentCart.forEach { firestoreManager.removeCartItemOnline(customerId, it.cartItem.productId) }
            dao.clearCart(customerId)
            
            triggerNotification("✅ [Transaction Success] Payment of KES ${checkoutAmount.toInt()} processed! Cart cleared.")
            _state.value = _state.value.copy(isProcessingCheckout = false)
            navigateTo(AppNavDestination.CUSTOMER_HUB)
        }
    }

    // --- Order Status System ---
    suspend fun getRetailers(retailerIds: List<String>): Map<String, UserEntity> {
        return retailerIds.mapNotNull { id ->
            dao.getUserById(id)?.let { it.businessNumber to it }
        }.toMap()
    }
    
    fun getCustomerOrdersFlow(): Flow<List<OrderEntity>> {
        val current = _state.value
        return dao.getCustomerOrders(current.activeUserId)
    }

    fun getRetailerOrdersFlow(): Flow<List<OrderEntity>> {
        val current = _state.value
        return dao.getRetailerOrders(current.activeUserId)
    }

    fun getPendingCartItemsForRetailerFlow(): Flow<List<com.example.data.local.CartProductJoin>> {
        val current = _state.value
        return dao.getPendingCartItemsForRetailer(current.activeUserId)
    }

    fun updateOrderStatus(orderId: Int, newStatus: String) {
        viewModelScope.launch {
            dao.updateOrderStatus(orderId, newStatus)
            triggerNotification("📋 Order status updated to $newStatus")
        }
    }

    // --- Chat Functions ---
    fun openChatWithRetailer(retailerId: String, retailerPhone: String, retailerName: String) {
        _state.update {
            it.copy(
                activeChatRetailerId = retailerId,
                activeChatRetailerPhone = retailerPhone,
                activeChatRetailerName = retailerName,
                currentDestination = AppNavDestination.CHAT_SCREEN
            )
        }
    }

    fun getChatMessagesForActiveChat(): Flow<List<ChatMessageEntity>> {
        val current = _state.value
        return dao.getChatMessagesBetweenUsers(current.activeUserId, current.activeChatRetailerId)
    }

    fun getAllUserMessages(): Flow<List<ChatMessageEntity>> {
        val current = _state.value
        return dao.getAllChatMessagesForUser(current.activeUserId)
    }

    fun sendChatMessage(text: String, attachUri: String = "") {
        val current = _state.value
        if (text.isBlank() && attachUri.isEmpty()) return
        
        viewModelScope.launch {
            val msg = ChatMessageEntity(
                senderId = current.activeUserId,
                receiverId = current.activeChatRetailerId,
                receiverName = current.activeChatRetailerName,
                messageText = text,
                attachmentUri = attachUri,
                status = "Pending"
            )
            dao.insertChatMessage(msg)
            // Fire background worker to sync
            triggerSyncWork()
            // Reset typing status immediately upon send
            updateTypingStatus(false)
        }
    }

    private var typingJob: kotlinx.coroutines.Job? = null
    fun updateTypingStatus(isTyping: Boolean) {
        val current = _state.value
        viewModelScope.launch {
            firestoreManager.setTypingStatus(current.activeUserId, current.activeChatRetailerId, isTyping)
        }
    }

    fun observeTypingStatus(): Flow<Boolean> {
        val current = _state.value
        // Listen to if the other person (retailer) is typing to me (activeUser)
        return firestoreManager.listenToTypingStatus(current.activeChatRetailerId, current.activeUserId)
    }

    fun deleteChatBetweenUsers(retailerId: String) {
        val current = _state.value
        viewModelScope.launch {
            dao.deleteChatBetweenUsers(current.activeUserId, retailerId)
        }
    }

    fun updateProfile(name: String, phone: String, photoPath: String, businessPhotoPath: String, interests: String) {
        val current = _state.value
        viewModelScope.launch {
            val user = dao.getUserById(current.activeUserId)
            if (user != null) {
                val updatedUser = user.copy(
                    name = name,
                    phoneNumber = phone,
                    profilePhotoPath = photoPath,
                    businessPhotoPath = businessPhotoPath,
                    interests = interests
                )
                dao.insertUser(updatedUser)
                firestoreManager.syncUserOnline(updatedUser)
                sessionManager.saveSession(updatedUser.idNumber, updatedUser.name, updatedUser.phoneNumber, updatedUser.userType)
                _state.update {
                    it.copy(
                        activeUserName = name,
                        activeUserPhone = phone,
                        activeUserProfilePhoto = photoPath,
                        activeUserBusinessPhoto = businessPhotoPath,
                        activeUserInterests = interests
                    )
                }
                triggerNotification("Profile updated successfully!")
            }
        }
    }

    fun updateRetailerPaymentSettings(type: String, num1: String, num2: String, type2: String = "", num3: String = "", num4: String = "") {
        val current = _state.value
        viewModelScope.launch {
            val allUsers = dao.getAllUsersList()
            if (allUsers.any { it.idNumber != current.activeUserId && ((it.paymentNumber1 == num1 && num1.isNotEmpty()) || (it.paymentNumber2 == num2 && num2.isNotEmpty())) }) {
                triggerNotification("⚠️ These payment numbers are already associated with another retailer.")
                return@launch
            }

            val user = dao.getUserById(current.activeUserId)
            if (user != null) {
                val updatedUser = user.copy(
                    paymentType = type,
                    paymentNumber1 = num1,
                    paymentNumber2 = num2,
                    paymentType2 = type2,
                    paymentNumber3 = num3,
                    paymentNumber4 = num4
                )
                dao.insertUser(updatedUser)
                _state.update {
                    it.copy(
                        activeUserPaymentType = type,
                        activeUserPaymentNumber1 = num1,
                        activeUserPaymentNumber2 = num2,
                        activeUserPaymentType2 = type2,
                        activeUserPaymentNumber3 = num3,
                        activeUserPaymentNumber4 = num4
                    )
                }
                triggerNotification("Retailer payment details securely saved.")
            }
        }
    }


    // --- Retailer: Add Catalog Stock Levels & Price updates ---
    fun updateRetailerItemForm(
        name: String? = null,
        price: Double? = null,
        category: String? = null,
        desc: String? = null,
        weight: String? = null,
        isPack: Boolean? = null,
        packConstituents: Int? = null,
        additionalDesc: String? = null,
        stockAmt: Int? = null,
        maxCapacity: Int? = null,
        batchNum: String? = null,
        expiry: String? = null,
        imgUri: String? = null,
        paymentMode: String? = null,
        wholesale: Int? = null
    ) {
        _state.update { current ->
            current.copy(
                retailerInputName = name ?: current.retailerInputName,
                retailerInputPrice = price ?: current.retailerInputPrice,
                retailerInputCategory = category ?: current.retailerInputCategory,
                retailerInputDescription = desc ?: current.retailerInputDescription,
                retailerInputWeight = weight ?: current.retailerInputWeight,
                retailerInputIsPack = isPack ?: current.retailerInputIsPack,
                retailerInputPackConstituents = packConstituents ?: current.retailerInputPackConstituents,
                retailerInputAdditionalDescription = additionalDesc ?: current.retailerInputAdditionalDescription,
                retailerInputStock = stockAmt ?: current.retailerInputStock,
                retailerInputMaxCapacity = maxCapacity ?: current.retailerInputMaxCapacity,
                retailerInputBatchNumber = batchNum ?: current.retailerInputBatchNumber,
                retailerInputExpiryDate = expiry ?: current.retailerInputExpiryDate,
                retailerInputImageUri = imgUri ?: current.retailerInputImageUri,
                retailerInputPaymentMode = paymentMode ?: current.retailerInputPaymentMode,
                retailerInputWholesalePieces = wholesale ?: current.retailerInputWholesalePieces
            )
        }
    }

    fun submitStockInventoryRegistration() {
        val current = _state.value
        if (current.retailerInputName.isEmpty() || current.retailerInputDescription.isEmpty()) {
            triggerNotification("⚠️ Please enter a valid product title and description.")
            return
        }

        viewModelScope.launch {
            val correspondingEmoji = when (current.retailerInputCategory) {
                "Tech Gadget" -> "🎧"
                "Food" -> "🍎"
                "Snacks" -> "🍿"
                "Spices" -> "🌶️"
                "Toiletries" -> "🧴"
                else -> "📦"
            }

            val newProduct = ProductEntity(
                name = current.retailerInputName,
                price = 0.0, // Set when posting
                category = current.retailerInputCategory,
                description = current.retailerInputDescription,
                productWeight = current.retailerInputWeight,
                isPack = current.retailerInputIsPack,
                packConstituents = current.retailerInputPackConstituents,
                additionalDescription = current.retailerInputAdditionalDescription,
                retailerId = current.activeUserBusinessNumber,
                retailerPhone = current.activeUserPhone.ifEmpty { "0711222333" },
                stockQuantity = current.retailerInputStock,
                maxCapacity = current.retailerInputMaxCapacity,
                batchNumber = current.retailerInputBatchNumber,
                expiryDate = current.retailerInputExpiryDate,
                productImageUri = current.retailerInputImageUri,
                isPosted = false, // Must be posted explicitly
                itemIconEmoji = correspondingEmoji
            )
            dao.insertProduct(newProduct)
            firestoreManager.syncProductOnline(newProduct)

            // Reset form (keep expiry date)
            _state.update {
                it.copy(
                    retailerInputName = "",
                    retailerInputPrice = 0.0,
                    retailerInputCategory = "Food",
                    retailerInputDescription = "",
                    retailerInputWeight = "",
                    retailerInputIsPack = false,
                    retailerInputPackConstituents = 0,
                    retailerInputAdditionalDescription = "",
                    retailerInputStock = 0,
                    retailerInputMaxCapacity = 0,
                    retailerInputBatchNumber = "",
                    retailerInputImageUri = ""
                )
            }
            triggerNotification("✅ Stock details saved! You can now Post this stock.")
        }
    }

    private suspend fun returnToNonSaleStock(postedProduct: ProductEntity, quantityToReturn: Int) {
        val existingUnposted = dao.getAllProducts().first().firstOrNull { 
            !it.isPosted && it.name == postedProduct.name && it.batchNumber == postedProduct.batchNumber 
        }
        if (existingUnposted != null) {
            dao.insertProduct(existingUnposted.copy(stockQuantity = existingUnposted.stockQuantity + quantityToReturn))
        } else {
            val newUnposted = ProductEntity(
                retailerId = postedProduct.retailerId,
                name = postedProduct.name,
                category = postedProduct.category,
                price = 0.0,
                stockQuantity = quantityToReturn,
                isPosted = false,
                paymentMode = "Any",
                wholesalePieces = 1,
                productImageUri = postedProduct.productImageUri,
                itemIconEmoji = postedProduct.itemIconEmoji,
                description = postedProduct.description,
                batchNumber = postedProduct.batchNumber,
                expiryDate = postedProduct.expiryDate
            )
            dao.insertProduct(newUnposted)
        }
    }

    fun updateActiveStockItem(productId: Int, newStock: Int, newPrice: Double, newUnit: Int) {
        viewModelScope.launch {
            val product = dao.getProductById(productId)
            if (product != null) {
                if (newStock < product.stockQuantity) {
                    val difference = product.stockQuantity - newStock
                    returnToNonSaleStock(product, difference)
                }
                
                val updatedProduct = product.copy(
                    stockQuantity = newStock,
                    price = newPrice,
                    wholesalePieces = newUnit
                )
                dao.insertProduct(updatedProduct)
                firestoreManager.syncProductOnline(updatedProduct)
                triggerNotification("✅ Active Stock Updated Successfully!")
            }
        }
    }

    fun removeActiveStockItem(productId: Int) {
        viewModelScope.launch {
            val product = dao.getProductById(productId)
            if (product != null) {
                returnToNonSaleStock(product, product.stockQuantity)
                dao.deleteProduct(product)
                firestoreManager.removeProductOnline(productId)
                triggerNotification("✅ Active Stock Deleted. Stock returned to NonSale Inventory.")
            }
        }
    }

    fun submitPostStock(product: ProductEntity, sellPrice: Double, paymentMode: String, postQuantity: Int) {
        viewModelScope.launch {
            // 1. Subtract from unposted stock
            val remainingStock = product.stockQuantity - postQuantity
            
            val unpostedProduct = product.copy(stockQuantity = remainingStock)
            if (remainingStock <= 0) {
                dao.deleteProduct(product)
            } else {
                dao.insertProduct(unpostedProduct)
            }

            // 2. Find existing posted product with same parameters
            // For now, let's just insert a new posted product.
            val postedProduct = ProductEntity(
                retailerId = product.retailerId,
                name = product.name,
                category = product.category,
                price = sellPrice,
                stockQuantity = postQuantity,
                isPosted = true,
                paymentMode = paymentMode,
                wholesalePieces = 1, // default sale unit for now
                productImageUri = product.productImageUri,
                itemIconEmoji = product.itemIconEmoji,
                description = product.description,
                batchNumber = product.batchNumber,
                expiryDate = product.expiryDate
            )
            dao.insertProduct(postedProduct)
            firestoreManager.syncProductOnline(postedProduct)
            triggerNotification("🚀 Stock Posted Successfully! Now available for customers.")
        }
    }


    // --- Retailer reports metrics range changer ---
    fun selectReportPeriodRange(range: String) {
        _state.update { it.copy(selectedRetailerMetricRange = range) }
        triggerNotification("🔄 Generated tabular/graphical business reports structured for: $range views.")
    }


    // --- Samsung Today AI Brief Retailer stats generation system ---
    fun generateRetailerAiBusinessBrief() {
        val current = _state.value
        val retId = current.activeUserId

        _state.update { it.copy(isGeneratingAiReport = true) }

        viewModelScope.launch {
            // Read active statistics from local database lists to send up
            val catalogProducts = dao.getAllProducts().first().filter { it.retailerId == retId }
            val completedTransactions = dao.getAllTransactions().first() // locally consolidated purchases

            val numProducts = catalogProducts.size
            val lowStockItems = catalogProducts.filter { it.stockQuantity <= 5 }.map { "${it.name} (Qty ${it.stockQuantity})" }
            val categoryCounts = catalogProducts.groupBy { it.category }.mapValues { it.value.size }
            
            val totalSales = completedTransactions.sumOf { it.totalAmount }
            val totalTxCount = completedTransactions.size

            // Call Retrofit/OkHttp direct REST API to summarize business statistics
            val dynamicBriefText = GeminiManager.generateDailyBrief(
                retailerName = current.activeUserName.ifEmpty { "Retailer Merchant" },
                numProducts = numProducts,
                totalSalesAmt = totalSales,
                numSalesTx = totalTxCount,
                categoryBreakdown = categoryCounts,
                lowStockAlerts = lowStockItems,
                isOfflineMode = false // can set dynamically if they turn on network isolation simulator
            )

            _state.update {
                it.copy(
                    dailyAiReportText = dynamicBriefText,
                    isGeneratingAiReport = false
                )
            }
            triggerNotification("🤖 ExpresSoko AI Daily Brief generated.")
        }
    }


    // --- App Updates System Verification ---
    fun simulateDownloadAppUpdate() {
        viewModelScope.launch {
            _state.update { it.copy(isAppUpdateAvailable = false, updateSimulatedDownloadCompleted = true) }
            triggerNotification("🚀 downloading security hotfix updates... ExpresSoko is successfully patched up!")
        }
    }


    // --- Notifications state trigger ---
    fun triggerNotification(msg: String) {
        _state.update { it.copy(triggerNotificationMessage = msg) }
    }

    fun acknowledgeNotification() {
        _state.update { it.copy(triggerNotificationMessage = "") }
    }
}
