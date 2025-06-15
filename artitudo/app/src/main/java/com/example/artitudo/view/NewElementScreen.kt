package com.example.artitudo.view

import com.example.artitudo.ui.theme.backgroundColor
import com.example.artitudo.ui.theme.buttonColor
import com.example.artitudo.ui.theme.textColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.artitudo.R
import com.example.artitudo.viewmodel.AuthViewModel
import com.example.artitudo.viewmodel.ElementsViewModel
import com.example.artitudo.model.ElementLevel
import android.net.Uri // Import Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewElementScreen(
    elementsViewModel: ElementsViewModel,
    authViewModel: AuthViewModel,
    onElementCreated: (newElementId: String) -> Unit,

    onNavigateToAccount: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToCheckmark: () -> Unit = {},
    onNavigateToHeart: () -> Unit = {},
    onNavigateToStar: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    var elementName by remember { mutableStateOf("") }
    var elementDescription by remember { mutableStateOf("") }

    // --- Using your ElementLevel Enum ---
    val levelOptionsFromEnum = remember { ElementLevel.values().toList() } // Get all defined levels
    var selectedLevelEnum by remember { mutableStateOf<ElementLevel?>(null) } // Store the selected enum object or null

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var videoUri by remember { mutableStateOf<Uri?>(null) }

    var imageFileName by remember { mutableStateOf("") }
    var videoFileName by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }

    val viewModelErrorMessage by elementsViewModel.error.collectAsState()
    val isLoading by elementsViewModel.isLoading.collectAsState()
    var uiErrorMessage by remember { mutableStateOf<String?>(null) }
    val displayErrorMessage = uiErrorMessage ?: viewModelErrorMessage

    val stringErrorNameBlank = stringResource(id = R.string.name_cant_be_blank)
    val stringErrorPleasePickLevel = stringResource(id = R.string.please_pick_level)

    // --- Image Picker ---
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        imageFileName = uri?.lastPathSegment ?: "" // Display the file name
    }

    // --- Video Picker ---
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        videoUri = uri
        videoFileName = uri?.lastPathSegment ?: "" // Display the file name
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(bottom = 100.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = stringResource(id = R.string.nav_icon_description_back),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onNavigateBack() },
                    colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.60f))
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(id = R.string.new_element_title),
                color = textColor,
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Start
            )

            OutlinedTextField(
                value = elementName,
                onValueChange = { elementName = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.new_element_title_placeholder),
                        color = Color.Gray
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedBorderColor = buttonColor,
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = buttonColor
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(id = R.string.new_element_description),
                color = textColor,
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Start
            )

            OutlinedTextField(
                value = elementDescription,
                onValueChange = { elementDescription = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.new_element_description_placeholder),
                        color = Color.Gray
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedBorderColor = buttonColor,
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = buttonColor
                ),
                singleLine = false
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(id = R.string.new_element_level),
                color = textColor,
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Start
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded},
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedLevelEnum?.displayName ?: "", // Show display name or empty
                    onValueChange = {},
                    readOnly = true,
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.new_element_level_placeholder),
                            color = Color.Gray
                        )
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = buttonColor,
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = buttonColor
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .height(56.dp)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color(0xFF444444))
                ) {
                    levelOptionsFromEnum.forEach { level ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = level.displayName,
                                    color = textColor
                                )
                            },
                            onClick = {
                                selectedLevelEnum = level
                                expanded = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = textColor
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(id = R.string.new_element_picture),
                color = textColor,
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Start
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(
                        width = 1.dp,
                        color = if (imageUri != null) buttonColor else Color.Gray, // Check imageUri
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clickable {
                        imagePickerLauncher.launch("image/*") // Launch image picker
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = if (imageUri == null) stringResource(id=R.string.new_element_picture_placeholder) else imageFileName.ifEmpty { stringResource(id=R.string.new_element_picture_picked) },
                    color = if (imageUri == null) Color.Gray else textColor,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(id = R.string.new_element_video),
                color = textColor,
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Start
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(
                        width = 1.dp,
                        color = if (videoUri != null) buttonColor else Color.Gray, // Check videoUri
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clickable {
                        videoPickerLauncher.launch("video/*") // Launch video picker
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = if (videoUri == null) stringResource(id=R.string.new_element_video_placeholder) else videoFileName.ifEmpty { stringResource(id=R.string.new_element_video_picked) },
                    color = if (videoUri == null) Color.Gray else textColor,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            displayErrorMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button( // elementName, elementDescription, selectedLevel, imageFile, videoFile
                onClick = {
                    uiErrorMessage = null
                    // ViewModel will clear its own error in addNewElement

                    // --- Client-side validation ---
                    if (elementName.isBlank()) {
                        uiErrorMessage = stringErrorNameBlank
                        return@Button
                    }
                    if (selectedLevelEnum == null) {
                        uiErrorMessage = stringErrorPleasePickLevel
                        return@Button
                    }
                    elementsViewModel.addNewElement(
                        name = elementName,
                        description = elementDescription,
                        level = selectedLevelEnum!!.displayName,
                        levelNumber = selectedLevelEnum!!.levelNumber,
                        imageLocalUri = imageUri, // Pass Uri
                        videoLocalUri = videoUri, // Pass Uri
                        onSuccess = { generatedId ->
                            onElementCreated(generatedId)
                        },
                        onFailure = { detailedErrorFromVM ->
                            // uiErrorMessage = detailedErrorFromVM // ViewModel error is already collected
                            println("Error from VM: $detailedErrorFromVM")
                        }
                    )
                },
                enabled = !isLoading,
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = textColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.new_element_add_new_element),
                        color = textColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(100.dp)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 0.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.account),
                    contentDescription = stringResource(id = R.string.nav_icon_description_account),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onNavigateToAccount() },
                    colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.60f))
                )

                Image(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = stringResource(id = R.string.nav_icon_description_search),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onNavigateToSearch() },
                    colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.60f))
                )

                Image(
                    painter = painterResource(id = R.drawable.checkmark),
                    contentDescription = stringResource(id = R.string.nav_icon_description_checkmark),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onNavigateToCheckmark() },
                    colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.60f))
                )

                Image(
                    painter = painterResource(id = R.drawable.heart),
                    contentDescription = stringResource(id = R.string.nav_icon_description_heart),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onNavigateToHeart() },
                    colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.60f))
                )

                Image(
                    painter = painterResource(id = R.drawable.star),
                    contentDescription = stringResource(id = R.string.nav_icon_description_star),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onNavigateToStar() },
                    colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.60f))
                )
            }
        }
    }
}