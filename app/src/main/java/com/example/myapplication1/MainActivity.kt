package com.example.myapplication1

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val done: Boolean = false
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TodoApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoApp() {
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    val tasks = remember { mutableStateListOf<Task>() }

    // Exportación rápida desde Main (sigue funcionando)
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
                    tasks.forEachIndexed { index, t ->
                        val check = if (t.done) "x" else " "
                        writer.write("${index + 1}. [$check] ${t.text}\n")
                    }
                }
            }
            Toast.makeText(context, "Tareas exportadas correctamente ✅", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error al exportar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun defaultFileName(): String {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        return "tareas_$stamp.txt"
    }

    // Drawer
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    MaterialTheme(
        shapes = Shapes(
            extraSmall = RoundedCornerShape(12.dp),
            small = RoundedCornerShape(14.dp),
            medium = RoundedCornerShape(16.dp),
            large = RoundedCornerShape(20.dp),
            extraLarge = RoundedCornerShape(24.dp)
        )
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Text("Menú", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))

                    NavigationDrawerItem(
                        label = { Text("Perfil") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            context.startActivity(Intent(context, ProfileActivity::class.java))
                        }
                    )

                    NavigationDrawerItem(
                        label = { Text("Acerca de") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            context.startActivity(Intent(context, AboutActivity::class.java))
                        }
                    )

                    NavigationDrawerItem(
                        label = { Text("Control de fechas (exportación)") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            context.startActivity(Intent(context, ExportControlActivity::class.java))
                        }
                    )

                    NavigationDrawerItem(
                        label = { Text("Exportar a TXT (rápido)") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (tasks.isEmpty()) {
                                Toast.makeText(context, "No hay tareas para exportar", Toast.LENGTH_SHORT).show()
                            } else {
                                exportLauncher.launch(defaultFileName())
                            }
                        }
                    )
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Aplicación de tareas") },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            }) { Icon(Icons.Filled.Menu, contentDescription = "Abrir menú") }
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    if (tasks.isEmpty()) {
                                        Toast.makeText(context, "No hay tareas para exportar", Toast.LENGTH_SHORT).show()
                                    } else {
                                        exportLauncher.launch(defaultFileName())
                                    }
                                }
                            ) { Icon(Icons.Filled.Share, contentDescription = "Exportar TXT") }
                        }
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .fillMaxSize()
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = input,
                            onValueChange = { input = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("Nueva tarea") },
                            shape = RoundedCornerShape(16.dp)
                        )
                        Button(
                            onClick = {
                                val text = input.trim()
                                if (text.isNotEmpty()) {
                                    tasks.add(Task(text = text))
                                    input = ""
                                }
                            },
                            enabled = input.isNotBlank(),
                            shape = RoundedCornerShape(16.dp)
                        ) { Text("Agregar") }
                    }

                    Spacer(Modifier.height(16.dp))

                    if (tasks.isEmpty()) {
                        Text("No hay tareas. ¡Agrega la primera! 🙂")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(tasks, key = { it.id }) { task ->
                                TaskItem(
                                    task = task,
                                    onToggle = {
                                        val index = tasks.indexOfFirst { t -> t.id == task.id }
                                        if (index != -1) tasks[index] = task.copy(done = !task.done)
                                    },
                                    onDelete = { tasks.removeAll { t -> t.id == task.id } }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItem(task: Task, onToggle: () -> Unit, onDelete: () -> Unit) {
    ElevatedCard(shape = RoundedCornerShape(18.dp)) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Checkbox(checked = task.done, onCheckedChange = { onToggle() })
            Text(
                text = task.text,
                modifier = Modifier.weight(1f),
                textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None
            )
            OutlinedButton(onClick = onDelete, shape = RoundedCornerShape(14.dp)) { Text("Eliminar") }
        }
    }
}