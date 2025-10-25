package com.example.myapplication1

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
class ExportControlActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val context = LocalContext.current

                // ===== Estado actual (fecha y hora) =====
                val now = Calendar.getInstance()
                var millis by remember { mutableStateOf(now.timeInMillis) }     // fecha (en ms)
                var hour by remember { mutableStateOf(now.get(Calendar.HOUR_OF_DAY)) }
                var minute by remember { mutableStateOf(now.get(Calendar.MINUTE)) }

                // Visibilidad de diálogos
                var showDatePicker by remember { mutableStateOf(false) }
                var showTimePicker by remember { mutableStateOf(false) }

                // Estados de los pickers
                val dateState = rememberDatePickerState(
                    initialSelectedDateMillis = millis
                )
                val timeState = rememberTimePickerState(
                    initialHour = hour,
                    initialMinute = minute,
                    is24Hour = true
                )

                // Texto de ejemplo a exportar (sustituye por tus tareas reales si lo deseas)
                val exportText = remember {
                    listOf(
                        "1. [ ] Tarea de ejemplo A",
                        "2. [x] Tarea de ejemplo B"
                    ).joinToString("\n")
                }

                // Launcher para crear el .txt
                val exportLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.CreateDocument("text/plain")
                ) { uri ->
                    if (uri == null) {
                        Toast.makeText(context, "Exportación cancelada", Toast.LENGTH_SHORT).show()
                        return@rememberLauncherForActivityResult
                    }
                    try {
                        context.contentResolver.openOutputStream(uri)?.use { output ->
                            OutputStreamWriter(output).use { writer ->
                                writer.write(exportText)
                                writer.write("\n")
                                writer.write("Exportado con fecha/hora establecida.")
                            }
                        }
                        Toast.makeText(context, "Exportado ✅", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }

                // Nombre del archivo con la fecha/hora elegida
                fun fileName(): String {
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = millis
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(cal.time)
                    return "tareas_$stamp.txt"
                }

                Scaffold(topBar = { TopAppBar(title = { Text("Control de fechas") }) }) { padding ->
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // ----- Fecha -----
                        val dateText = remember(millis) {
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
                        }
                        OutlinedButton(
                            onClick = {
                                // sincroniza el state con el valor actual antes de abrir
                                dateState.selectedDateMillis = millis
                                showDatePicker = true
                            },
                            shape = RoundedCornerShape(16.dp)
                        ) { Text("Fecha: $dateText") }

                        if (showDatePicker) {
                            DatePickerDialog(
                                onDismissRequest = { showDatePicker = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        dateState.selectedDateMillis?.let { selected ->
                                            millis = selected
                                        }
                                        showDatePicker = false
                                    }) { Text("OK") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
                                }
                            ) {
                                DatePicker(
                                    state = dateState,
                                    showModeToggle = true
                                )
                            }
                        }

                        // ----- Hora -----
                        OutlinedButton(
                            onClick = {
                                // sincroniza el state con el valor actual antes de abrir
                                timeState.hour = hour
                                timeState.minute = minute
                                showTimePicker = true
                            },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            val hh = String.format("%02d", hour)
                            val mm = String.format("%02d", minute)
                            Text("Hora: $hh:$mm")
                        }

                        if (showTimePicker) {
                            // No existe TimePickerDialog en Material3: usamos AlertDialog + TimePicker
                            AlertDialog(
                                onDismissRequest = { showTimePicker = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        hour = timeState.hour
                                        minute = timeState.minute
                                        showTimePicker = false
                                    }) { Text("OK") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") }
                                },
                                text = {
                                    TimePicker(state = timeState)
                                }
                            )
                        }

                        Text("Nombre sugerido: ${fileName()}")

                        Button(
                            onClick = { exportLauncher.launch(fileName()) },
                            shape = RoundedCornerShape(16.dp)
                        ) { Text("Exportar con esta fecha/hora") }

                        OutlinedButton(
                            onClick = { finish() },
                            shape = RoundedCornerShape(16.dp)
                        ) { Text("Volver") }
                    }
                }
            }
        }
    }
}