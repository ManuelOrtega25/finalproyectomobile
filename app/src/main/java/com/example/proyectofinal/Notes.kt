package com.example.proyectofinal

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = Folder::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["folderId"])]
)
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val folderId: Long = 1,
    val title: String,
    val content: String,
    val date: LocalDateTime,
    val category: String = "Personal",
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val colorHex: String = "#151515",
    val imageUri: String? = null,
    val reminderDateTime: LocalDateTime? = null
) {
    @Ignore var isVisible: Boolean = true
}
