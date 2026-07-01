package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.viewmodel.EcommerceViewModel
import com.example.viewmodel.AppNavDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerOrdersScreen(viewModel: EcommerceViewModel) {
    val orders by viewModel.getCustomerOrdersFlow().collectAsState(initial = emptyList())
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("My Orders") },
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateTo(AppNavDestination.CUSTOMER_HUB) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
        
        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("You have no recent orders.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(orders) { order ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Order #${order.orderId}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                StatusBadge(order.status)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Items: ${order.itemSummary}", style = MaterialTheme.typography.bodyMedium)
                            if (order.pickupTime.isNotEmpty()) {
                                Text("Scheduled Pick-up: ${order.pickupTime}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Amount: KES ${order.totalAmount.toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetailerOrdersScreen(viewModel: EcommerceViewModel) {
    val orders by viewModel.getRetailerOrdersFlow().collectAsState(initial = emptyList())
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Manage Orders") },
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateTo(AppNavDestination.RETAILER_HUB) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
        
        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No orders received yet.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(orders) { order ->
                    var expanded by remember { mutableStateOf(false) }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        onClick = { expanded = !expanded }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Order #${order.orderId}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("KES ${order.totalAmount.toInt()}", style = MaterialTheme.typography.bodyMedium)
                                }
                                StatusBadge(order.status)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Items: ${order.itemSummary}", style = MaterialTheme.typography.bodyMedium)
                            if (order.pickupTime.isNotEmpty()) {
                                Text("Scheduled Pick-up: ${order.pickupTime}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Text("Customer ID: ${order.customerId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            
                            if (expanded) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    if (order.status == "PENDING") {
                                        Button(onClick = { viewModel.updateOrderStatus(order.orderId, "PROCESSING") }) {
                                            Text("Accept")
                                        }
                                        OutlinedButton(onClick = { viewModel.updateOrderStatus(order.orderId, "CANCELLED") }) {
                                            Text("Cancel")
                                        }
                                    } else if (order.status == "PROCESSING") {
                                        Button(onClick = { viewModel.updateOrderStatus(order.orderId, "READY_FOR_PICKUP") }) {
                                            Text("Ready")
                                        }
                                        OutlinedButton(onClick = { viewModel.updateOrderStatus(order.orderId, "SHIPPED") }) {
                                            Text("Shipped")
                                        }
                                    } else if (order.status == "READY_FOR_PICKUP" || order.status == "SHIPPED") {
                                        Button(onClick = { viewModel.updateOrderStatus(order.orderId, "DELIVERED") }) {
                                            Text("Mark Delivered")
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
}

@Composable
fun StatusBadge(status: String) {
    val (color, icon, text) = when (status) {
        "PENDING" -> Triple(Color(0xFFFFA000), Icons.Default.Warning, "Pending")
        "PROCESSING" -> Triple(Color(0xFF1976D2), Icons.Default.Inventory, "Processing")
        "READY_FOR_PICKUP" -> Triple(Color(0xFF388E3C), Icons.Default.CheckCircle, "Ready")
        "SHIPPED" -> Triple(Color(0xFF1976D2), Icons.Default.LocalShipping, "Shipped")
        "DELIVERED" -> Triple(Color(0xFF388E3C), Icons.Default.CheckCircle, "Delivered")
        "CANCELLED" -> Triple(Color(0xFFD32F2F), Icons.Default.Close, "Cancelled")
        else -> Triple(Color.Gray, Icons.Default.Warning, status)
    }
    
    Surface(
        color = color.copy(alpha = 0.15f),
        contentColor = color,
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}
