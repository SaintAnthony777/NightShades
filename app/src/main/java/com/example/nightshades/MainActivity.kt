package com.example.nightshades

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121212)) {
                    ShaderEditorScreen()
                }
            }
        }
    }
}

@Composable
fun ShaderEditorScreen() {
    val activity = LocalContext.current as ComponentActivity
    var shaderValue by remember { mutableStateOf(TextFieldValue("// NightShades prêt\n")) }
    var fileName by remember { mutableStateOf("my_shader") }
    // Variable pour gérer le surlignage unique des boutons
    var selectedMode by remember { mutableStateOf("canvas_item") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("NightShades Editor", style = MaterialTheme.typography.headlineMedium, color = Color.White)

        // --- SECTION 1 : CHOIX DU TYPE ---
        Text("Type de Shader :", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val modes = listOf("canvas_item" to "2D", "spatial" to "3D", "particles" to "Particles", "2d_RadialBlur" to "Radial Blur")

            modes.forEach { (id, label) ->
                FilterChip(
                    selected = (selectedMode == id),
                    onClick = {
                        selectedMode = id
                        shaderValue = updateShaderType(shaderValue, id)
                    },
                    label = { Text(label) }
                )
            }
        }

        // --- SECTION 2 : NOM DU FICHIER ---
        OutlinedTextField(
            value = fileName,
            onValueChange = { fileName = it },
            label = { Text("Nom du fichier (ex: $selectedMode)") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(color = Color.White)
        )

        // --- SECTION 3 : BARRE D'OUTILS RAPIDE ---
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickButton("Float") { shaderValue = insertTextAtCursor(shaderValue, "uniform float val = 1.0;\n") }
            QuickButton("Vec3") { shaderValue = insertTextAtCursor(shaderValue, "uniform vec3 color = vec3(0,0,0);\n") }
            QuickButton("Sampler2D") { shaderValue = insertTextAtCursor(shaderValue, "uniform sampler2D tex : hint_screen_texture;\n") }
        }

        // --- SECTION 4 : ÉDITEUR ---
        TextField(
            value = shaderValue,
            onValueChange = { shaderValue = it },
            modifier = Modifier.weight(1f).fillMaxWidth(),
            visualTransformation = ShaderSyntaxTransformation(), // Déplacé dans Utils ou resté ici
            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, color = Color(0xFF4AF626)),
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Black, unfocusedContainerColor = Color.Black)
        )

        // --- SECTION 5 : ACTIONS ---
        Button(
            onClick = { saveShaderFile(activity, shaderValue.text, fileName) },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Sauvegarder $fileName.gdshader")
        }
    }
}

@Composable
fun QuickButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}