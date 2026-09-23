package com.example.proyectofinal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.proyectofinal.ui.theme.ProyectoFinalTheme
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Solicitar permisos de notificacion al usuario en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        val app = application as NoteApplication
        val noteViewModel = NoteViewModel(app.repository)
        val folderViewModel = FolderViewModel(app.folderRepository)

        setContent {
            ProyectoFinalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    var selectedFolderState by remember { mutableStateOf<Pair<Long, String>?>(null) }
                    var activeNoteState by remember { mutableStateOf<Note?>(null) }
                    var isWritingNote by remember { mutableStateOf(false) }

                    when {
                        // 1. Pantalla para redactar / ver el detalle de la nota estilo iOS
                        isWritingNote || activeNoteState != null -> {
                            val currentFolderId = selectedFolderState?.first ?: 1L
                            NoteDetailScreen(
                                note = activeNoteState,
                                onBackClick = {
                                    activeNoteState = null
                                    isWritingNote = false
                                },
                                onSaveNote = { targetNote, title, content, imageUri, reminderDateTime ->
                                    val noteTitle = title.ifBlank { "Nota sin título" }
                                    if (targetNote != null) {
                                        val updatedNote = targetNote.copy(
                                            title = title,
                                            content = content,
                                            imageUri = imageUri,
                                            reminderDateTime = reminderDateTime
                                        )
                                        noteViewModel.updateNote(updatedNote)
                                        activeNoteState = updatedNote

                                        if (reminderDateTime != null) {
                                            NotificationHelper.scheduleReminder(
                                                context = this@MainActivity,
                                                noteId = updatedNote.id,
                                                title = noteTitle,
                                                reminderAt = reminderDateTime
                                            )
                                        } else {
                                            NotificationHelper.cancelReminder(
                                                context = this@MainActivity,
                                                noteId = updatedNote.id
                                            )
                                        }
                                    } else {
                                        val newNote = Note(
                                            folderId = currentFolderId,
                                            title = title,
                                            content = content,
                                            date = LocalDateTime.now(),
                                            imageUri = imageUri,
                                            reminderDateTime = reminderDateTime
                                        )
                                        noteViewModel.insertNote(newNote) { insertedId ->
                                            val insertedNote = newNote.copy(id = insertedId)
                                            activeNoteState = insertedNote
                                            if (reminderDateTime != null) {
                                                NotificationHelper.scheduleReminder(
                                                    context = this@MainActivity,
                                                    noteId = insertedId,
                                                    title = noteTitle,
                                                    reminderAt = reminderDateTime
                                                )
                                            }
                                        }
                                    }
                                },
                                onDeleteNote = { noteToDelete ->
                                    NotificationHelper.cancelReminder(this@MainActivity, noteToDelete.id)
                                    noteViewModel.deleteNote(noteToDelete)
                                },
                                onNewNoteClick = {
                                    activeNoteState = null
                                    isWritingNote = true
                                }
                            )
                        }

                        // 2. Pantalla de lista de notas de la carpeta seleccionada
                        selectedFolderState != null -> {
                            val (_, folderName) = selectedFolderState!!
                            NoteListScreen(
                                viewModel = noteViewModel,
                                folderName = folderName,
                                onBackClick = { selectedFolderState = null },
                                onNoteClick = { note ->
                                    activeNoteState = note
                                },
                                onNewNoteClick = {
                                    activeNoteState = null
                                    isWritingNote = true
                                }
                            )
                        }

                        // 3. Pantalla inicial: Lista de carpetas estilo Apple Notes
                        else -> {
                            FolderListScreen(
                                folderViewModel = folderViewModel,
                                onFolderSelect = { folderId, folderName ->
                                    noteViewModel.setSelectedFolderId(folderId)
                                    selectedFolderState = Pair(folderId, folderName)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
