package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TransactionEntity
import com.example.viewmodel.AppNavDestination
import com.example.viewmodel.EcommerceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetailerDetailedReportsScreen(viewModel: EcommerceViewModel, transactions: List<TransactionEntity>) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var selectedChartType by remember { mutableStateOf(0) } // 0: Bar, 1: Line, 2: Pie

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detailed Business Reports") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppNavDestination.RETAILER_HUB) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                listOf("Daily", "Weekly", "Monthly", "Yearly").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // Controls for visualization type
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                FilterChip(selected = selectedChartType == 0, onClick = { selectedChartType = 0 }, label = { Text("Tabular Data") }, leadingIcon = { Icon(Icons.Default.TableChart, null) })
                FilterChip(selected = selectedChartType == 1, onClick = { selectedChartType = 1 }, label = { Text("Line Graph") }, leadingIcon = { Icon(Icons.Default.ShowChart, null) })
                FilterChip(selected = selectedChartType == 2, onClick = { selectedChartType = 2 }, label = { Text("Pie Chart") }, leadingIcon = { Icon(Icons.Default.PieChart, null) })
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Data Processing
            val sdf = SimpleDateFormat(
                when (selectedTab) {
                    0 -> "yyyy-MM-dd"
                    1 -> "yyyy-'W'ww"
                    2 -> "yyyy-MM"
                    else -> "yyyy"
                }, Locale.getDefault()
            )
            
            val groupedData = transactions.groupBy { sdf.format(Date(it.timestamp)) }
                .mapValues { entry -> entry.value.sumOf { it.totalAmount } }
                .toList().sortedByDescending { it.first }

            if (selectedChartType == 0) {
                // Tabular Representation
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer).padding(12.dp)) {
                            Text("Period", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            Text("Revenue", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                        }
                        Divider()
                        if (groupedData.isEmpty()) {
                            Text("No transaction data available for this view.", modifier = Modifier.padding(16.dp))
                        } else {
                            LazyColumn {
                                items(groupedData) { (period, amount) ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                        Text(period, modifier = Modifier.weight(1f))
                                        Text("KES ${amount.toInt()}", modifier = Modifier.weight(1f), textAlign = TextAlign.End, fontWeight = FontWeight.SemiBold)
                                    }
                                    Divider()
                                }
                            }
                        }
                    }
                }
            } else {
                // Graphical Representation (Placeholder for complex charts)
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            if (selectedChartType == 1) Icons.Default.ShowChart else Icons.Default.PieChart,
                            contentDescription = null,
                            modifier = Modifier.size(100.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Interactive ${if (selectedChartType == 1) "Line Graph" else "Pie Chart"}\nFeature simulated. Data points: ${groupedData.size}",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}
