package com.example.proyectofinal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class NoteLogicUnitTest {

    @Test
    fun testFormatShortDate() {
        val date = LocalDateTime.of(2026, 9, 22, 14, 30)
        val formatted = formatShortDate(date)
        assertEquals("22/9/26", formatted)
    }

    @Test
    fun testGroupNotesByPeriod_RecentNotes() {
        val now = LocalDateTime.now()
        val noteToday = Note(
            id = 1,
            title = "Nota Hoy",
            content = "Contenido hoy",
            date = now
        )
        val noteYesterday = Note(
            id = 2,
            title = "Nota Ayer",
            content = "Contenido ayer",
            date = now.minusDays(1)
        )
        val noteWeek = Note(
            id = 3,
            title = "Nota 7 dias",
            content = "Contenido semana",
            date = now.minusDays(5)
        )
        val noteMonth = Note(
            id = 4,
            title = "Nota 30 dias",
            content = "Contenido mes",
            date = now.minusDays(15)
        )

        val groups = groupNotesByPeriod(listOf(noteToday, noteYesterday, noteWeek, noteMonth))
        assertTrue(groups.containsKey("Hoy"))
        assertTrue(groups.containsKey("Ayer"))
        assertTrue(groups.containsKey("Anteriores 7 días"))
        assertTrue(groups.containsKey("Anteriores 30 días"))
        assertEquals(1, groups["Hoy"]?.size)
        assertEquals(1, groups["Ayer"]?.size)
        assertEquals(1, groups["Anteriores 7 días"]?.size)
        assertEquals(1, groups["Anteriores 30 días"]?.size)
    }

    @Test
    fun testGroupNotesByPeriod_PreviousYearNotes() {
        val oldNote = Note(
            id = 3,
            title = "Nota Antigua",
            content = "Contenido antiguo",
            date = LocalDateTime.of(2023, 5, 10, 10, 0)
        )

        val groups = groupNotesByPeriod(listOf(oldNote))
        assertTrue(groups.containsKey("2023"))
        assertEquals(1, groups["2023"]?.size)
        assertEquals("Nota Antigua", groups["2023"]?.first()?.title)
    }

    @Test
    fun testNoteEntityDefaults() {
        val note = Note(
            id = 1,
            title = "Prueba",
            content = "Texto",
            date = LocalDateTime.now()
        )

        assertEquals("Personal", note.category)
        assertFalse(note.isPinned)
        assertFalse(note.isArchived)
        assertEquals("#151515", note.colorHex)
        assertEquals(1L, note.folderId)
        assertEquals(null, note.imageUri)
        assertTrue(note.isVisible)
    }

    @Test
    fun testFolderWithCountRelation() {
        val folder = Folder(id = 10, name = "Universidad", iconName = "ic_folder_1", section = "En mi dispositivo")
        val folderWithCount = FolderWithCount(folder = folder, noteCount = 5)

        assertEquals(10L, folderWithCount.folder.id)
        assertEquals("Universidad", folderWithCount.folder.name)
        assertEquals(5, folderWithCount.noteCount)
    }

    @Test
    fun testNoteCopyPreservesValues() {
        val original = Note(
            id = 5,
            folderId = 1,
            title = "Título Original",
            content = "Cuerpo Original",
            date = LocalDateTime.now(),
            imageUri = "file:///data/local/img_1.jpg"
        )

        val modified = original.copy(
            title = "Título Modificado",
            folderId = 2
        )

        assertEquals(5L, modified.id)
        assertEquals(2L, modified.folderId)
        assertEquals("Título Modificado", modified.title)
        assertEquals("Cuerpo Original", modified.content)
        assertEquals("file:///data/local/img_1.jpg", modified.imageUri)
    }

    @Test
    fun testNoteWithReminderDateTime() {
        val reminder = LocalDateTime.of(2026, 9, 23, 10, 0)
        val note = Note(
            id = 7,
            title = "Reunión",
            content = "Discutir proyecto",
            date = LocalDateTime.now(),
            reminderDateTime = reminder
        )

        assertNotNull(note.reminderDateTime)
        assertEquals(reminder, note.reminderDateTime)
    }

    @Test
    fun testReminderDateFutureValidation() {
        val now = LocalDateTime.now()
        val pastDate = now.minusMinutes(1)
        val futureDate = now.plusHours(2)

        assertFalse("Una fecha pasada no debe ser válida para recordatorio", pastDate.isAfter(now))
        assertTrue("Una fecha futura debe ser válida para recordatorio", futureDate.isAfter(now))
    }

    @Test
    fun testFolderNameValidationRules() {
        val existingFolders = listOf("Notas", "Trabajo", "Ideas")

        // Regla 1: No debe ser vacía o espacios en blanco
        assertTrue("".trim().isBlank())
        assertTrue("   ".trim().isBlank())
        assertFalse("Proyectos".trim().isBlank())

        // Regla 2: Longitud máxima de 25 caracteres
        val validName = "Diseño de Interfaces"
        val tooLongName = "Este es un nombre excesivamente largo para una carpeta"
        assertTrue(validName.length <= 25)
        assertTrue(tooLongName.length > 25)

        // Regla 3: No duplicados (case-insensitive)
        val isDuplicate = existingFolders.any { it.equals("trabajo", ignoreCase = true) }
        val isUnique = existingFolders.none { it.equals("Finanzas", ignoreCase = true) }
        assertTrue(isDuplicate)
        assertTrue(isUnique)
    }

    @Test
    fun testFormatReminderDateTime() {
        val dateTime = LocalDateTime.of(2026, 9, 23, 9, 30)
        val formatted = formatReminderDateTime(dateTime)
        assertTrue(formatted.contains("23"))
        assertTrue(formatted.contains("09:30"))
    }
}
