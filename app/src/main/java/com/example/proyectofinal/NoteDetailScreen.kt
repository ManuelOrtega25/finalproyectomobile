package com.example.proyectofinal

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectofinal.ui.theme.AccentYellow
import com.example.proyectofinal.ui.theme.BorderColor
import com.example.proyectofinal.ui.theme.CardBackground
import com.example.proyectofinal.ui.theme.DarkBackground
import com.example.proyectofinal.ui.theme.SecondarySurface
import com.example.proyectofinal.ui.theme.TextPrimary
import com.example.proyectofinal.ui.theme.TextSecondary

//transformador visual para procesar negrita (**), cursiva (*) y subrayado (<u>) en tiempo real con mapeo seguro de offsets
class MarkdownVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val rawText = text.text
        val builder = AnnotatedString.Builder()

        val rawToTransformed = IntArray(rawText.length + 1)
        val transformedToRawList = mutableListOf<Int>()

        var rawIndex = 0
        var transformedIndex = 0

        while (rawIndex < rawText.length) {
            when {
                // Negrita: **texto**
                rawText.startsWith("**", rawIndex) -> {
                    val end = rawText.indexOf("**", rawIndex + 2)
                    if (end != -1) {
                        val innerText = rawText.substring(rawIndex + 2, end)

                        rawToTransformed[rawIndex] = transformedIndex
                        rawToTransformed[rawIndex + 1] = transformedIndex

                        builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(innerText)
                        }

                        for (k in 0 until innerText.length) {
                            transformedToRawList.add(rawIndex + 2 + k)
                            rawToTransformed[rawIndex + 2 + k] = transformedIndex + k
                        }
                        transformedIndex += innerText.length

                        rawToTransformed[end] = transformedIndex
                        rawToTransformed[end + 1] = transformedIndex

                        rawIndex = end + 2
                    } else {
                        rawToTransformed[rawIndex] = transformedIndex
                        transformedToRawList.add(rawIndex)
                        builder.append(rawText[rawIndex])
                        rawIndex++
                        transformedIndex++
                    }
                }
                // Subrayado: <u>texto</u>
                rawText.startsWith("<u>", rawIndex) -> {
                    val end = rawText.indexOf("</u>", rawIndex + 3)
                    if (end != -1) {
                        val innerText = rawText.substring(rawIndex + 3, end)

                        rawToTransformed[rawIndex] = transformedIndex
                        rawToTransformed[rawIndex + 1] = transformedIndex
                        rawToTransformed[rawIndex + 2] = transformedIndex

                        builder.withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                            append(innerText)
                        }

                        for (k in 0 until innerText.length) {
                            transformedToRawList.add(rawIndex + 3 + k)
                            rawToTransformed[rawIndex + 3 + k] = transformedIndex + k
                        }
                        transformedIndex += innerText.length

                        rawToTransformed[end] = transformedIndex
                        rawToTransformed[end + 1] = transformedIndex
                        rawToTransformed[end + 2] = transformedIndex
                        rawToTransformed[end + 3] = transformedIndex

                        rawIndex = end + 4
                    } else {
                        rawToTransformed[rawIndex] = transformedIndex
                        transformedToRawList.add(rawIndex)
                        builder.append(rawText[rawIndex])
                        rawIndex++
                        transformedIndex++
                    }
                }
                // Cursiva: *texto*
                rawText.startsWith("*", rawIndex) && !rawText.startsWith("**", rawIndex) -> {
                    val end = rawText.indexOf("*", rawIndex + 1)
                    if (end != -1 && (end == rawIndex + 1 || rawText[end - 1] != '*')) {
                        val innerText = rawText.substring(rawIndex + 1, end)

                        rawToTransformed[rawIndex] = transformedIndex

                        builder.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(innerText)
                        }

                        for (k in 0 until innerText.length) {
                            transformedToRawList.add(rawIndex + 1 + k)
                            rawToTransformed[rawIndex + 1 + k] = transformedIndex + k
                        }
                        transformedIndex += innerText.length

                        rawToTransformed[end] = transformedIndex

                        rawIndex = end + 1
                    } else {
                        rawToTransformed[rawIndex] = transformedIndex
                        transformedToRawList.add(rawIndex)
                        builder.append(rawText[rawIndex])
                        rawIndex++
                        transformedIndex++
                    }
                }
                else -> {
                    rawToTransformed[rawIndex] = transformedIndex
                    transformedToRawList.add(rawIndex)
                    builder.append(rawText[rawIndex])
                    rawIndex++
                    transformedIndex++
                }
            }
        }
        rawToTransformed[rawText.length] = transformedIndex
        transformedToRawList.add(rawText.length)
        val transformedToRaw = transformedToRawList.toIntArray()

        val transformedText = builder.toAnnotatedString()
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return if (offset in rawToTransformed.indices) rawToTransformed[offset] else transformedText.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                return if (offset in transformedToRaw.indices) transformedToRaw[offset] else rawText.length
            }
        }

        return TransformedText(transformedText, offsetMapping)
    }
}

