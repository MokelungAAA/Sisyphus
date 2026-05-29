package com.mokelab.sisyphus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mokelab.sisyphus.core.preferences.ThemePreferences
import com.mokelab.sisyphus.core.ui.theme.SisyphusTheme
import com.mokelab.sisyphus.ui.SisyphusApp
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val themePreferences: ThemePreferences by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var darkMode by remember { mutableStateOf(themePreferences.isDarkMode()) }

            SisyphusTheme(darkTheme = darkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SisyphusApp()
                }
            }
        }
    }
}
