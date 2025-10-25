package com.example.myapplication1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { AboutScreen(onClose = { finish() }) } }
    }
}

@Composable
fun AboutScreen(onClose: () -> Unit) {
    @file:OptIn(ExperimentalMaterial3Api::class)
    Scaffold(topBar = { TopAppBar(title = { Text("Acerca de") }) }) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("To-Do Kotlin + Compose", style = MaterialTheme.typography.titleLarge)
            Text("Demo educativa para clase.\nAutor: Axioma Network.\nLicencia: uso didáctico.")
            Button(onClick = onClose, shape = RoundedCornerShape(16.dp)) { Text("Cerrar") }
        }
    }
}