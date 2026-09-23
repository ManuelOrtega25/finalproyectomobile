package com.example.proyectofinal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

//relacion entre carpeta y la cantidad de notas que contiene
data class FolderWithCount(
    @Embedded val folder: Folder,
    val noteCount: Int
)

@Dao
interface FolderDao {
    //obtener lista de carpetas con el conteo de notas de cada una
    @Query("SELECT folders.*, COUNT(notes.id) AS noteCount FROM folders LEFT JOIN notes ON folders.id = notes.folderId GROUP BY folders.id")
    fun getFoldersWithCount(): Flow<List<FolderWithCount>>

    //insertar o reemplazar carpeta
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: Folder): Long

    //actualizar datos de la carpeta
    @Update
    suspend fun update(folder: Folder)

    //eliminar una carpeta
    @Delete
    suspend fun delete(folder: Folder)

    //eliminar todas las notas asociadas a una carpeta
    @Query("DELETE FROM notes WHERE folderId = :folderId")
    suspend fun deleteNotesByFolderId(folderId: Long)

    //obtener carpeta por su id
    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getFolderById(id: Long): Folder?

    //contar cuantas carpetas existen
    @Query("SELECT COUNT(*) FROM folders")
    suspend fun getFolderCount(): Int
}
