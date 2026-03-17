package com.example.nightshades

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.core.app.ActivityCompat
import java.io.File
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.VisualTransformation
// Logique de mise à jour du type de shader
fun updateShaderType(currentValue: TextFieldValue, newType: String): TextFieldValue {
    val newText = when (newType) {
        "canvas_item" -> ShaderTemplates.CANVAS_ITEM
        "spatial" -> ShaderTemplates.SPATIAL
        "particles" -> ShaderTemplates.PARTICLES
        "2d_RadialBlur" -> ShaderTemplates.RADIAL_BLUR
        else -> "shader_type $newType;"
    }.trimIndent()

    return TextFieldValue(text = newText, selection = TextRange(newText.length))
}

// Logique d'insertion au curseur
fun insertTextAtCursor(currentValue: TextFieldValue, textToInsert: String): TextFieldValue {
    val content = currentValue.text
    val selection = currentValue.selection
    val newText = content.substring(0, selection.start) + textToInsert + content.substring(selection.end)
    return TextFieldValue(text = newText, selection = TextRange(selection.start + textToInsert.length))
}

// Logique de sauvegarde
fun saveShaderFile(activity: ComponentActivity, content: String, name: String) {
    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        return
    }
    try {
        val finalName = if (name.endsWith(".gdshader")) name else "$name.gdshader"
        val file = File(activity.getExternalFilesDir(null), finalName)
        file.writeText(content)
        Toast.makeText(activity, "Sauvegardé : ${file.name}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(activity, "Erreur : ${e.message}", Toast.LENGTH_SHORT).show()
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
                    word in keywords -> withStyle(style = SpanStyle(color = Color(0xFFF92672))) { append(word) }
                    word in builtIns -> withStyle(style = SpanStyle(color = Color(0xFF66D9EF))) { append(word) }
                    word.startsWith("//") -> withStyle(style = SpanStyle(color = Color.Gray)) { append(word) }
                    else -> append(word)
                }
            }
        }
        return TransformedText(result, OffsetMapping.Identity)
    }
}