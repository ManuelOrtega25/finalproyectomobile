package com.example.proyectofinal

import android.app.Application
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteApplication : Application() {
    val database by lazy {
        Room.databaseBuilder(
            this,
            NoteDatabase::class.java,
            "note_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    val repository by lazy { NoteRepository(database.noteDao()) }
    val folderRepository by lazy { FolderRepository(database.folderDao()) }

    override fun onCreate() {
        super.onCreate()
        // Crear canal de notificaciones para recordatorios
        NotificationHelper.createNotificationChannel(this)

        // Inicializar carpetas por defecto si la base de datos está vacía
        CoroutineScope(Dispatchers.IO).launch {
            if (folderRepository.getFolderCount() == 0) {
                folderRepository.insert(Folder(name = "Notas", iconName = "ic_folder_4", section = "En mi dispositivo"))
                folderRepository.insert(Folder(name = "Trabajo", iconName = "ic_folder_1", section = "En mi dispositivo"))
                folderRepository.insert(Folder(name = "Ideas", iconName = "ic_folder_2", section = "En mi dispositivo"))
            }
        }
    }
}
