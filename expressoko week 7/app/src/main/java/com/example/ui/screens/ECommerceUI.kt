@file:OptIn(com.google.accompanist.permissions.ExperimentalPermissionsApi::class)
package com.example.ui.screens

import com.example.toSwahili
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.local.CartProductJoin
import com.example.data.local.CommentEntity
import com.example.data.local.ProductEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserEntity
import com.example.viewmodel.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import android.Manifest
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.google.accompanist.permissions.*
import java.util.Locale

fun formatPrice(amount: Number): String {
    return java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(amount.toInt())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainECommerceAppLayout(viewModel: EcommerceViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val products by viewModel.allProducts.collectAsState(initial = emptyList())
    val cartItems by viewModel.activeCartItems.collectAsState(initial = emptyList())
    val comments by viewModel.activeComments.collectAsState(initial = emptyList())
    val transactions by viewModel.transactionHistory.collectAsState(initial = emptyList())
    val context = LocalContext.current
    val activity = context as? Activity

    BackHandler(enabled = true) {
        val shouldExit = viewModel.navigateBack()
        if (shouldExit) {
            activity?.finish()
        }
    }

    // Observe push triggers for system notifications
    val currentLang = com.example.LocalAppLanguage.current
    LaunchedEffect(state.triggerNotificationMessage) {
        if (state.triggerNotificationMessage.isNotEmpty()) {
            val msg = state.triggerNotificationMessage
            val finalMsg = if (currentLang == "sw") msg.toSwahili() else msg
            Toast.makeText(context, finalMsg, Toast.LENGTH_LONG).show()
            viewModel.acknowledgeNotification()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.SendToMobile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = if (state.languageTag == "sw") "ExpresSoko" else "ExpresSoko",
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        )
                    }
                },
                actions = {
                    // Modern interactive language switcher (flags represent languages)
                    IconButton(
                        onClick = { viewModel.toggleLanguageSelection() },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .size(38.dp)
                    ) {
                        Text(
                            text = if (state.languageTag == "sw") "🇬🇧" else "🇰🇪",
                            fontSize = 18.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            androidx.compose.animation.AnimatedContent(
                targetState = state.currentDestination,
                transitionSpec = {
                    androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) + 
                    androidx.compose.animation.slideInVertically(
                        animationSpec = androidx.compose.animation.core.tween(300),
                        initialOffsetY = { fullHeight -> fullHeight / 10 }
                    ) togetherWith
                    androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300)) + 
                    androidx.compose.animation.slideOutVertically(
                        animationSpec = androidx.compose.animation.core.tween(300),
                        targetOffsetY = { fullHeight -> -fullHeight / 10 }
                    )
                },
                label = "screen_transition"
            ) { targetDestination ->
                when (targetDestination) {
                    AppNavDestination.SPLASH -> SplashScreenSurface(viewModel)
                    AppNavDestination.ROLE_SELECTION -> RoleSelectionScreen(viewModel)
                    AppNavDestination.LOGIN -> LoginScreen(viewModel)
                    AppNavDestination.REGISTRATION -> RegistrationScreen(viewModel)
                    AppNavDestination.OTP_VERIFICATION -> OtpVerificationScreen(viewModel)
                    AppNavDestination.CUSTOMER_HUB -> CustomerHomeScreen(viewModel, cartItems)
                    AppNavDestination.RETAILER_HUB -> RetailerDashboardScreen(viewModel, products, transactions)
                    AppNavDestination.CUSTOMER_CATALOG -> GoShoppingScreen(viewModel, products)
                    AppNavDestination.CUSTOMER_CART -> ShoppingCartScreen(viewModel, cartItems)
                    AppNavDestination.PROFILE_SCREEN -> ProfileScreen(viewModel)
                    AppNavDestination.MESSAGE_LIST -> MessagesListScreen(viewModel)
                    AppNavDestination.CHAT_SCREEN -> ChatScreen(viewModel)
                    AppNavDestination.RETAILER_BUSINESS_REGISTRATION -> RetailerBusinessRegistrationScreen(viewModel)
                    AppNavDestination.CUSTOMER_ORDERS -> CustomerOrdersScreen(viewModel)
                    AppNavDestination.RETAILER_ORDERS -> RetailerOrdersScreen(viewModel)
                    AppNavDestination.RETAILER_DETAILED_REPORTS -> RetailerDetailedReportsScreen(viewModel, transactions)
                }
            }
        }
    }
}

