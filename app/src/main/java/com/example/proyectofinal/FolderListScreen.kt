package com.example.proyectofinal

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectofinal.ui.theme.AccentYellow
import com.example.proyectofinal.ui.theme.BorderColor
import com.example.proyectofinal.ui.theme.DarkBackground
import com.example.proyectofinal.ui.theme.SecondarySurface
import com.example.proyectofinal.ui.theme.TextPrimary
import com.example.proyectofinal.ui.theme.TextSecondary

//iconos personalizados para carpeta
val AvailableFolderIcons = listOf(
    "ic_folder_4", // Notas (punto)
    "ic_folder_1", // Trabajo (clip)
    "ic_folder_2", // Ideas (estrella)
    "ic_folder_3"  // Interrogación
)

//funcion para obtener el recurso drawable segun el nombre guardado
fun getFolderIconRes(iconName: String): Int {
    return when (iconName) {
        "ic_folder_1" -> R.drawable.ic_folder_1
        "ic_folder_2" -> R.drawable.ic_folder_2
        "ic_folder_3" -> R.drawable.ic_folder_3
        "ic_folder_4" -> R.drawable.ic_folder_4
        else -> R.drawable.ic_folder_4
    }
}

//pantalla principal con el listado de carpetas
@Composable
fun FolderListScreen(
    folderViewModel: FolderViewModel,
    onFolderSelect: (folderId: Long, folderName: String) -> Unit
) {
    val foldersWithCount by folderViewModel.foldersWithCount.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var selectedIconName by remember { mutableStateOf("ic_folder_4") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredFolders = foldersWithCount.filter {
        searchQuery.isEmpty() || it.folder.name.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            //encabezado de la pantalla
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Carpetas",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 32.sp
                )

                IconButton(
                    onClick = {
                        selectedIconName = "ic_folder_4"
                        showCreateDialog = true
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SecondarySurface)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Nueva carpeta",
                        tint = AccentYellow
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            //barra de busqueda de carpetas
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar carpetas...", color = TextSecondary) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = TextSecondary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SecondarySurface,
                    unfocusedContainerColor = SecondarySurface,
                    focusedBorderColor = AccentYellow,
                    unfocusedBorderColor = BorderColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            //seccion en mi dispositivo
            Text(
                text = "EN MI DISPOSITIVO",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            if (filteredFolders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay carpetas",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderColor, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = SecondarySurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    LazyColumn {
                        itemsIndexed(filteredFolders) { index, folderWithCount ->
                            FolderItemRow(
                                folderWithCount = folderWithCount,
                                onClick = {
                                    onFolderSelect(
                                        folderWithCount.folder.id,
                                        folderWithCount.folder.name
                                    )
                                }
                            )

                            if (index < filteredFolders.size - 1) {
                                HorizontalDivider(
                                    color = BorderColor,
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(start = 64.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        //dialogo modal para crear nueva carpeta con selector de icono y validaciones avanzadas
        if (showCreateDialog) {
            val trimmedName = newFolderName.trim()
            val isDuplicate = foldersWithCount.any { it.folder.name.trim().equals(trimmedName, ignoreCase = true) }
            val isTooLong = newFolderName.length > 25
            val isBlank = trimmedName.isBlank()
            val hasError = (newFolderName.isNotBlank() && isDuplicate) || isTooLong
            val isValid = !isBlank && !isDuplicate && !isTooLong

            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                containerColor = SecondarySurface,
                title = {
                    Text(
                        text = "Nueva carpeta",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = newFolderName,
                            onValueChange = { newFolderName = it },
                            placeholder = { Text("Nombre de la carpeta", color = TextSecondary) },
                            singleLine = true,
                            isError = hasError,
                            supportingText = {
                                when {
                                    newFolderName.isNotBlank() && isDuplicate -> {
                                        Text(
                                            text = "Ya existe una carpeta con este nombre",
                                            color = MaterialTheme.colorScheme.error,
                                            fontSize = 12.sp
                                        )
                                    }
                                    isTooLong -> {
                                        Text(
                                            text = "Máximo 25 caracteres permitidos",
                                            color = MaterialTheme.colorScheme.error,
                                            fontSize = 12.sp
                                        )
                                    }
                                    else -> {
                                        Text(
                                            text = "${newFolderName.length}/25",
                                            color = TextSecondary,
                                            fontSize = 12.sp,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentYellow,
                                unfocusedBorderColor = BorderColor,
                                errorBorderColor = MaterialTheme.colorScheme.error,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Selecciona un icono:",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        //selector de iconos en fila
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvailableFolderIcons.forEach { iconName ->
                                val isSelected = selectedIconName == iconName
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) BorderColor else Color.Transparent)
                                        .border(
                                             width = if (isSelected) 2.dp else 1.dp,
                                             color = if (isSelected) AccentYellow else BorderColor,
                                             shape = RoundedCornerShape(12.dp)
                                         )
                                        .clickable { selectedIconName = iconName }
                                        .padding(6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(getFolderIconRes(iconName)),
                                        contentDescription = "Icono $iconName",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (isValid) {
                                folderViewModel.createFolder(
                                    name = trimmedName,
                                    iconName = selectedIconName
                                )
                                newFolderName = ""
                                showCreateDialog = false
                            }
                        },
                        enabled = isValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentYellow,
                            contentColor = Color.Black,
                            disabledContainerColor = SecondarySurface,
                            disabledContentColor = TextSecondary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Guardar", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancelar", color = TextSecondary)
                    }
                }
            )
        }
    }
}

//tarjeta individual para cada fila de la lista de carpetas
@Composable
fun FolderItemRow(
    folderWithCount: FolderWithCount,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(getFolderIconRes(folderWithCount.folder.iconName)),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = folderWithCount.folder.name,
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${folderWithCount.noteCount}",
                color = TextSecondary,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
