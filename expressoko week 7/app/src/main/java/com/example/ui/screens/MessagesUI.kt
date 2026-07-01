package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.viewmodel.EcommerceViewModel
import com.example.viewmodel.AppNavDestination

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MessagesListScreen(viewModel: EcommerceViewModel) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    
    // We can simulate fetching the active cart to guide chatting
    val cartItems by viewModel.activeCartItems.collectAsState(initial = emptyList())
    // Get all chat messages to show chat history list
    val allChatMessages by viewModel.getAllUserMessages().collectAsState(initial = emptyList())
    
    // Get all unique retailers from cart AND chat history
    val retailersInCart = cartItems.map { Pair(it.product.retailerId, it.product.retailerPhone) }
    // From chat history, find the other person's ID
    val retailersInChat = allChatMessages.map { 
        val otherId = if (it.senderId == state.activeUserId) it.receiverId else it.senderId
        // Try to infer phone number or use placeholder
        Pair(otherId, otherId) // Use ID as name if we don't have it
    }
    
    val combinedRetailers = (retailersInCart + retailersInChat).distinctBy { it.first }.filter { it.first != state.activeUserId }

    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Messages") },
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateTo(if (state.activeUserRole == "Retailer") AppNavDestination.RETAILER_HUB else AppNavDestination.CUSTOMER_HUB) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
        
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            item {
                Text("Welcome to App Messages!", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Long press on a chat to delete the conversation history.", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            items(combinedRetailers) { retailer ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .combinedClickable(
                            onClick = {
                                if (NetworkUtils.isInternetAvailable(context)) {
                                    viewModel.openChatWithRetailer(retailer.first, retailer.second, retailer.first)
                                } else {
                                    Toast.makeText(context, "Online connection required for chatting.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onLongClick = {
                                showDeleteDialog = retailer.first
                            }
                        )
                ) {
                    ListItem(
                        headlineContent = { Text(retailer.first) },
                        supportingContent = { Text(retailer.second) },
                        leadingContent = {
                            Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    )
                }
            }
        }
        
        if (showDeleteDialog != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Delete Chat?") },
                text = { Text("Are you sure you want to permanently delete your chat history with ${showDeleteDialog}?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteChatBetweenUsers(showDeleteDialog!!)
                        Toast.makeText(context, "Chat deleted.", Toast.LENGTH_SHORT).show()
                        showDeleteDialog = null
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: EcommerceViewModel) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val messages by viewModel.getChatMessagesForActiveChat().collectAsState(initial = emptyList())
    var inputText by remember { mutableStateOf("") }
    
    val isTyping by viewModel.observeTypingStatus().collectAsState(initial = false)

    LaunchedEffect(inputText) {
        if (inputText.isNotEmpty()) {
            viewModel.updateTypingStatus(true)
            kotlinx.coroutines.delay(5000)
            viewModel.updateTypingStatus(false)
        } else {
            viewModel.updateTypingStatus(false)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { 
                Column {
                    Text(state.activeChatRetailerName, style = MaterialTheme.typography.titleMedium)
                    if (isTyping) {
                        Text("typing...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    } else {
                        Text(state.activeChatRetailerPhone, style = MaterialTheme.typography.labelSmall)
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateTo(AppNavDestination.MESSAGE_LIST) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { 
                    viewModel.deleteChatBetweenUsers(state.activeChatRetailerId)
                    Toast.makeText(context, "Chat history deleted", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Chat")
                }
            }
        )
        
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp),
            reverseLayout = false
        ) {
            itemsIndexed(messages) { index, msg ->
                // Date separator logic
                var showDateHeader = false
                if (index == 0) {
                    showDateHeader = true
                } else {
                    val prevMsg = messages[index - 1]
                    val delta = msg.timestamp - prevMsg.timestamp
                    if (delta > 24 * 60 * 60 * 1000L) { // > 24 hours
                        showDateHeader = true
                    }
                }

                if (showDateHeader) {
                    val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                    val dateStr = dateFormat.format(java.util.Date(msg.timestamp))
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
                            Text(dateStr, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }

                val isMine = msg.senderId == state.activeUserId
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = msg.messageText,
                                color = if (isMine) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (isMine) {
                                Row(modifier = Modifier.align(Alignment.End).padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    val icon = when (msg.status) {
                                        "Pending" -> Icons.Default.Schedule
                                        "Sent" -> Icons.Default.Check
                                        "Delivered", "Read" -> Icons.Default.DoneAll
                                        "Failed" -> Icons.Default.Error
                                        else -> Icons.Default.Schedule
                                    }
                                    val iconTint = if (msg.status == "Read") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                                    Icon(icon, contentDescription = msg.status, modifier = Modifier.size(16.dp), tint = iconTint)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { 
                Toast.makeText(context, "Attachment functionality goes here.", Toast.LENGTH_SHORT).show() 
            }) {
                Icon(Icons.Default.AttachFile, contentDescription = "Attach")
            }
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message") },
                maxLines = 3
            )
            IconButton(onClick = {
                if (NetworkUtils.isInternetAvailable(context)) {
                    viewModel.sendChatMessage(inputText)
                    inputText = ""
                } else {
                    Toast.makeText(context, "Online connection required to send messages.", Toast.LENGTH_SHORT).show()
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
    }
}