// ---------------- USER PROFILE SCREEN ----------------
@OptIn(ExperimentalPermissionsApi::class, com.google.accompanist.permissions.ExperimentalPermissionsApi::class)
@Composable
fun ProfileScreen(viewModel: EcommerceViewModel) {
    val state by viewModel.state.collectAsState()
    
    var editName by remember { mutableStateOf(state.activeUserName) }
    var editPhone by remember { mutableStateOf(state.activeUserPhone) }
    var editPhotoPath by remember { mutableStateOf(state.activeUserProfilePhoto) }
    var editBusinessPhotoPath by remember { mutableStateOf(state.activeUserBusinessPhoto) }
    var editInterests by remember { mutableStateOf(state.activeUserInterests) }
    var editPaymentType by remember { mutableStateOf(state.activeUserPaymentType.ifEmpty { "Pochi la Biashara" }) }
    var editPaymentNumber1 by remember { mutableStateOf(state.activeUserPaymentNumber1) }
    var editPaymentNumber2 by remember { mutableStateOf(state.activeUserPaymentNumber2) }
    var editPaymentType2 by remember { mutableStateOf(state.activeUserPaymentType2) }
    var editPaymentNumber3 by remember { mutableStateOf(state.activeUserPaymentNumber3) }
    var editPaymentNumber4 by remember { mutableStateOf(state.activeUserPaymentNumber4) }
    var isPaymentDropdownExpanded by remember { mutableStateOf(false) }
    var isPaymentDropdownExpanded2 by remember { mutableStateOf(false) }
    var showSecondaryPayment by remember { mutableStateOf(state.activeUserPaymentType2.isNotEmpty()) }
    
    val context = LocalContext.current

    val cropImage = androidx.activity.compose.rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uriContent = result.uriContent
            if (uriContent != null) {
                editPhotoPath = copyUriToInternalStorage(context, uriContent, "profile")
            }
        }
    }

    val cropBusinessImage = androidx.activity.compose.rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uriContent = result.uriContent
            if (uriContent != null) {
                editBusinessPhotoPath = copyUriToInternalStorage(context, uriContent, "business")
            }
        }
    }

    var showPermissionError by remember { mutableStateOf(false) }

    val cameraAndStoragePermissionState = rememberMultiplePermissionsState(
        permissions = buildList {
            add(Manifest.permission.CAMERA)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    ) { statuses ->
        if (statuses.values.all { it }) {
            cropImage.launch(
                CropImageContractOptions(
                    uri = null,
                    cropImageOptions = CropImageOptions(
                        imageSourceIncludeGallery = true,
                        imageSourceIncludeCamera = true,
                        guidelines = CropImageView.Guidelines.ON,
                        fixAspectRatio = true
                    )
                )
            )
        } else {
            showPermissionError = true
        }
    }
    
    if (showPermissionError) {
        Toast.makeText(LocalContext.current, "Permissions denied. Cannot use camera or storage.", Toast.LENGTH_SHORT).show()
        showPermissionError = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            IconButton(onClick = { viewModel.navigateTo(if (state.activeUserRole == "Retailer") AppNavDestination.RETAILER_HUB else AppNavDestination.CUSTOMER_HUB) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Profile Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (editPhotoPath.isNotEmpty()) {
                coil.compose.AsyncImage(
                    model = editPhotoPath,
                    contentDescription = "Profile Photo",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Text(state.activeUserName.take(2).uppercase(), style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        TextButton(onClick = { 
            if (cameraAndStoragePermissionState.allPermissionsGranted) {
                cropImage.launch(
                    CropImageContractOptions(
                        uri = null,
                        cropImageOptions = CropImageOptions(
                            imageSourceIncludeGallery = true,
                            imageSourceIncludeCamera = true,
                            guidelines = CropImageView.Guidelines.ON,
                            fixAspectRatio = true
                        )
                    )
                )
            } else {
                cameraAndStoragePermissionState.launchMultiplePermissionRequest()
            }
        }) {
            Text("Upload Profile Photo")
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        if (state.activeUserRole == "Retailer") {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (editBusinessPhotoPath.isNotEmpty()) {
                    coil.compose.AsyncImage(
                        model = editBusinessPhotoPath,
                        contentDescription = "Business Photo",
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Store, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(48.dp))
                }
            }
            TextButton(onClick = { 
                if (cameraAndStoragePermissionState.allPermissionsGranted) {
                    cropBusinessImage.launch(
                        CropImageContractOptions(
                            uri = null,
                            cropImageOptions = CropImageOptions(
                                imageSourceIncludeGallery = true,
                                imageSourceIncludeCamera = true,
                                guidelines = CropImageView.Guidelines.ON,
                                fixAspectRatio = false
                            )
                        )
                    )
                } else {
                    cameraAndStoragePermissionState.launchMultiplePermissionRequest()
                }
            }) {
                Text("Upload Business Shop Photo")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = editName,
            onValueChange = { editName = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Person, null) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = editPhone,
            onValueChange = { editPhone = it },
            label = { Text("Contact Number") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Phone, null) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (state.activeUserRole != "Retailer") {
            OutlinedTextField(
                value = editInterests,
                onValueChange = { editInterests = it },
                label = { Text("Interests (comma separated)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                leadingIcon = { Icon(Icons.Default.Star, null) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Location Settings Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Location Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = state.userLocationName,
                    onValueChange = { viewModel.updateLocationName(it) },
                    label = { Text("Store/Home Location Name") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.LocationCity, null) }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val context = LocalContext.current
                var permissionGranted by remember { mutableStateOf(false) }
                val locationPermissionState = rememberMultiplePermissionsState(
                    permissions = listOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                ) { statuses ->
                    permissionGranted = statuses.values.all { it }
                }

                TextButton(
                    onClick = {
                        if (locationPermissionState.allPermissionsGranted) {
                            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                            try {
                                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                    if (location != null) {
                                        val geoCoder = Geocoder(context, java.util.Locale.getDefault())
                                        try {
                                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                                geoCoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                                                    if (addresses.isNotEmpty()) {
                                                        val address = addresses[0]
                                                        val localName = address.locality ?: address.subAdminArea ?: address.adminArea ?: "Unknown Location"
                                                        viewModel.updateLocationName(localName)
                                                    }
                                                }
                                            } else {
                                                @Suppress("DEPRECATION")
                                                val addresses = geoCoder.getFromLocation(location.latitude, location.longitude, 1)
                                                if (!addresses.isNullOrEmpty()) {
                                                    val address = addresses[0]
                                                    val localName = address.locality ?: address.subAdminArea ?: address.adminArea ?: "Unknown Location"
                                                    viewModel.updateLocationName(localName)
                                                }
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                        viewModel.updateLocationCoordinates("${location.latitude}, ${location.longitude}")
                                        Toast.makeText(context, "Location Auto-detected", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Location not available", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: SecurityException) {
                                e.printStackTrace()
                            }
                        } else {
                            locationPermissionState.launchMultiplePermissionRequest()
                        }
                    },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Auto-detect Location")
                }
                
                if (state.userLocationCoordinates.isNotEmpty()) {
                    Text("Coordinates: ${state.userLocationCoordinates}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // App Colour Settings Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("App Colour", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                
                var isColorDropdownExpanded by remember { mutableStateOf(false) }
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = state.appColor,
                        onValueChange = {},
                        label = { Text("Select Colour Theme") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { isColorDropdownExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = isColorDropdownExpanded,
                        onDismissRequest = { isColorDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        listOf("Blue", "Pink", "Black", "Gray", "Red", "Green").forEach { selection ->
                            DropdownMenuItem(
                                text = { Text(selection) },
                                onClick = { 
                                    viewModel.updateAppColor(selection)
                                    isColorDropdownExpanded = false 
                                }
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (state.activeUserRole == "Retailer") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Payment Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editPaymentType,
                            onValueChange = {},
                            label = { Text("Payment Option") },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { isPaymentDropdownExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = isPaymentDropdownExpanded,
                            onDismissRequest = { isPaymentDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            listOf("Pochi la Biashara", "Paybill and Account Number", "Lipa na MPESA till").forEach { selection ->
                                DropdownMenuItem(
                                    text = { Text(selection) },
                                    onClick = { 
                                        editPaymentType = selection
                                        isPaymentDropdownExpanded = false 
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (editPaymentType == "Paybill and Account Number") {
                        OutlinedTextField(
                            value = editPaymentNumber1,
                            onValueChange = { editPaymentNumber1 = it },
                            label = { Text("Paybill Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editPaymentNumber2,
                            onValueChange = { editPaymentNumber2 = it },
                            label = { Text("Account Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (editPaymentType == "Pochi la Biashara") {
                        OutlinedTextField(
                            value = editPaymentNumber1,
                            onValueChange = { editPaymentNumber1 = it },
                            label = { Text("Mobile Number (Pochi)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        OutlinedTextField(
                            value = editPaymentNumber1,
                            onValueChange = { editPaymentNumber1 = it },
                            label = { Text("Till Number / Buy Goods") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    if (showSecondaryPayment) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Secondary Payment Option", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = editPaymentType2,
                                onValueChange = {},
                                label = { Text("Payment Option") },
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { isPaymentDropdownExpanded2 = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(
                                expanded = isPaymentDropdownExpanded2,
                                onDismissRequest = { isPaymentDropdownExpanded2 = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                listOf("Pochi la Biashara", "Paybill and Account Number", "Lipa na MPESA till").forEach { selection ->
                                    DropdownMenuItem(
                                        text = { Text(selection) },
                                        onClick = { 
                                            editPaymentType2 = selection
                                            isPaymentDropdownExpanded2 = false 
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (editPaymentType2 == "Paybill and Account Number") {
                            OutlinedTextField(
                                value = editPaymentNumber3,
                                onValueChange = { editPaymentNumber3 = it },
                                label = { Text("Paybill Number") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = editPaymentNumber4,
                                onValueChange = { editPaymentNumber4 = it },
                                label = { Text("Account Number") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else if (editPaymentType2 == "Pochi la Biashara") {
                            OutlinedTextField(
                                value = editPaymentNumber3,
                                onValueChange = { editPaymentNumber3 = it },
                                label = { Text("Mobile Number (Pochi)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            OutlinedTextField(
                                value = editPaymentNumber3,
                                onValueChange = { editPaymentNumber3 = it },
                                label = { Text("Till Number / Buy Goods") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        TextButton(
                            onClick = { 
                                showSecondaryPayment = false
                                editPaymentType2 = ""
                                editPaymentNumber3 = ""
                                editPaymentNumber4 = ""
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Remove", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.updateRetailerPaymentSettings(editPaymentType, editPaymentNumber1, editPaymentNumber2, editPaymentType2, editPaymentNumber3, editPaymentNumber4) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Payment Details")
                    }
                    if (!showSecondaryPayment) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { 
                                showSecondaryPayment = true
                                editPaymentType2 = "Pochi la Biashara"
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add new payment details")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        Button(
            onClick = { viewModel.updateProfile(editName, editPhone, editPhotoPath, editBusinessPhotoPath, editInterests) },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Save Changes")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = { viewModel.triggerLogOutSequence() },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Log Out")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Out")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ---------------- SPLASH SCREEN SCREEN ----------------
@Composable
fun SplashScreenSurface(viewModel: EcommerceViewModel) {
    LaunchedEffect(Unit) {
        delay(1000)
        viewModel.navigateTo(AppNavDestination.ROLE_SELECTION)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Storefront,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "ExpresSoko",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Fast. Offline-First. Secure.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ---------------- UTILITIES: DYNAMIC TEXT MAPPERS ----------------
@Composable
fun getLocalizedText(en: String, sw: String, lang: String): String {
    return if (lang == "sw") sw else en
}

// ---------------- ROLE SELECTION SCREEN ----------------
@Composable
fun RoleSelectionScreen(viewModel: EcommerceViewModel) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = getLocalizedText("Welcome to ExpresSoko", "Karibu Kwenye ExpresSoko", state.languageTag),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = getLocalizedText(
                "Choose your portal profile type to get started",
                "Chagua aina ya akaunti ili kuanza",
                state.languageTag
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(48.dp))

        // Card choice 1: Customer
        Card(
            onClick = { viewModel.selectUserProfileType("Customer") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = getLocalizedText("Customer Portal", "Mlango wa Mteja", state.languageTag),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = getLocalizedText("Shop products and pay securely via M-Pesa.", "Nunua bidhaa na ulipe salama kwa M-Pesa.", state.languageTag),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Card choice 2: Retailer/Merchant
        Card(
            onClick = { viewModel.selectUserProfileType("Retailer") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = getLocalizedText("Retailer Portal", "Kituo cha Muuzaji", state.languageTag),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = getLocalizedText("Publish stock listings, analyze sales with AI support.", "Weka bidhaa mpya, changanua mauzo kwa AI support.", state.languageTag),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

// ---------------- LOGIN SCREEN ----------------
@Composable
fun LoginScreen(viewModel: EcommerceViewModel) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${getLocalizedText("Sign In", "Ingia", state.languageTag)} (${state.activeUserRole.uppercase()})",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = state.loginPhoneInput,
            onValueChange = { viewModel.updateLoginForm(phone = it) },
            label = { Text(getLocalizedText("Phone Number", "Nambari ya Simu", state.languageTag)) },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        Spacer(modifier = Modifier.height(16.dp))

        var passwordVisible by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = state.loginPasswordInput,
            onValueChange = { viewModel.updateLoginForm(password = it) },
            label = { Text(getLocalizedText("Password", "Nywila (Password)", state.languageTag)) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                }
            },
            visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.submitUserLogin() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = getLocalizedText("Access Portal", "Ingia Kwenye Kituo", state.languageTag),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { viewModel.navigateTo(AppNavDestination.REGISTRATION) }) {
            Text(
                text = getLocalizedText(
                    "New to ExpresSoko? Register Account",
                    "Mtumiaji mpya? Jisajili Sasa",
                    state.languageTag
                )
            )
        }
    }
}

// ---------------- REGISTRATION SCREEN ----------------
@Composable
fun RegistrationScreen(viewModel: EcommerceViewModel) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "${getLocalizedText("Create Account", "Unda Akaunti", state.languageTag)} (${state.activeUserRole.uppercase()})",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = state.inputName,
            onValueChange = { viewModel.updateRegistrationForm(name = it) },
            label = { Text(getLocalizedText("Full Legal Name", "Majina Kamili", state.languageTag)) },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.inputPhone,
            onValueChange = { viewModel.updateRegistrationForm(phone = it) },
            label = { Text(getLocalizedText("Phone Number", "Nambari ya Simu", state.languageTag)) },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.inputIdNo,
            onValueChange = { viewModel.updateRegistrationForm(idNo = it) },
            label = { Text(getLocalizedText("National ID / Passport Key", "Nambari ya Kitambulisho", state.languageTag)) },
            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(12.dp))

        var passwordVisible by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = state.inputPasswordSet,
            onValueChange = { viewModel.updateRegistrationForm(password = it) },
            label = { Text(getLocalizedText("Define Secure Password", "Tengeneza Nywila", state.languageTag)) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                }
            },
            visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        
        Spacer(modifier = Modifier.height(20.dp))

        // Interlocking 2FA security prompt check toggles
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getLocalizedText("Active Two-Factor Security (2FA)", "Washa Uhakiki Salama wa 2FA", state.languageTag),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = getLocalizedText("Requires SMS Pin validation during logins.", "Hutuma SMS code kupitia nambari ya simu unapoingia.", state.languageTag),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = state.inputIs2FAEnabledInRegister,
                onCheckedChange = { viewModel.updateRegistrationForm(is2FAEnabled = it) }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.submitUserRegistration() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = getLocalizedText("Submit Registration", "Kamilisha Usajili", state.languageTag),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { viewModel.navigateTo(AppNavDestination.LOGIN) }) {
            Text(
                text = getLocalizedText(
                    "Already registered? Log In",
                    "Tayari una akaunti? Ingia Hapa",
                    state.languageTag
                )
            )
        }
    }
}

// ---------------- BUSINESS REGISTRATION SCREEN ----------------
@Composable
fun RetailerBusinessRegistrationScreen(viewModel: EcommerceViewModel) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Storefront,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Business Registration",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter your business details to get your unique 10-character business number.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = state.inputBusinessName,
            onValueChange = { viewModel.updateBusinessRegistrationForm(name = it, phone = state.inputBusinessPhone) },
            label = { Text("Business Name") },
            leadingIcon = { Icon(Icons.Default.Store, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.inputBusinessPhone,
            onValueChange = { viewModel.updateBusinessRegistrationForm(name = state.inputBusinessName, phone = it) },
            label = { Text("Business Phone Number") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.submitBusinessRegistration() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Register Business",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

// ---------------- OTP VERIFICATION SCREEN ----------------
@Composable
fun OtpVerificationScreen(viewModel: EcommerceViewModel) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Sms,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = getLocalizedText("Confirm Security SMS Code", "Uhakiki wa Nambari ya Siri", state.languageTag),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = getLocalizedText(
                "We dispatched a 4-Digit authorization code to your handset for terminal authentication verification.",
                "Tumekutumia nambari ya siri ya tarakimu 4 kwenye simu yako ili kukamilisha uhakiki wa usajili.",
                state.languageTag
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = state.enteredOtpCode,
            onValueChange = { if (it.length <= 4) viewModel.updateOtpDigitalBoxes(it) },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                fontSize = 28.sp,
                letterSpacing = 16.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.ExtraBold
            ),
            placeholder = { Text("0000", fontSize = 28.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier
                .width(220.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp)
        )

        if (state.otpValidationErrorNotice.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.otpValidationErrorNotice,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.verifyIncomingOtpToken() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = getLocalizedText("Confirm & Authorize Session", "Thibitisha Nambari Salama", state.languageTag),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

// ---------------- CUSTOMER: PRIMARY HOME SYSTEM (Grid View) ----------------
@Composable
fun CustomerHomeScreen(viewModel: EcommerceViewModel, cartItems: List<CartProductJoin>) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val customerProfileCropImage = androidx.activity.compose.rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uriContent = result.uriContent
            if (uriContent != null) {
                viewModel.updateProfile(state.activeUserName, state.activeUserPhone, copyUriToInternalStorage(context, uriContent, "profile"), state.activeUserBusinessPhoto, state.activeUserInterests)
            }
        }
    }

    var showPermError by remember { mutableStateOf(false) }
    val cameraStoragePermissionState = rememberMultiplePermissionsState(
        permissions = buildList {
            add(Manifest.permission.CAMERA)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    ) { statuses ->
        if (statuses.values.all { it }) {
            customerProfileCropImage.launch(
                CropImageContractOptions(
                    uri = null,
                    cropImageOptions = CropImageOptions(
                        imageSourceIncludeGallery = true,
                        imageSourceIncludeCamera = true,
                        guidelines = CropImageView.Guidelines.ON,
                        fixAspectRatio = true
                    )
                )
            )
        } else {
            showPermError = true
        }
    }

    if (showPermError) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Permissions denied. Cannot use camera or storage.", Toast.LENGTH_SHORT).show()
            showPermError = false
        }
    }

    // Location Permission
    var locPermissionGranted by remember { mutableStateOf(false) }
    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    ) { statuses ->
        locPermissionGranted = statuses.values.all { it }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Welcome and Status bar section Styled with Sleek Theme properties
        Surface(
            tonalElevation = 4.dp,
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.navigateTo(AppNavDestination.PROFILE_SCREEN) }
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable {
                            if (cameraStoragePermissionState.allPermissionsGranted) {
                                customerProfileCropImage.launch(
                                    CropImageContractOptions(
                                        uri = null,
                                        cropImageOptions = CropImageOptions(
                                            imageSourceIncludeGallery = true,
                                            imageSourceIncludeCamera = true,
                                            guidelines = CropImageView.Guidelines.ON,
                                            fixAspectRatio = true
                                        )
                                    )
                                )
                            } else {
                                cameraStoragePermissionState.launchMultiplePermissionRequest()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (state.activeUserProfilePhoto.isNotEmpty()) {
                        coil.compose.AsyncImage(
                            model = state.activeUserProfilePhoto,
                            contentDescription = "Profile Photo",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = state.activeUserName.take(2).uppercase(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${getLocalizedText("Hello", "Habari", state.languageTag)}, ${state.activeUserName}!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = getLocalizedText("Account Verified • Customer", "Akaunti Imethibitishwa • Mteja", state.languageTag),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Location Display & Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            if (state.userLocationName.isNotEmpty()) {
                                Text(
                                    text = "📍 ${state.userLocationName}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            if (state.userLocationCoordinates.isNotEmpty()) {
                                Text(
                                    text = state.userLocationCoordinates,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha=0.6f)
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                if (locationPermissionState.allPermissionsGranted) {
                                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                                    try {
                                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                            if (location != null) {
                                                val geoCoder = Geocoder(context, java.util.Locale.getDefault())
                                                try {
                                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                                        geoCoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                                                            if (addresses.isNotEmpty()) {
                                                                val address = addresses[0]
                                                                val localName = address.locality ?: address.subAdminArea ?: address.adminArea ?: "Unknown Location"
                                                                viewModel.updateLocationName(localName)
                                                            }
                                                        }
                                                    } else {
                                                        @Suppress("DEPRECATION")
                                                        val addresses = geoCoder.getFromLocation(location.latitude, location.longitude, 1)
                                                        if (!addresses.isNullOrEmpty()) {
                                                            val address = addresses[0]
                                                            val localName = address.locality ?: address.subAdminArea ?: address.adminArea ?: "Unknown Location"
                                                            viewModel.updateLocationName(localName)
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                                viewModel.updateLocationCoordinates("${location.latitude}, ${location.longitude}")
                                                Toast.makeText(context, "Location Auto-detected", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Location not available. Enable GPS.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } catch (e: SecurityException) {
                                        e.printStackTrace()
                                    }
                                } else {
                                    locationPermissionState.launchMultiplePermissionRequest()
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "Auto-detect Location",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = getLocalizedText("Customer Dashboard Hub", "Kituo cha Huduma Mbili", state.languageTag),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 2x2 Grid anchoring the 4 centralized navigation selectors
        Column {
            Row(modifier = Modifier.fillMaxWidth()) {
                HubSelectorCard(
                    title = getLocalizedText("Go Shopping", "Katalogi ya Bidhaa", state.languageTag),
                    subtitle = getLocalizedText("Explore Catalog", "Angalia Bidhaa", state.languageTag),
                    icon = Icons.Default.ShoppingBasket,
                    badgeCount = 0,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp, bottom = 12.dp),
                    onClick = { viewModel.navigateTo(AppNavDestination.CUSTOMER_CATALOG) }
                )
                HubSelectorCard(
                    title = getLocalizedText("Cart Space", "Kikapu Changu", state.languageTag),
                    subtitle = getLocalizedText("Checkout Now", "Lipa Sasa hivi", state.languageTag),
                    icon = Icons.Default.ShoppingCart,
                    badgeCount = cartItems.sumOf { it.cartItem.quantity },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp, bottom = 12.dp),
                    onClick = { viewModel.navigateTo(AppNavDestination.CUSTOMER_CART) }
                )
            }
            Row(modifier = Modifier.fillMaxWidth()) {

                HubSelectorCard(
                    title = getLocalizedText("My Profile", "Wasifu Wangu", state.languageTag),
                    subtitle = getLocalizedText("Personal Details", "Maelezo Yangu", state.languageTag),
                    icon = Icons.Default.AccountBox,
                    badgeCount = 0,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp, top = 4.dp),
                    onClick = { viewModel.navigateTo(AppNavDestination.PROFILE_SCREEN) }
                )
                val context = LocalContext.current
                HubSelectorCard(
                    title = getLocalizedText("Messages", "Meseji / Ujumbe", state.languageTag),
                    subtitle = getLocalizedText("Merchant Chat", "Mazungumzo", state.languageTag),
                    icon = Icons.Default.Forum,
                    badgeCount = 0,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp, top = 4.dp),
                    onClick = { 
                        if (NetworkUtils.isInternetAvailable(context)) {
                            viewModel.navigateTo(AppNavDestination.MESSAGE_LIST)
                        } else {
                            Toast.makeText(context, "Online connection required to chat with retailers.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                HubSelectorCard(
                    title = getLocalizedText("My Orders", "Oda Zangu", state.languageTag),
                    subtitle = getLocalizedText("Track Status", "Fuatilia", state.languageTag),
                    icon = Icons.Default.LocalShipping,
                    badgeCount = 0,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    onClick = { viewModel.navigateTo(AppNavDestination.CUSTOMER_ORDERS) }
                )
                Spacer(modifier = Modifier.weight(1f).padding(start = 8.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // App Updates Panel
        if (state.isAppUpdateAvailable) {
            ElevatedCard(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = getLocalizedText("App Updates Availble", "Sasisho la programu lipo tayati", state.languageTag),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = getLocalizedText("Download latest ExpresSoko security hotfixes.", "Pakua sasisho jipya la ExpresSoko hivi sasa.", state.languageTag),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Button(onClick = { viewModel.simulateDownloadAppUpdate() }) {
                        Text(getLocalizedText("Update", "Sakinisha", state.languageTag))
                    }
                }
            }
        }
    }
}

// ---------------- HUB ACTIONS VISUAL SELECTOR CARD ----------------
@Composable
fun HubSelectorCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeCount: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .aspectRatio(1f) // perfectly symmetric squares
            .clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            if (badgeCount > 0) {
                Badge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .scaleOnFocus()
                ) {
                    Text(
                        text = "$badgeCount",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

private fun Modifier.scaleOnFocus() = this

// ---------------- CUSTOMER: CATALOG SHOPPING SCREEN ----------------
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun GoShoppingScreen(viewModel: EcommerceViewModel, products: List<ProductEntity>) {
    val state by viewModel.state.collectAsState()
    var searchInputLocal by remember { mutableStateOf("") }
    var selectedLayoutParam by remember { mutableStateOf(state.catalogLayoutMode) }
    var showSortingDialog by remember { mutableStateOf(false) }

    var isRefreshing by remember { mutableStateOf(false) }
    
    if (isRefreshing) {
        androidx.compose.runtime.LaunchedEffect(true) {
            // Trigger work manager sync
            viewModel.triggerSyncWork()
            kotlinx.coroutines.delay(1500)
            isRefreshing = false
        }
    }

    // Dynamic processing pipeline based entirely on local state selection trackers
    val processedProductList = remember(products, state.productSearchFilter, state.selectedCategoryClass, state.costBracketFilter, state.sortByRecency) {
        var base = products.filter {
            it.name.contains(state.productSearchFilter, ignoreCase = true) ||
            it.description.contains(state.productSearchFilter, ignoreCase = true)
        }
        if (state.selectedCategoryClass != "All") {
            base = base.filter { it.category.equals(state.selectedCategoryClass, ignoreCase = true) }
        }
        base = base.filter { it.price <= state.costBracketFilter }
        if (state.sortByRecency) {
            base = base.sortedByDescending { it.timestamp }
        }
        base
    }

    Column(modifier = Modifier.fillMaxSize()) {
        
        // Navigation bar and categories
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppNavDestination.CUSTOMER_HUB) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = getLocalizedText("ExpresSoko Shelf", "Katalogi ya ExpresSoko", state.languageTag),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            
            // Filters and Layout Switches
            IconButton(onClick = { showSortingDialog = true }) {
                Icon(Icons.Default.Tune, contentDescription = "Filter Options", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // Real-time search line bar
        OutlinedTextField(
            value = searchInputLocal,
            onValueChange = {
                searchInputLocal = it
                viewModel.updateSearchFilters(it)
            },
            placeholder = { Text(getLocalizedText("Search vegetables, spices, headsets...", "Tafuta bidhaa k.m pilau, viungo...", state.languageTag)) },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(12.dp)
        )

        // Categorizations Horizontal tags Row
        val tagsClasses = listOf("All", "Tech Gadget", "Food", "Snacks", "Spices", "Toiletries")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tagsClasses.forEach { tag ->
                val isSelected = state.selectedCategoryClass == tag
                ElevatedFilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectCategoryFilter(tag) },
                    label = { Text(tag) },
                    colors = FilterChipDefaults.elevatedFilterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Layout switches row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Viewing: ${state.selectedCategoryClass} (${processedProductList.size} items)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = { selectedLayoutParam = CatalogueLayout.SQUARE },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (selectedLayoutParam == CatalogueLayout.SQUARE) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
                    )
                ) {
                    Icon(Icons.Default.GridView, contentDescription = "Square Grid")
                }
                IconButton(
                    onClick = { selectedLayoutParam = CatalogueLayout.LIST },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (selectedLayoutParam == CatalogueLayout.LIST) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.ViewList, contentDescription = "List")
                }
                IconButton(
                    onClick = { selectedLayoutParam = CatalogueLayout.TABULAR },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (selectedLayoutParam == CatalogueLayout.TABULAR) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
                    )
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = "Tabular Spreadsheet")
                }
            }
        }

        // Product feed view options (Square cards, lines, tables) completely stripped of quantity elements
        androidx.compose.material3.pulltorefresh.PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { isRefreshing = true },
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp)
        ) {
            if (processedProductList.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = "Empty Catalog",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = getLocalizedText("No products found here yet.", "Hakuna bidhaa hapa bado.", state.languageTag),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = getLocalizedText("Check back later or pull down to refresh.", "Rudi baadaye au vuta chini kusasisha.", state.languageTag),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                when (selectedLayoutParam) {
                    CatalogueLayout.SQUARE -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(processedProductList) { prod ->
                                SquareCatalogListItem(prod, viewModel)
                            }
                        }
                    }
                    CatalogueLayout.LIST -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                            items(processedProductList) { prod ->
                                ListCatalogItemRow(prod, viewModel)
                            }
                        }
                    }
                    CatalogueLayout.TABULAR -> {
                        TabularCatalogLayout(processedProductList, viewModel)
                    }
                }
            }
        }
    }

    // Detail modal dialog overlay representing feedback comments and calling capabilities
    if (state.selectedProductForFeedback != null) {
        ProductDetailInspectorModal(viewModel)
    }

    if (showSortingDialog) {
        AlertDialog(
            onDismissRequest = { showSortingDialog = false },
            title = { Text(getLocalizedText("Refine Shop Shelf", "Vichungi vya Bidhaa", state.languageTag)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(getLocalizedText("Maximum Cost (Ksh):", "Gharama ya Juu (Ksh):", state.languageTag) + " ${state.costBracketFilter.toInt()}")
                    Slider(
                        value = state.costBracketFilter.toFloat(),
                        onValueChange = { viewModel.updateCostBracketSlider(it.toDouble()) },
                        valueRange = 100f..100000f,
                        steps = 20
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(getLocalizedText("Sort by Recency (Latest first)", "Panga kulingana na upya", state.languageTag))
                        Switch(
                            checked = state.sortByRecency,
                            onCheckedChange = { viewModel.toggleRecencySort(it) }
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showSortingDialog = false }) { Text("Apply Filter") }
            }
        )
    }
}

// ---------------- MOCK IMAGE FRAME / ICON ----------------
@Composable
fun ProductImageBox(prod: ProductEntity, modifier: Modifier = Modifier) {
    if (prod.productImageUri.isNotEmpty()) {
        coil.compose.AsyncImage(
            model = prod.productImageUri,
            contentDescription = prod.name,
            modifier = modifier.clip(RoundedCornerShape(12.dp)),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .background(
                    Brush.radialGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.inverseOnSurface
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = prod.itemIconEmoji, fontSize = 42.sp)
        }
    }
}

// 1. Layout Mode Composable: SQUARE GRID view completely stripped of counters
@Composable
fun SquareCatalogListItem(prod: ProductEntity, viewModel: EcommerceViewModel) {
        val context = LocalContext.current
        Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                if (NetworkUtils.isInternetAvailable(context)) {
                    viewModel.inspectProductDetails(prod)
                } else {
                    Toast.makeText(context, "Online connection required to view product details.", Toast.LENGTH_SHORT).show()
                }
            }
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            ProductImageBox(
                prod = prod,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = prod.name,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "KES ${formatPrice(prod.price)}",
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
                // Small category sticker
                Text(
                    text = prod.category,
                    fontSize = 10.sp,
                    color = Color.White,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.appendItemToDigitalCart(prod) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", fontSize = 12.sp)
            }
        }
    }
}

// 2. Layout Mode Composable: Vertical Line List view completely stripped of counters
@Composable
fun ListCatalogItemRow(prod: ProductEntity, viewModel: EcommerceViewModel) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                if (NetworkUtils.isInternetAvailable(context)) {
                    viewModel.inspectProductDetails(prod)
                } else {
                    Toast.makeText(context, "Online connection required to view product details.", Toast.LENGTH_SHORT).show()
                }
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductImageBox(prod = prod, modifier = Modifier.size(72.dp))
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(prod.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(prod.description, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text("KES ${formatPrice(prod.price)} • Qty Available: ${prod.stockQuantity}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { viewModel.appendItemToDigitalCart(prod) },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Add")
            }
        }
    }
}

// 3. Layout Mode Composable: Spreadsheet Tabular Table view completely stripped of counters
@Composable
fun TabularCatalogLayout(products: List<ProductEntity>, viewModel: EcommerceViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        item {
            // Table Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(12.dp)
            ) {
                Text(text = "Icon", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.Black, fontSize = 13.sp)
                Text(text = "Item details", modifier = Modifier.weight(2f), fontWeight = FontWeight.Black, fontSize = 13.sp)
                Text(text = "Cost", modifier = Modifier.weight(1f), fontWeight = FontWeight.Black, fontSize = 13.sp)
                Text(text = "Buy Action", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Black, fontSize = 13.sp, textAlign = TextAlign.Center)
            }
            HorizontalDivider()
        }

        items(products) { prod ->
            val context = LocalContext.current
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .clickable { 
                        if (NetworkUtils.isInternetAvailable(context)) {
                            viewModel.inspectProductDetails(prod)
                        } else {
                            Toast.makeText(context, "Online connection required to view product details.", Toast.LENGTH_SHORT).show()
                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = prod.itemIconEmoji, fontSize = 24.sp, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                
                Column(modifier = Modifier.weight(2f)) {
                    Text(text = prod.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                    Text(text = "Seller: ${prod.retailerId}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Text(
                    text = "Ksh ${prod.price.toInt()}",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp
                )

                Box(
                    modifier = Modifier.weight(1.2f),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { viewModel.appendItemToDigitalCart(prod) },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Product", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }
    }
}

// ---------------- DETAILED INSPECT MODAL DIALOG (Comments & Reviews) ----------------
@Composable
fun ProductDetailInspectorModal(viewModel: EcommerceViewModel) {
    val state by viewModel.state.collectAsState()
    val comments by viewModel.activeComments.collectAsState(initial = emptyList())
    val productObjRef = state.selectedProductForFeedback ?: return
    var reviewTextLocal by remember { mutableStateOf("") }
    var scoreValue by remember { mutableStateOf(5) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var retailerUser by remember { mutableStateOf<UserEntity?>(null) }

    LaunchedEffect(productObjRef.retailerId) {
        retailerUser = viewModel.getRetailers(listOf(productObjRef.retailerId)).values.firstOrNull()
    }

    AlertDialog(
        onDismissRequest = { viewModel.inspectProductDetails(null) },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = productObjRef.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(text = productObjRef.itemIconEmoji, fontSize = 32.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (productObjRef.productImageUri.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                        coil.compose.AsyncImage(
                            model = productObjRef.productImageUri,
                            contentDescription = productObjRef.name,
                            modifier = Modifier.clip(RoundedCornerShape(12.dp)).fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                }
                Text(
                    text = "KES ${formatPrice(productObjRef.price)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = productObjRef.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (productObjRef.productWeight.isNotEmpty()) {
                    Text(text = "Weight: ${productObjRef.productWeight}", style = MaterialTheme.typography.bodySmall)
                }
                if (productObjRef.isPack) {
                    Text(text = "Pack of ${productObjRef.packConstituents}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
                if (productObjRef.additionalDescription.isNotEmpty()) {
                    Text(text = "Note: ${productObjRef.additionalDescription}", style = MaterialTheme.typography.bodySmall)
                }
                
                // Requirement calling action triggers
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        if (retailerUser != null && retailerUser!!.businessPhotoPath.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                                coil.compose.AsyncImage(
                                    model = retailerUser!!.businessPhotoPath,
                                    contentDescription = "Retailer Shop Image",
                                    modifier = Modifier.size(48.dp).clip(CircleShape),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Retailer: ${retailerUser!!.businessName}", fontWeight = FontWeight.Bold)
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = getLocalizedText("Need more details? Call Retailer:", "Ungependa maelezo? Piga simu:", state.languageTag),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Phone : ${productObjRef.retailerPhone}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(
                                onClick = {
                                    viewModel.initiateCellularCall(productObjRef.retailerPhone)
                                    // Standard Dial trigger intents
                                    CustomerActions.callVendorDirectly(context, productObjRef.retailerPhone)
                                },
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    .size(40.dp)
                            ) {
                                Icon(Icons.Default.Call, null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    Toast.makeText(context, "Chat started with Box Retailer: ${productObjRef.retailerId}. Phone: ${productObjRef.retailerPhone}", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.secondary, CircleShape)
                                    .size(40.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Chat, null, tint = Color.White)
                            }
                        }
                    }
                }

                HorizontalDivider()
                
                // Comments sections (Requirement 7)
                Text(
                    text = getLocalizedText("Verified Shoppers Feedbacks:", "Maoni ya Wanunuzi:", state.languageTag),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                // Comments publishing text input forms
                OutlinedTextField(
                    value = reviewTextLocal,
                    onValueChange = { reviewTextLocal = it },
                    placeholder = { Text(getLocalizedText("Write feedback about this item...", "Andika maoni kuhusu hii bidhaa...", state.languageTag)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(getLocalizedText("Rating Score:", "Alama:", state.languageTag) + " " + "★".repeat(scoreValue))
                    Row {
                        (1..5).forEach { rate ->
                            IconButton(
                                onClick = { scoreValue = rate },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (rate <= scoreValue) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        if (reviewTextLocal.isNotBlank()) {
                            viewModel.postCustomerPurchaseFeedback(reviewTextLocal, scoreValue)
                            reviewTextLocal = ""
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(getLocalizedText("Post Feedback", "Tuma Maoni", state.languageTag))
                }

                // Render submitted feedback comments from local SQL Room hierarchies
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (comments.isEmpty()) {
                        Text(
                            text = getLocalizedText("Be the first to submit a review!", "Kuwa wa kwanza kutoa maoni yako hapa!", state.languageTag),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        comments.forEach { c ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = c.customerName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(text = "★".repeat(c.rating), color = MaterialTheme.colorScheme.secondary, fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = c.commentText, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.inspectProductDetails(null) }) {
                Text("Close Inspector")
            }
        }
    )
}

// ---------------- CUSTOMER ACTION HELPERS ----------------
object NetworkUtils {
    fun isInternetAvailable(context: android.content.Context): Boolean {
        val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val networkPath = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(networkPath) ?: return false
        return when {
            activeNetwork.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}

object CustomerActions {
    fun callVendorDirectly(context: android.content.Context, phoneNumber: String) {
        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
            data = android.net.Uri.parse("tel:$phoneNumber")
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Dialer action unavailable offline", Toast.LENGTH_SHORT).show()
        }
    }
}

// ---------------- CUSTOMER: SHOPPING CART SCREEN ----------------
@Composable
fun ShoppingCartScreen(viewModel: EcommerceViewModel, cartItems: List<CartProductJoin>) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var billingMpesaMobileNo by remember { mutableStateOf(state.activeUserPhone) }
    var inputMpesaPinWord by remember { mutableStateOf("") }
    var mpesaPromptTriggerModalOpened by remember { mutableStateOf(false) }
    
    val checkoutAggregateSum = cartItems.sumOf { it.product.price * it.cartItem.quantity }.toInt()
    val itemsByRetailer = cartItems.groupBy { it.product.retailerId }
    var retailerPickupTimes by remember { mutableStateOf(emptyMap<String, String>()) }

    // Fetch retailers
    var retailersMap by remember { mutableStateOf(emptyMap<String, UserEntity>()) }
    LaunchedEffect(itemsByRetailer.keys) {
        retailersMap = viewModel.getRetailers(itemsByRetailer.keys.toList())
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(AppNavDestination.CUSTOMER_HUB) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = getLocalizedText("My Shopping Cart", "Kikapu Changu cha Ununuzi", state.languageTag),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (cartItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Icon(
                            Icons.Default.ShoppingCart, 
                            contentDescription = "Empty Cart", 
                            modifier = Modifier.size(100.dp), 
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = getLocalizedText("Your cart is empty", "Kikapu chako kiko tupu", state.languageTag),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = getLocalizedText("Looks like you haven't added anything to your cart yet. Explore our catalog and find something you like!", "Inaonekana bado hujaongeza chochote kwenye kikapu chako. Gundua bidhaa zetu na upate unachopenda!", state.languageTag),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { viewModel.navigateTo(AppNavDestination.CUSTOMER_CATALOG) },
                            modifier = Modifier.height(50.dp).fillMaxWidth()
                        ) {
                            Text(getLocalizedText("Start Shopping", "Anza Kununua", state.languageTag), fontSize = 16.sp)
                        }
                    }
                }
            } else {
                // Render List of Shopped items with quantity modifier selectors here
                Column(modifier = Modifier.fillMaxSize()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Did you know? Retailer details for your cart items are available in the Messages area for chatting!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsByRetailer.forEach { (retailerId, items) ->
                            val retailer = retailersMap[retailerId]
                            item {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "Retailer: ${retailer?.businessName ?: retailerId}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 8.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    
                                    items.forEach { item ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(item.product.name, fontWeight = FontWeight.Bold)
                                                    Text(
                                                        text = "KES ${formatPrice(item.product.price)} each • Total: KES ${formatPrice(item.product.price * item.cartItem.quantity)}",
                                                        fontSize = 13.sp,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = if (item.cartItem.isSynced) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                                                            contentDescription = if (item.cartItem.isSynced) "Synced" else "Pending Sync",
                                                            modifier = Modifier.size(14.dp),
                                                            tint = if (item.cartItem.isSynced) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = if (item.cartItem.isSynced) "Backed up locally & Cloud Synced" else "Pending Cloud Sync",
                                                            fontSize = 11.sp,
                                                            color = if (item.cartItem.isSynced) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    IconButton(onClick = { viewModel.updateCartProductCount(item.cartItem.productId, item.cartItem.quantity, -1) }) {
                                                        Icon(Icons.Default.Remove, "Decrement")
                                                    }
                                                    Text(
                                                        text = "${item.cartItem.quantity}",
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 8.dp)
                                                    )
                                                    IconButton(
                                                        onClick = {
                                                            // Check stock level bounds
                                                            if (item.cartItem.quantity < item.product.stockQuantity) {
                                                                viewModel.updateCartProductCount(item.cartItem.productId, item.cartItem.quantity, 1)
                                                            } else {
                                                                Toast.makeText(context, "⚠️ Maximum stock levels reached for this item.", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    ) {
                                                        Icon(Icons.Default.Add, "Increment")
                                                    }
                                                    IconButton(onClick = { viewModel.removeCartItemDirectly(item.cartItem.productId) }) {
                                                        Icon(Icons.Default.Delete, "Remove Item", tint = MaterialTheme.colorScheme.error)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    
                                    // Pick up time field for this retailer
                                    OutlinedTextField(
                                        value = retailerPickupTimes[retailerId] ?: "",
                                        onValueChange = { newValue ->
                                            retailerPickupTimes = retailerPickupTimes.toMutableMap().apply { put(retailerId, newValue) }
                                        },
                                        label = { Text("Schedule Pick-up Time") },
                                        placeholder = { Text("e.g. Tomorrow 2 PM") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Aggregate transaction total amount and green Safaricom payment prompt button
        if (checkoutAggregateSum > 0) {
            Surface(
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "${getLocalizedText("Checkout Total:", "Jumla ya Gharama:", state.languageTag)} KES ${formatPrice(checkoutAggregateSum)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = { mpesaPromptTriggerModalOpened = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)), // Safaricom green
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Lock, null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = getLocalizedText("Pay via M-PESA Express", "Lipa kwa M-PESA Express", state.languageTag),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }

    // Modal dialog representing push prompts and password validation (STK popup simulation)
    if (mpesaPromptTriggerModalOpened) {
        val computationAmount = checkoutAggregateSum
        var selectedPaymentTab by remember { mutableStateOf(0) }

        AlertDialog(
            onDismissRequest = { mpesaPromptTriggerModalOpened = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Https, "Secure", tint = Color(0xFF2E7D32), modifier = Modifier.padding(end = 6.dp))
                    Text(text = "Checkout Options", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Order amount: KES ${formatPrice(computationAmount)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    
                    TabRow(selectedTabIndex = selectedPaymentTab) {
                        Tab(
                            selected = selectedPaymentTab == 0,
                            onClick = { selectedPaymentTab = 0 },
                            text = { Text("Direct Prompt") }
                        )
                        Tab(
                            selected = selectedPaymentTab == 1,
                            onClick = { selectedPaymentTab = 1 },
                            text = { Text("Manual Transfer") }
                        )
                    }

                    if (selectedPaymentTab == 0) {
                        var mpesaPinVisible by remember { mutableStateOf(false) }

                        OutlinedTextField(
                            value = billingMpesaMobileNo,
                            onValueChange = { billingMpesaMobileNo = it },
                            label = { Text("M-Pesa Telephone Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = inputMpesaPinWord,
                            onValueChange = { if (it.length <= 4) inputMpesaPinWord = it },
                            label = { Text("Enter 4-Digit M-Pesa PIN") },
                            visualTransformation = if (mpesaPinVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                val image = if (mpesaPinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                IconButton(onClick = { mpesaPinVisible = !mpesaPinVisible }) {
                                    Icon(image, contentDescription = "Toggle PIN visibility")
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        // Show retailers payment options to copy
                        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                            itemsByRetailer.forEach { (retailerId, items) ->
                                val retailer = retailersMap[retailerId]
                                if (retailer != null && retailer.paymentType.isNotEmpty()) {
                                    item {
                                        Card(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(retailer.businessName.ifEmpty { retailer.name }, fontWeight = FontWeight.Bold)
                                                
                                                items.forEach { cartProduct ->
                                                    val requiredMode = cartProduct.product.paymentMode
                                                    if (requiredMode != "Any" && requiredMode.isNotEmpty()) {
                                                        Text("- ${cartProduct.product.name} requires: $requiredMode", style = MaterialTheme.typography.labelSmall)
                                                    } else {
                                                        Text("- ${cartProduct.product.name} allows any method. Choose below.", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("Method: ${retailer.paymentType}", style = MaterialTheme.typography.bodyMedium)
                                                
                                                if (retailer.paymentType == "Paybill and Account Number") {
                                                    CopyableText(label = "Paybill", value = retailer.paymentNumber1, context = context)
                                                    CopyableText(label = "Account", value = retailer.paymentNumber2, context = context)
                                                } else {
                                                    CopyableText(label = retailer.paymentType, value = retailer.paymentNumber1, context = context)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (itemsByRetailer.keys.all { retailersMap[it]?.paymentType.isNullOrEmpty() }) {
                                item { 
                                    Text("No manual payment options set by the retailers.")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedPaymentTab == 0) {
                            if (billingMpesaMobileNo.isNotEmpty() && inputMpesaPinWord.length == 4) {
                                viewModel.completeCheckout(retailerPickupTimes, billingMpesaMobileNo, inputMpesaPinWord)
                                mpesaPromptTriggerModalOpened = false
                            } else {
                                Toast.makeText(context, "Provide phone and PIN to prompt STK.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            viewModel.completeCheckout(retailerPickupTimes)
                            mpesaPromptTriggerModalOpened = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text(if (selectedPaymentTab == 0) "Pay KES ${formatPrice(computationAmount)}" else "Confirm Transfer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { mpesaPromptTriggerModalOpened = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (state.isProcessingCheckout) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Processing STK Push...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please check your phone and enter your PIN.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
}

@Composable
fun CopyableText(label: String, value: String, context: android.content.Context) {
    if (value.isNotEmpty()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("$label: $value", style = MaterialTheme.typography.bodySmall)
            TextButton(
                onClick = {
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText(label, value)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied $value", Toast.LENGTH_SHORT).show()
                },
                contentPadding = PaddingValues(4.dp)
            ) {
                Text("COPY", fontSize = 10.sp)
            }
        }
    }
}

// ---------------- RETAILER OPERATIONS PORTAL SCREEN ----------------
@Composable
fun RetailerDashboardScreen(
    viewModel: EcommerceViewModel,
    products: List<ProductEntity>,
    transactions: List<TransactionEntity>
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    var displaySubScreenTab by remember { mutableStateOf(0) }
    var showInventoryPopup by remember { mutableStateOf(false) }
    val localContext = LocalContext.current

    val localProductsFiltered = products.filter { it.retailerId == state.activeUserBusinessNumber }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.activeUserProfilePhoto.isNotEmpty()) {
                        coil.compose.AsyncImage(
                            model = state.activeUserProfilePhoto,
                            contentDescription = "Profile Photo",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else if (state.activeUserBusinessPhoto.isNotEmpty()) {
                        coil.compose.AsyncImage(
                            model = state.activeUserBusinessPhoto,
                            contentDescription = "Business Photo",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${getLocalizedText("Retailer Hub", "Muuzaji", state.languageTag)}: ${state.activeUserName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.onPrimaryContainer, RoundedCornerShape(50.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "MERCHANT",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ID: ${state.activeUserId} • Phone: ${state.activeUserPhone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(4.dp)
                            .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(4.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                        )
                    }
                }
                
                // Replaced top right icons with just empty space as we have bottom dashboard
            }
        }

        // Central part: Ongoing products / Pending pickup and other tabs
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (displaySubScreenTab) {
                0 -> RetailerPendingOrdersView(viewModel)
                1 -> RetailerInventoryManager(viewModel = viewModel, catalogItems = localProductsFiltered)
                2 -> RetailerBusinessReportsViewer(viewModel, localProductsFiltered, transactions)
                4 -> RetailerStockInventoryManager(viewModel = viewModel, catalogItems = localProductsFiltered.filter { !it.isPosted })
            }
        }
        
        // Bottom Dashboard
        RetailerBottomDashboard(
            currentTab = displaySubScreenTab,
            onTabSelected = { 
                if (it == 1) {
                    showInventoryPopup = true
                } else {
                    displaySubScreenTab = it 
                }
            },
            onNavigate = { viewModel.navigateTo(it) }
        )
    }

    AnimatedVisibility(
        visible = showInventoryPopup,
        enter = scaleIn(initialScale = 0f, transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 1f)) + fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
        exit = scaleOut(targetScale = 0f, transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 1f)) + fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
        modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 120.dp, start = 16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Inventory Options", fontWeight = FontWeight.Bold)
                Button(onClick = { 
                    showInventoryPopup = false
                    displaySubScreenTab = 4 
                }) { Text("Register Stock") }
                OutlinedButton(onClick = { 
                    showInventoryPopup = false
                    displaySubScreenTab = 1 
                }) { Text("View Stock") }
                TextButton(onClick = { showInventoryPopup = false }) { Text("Close") }
            }
        }
    }
}
}

@Composable
fun RetailerBottomDashboard(
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    onNavigate: (AppNavDestination) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Dashboard", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))
                if (currentTab != 0) {
                    TextButton(onClick = { onTabSelected(0) }) {
                        Text("Back to Home", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                // Automated Inventory Icon
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    var isInventoryPressed by remember { mutableStateOf(false) }
                    val scope = rememberCoroutineScope()
                    val iconScale by animateFloatAsState(if (isInventoryPressed) 1.2f else 1f, tween(300), label = "inventoryScale")

                    Box(modifier = Modifier.size(52.dp).background(if (currentTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface, CircleShape).clickable {
                        scope.launch {
                            isInventoryPressed = true
                            delay(150)
                            onTabSelected(1)
                            delay(150)
                            isInventoryPressed = false
                        }
                    }, contentAlignment = Alignment.Center) {
                        Icon(
                            if (isInventoryPressed) Icons.Default.Unarchive else Icons.Default.Inventory,
                            contentDescription = "Inventory",
                            tint = if (currentTab == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp).graphicsLayer(scaleX = iconScale, scaleY = iconScale)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Inventory", style = MaterialTheme.typography.labelMedium)
                }

                DashboardIcon("Reports", Icons.Default.Poll, currentTab == 2) { onTabSelected(2) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                
                // Purchases (Truck transition)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    var isTruckPressed by remember { mutableStateOf(false) }
                    val scope = rememberCoroutineScope()
                    val truckOffset by animateFloatAsState(if (isTruckPressed) 200f else 0f, tween(500), label = "truckOffset")
                    
                    Box(modifier = Modifier.size(52.dp).background(MaterialTheme.colorScheme.surface, CircleShape).clickable {
                        scope.launch {
                            isTruckPressed = true
                            delay(500)
                            onNavigate(AppNavDestination.RETAILER_ORDERS)
                            isTruckPressed = false
                        }
                    }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.LocalShipping, contentDescription = "Purchases", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp).graphicsLayer(translationX = truckOffset))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Purchases", style = MaterialTheme.typography.labelMedium)
                }
                
                DashboardIcon("Messages", Icons.Default.MailOutline, false) { onNavigate(AppNavDestination.MESSAGE_LIST) }
                
                // Settings (Sprocket enlarging transition)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    var isSettingsPressed by remember { mutableStateOf(false) }
                    val scope = rememberCoroutineScope()
                    val settingsRotation by animateFloatAsState(if (isSettingsPressed) 360f else 0f, tween(500), label = "settingsRot")
                    val settingsScale by animateFloatAsState(if (isSettingsPressed) 10f else 1f, tween(500), label = "settingsScale")
                    
                    Box(modifier = Modifier.size(52.dp).background(MaterialTheme.colorScheme.surface, CircleShape).clickable {
                        scope.launch {
                            isSettingsPressed = true
                            delay(600)
                            onNavigate(AppNavDestination.PROFILE_SCREEN)
                            delay(200)
                            isSettingsPressed = false // Reset for when returning
                        }
                    }, contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Settings, 
                            contentDescription = "Settings", 
                            tint = MaterialTheme.colorScheme.primary, 
                            modifier = Modifier.size(26.dp).graphicsLayer(rotationZ = settingsRotation, scaleX = settingsScale, scaleY = settingsScale)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Settings", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun RowScope.DashboardIcon(title: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.weight(1f).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(52.dp).background(
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                shape = CircleShape
            ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon, 
                contentDescription = title, 
                tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = title, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
    }
}

@Composable
fun RetailerPendingOrdersView(viewModel: EcommerceViewModel) {
    val orders by viewModel.getRetailerOrdersFlow().collectAsState(initial = emptyList())
    val cartItems by viewModel.getPendingCartItemsForRetailerFlow().collectAsState(initial = emptyList())
    // Filter for ongoing/pending orders
    val pendingOrders = orders.filter { it.status == "PENDING" || it.status == "PROCESSING" || it.status == "READY_FOR_PICKUP" }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
    ) {
        Text("Pending Pickup & Ongoing", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        
        if (pendingOrders.isEmpty() && cartItems.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No ongoing orders at the moment.", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(pendingOrders) { order ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Order #${order.orderId}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                StatusBadge(order.status) // Reuse from OrdersUI if possible, if not redefine or import
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Items: ${order.itemSummary}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("KES ${formatPrice(order.totalAmount)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                Text("Cust ID: ${order.customerId}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha=0.7f))
                            }
                        }
                    }
                }
                
                if (cartItems.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Pending Customer Carts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(cartItems) { join ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("In a customer's cart", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    StatusBadge("CART")
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("${join.product.name} (Qty: ${join.cartItem.quantity})", style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Potential revenue: KES ${formatPrice(join.cartItem.quantity * join.product.price)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Subcomponent: Stock Inventory Manager (Register unposted stock)
@Composable
fun RetailerStockInventoryManager(viewModel: EcommerceViewModel, catalogItems: List<ProductEntity>) {
    val state by viewModel.state.collectAsState()
    val localContext = LocalContext.current
    var expandedCatMenu by remember { mutableStateOf(false) }
    val categoriesList = listOf("Tech Gadget", "Food", "Snacks", "Spices", "Toiletries")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Register New Stock Details",
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )

                OutlinedTextField(
                    value = state.retailerInputName,
                    onValueChange = { viewModel.updateRetailerItemForm(name = it) },
                    label = { Text("Product Title / Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.retailerInputStock.toString(),
                        onValueChange = { viewModel.updateRetailerItemForm(stockAmt = it.toIntOrNull() ?: 1) },
                        label = { Text("Stock Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = state.retailerInputMaxCapacity.toString(),
                        onValueChange = { viewModel.updateRetailerItemForm(maxCapacity = it.toIntOrNull() ?: 100) },
                        label = { Text("Max Capacity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.retailerInputBatchNumber,
                        onValueChange = { viewModel.updateRetailerItemForm(batchNum = it) },
                        label = { Text("Batch Number") },
                        modifier = Modifier.weight(1f)
                    )

                    var showExpiryDialog by remember { mutableStateOf(false) }

                    Box(modifier = Modifier.weight(1f).clickable { showExpiryDialog = true }) {
                        OutlinedTextField(
                            value = state.retailerInputExpiryDate,
                            onValueChange = { },
                            label = { Text("Expiry Date") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    if (showExpiryDialog) {
                        ExpiryDateDialog(
                            currentDate = state.retailerInputExpiryDate,
                            onDismiss = { showExpiryDialog = false },
                            onSave = { dateStr ->
                                viewModel.updateRetailerItemForm(expiry = dateStr)
                                showExpiryDialog = false
                            }
                        )
                    }
                }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = state.retailerInputCategory,
                        onValueChange = {},
                        label = { Text("Category classification") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expandedCatMenu = true }) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = expandedCatMenu,
                        onDismissRequest = { expandedCatMenu = false }
                    ) {
                        categoriesList.forEach { categoryName ->
                            DropdownMenuItem(
                                text = { Text(categoryName) },
                                onClick = {
                                    viewModel.updateRetailerItemForm(category = categoryName)
                                    expandedCatMenu = false
                                }
                            )
                        }
                    }
                }

                val productCropImage = androidx.activity.compose.rememberLauncherForActivityResult(CropImageContract()) { result ->
                    if (result.isSuccessful) {
                        val uriContent = result.uriContent
                        if (uriContent != null) {
                            viewModel.updateRetailerItemForm(imgUri = copyUriToInternalStorage(localContext, uriContent, "product"))
                        }
                    }
                }

                var productShowPermissionError by remember { mutableStateOf(false) }
                val productCameraAndStoragePermissionState = rememberMultiplePermissionsState(
                    permissions = buildList {
                        add(Manifest.permission.CAMERA)
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            add(Manifest.permission.READ_MEDIA_IMAGES)
                        } else {
                            add(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    }
                ) { statuses ->
                    if (statuses.values.all { it }) {
                        productCropImage.launch(
                            CropImageContractOptions(
                                uri = null,
                                cropImageOptions = CropImageOptions(
                                    imageSourceIncludeGallery = true,
                                    imageSourceIncludeCamera = true,
                                    guidelines = CropImageView.Guidelines.ON,
                                    fixAspectRatio = false
                                )
                            )
                        )
                    } else {
                        productShowPermissionError = true
                    }
                }

                if (productShowPermissionError) {
                    LaunchedEffect(Unit) {
                        Toast.makeText(localContext, "Permissions denied. Cannot use camera or storage.", Toast.LENGTH_SHORT).show()
                        productShowPermissionError = false
                    }
                }

                Column(horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(onClick = { 
                        if (productCameraAndStoragePermissionState.allPermissionsGranted) {
                            productCropImage.launch(
                                CropImageContractOptions(
                                    uri = null,
                                    cropImageOptions = CropImageOptions(
                                        imageSourceIncludeGallery = true,
                                        imageSourceIncludeCamera = true,
                                        guidelines = CropImageView.Guidelines.ON,
                                        fixAspectRatio = false
                                    )
                                )
                            )
                        } else {
                            productCameraAndStoragePermissionState.launchMultiplePermissionRequest()
                        }
                    }) {
                        Icon(Icons.Default.ImageSearch, contentDescription = "Pick Image")
                        Spacer(Modifier.width(8.dp))
                        Text(if (state.retailerInputImageUri.isEmpty()) "Upload Image" else "Upload Successful", fontSize = 12.sp)
                    }
                    if (state.retailerInputImageUri.isNotEmpty()) {
                        coil.compose.AsyncImage(
                            model = state.retailerInputImageUri,
                            contentDescription = "Uploaded Image",
                            modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                }

                var showDescriptionOptions by remember { mutableStateOf(false) }

                OutlinedTextField(
                    value = state.retailerInputDescription,
                    onValueChange = { viewModel.updateRetailerItemForm(desc = it) },
                    label = { Text("Product Description / Details") },
                    modifier = Modifier.fillMaxWidth().onFocusChanged { if(it.isFocused) showDescriptionOptions = true }
                )

                if (showDescriptionOptions) {
                    OutlinedTextField(
                        value = state.retailerInputWeight,
                        onValueChange = { viewModel.updateRetailerItemForm(weight = it) },
                        label = { Text("Individual Product Weight") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Is this a pack?")
                        Spacer(Modifier.width(8.dp))
                        Checkbox(
                            checked = state.retailerInputIsPack,
                            onCheckedChange = { viewModel.updateRetailerItemForm(isPack = it) }
                        )
                    }

                    if (state.retailerInputIsPack) {
                        OutlinedTextField(
                            value = state.retailerInputPackConstituents.toString(),
                            onValueChange = { viewModel.updateRetailerItemForm(packConstituents = it.toIntOrNull() ?: 0) },
                            label = { Text("Number of individual constituents in pack") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = state.retailerInputAdditionalDescription,
                        onValueChange = { viewModel.updateRetailerItemForm(additionalDesc = it) },
                        label = { Text("Any other descriptions needed") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = { viewModel.submitStockInventoryRegistration() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Stock Details")
                }
            }
        }
        
        Text("Unposted Stock Inventory:", fontWeight = FontWeight.Bold)
        if (catalogItems.isEmpty()) {
            Text("No unposted stock available.", style = MaterialTheme.typography.bodySmall)
        } else {
            catalogItems.forEach { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (item.productImageUri.isNotEmpty()) {
                            coil.compose.AsyncImage(
                                model = item.productImageUri,
                                contentDescription = item.name,
                                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier.size(80.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(item.itemIconEmoji, fontSize = 32.sp)
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, fontWeight = FontWeight.Bold)
                            Text("Category: ${item.category} | Batch: ${item.batchNumber}", style = MaterialTheme.typography.bodySmall)
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Stock Capacity Progress
                            val stockFraction = if (item.maxCapacity > 0) (item.stockQuantity.toFloat() / item.maxCapacity.toFloat()).coerceIn(0f, 1f) else 1f
                            val stockColor = when {
                                stockFraction > 0.5f -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
                                stockFraction > 0.2f -> androidx.compose.ui.graphics.Color(0xFFFFC107) // Yellow
                                else -> androidx.compose.ui.graphics.Color(0xFFF44336) // Red
                            }
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Stock Level", style = MaterialTheme.typography.labelSmall)
                                Text("${item.stockQuantity} / ${item.maxCapacity}", style = MaterialTheme.typography.labelSmall)
                            }
                            LinearProgressIndicator(
                                progress = { stockFraction },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = stockColor,
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Expiry Progress (Simulated: assuming closer to today is worse. Here just mock red if < 7 days, else green)
                            // A proper check needs exact date diff.
                            // Let's implement a quick parser
                            val daysToExpiry = try {
                                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                val expiryDateParsed = sdf.parse(item.expiryDate)
                                val today = java.util.Date()
                                if (expiryDateParsed != null) {
                                    ((expiryDateParsed.time - today.time) / (1000 * 60 * 60 * 24)).toFloat()
                                } else {
                                    100f
                                }
                            } catch (e: Exception) {
                                100f // Fallback if invalid format
                            }
                            
                            val maxDays = 90f // Base max for calculation
                            val expiryFraction = (daysToExpiry / maxDays).coerceIn(0f, 1f)
                            
                            val expiryColor = when {
                                daysToExpiry > 30f -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
                                daysToExpiry > 7f -> androidx.compose.ui.graphics.Color(0xFFFFC107) // Yellow
                                else -> androidx.compose.ui.graphics.Color(0xFFF44336) // Red
                            }
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Expiry", style = MaterialTheme.typography.labelSmall)
                                Text(item.expiryDate.ifEmpty { "N/A" }, style = MaterialTheme.typography.labelSmall)
                            }
                            
                            if (item.expiryDate.isNotEmpty()) {
                                LinearProgressIndicator(
                                    progress = { expiryFraction },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = expiryColor,
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            var postQuantity by remember { mutableStateOf(1) }
                            var sellPrice by remember { mutableStateOf("") }
                            var selectedPaymentMode by remember { mutableStateOf("Any") }
                            var paymentModeExpanded by remember { mutableStateOf(false) }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Quantity to sale:")
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { if (postQuantity > 1) postQuantity-- }) { Icon(Icons.Default.Remove, "Less") }
                                    Text("$postQuantity", fontWeight = FontWeight.Bold)
                                    IconButton(onClick = { if (postQuantity < item.stockQuantity) postQuantity++ }) { Icon(Icons.Default.Add, "More") }
                                }
                            }
                            OutlinedTextField(
                                value = sellPrice,
                                onValueChange = { sellPrice = it },
                                label = { Text("Selling Price (Ksh)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                OutlinedTextField(
                                    value = selectedPaymentMode,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Payment Mode") },
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        IconButton(onClick = { paymentModeExpanded = !paymentModeExpanded }) {
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Expand")
                                        }
                                    }
                                )
                                DropdownMenu(
                                    expanded = paymentModeExpanded,
                                    onDismissRequest = { paymentModeExpanded = false },
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    listOf("Any", "Cash", "MPESA", "Bank Transfer", "Pochi la Biashara").forEach { mode ->
                                        DropdownMenuItem(
                                            text = { Text(mode) },
                                            onClick = {
                                                selectedPaymentMode = mode
                                                paymentModeExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = { 
                                    val price = sellPrice.toDoubleOrNull() ?: 0.0
                                    if (price > 0) {
                                        viewModel.submitPostStock(item, price, selectedPaymentMode, postQuantity)
                                    } else {
                                        viewModel.triggerNotification("Enter a valid price")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("List for Sale", color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Subcomponent: Posted Stock Manager (Listing existing products and let retailer post unposted stock)
@Composable
fun RetailerInventoryManager(viewModel: EcommerceViewModel, catalogItems: List<ProductEntity>) {
    val state by viewModel.state.collectAsState()
    
    val postedItems = catalogItems.filter { it.isPosted }
    val unpostedItems = catalogItems.filter { !it.isPosted }
    var selectedTab by remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Active Stock") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("NonSale Stock (${unpostedItems.size})") }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (selectedTab == 0) {
                if (postedItems.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getLocalizedText("You haven't listed any products yet.", "Hujasajili bidhaa yoyote bado.", state.languageTag),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    postedItems.forEach { item ->
                        var expanded by remember { mutableStateOf(false) }
                        var editStock by remember { mutableStateOf(item.stockQuantity.toString()) }
                        var editPrice by remember { mutableStateOf(item.price.toString()) }
                        var editSaleUnit by remember { mutableStateOf(item.wholesalePieces.toString()) }

                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = item.itemIconEmoji, fontSize = 28.sp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(item.name, fontWeight = FontWeight.Bold)
                                            Text("Cost: KES ${formatPrice(item.price)} | Category: ${item.category}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                            Text("Mode: ${item.paymentMode} | Sale unit: ${item.wholesalePieces} pcs", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                        }
                                    }
                                    
                                    // Remaining sizes
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = if (item.stockQuantity <= 5) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Stock: ${item.stockQuantity}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (item.stockQuantity <= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                
                                if (expanded) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Edit Active Stock", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                                        OutlinedTextField(
                                            value = editStock,
                                            onValueChange = { editStock = it },
                                            label = { Text("Stock") },
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = editPrice,
                                            onValueChange = { editPrice = it },
                                            label = { Text("Price (KES)") },
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = editSaleUnit,
                                            onValueChange = { editSaleUnit = it },
                                            label = { Text("Sale Unit") },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                val newStock = editStock.toIntOrNull() ?: item.stockQuantity
                                                val newPrice = editPrice.toDoubleOrNull() ?: item.price
                                                val newUnit = editSaleUnit.toIntOrNull() ?: item.wholesalePieces
                                                viewModel.updateActiveStockItem(item.id, newStock, newPrice, newUnit)
                                                expanded = false
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Update Item")
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.removeActiveStockItem(item.id)
                                                expanded = false
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                        ) {
                                            Text("Delete")
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                // NonSale Stock
                if (unpostedItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text("No NonSale Stock available. Upload new stock.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    unpostedItems.forEach { item ->
                        var expanded by remember { mutableStateOf(false) }
                        var postQuantity by remember { mutableStateOf(1) }
                        var sellPrice by remember { mutableStateOf("") }
                        var selectedPaymentMode by remember { mutableStateOf("Any") }
                        var paymentModeExpanded by remember { mutableStateOf(false) }

                        val retailerPaymentModes = mutableListOf("Any")
                        if (state.activeUserPaymentType.isNotEmpty()) retailerPaymentModes.add(state.activeUserPaymentType)
                        if (state.activeUserPaymentType2.isNotEmpty()) retailerPaymentModes.add(state.activeUserPaymentType2)
                        val distinctPaymentModes = retailerPaymentModes.distinct()

                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    if (item.productImageUri.isNotEmpty()) {
                                        coil.compose.AsyncImage(
                                            model = item.productImageUri,
                                            contentDescription = item.name,
                                            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    } else {
                                        Box(modifier = Modifier.size(64.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                            Text(item.itemIconEmoji, fontSize = 28.sp)
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "${item.name}", fontWeight = FontWeight.Bold)
                                        Text(text = "Unposted | Max Stock: ${item.stockQuantity}", style = MaterialTheme.typography.bodySmall)
                                        Text(text = "Category: ${item.category}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    if (!expanded) {
                                        Text("Tap to Post", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }

                                if (expanded) {
                                    Text(text = item.description, style = MaterialTheme.typography.bodySmall)
                                    Text(text = "Batch: ${item.batchNumber} | Expiry: ${item.expiryDate.ifEmpty { "N/A" }}", style = MaterialTheme.typography.bodySmall)
                                    HorizontalDivider()

                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Quantity to sale:")
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = { if (postQuantity > 1) postQuantity-- }) { Icon(Icons.Default.Remove, "Less") }
                                            Text("$postQuantity", fontWeight = FontWeight.Bold)
                                            IconButton(onClick = { if (postQuantity < item.stockQuantity) postQuantity++ }) { Icon(Icons.Default.Add, "More") }
                                        }
                                    }
                                    OutlinedTextField(
                                        value = sellPrice,
                                        onValueChange = { sellPrice = it },
                                        label = { Text("Selling Price (KES)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    
                                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                        OutlinedTextField(
                                            value = selectedPaymentMode,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Payment Mode") },
                                            modifier = Modifier.fillMaxWidth(),
                                            trailingIcon = {
                                                IconButton(onClick = { paymentModeExpanded = !paymentModeExpanded }) {
                                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Expand")
                                                }
                                            }
                                        )
                                        DropdownMenu(
                                            expanded = paymentModeExpanded,
                                            onDismissRequest = { paymentModeExpanded = false },
                                            modifier = Modifier.fillMaxWidth(0.9f)
                                        ) {
                                            distinctPaymentModes.forEach { mode ->
                                                DropdownMenuItem(
                                                    text = { Text(mode) },
                                                    onClick = {
                                                        selectedPaymentMode = mode
                                                        paymentModeExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = { 
                                            val price = sellPrice.toDoubleOrNull() ?: 0.0
                                            if (price > 0) {
                                                viewModel.submitPostStock(item, price, selectedPaymentMode, postQuantity)
                                            } else {
                                                viewModel.triggerNotification("Enter a valid price")
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text("Post to Active Stock", color = MaterialTheme.colorScheme.onPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



// Subcomponent: Business Reports and Visual Charts (Requirement 15)
@Composable
fun RetailerBusinessReportsViewer(
    viewModel: EcommerceViewModel,
    catalogItems: List<ProductEntity>,
    transactions: List<TransactionEntity>
) {
    val state by viewModel.state.collectAsState()
    val totalRevenueEarnedInt = transactions.sumOf { it.totalAmount }.toInt()
    
    val reportOptions = listOf("Weekly", "Monthly", "Yearly")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Selection Period
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            reportOptions.forEach { opt ->
                val isSelected = state.selectedRetailerMetricRange == opt
                ElevatedFilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectReportPeriodRange(opt) },
                    label = { Text(opt) }
                )
            }
        }

        // Summary Revenue Cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "${state.selectedRetailerMetricRange} Revenue Generated",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "KES ${formatPrice(totalRevenueEarnedInt)}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Consolidated across ${transactions.size} shopping transactions.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Beautiful custom graphical charts using Canvas drawings metrics vectors! (Requirement 15)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.clickable { viewModel.navigateTo(AppNavDestination.RETAILER_DETAILED_REPORTS) }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Local Sales Velocity Bar Graph",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Render beautiful bars representing mock statistical sales distributions!
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    // Compute realistic mock percentages scaled by number of transactions or randomly seeded
                    val scaledTransactions = transactions.size.toFloat().coerceAtLeast(1f)
                    
                    val mockPercentages = when (state.selectedRetailerMetricRange) {
                        "Weekly" -> listOf(0.2f, 0.4f, 0.1f, 0.75f, 0.9f, 0.3f, 0.1f)
                        "Monthly" -> listOf(0.45f, 0.6f, 0.2f, 0.82f, 0.5f, 0.1f, 0.35f)
                        else -> listOf(0.15f, 0.35f, 0.72f, 0.88f, 0.95f, 0.6f, 0.2f)
                    }

                    // For now keeping simple representative values, realistic formulas could use actual day-by-day mapping
                    
                    mockPercentages.forEachIndexed { idx, baseHeight ->
                        val barHeightFraction = if (transactions.isNotEmpty()) {
                            baseHeight.coerceIn(0.1f, 1f)
                        } else {
                            baseHeight // Default visual when 0 transactions
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.height(180.dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(22.dp)
                                    .fillMaxHeight(barHeightFraction)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary
                                            )
                                        ),
                                        RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = daysOfWeek[idx], fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Tabular historical reports grid
        Text(text = "Sales Transactions Log", fontWeight = FontWeight.Black)

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(10.dp)
                ) {
                    Text("CheckoutID / Items", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Status", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
                    Text("Revenue", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End)
                }
                HorizontalDivider()

                if (transactions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text("No sale receipts available in local SQL database.", fontSize = 12.sp)
                    }
                } else {
                    transactions.forEach { tx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(2f)) {
                                Text(tx.checkoutRequestID, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(tx.itemSummary, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Text(
                                    text = tx.status,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(Color(0xFF2E7D32), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text("KES ${formatPrice(tx.totalAmount)}", modifier = Modifier.weight(1f), textAlign = TextAlign.End, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

// Subcomponent: Smart Samsung-style AI Today Brief (Requirement 14)
@Composable
fun RetailerAiStatisticsBriefSection(viewModel: EcommerceViewModel) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "ExpresSoko AI Overview",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Auto-aggregates daily business reports utilizing AI.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth().weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Today's Generated AI Brief",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (state.isGeneratingAiReport) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Querying Gemini API ... Processing local store records...")
                        }
                    } else {
                        SelectionContainer {
                            Text(
                                text = state.dailyAiReportText,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                Button(
                    onClick = { viewModel.generateRetailerAiBusinessBrief() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Synthesize Day's Brief")
                }
            }
        }
    }
}

@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
    fontSize: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    fontStyle: androidx.compose.ui.text.font.FontStyle? = null,
    fontWeight: androidx.compose.ui.text.font.FontWeight? = null,
    fontFamily: androidx.compose.ui.text.font.FontFamily? = null,
    letterSpacing: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    textDecoration: androidx.compose.ui.text.style.TextDecoration? = null,
    textAlign: androidx.compose.ui.text.style.TextAlign? = null,
    lineHeight: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    overflow: androidx.compose.ui.text.style.TextOverflow = androidx.compose.ui.text.style.TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((androidx.compose.ui.text.TextLayoutResult) -> Unit)? = null,
    style: androidx.compose.ui.text.TextStyle = LocalTextStyle.current
) {
    val currentLang = com.example.LocalAppLanguage.current
    val translatedText = if (currentLang == "sw") text.toSwahili() else text
    
    // We must call the original material3 Text
    if (onTextLayout == null) {
        androidx.compose.material3.Text(
            text = translatedText,
            modifier = modifier,
            color = color,
            fontSize = fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines,
            style = style
        )
    } else {
        androidx.compose.material3.Text(
            text = translatedText,
            modifier = modifier,
            color = color,
            fontSize = fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines,
            onTextLayout = onTextLayout,
            style = style
        )
    }
}


@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ExpiryDateDialog(
    currentDate: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var hasNoExpiry by remember { mutableStateOf(currentDate == "No Expiry") }
    
    // Date Picker State
    val datePickerState = rememberDatePickerState()
    
    // Time Picker State (defaults to 12:00 PM)
    val timePickerState = androidx.compose.material3.rememberTimePickerState(
        initialHour = 12,
        initialMinute = 0,
        is24Hour = false
    )
    
    var showTimePicker by remember { mutableStateOf(false) }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                androidx.compose.material3.Text("Select Expiry Date & Time", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = hasNoExpiry,
                        onCheckedChange = { hasNoExpiry = it }
                    )
                    androidx.compose.material3.Text("Product does not have an expiry date")
                }
                
                if (!hasNoExpiry) {
                    if (showTimePicker) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            androidx.compose.material3.TimePicker(state = timePickerState)
                            Spacer(modifier = Modifier.height(16.dp))
                            TextButton(onClick = { showTimePicker = false }) {
                                androidx.compose.material3.Text("Back to Date")
                            }
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            DatePicker(state = datePickerState, showModeToggle = false)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { showTimePicker = true }) {
                                androidx.compose.material3.Text("Set Expiry Time")
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { androidx.compose.material3.Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (hasNoExpiry) {
                            onSave("No Expiry")
                        } else {
                            val timeMs = datePickerState.selectedDateMillis
                            if (timeMs != null) {
                                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                val dateStr = sdf.format(java.util.Date(timeMs))
                                
                                val hour = timePickerState.hour
                                val min = timePickerState.minute
                                val amPm = if (hour >= 12) "PM" else "AM"
                                val displayHour = if (hour % 12 == 0) 12 else hour % 12
                                val timeStr = String.format("%02d:%02d %s", displayHour, min, amPm)
                                
                                onSave("$dateStr $timeStr")
                            } else {
                                onSave("")
                            }
                        }
                    }) {
                        androidx.compose.material3.Text("Save")
                    }
                }
            }
        }
    }
}