//guardar imagen localmente en almacenamiento interno privado de la app
fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val imagesDir = File(context.filesDir, "note_images").apply {
            if (!exists()) mkdirs()
        }
        val destinationFile = File(imagesDir, "img_${System.currentTimeMillis()}.jpg")
        FileOutputStream(destinationFile).use { outputStream ->
            inputStream.use { input ->
                input.copyTo(outputStream)
            }
        }
        Uri.fromFile(destinationFile).toString()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

//formato legible de fecha y hora para recordatorios (muestra segundos si se configuran para pruebas)
fun formatReminderDateTime(dateTime: LocalDateTime?): String {
    val dt = dateTime ?: return ""
    val pattern = if (dt.second != 0) "d MMM, HH:mm:ss" else "d MMM, HH:mm"
    val formatter = DateTimeFormatter.ofPattern(pattern, Locale.forLanguageTag("es-ES"))
    return dt.format(formatter)
}

//pantalla para redactar y ver el detalle de una nota estilo ios
@Composable
fun NoteDetailScreen(
    note: Note?,
    onBackClick: () -> Unit,
    onSaveNote: (targetNote: Note?, title: String, content: String, imageUri: String?, reminderDateTime: LocalDateTime?) -> Unit,
    onDeleteNote: ((note: Note) -> Unit)? = null,
    onNewNoteClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(note?.title ?: "") }
    var contentValue by remember { mutableStateOf(TextFieldValue(note?.content ?: "")) }
    var selectedImageUri by remember { mutableStateOf(note?.imageUri) }
    var selectedReminderDateTime by remember { mutableStateOf(note?.reminderDateTime) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var isDeleted by remember { mutableStateOf(false) }
    var hasSaved by remember { mutableStateOf(false) }

    //lanzador para solicitar permisos de notificacion en Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    //launcher para seleccionar imagenes de la galeria con guardado local persistente
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val localUri = saveImageToInternalStorage(context, uri)
            selectedImageUri = localUri ?: uri.toString()
        }
    }

    //funcion para guardar la nota solo si ha cambiado, no se ha eliminado y no se ha guardado ya
    fun saveCurrentNote() {
        if (!isDeleted && !hasSaved) {
            if (title.isNotBlank() || contentValue.text.isNotBlank() || !selectedImageUri.isNullOrBlank() || selectedReminderDateTime != null) {
                val isChanged = note == null ||
                        note.title != title ||
                        note.content != contentValue.text ||
                        note.imageUri != selectedImageUri ||
                        note.reminderDateTime != selectedReminderDateTime

                if (isChanged) {
                    hasSaved = true
                    onSaveNote(note, title, contentValue.text, selectedImageUri, selectedReminderDateTime)
                }
            }
        }
    }

    //guardar automaticamente al salir
    DisposableEffect(Unit) {
        onDispose {
            saveCurrentNote()
        }
    }

    //funcion aux para envolver texto seleccionado con etiquetas
    fun applyFormatting(prefix: String, suffix: String) {
        val text = contentValue.text
        val selection = contentValue.selection

        val newText: String
        val newSelection: androidx.compose.ui.text.TextRange

        if (selection.collapsed) {
            val start = selection.start.coerceIn(0, text.length)
            val before = text.substring(0, start)
            val after = text.substring(start)
            newText = "$before$prefix$suffix$after"
            val cursor = start + prefix.length
            newSelection = androidx.compose.ui.text.TextRange(cursor, cursor)
        } else {
            val start = minOf(selection.start, selection.end).coerceIn(0, text.length)
            val end = maxOf(selection.start, selection.end).coerceIn(0, text.length)
            val before = text.substring(0, start)
            val selected = text.substring(start, end)
            val after = text.substring(end)
            newText = "$before$prefix$selected$suffix$after"
            newSelection = androidx.compose.ui.text.TextRange(start + prefix.length, end + prefix.length)
        }

        contentValue = TextFieldValue(
            text = newText,
            selection = newSelection
        )
    }

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            //barra inferior de herramientas estilo ios (negrita, cursiva, subrayado, viñeta, imagen, eliminar)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBackground)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // B (Negrita)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SecondarySurface)
                                .clickable { applyFormatting("**", "**") },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("B", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        // I (Cursiva)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SecondarySurface)
                                .clickable { applyFormatting("*", "*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("I", color = TextPrimary, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        // U (Subrayado)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SecondarySurface)
                                .clickable { applyFormatting("<u>", "</u>") },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("U", color = TextPrimary, textDecoration = TextDecoration.Underline, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        // Viñeta
                        IconButton(
                            onClick = {
                                val currentText = contentValue.text
                                val newText = if (currentText.endsWith("\n") || currentText.isEmpty()) {
                                    "$currentText• "
                                } else {
                                    "$currentText\n• "
                                }
                                contentValue = TextFieldValue(newText)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = "Lista",
                                tint = TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Insertar Imagen (icono +)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SecondarySurface)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📷", fontSize = 16.sp)
                        }

                        // Eliminar
                        if (note != null && onDeleteNote != null) {
                            IconButton(
                                onClick = {
                                    isDeleted = true // Prevenir que se guarde despues de eliminar
                                    onDeleteNote(note)
                                    onBackClick()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = Color.Red,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            //barra superior
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SecondarySurface)
                        .clickable {
                            saveCurrentNote()
                            onBackClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Boton de recordatorio / notificacion
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (selectedReminderDateTime != null) AccentYellow.copy(alpha = 0.2f) else SecondarySurface)
                            .border(
                                width = if (selectedReminderDateTime != null) 1.dp else 0.dp,
                                color = if (selectedReminderDateTime != null) AccentYellow else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                showReminderDialog = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Configurar recordatorio",
                            tint = if (selectedReminderDateTime != null) AccentYellow else TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(SecondarySurface)
                                .clickable { showMenu = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Opciones",
                                tint = TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(SecondarySurface)
                        ) {
                            if (note != null && onDeleteNote != null) {
                                DropdownMenuItem(
                                    text = { Text("Eliminar nota", color = Color.Red) },
                                    onClick = {
                                        showMenu = false
                                        isDeleted = true
                                        onDeleteNote(note)
                                        onBackClick()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Insignia / chip de recordatorio activo si existe
            if (selectedReminderDateTime != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SecondarySurface)
                        .border(1.dp, AccentYellow.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable { showReminderDialog = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = AccentYellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Recordatorio: ${formatReminderDateTime(selectedReminderDateTime)}",
                        color = AccentYellow,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Quitar recordatorio",
                        tint = TextSecondary,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable {
                                selectedReminderDateTime = null
                                onSaveNote(note, title, contentValue.text, selectedImageUri, null)
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            //campo de titulo
            BasicTextField(
                value = title,
                onValueChange = { title = it },
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                ),
                cursorBrush = SolidColor(AccentYellow),
                decorationBox = { innerTextField ->
                    if (title.isEmpty()) {
                        Text(
                            text = "Título",
                            color = TextSecondary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            //visualizacion de la imagen insertada si existe
            if (!selectedImageUri.isNullOrBlank()) {
                val imageBitmap = remember(selectedImageUri) {
                    try {
                        val uri = Uri.parse(selectedImageUri)
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BitmapFactory.decodeStream(stream)?.asImageBitmap()
                        }
                    } catch (_: Exception) {
                        null
                    }
                }

                if (imageBitmap != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                    ) {
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = "Imagen adjunta",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        //boton para quitar imagen
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.7f))
                                .clickable { selectedImageUri = null },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Quitar imagen",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            //campo de contenido con transformador de formato negrita/cursiva/subrayado
            BasicTextField(
                value = contentValue,
                onValueChange = { contentValue = it },
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 17.sp,
                    lineHeight = 24.sp
                ),
                cursorBrush = SolidColor(AccentYellow),
                visualTransformation = MarkdownVisualTransformation(),
                decorationBox = { innerTextField ->
                    if (contentValue.text.isEmpty()) {
                        Text(
                            text = "Empieza a escribir...",
                            color = TextSecondary,
                            fontSize = 17.sp
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            )
        }

        // Dialogo para configurar recordatorio con validacion de fecha futura y soporte para segundos y minutos
        if (showReminderDialog) {
            var tempDateTime by remember(showReminderDialog) {
                val current = selectedReminderDateTime
                mutableStateOf(
                    if (current != null && current.isAfter(LocalDateTime.now())) {
                        current
                    } else {
                        LocalDateTime.now().plusSeconds(15)
                    }
                )
            }
            var selectedPreset by remember(showReminderDialog) {
                val current = selectedReminderDateTime
                mutableStateOf(
                    if (current != null && current.isAfter(LocalDateTime.now())) "custom" else "15s"
                )
            }

            val now = LocalDateTime.now()
            val isFuture = tempDateTime.isAfter(now)

            val relativeTimeText = remember(tempDateTime, now) {
                if (!tempDateTime.isAfter(now)) {
                    "Tiempo transcurrido"
                } else {
                    val duration = java.time.Duration.between(now, tempDateTime)
                    val seconds = duration.seconds
                    val days = duration.toDays()
                    val hours = duration.toHours() % 24
                    val minutes = duration.toMinutes() % 60
                    val secs = seconds % 60

                    buildString {
                        append("En ")
                        if (days > 0) append("${days}d ")
                        if (hours > 0) append("${hours}h ")
                        if (minutes > 0) append("${minutes}m ")
                        if (secs > 0 || (days == 0L && hours == 0L && minutes == 0L)) append("${secs}s")
                    }.trim()
                }
            }

            AlertDialog(
                onDismissRequest = { showReminderDialog = false },
                containerColor = SecondarySurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = AccentYellow,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recordatorio",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Elige cuándo recibir la notificación:",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )

                        // Presets rápidos (incluye segundos y minutos para pruebas rápidas)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val presets = listOf(
                                Triple("15s", "15 seg") { LocalDateTime.now().plusSeconds(15) },
                                Triple("1m", "1 min") { LocalDateTime.now().plusMinutes(1) },
                                Triple("5m", "5 min") { LocalDateTime.now().plusMinutes(5) },
                                Triple("1h", "1 hora") { LocalDateTime.now().plusHours(1) }
                            )

                            presets.forEach { (id, label, calcTime) ->
                                val isSelected = selectedPreset == id
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) AccentYellow else CardBackground)
                                        .clickable {
                                            selectedPreset = id
                                            tempDateTime = calcTime()
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color.Black else TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        // Ajustes personalizados de Segundos (modo test)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CardBackground)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Segundos (test):", color = TextSecondary, fontSize = 12.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Button(
                                    onClick = {
                                        selectedPreset = "custom"
                                        tempDateTime = tempDateTime.minusSeconds(10)
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondarySurface),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("-10s", color = TextPrimary, fontSize = 11.sp)
                                }
                                Button(
                                    onClick = {
                                        selectedPreset = "custom"
                                        tempDateTime = tempDateTime.plusSeconds(10)
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondarySurface),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("+10s", color = AccentYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = {
                                        selectedPreset = "custom"
                                        tempDateTime = tempDateTime.plusSeconds(30)
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondarySurface),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("+30s", color = AccentYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Ajustes de Minutos y Horas
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CardBackground)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Minutos / Horas:", color = TextSecondary, fontSize = 12.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Button(
                                    onClick = {
                                        selectedPreset = "custom"
                                        tempDateTime = tempDateTime.minusMinutes(1)
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondarySurface),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("-1m", color = TextPrimary, fontSize = 11.sp)
                                }
                                Button(
                                    onClick = {
                                        selectedPreset = "custom"
                                        tempDateTime = tempDateTime.plusMinutes(1)
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondarySurface),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("+1m", color = AccentYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = {
                                        selectedPreset = "custom"
                                        tempDateTime = tempDateTime.plusHours(1)
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondarySurface),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("+1h", color = AccentYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Resumen programado con cuenta regresiva
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CardBackground)
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Programado para:",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = relativeTimeText,
                                    color = if (isFuture) AccentYellow else MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatReminderDateTime(tempDateTime),
                                color = if (isFuture) AccentYellow else MaterialTheme.colorScheme.error,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Validación de fecha futura
                        if (!isFuture) {
                            Text(
                                text = "⚠ La hora seleccionada ya pasó. Debe ser posterior al momento actual.",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (isFuture) {
                                selectedReminderDateTime = tempDateTime
                                showReminderDialog = false
                                // Guardar y programar de inmediato en el sistema
                                onSaveNote(note, title, contentValue.text, selectedImageUri, tempDateTime)
                            }
                        },
                        enabled = isFuture,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentYellow,
                            contentColor = Color.Black,
                            disabledContainerColor = SecondarySurface,
                            disabledContentColor = TextSecondary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Guardar recordatorio", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    Row {
                        if (selectedReminderDateTime != null) {
                            TextButton(
                                onClick = {
                                    selectedReminderDateTime = null
                                    showReminderDialog = false
                                    onSaveNote(note, title, contentValue.text, selectedImageUri, null)
                                }
                            ) {
                                Text("Quitar", color = Color.Red)
                            }
                        }
                        TextButton(onClick = { showReminderDialog = false }) {
                            Text("Cancelar", color = TextSecondary)
                        }
                    }
                }
            )
        }
    }
}
