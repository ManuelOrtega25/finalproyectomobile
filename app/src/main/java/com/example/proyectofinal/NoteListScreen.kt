package com.example.proyectofinal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectofinal.ui.theme.AccentYellow
import com.example.proyectofinal.ui.theme.BorderColor
import com.example.proyectofinal.ui.theme.DarkBackground
import com.example.proyectofinal.ui.theme.SecondarySurface
import com.example.proyectofinal.ui.theme.TextPrimary
import com.example.proyectofinal.ui.theme.TextSecondary
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

//formato corto de fecha estilo ios
fun formatShortDate(dateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("d/M/yy")
    return dateTime.format(formatter)
}

//agrupar notas por periodo de tiempo estilo ios (Hoy, Ayer, Anteriores 7 dias, Anteriores 30 dias, Mes, Ano)
fun groupNotesByPeriod(notes: List<Note>): Map<String, List<Note>> {
    val now = LocalDateTime.now()
    val today = now.toLocalDate()
    val yesterday = today.minusDays(1)
    val sevenDaysAgo = now.minusDays(7)
    val thirtyDaysAgo = now.minusDays(30)

    return notes.groupBy { note ->
        val noteDate = note.date.toLocalDate()
        when {
            noteDate == today -> "Hoy"
            noteDate == yesterday -> "Ayer"
            note.date.isAfter(sevenDaysAgo) -> "Anteriores 7 días"
            note.date.isAfter(thirtyDaysAgo) -> "Anteriores 30 días"
            note.date.year == now.year -> {
                note.date.month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-ES"))
                    .replaceFirstChar { it.uppercase() }
            }
            else -> note.date.year.toString()
        }
    }
}

//pantalla de notas de una carpeta estilo ios apple notes
@Composable
fun NoteListScreen(
    viewModel: NoteViewModel,
    folderName: String = "Notas",
    onBackClick: (() -> Unit)? = null,
    onNoteClick: ((note: Note) -> Unit)? = null,
    onNewNoteClick: (() -> Unit)? = null
) {
    val notes by viewModel.allNotes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val groupedNotes = groupNotesByPeriod(notes)

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            //barra inferior con buscador y boton de nueva nota
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBackground)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    //campo de busqueda estilo buscador ios
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Buscar", color = TextSecondary, fontSize = 15.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SecondarySurface,
                            unfocusedContainerColor = SecondarySurface,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    //boton para crear nueva nota
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SecondarySurface)
                            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                            .clickable {
                                onNewNoteClick?.invoke()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Nueva nota",
                            tint = AccentYellow,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            //barra superior con boton de regresar si aplica
            if (onBackClick != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SecondarySurface)
                            .clickable { onBackClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            //titulo de la carpeta y contador de notas
            Text(
                text = folderName,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${notes.size} notas",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            //lista de notas agrupadas en tarjetas redondeadas estilo ios
            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay notas",
                        color = TextSecondary,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    groupedNotes.forEach { (periodTitle, periodNotes) ->
                        item {
                            Column {
                                //encabezado del periodo de tiempo
                                Text(
                                    text = periodTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
                                )

                                //tarjeta contenedora agrupada
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, BorderColor, RoundedCornerShape(16.dp)),
                                    colors = CardDefaults.cardColors(containerColor = SecondarySurface),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column {
                                        periodNotes.forEachIndexed { index, note ->
                                            SwipeableNoteItemRow(
                                                note = note,
                                                onDelete = { viewModel.deleteNote(note) },
                                                onClick = {
                                                    onNoteClick?.invoke(note)
                                                }
                                            )

                                            if (index < periodNotes.size - 1) {
                                                HorizontalDivider(
                                                    color = BorderColor,
                                                    thickness = 0.5.dp,
                                                    modifier = Modifier.padding(start = 16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

//fila deslizable para eliminar o abrir nota
@Composable
fun SwipeableNoteItemRow(
    note: Note,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }

    AnimatedVisibility(
        visible = note.isVisible,
        enter = slideInHorizontally { -it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            //fondo de eliminacion
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color.White
                )
            }

            //contenido de la fila estilo ios
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .background(SecondarySurface)
                    .clickable { onClick() }
                    .pointerInput(note.id) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                coroutineScope.launch {
                                    val threshold = size.width * 0.3f
                                    if (offsetX.value < -threshold) {
                                        offsetX.animateTo(
                                            targetValue = -size.width.toFloat(),
                                            animationSpec = tween(200)
                                        )
                                        onDelete()
                                    } else {
                                        offsetX.animateTo(0f, animationSpec = tween(200))
                                    }
                                }
                            },
                            onDragCancel = {
                                coroutineScope.launch { offsetX.animateTo(0f) }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                if (dragAmount < 0 || offsetX.value < 0) {
                                    change.consume()
                                    coroutineScope.launch {
                                        val newOffset = (offsetX.value + dragAmount).coerceAtMost(0f)
                                        offsetX.snapTo(newOffset)
                                    }
                                }
                            }
                        )
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    //titulo de la nota con indicador de recordatorio
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (note.title.isNotBlank()) note.title else "Nueva nota",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (note.reminderDateTime != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Tiene recordatorio activo",
                                tint = AccentYellow,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    //fecha corta y previsualizacion del contenido
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatShortDate(note.date),
                            color = TextSecondary,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = if (note.content.isNotBlank()) note.content else "Sin texto adicional",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
