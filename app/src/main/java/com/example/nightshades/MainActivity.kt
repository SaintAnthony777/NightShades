package com.example.nightshades

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.platform.LocalContext
import java.io.File
import android.os.Environment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.Manifest
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Utilise le thème généré par Android Studio
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF121212) // Un fond sombre typique des éditeurs
                ) {
                    ShaderEditorScreen()
                }
            }
        }
    }
}

@Composable
fun ShaderEditorScreen() {
    val context = LocalContext.current
    val activity = LocalContext.current as ComponentActivity
    var shaderValue by remember { mutableStateOf(TextFieldValue("// NightShades prêt\n")) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("NightShades Editor", style = MaterialTheme.typography.headlineMedium, color = Color.White)

        Text("Type de Shader :", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Boutons pour les types principaux de Godot
            FilterChip(
                selected = shaderValue.text.contains("canvas_item"),
                onClick = { shaderValue = updateShaderType(shaderValue, "canvas_item") },
                label = { Text("2D (Canvas)") }
            )
            FilterChip(
                selected = shaderValue.text.contains("spatial"),
                onClick = { shaderValue = updateShaderType(shaderValue, "spatial") },
                label = { Text("3D (Spatial)") }
            )
            FilterChip(
                selected = shaderValue.text.contains("particles"),
                onClick = { shaderValue = updateShaderType(shaderValue, "particles") },
                label = { Text("Particles") }
            )
        }
        // Nouvelle section : Barre d'outils rapide
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            QuickButton("Add Float") {
                shaderValue = insertTextAtCursor(shaderValue, "uniform float my_value = 1.0;\n")
            }
        }

        TextField(
            value = shaderValue, // On utilise shaderValue au lieu de shaderCode
            onValueChange = { shaderValue = it },
            modifier = Modifier.weight(1f).fillMaxWidth(),
            visualTransformation = ShaderSyntaxTransformation(),
            textStyle = LocalTextStyle.current.copy(
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF4AF626)
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Black,
                unfocusedContainerColor = Color.Black
            )
        )

        Button(
            onClick = {
                // On crée un nouvel objet TextFieldValue avec le texte de base
                shaderValue = TextFieldValue(
                    text = """
                shader_type canvas_item;

                void fragment() {
                    COLOR = texture(TEXTURE, UV);
                }
            """.trimIndent()
                )
            },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Générer Base")
        }
        Button(
            onClick = { saveShaderFile(activity, shaderValue.text) },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Vert pour "Sauvegarder"
        ) {
            Text("Sauvegarder .gdshader")
        }

    }
}

@Composable
fun QuickButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}
class ShaderSyntaxTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val keywords = listOf("shader_type", "void", "fragment", "vertex", "uniform", "if", "else")
        val builtIns = listOf("COLOR", "UV", "TIME", "TEXTURE", "vec2", "vec3", "vec4", "float")

        val result = buildAnnotatedString {
            val words = text.text.split(Regex("(?<=\\W)|(?=\\W)"))
            for (word in words) {
                when {
                    word in keywords -> withStyle(style = SpanStyle(color = Color(0xFFF92672))) { append(word) } // Rose
                    word in builtIns -> withStyle(style = SpanStyle(color = Color(0xFF66D9EF))) { append(word) } // Bleu ciel
                    word.startsWith("//") -> withStyle(style = SpanStyle(color = Color.Gray)) { append(word) }
                    else -> append(word)
                }
            }
        }
        return TransformedText(result, OffsetMapping.Identity)
    }
}
fun saveShaderFile(activity: ComponentActivity, content: String) {
    // Vérifier si on a la permission
    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {

        // Si non, on la demande
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        return
    }

    try {
        // Sur les versions récentes, on utilise le dossier spécifique à l'app pour éviter les soucis
        val file = File(activity.getExternalFilesDir(null), "my_shader.gdshader")
        file.writeText(content)
        Toast.makeText(activity, "Sauvegardé dans : ${file.absolutePath}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(activity, "Erreur : ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
fun insertTextAtCursor(currentValue: TextFieldValue, textToInsert: String): TextFieldValue {
    val content = currentValue.text
    val selection = currentValue.selection

    // On coupe le texte en deux au niveau du curseur et on insère le nouveau texte au milieu
    val newText = content.substring(0, selection.start) + textToInsert + content.substring(selection.end)

    // On replace le curseur juste après le texte inséré
    val newCursorPosition = selection.start + textToInsert.length

    return TextFieldValue(
        text = newText,
        selection = TextRange(newCursorPosition)
    )
}
fun updateShaderType(currentValue: TextFieldValue, newType: String): TextFieldValue {
    val shaderTemplates = mapOf(
        "canvas_item" to """
            shader_type canvas_item;

            void fragment() {
                COLOR = texture(TEXTURE, UV);
            }
        """.trimIndent(),
        "spatial" to """
            shader_type spatial;

            void vertex() {
                // Modifie la position des sommets ici
            }

            void fragment() {
                // Code PBR (ALBEDO, ROUGHNESS, etc.)
                ALBEDO = vec3(0.5);
            }
        """.trimIndent(),
        "particles" to """
            shader_type particles;

            void start() {
                // Initialisation des particules
            }

            void process() {
                // Animation par frame
            }
        """.trimIndent()
    )

    val newText = shaderTemplates[newType] ?: "shader_type $newType;"

    // On remplace tout le texte pour avoir une base propre,
    // ou on pourrait fusionner si tu préfères garder tes variables.
    return TextFieldValue(text = newText, selection = TextRange(newText.length))
}