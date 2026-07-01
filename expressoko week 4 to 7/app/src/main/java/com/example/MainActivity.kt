package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.screens.MainECommerceAppLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewmodel.EcommerceViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appViewModel: EcommerceViewModel = viewModel()
            val state by appViewModel.state.collectAsState()
            
            androidx.compose.runtime.CompositionLocalProvider(
                com.example.LocalAppLanguage provides state.languageTag
            ) {
                MyApplicationTheme(appColor = state.appColor) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.background
                    ) {
                        MainECommerceAppLayout(appViewModel)
                    }
                }
            }
        }
    }
}
