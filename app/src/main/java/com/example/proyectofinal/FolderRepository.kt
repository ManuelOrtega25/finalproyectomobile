package com.example.proyectofinal

import kotlinx.coroutines.flow.Flow

//repositorio para gestionar los datos de las carpetas
class FolderRepository(private val folderDao: FolderDao) {
    val foldersWithCount: Flow<List<FolderWithCount>> = folderDao.getFoldersWithCount()

    //insertar carpeta en la base de datos
    suspend fun insert(folder: Folder): Long {
        return folderDao.insert(folder)
    }

    //actualizar carpeta
    suspend fun update(folder: Folder) {
        folderDao.update(folder)
    }

    //eliminar carpeta y sus notas asociadas
    suspend fun delete(folder: Folder) {
        folderDao.deleteNotesByFolderId(folder.id)
        folderDao.delete(folder)
    }

    //buscar carpeta por id
    suspend fun getFolderById(id: Long): Folder? {
        return folderDao.getFolderById(id)
    }

    //obtener el total de carpetas creadas
    suspend fun getFolderCount(): Int {
        return folderDao.getFolderCount()
    }
}
