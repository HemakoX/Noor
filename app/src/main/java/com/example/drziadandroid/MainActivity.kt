package com.example.drziadandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.drziadandroid.ui.screens.QuotelyHomeScreen
import com.example.drziadandroid.ui.theme.QuotelyTheme
import com.example.drziadandroid.ui.viewmodel.QuotelyViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: QuotelyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            QuotelyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    QuotelyHomeScreen(viewModel = viewModel)
                }
            }
        }
    }
}
