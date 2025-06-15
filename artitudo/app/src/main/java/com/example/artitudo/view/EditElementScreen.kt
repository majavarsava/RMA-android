// EditElementScreen.kt
package com.example.artitudo.view

import com.example.artitudo.ui.theme.backgroundColor
import com.example.artitudo.ui.theme.buttonColor
import com.example.artitudo.ui.theme.textColor
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage // For displaying existing image
import com.example.artitudo.R
import com.example.artitudo.model.ElementLevel
import com.example.artitudo.viewmodel.AuthViewModel
import com.example.artitudo.viewmodel.ElementsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditElementScreen(
    elementId: String, // Element to edit
    elementsViewModel: ElementsViewModel,
    authViewModel: AuthViewModel, // If needed for auth checks, though rules handle security
    onElementUpdated: () -> Unit, // Navigate back or to detail after update

    onNavigateToAccount: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToCheckmark: () -> Unit = {},
    onNavigateToHeart: () -> Unit = {},
    onNavigateToStar: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val existingElement by elementsViewModel.selectedElement.collectAsState()
    val isLoadingElementDetails by elementsViewModel.isLoading.collectAsState() // For fetching initial details

    // --- Form State ---
    var elementName by remember { mutableStateOf("") }
    var elementDescription by remember { mutableStateOf("") }
    val levelOptionsFromEnum = remember { ElementLevel.values().toList() }
    var selectedLevelEnum by remember { mutableStateOf<ElementLevel?>(null) }

    // --- File State ---
    var imageUri by remember { mutableStateOf<Uri?>(null) } // New image to upload
    var videoUri by remember { mutableStateOf<Uri?>(null) } // New video to upload
    var imageFileName by remember { mutableStateOf("") } // For display of new image
    var videoFileName by remember { mutableStateOf("") } // For display of new video

    // --- For displaying existing files (URLs from Firestore) ---
    var existingImageUrl by remember { mutableStateOf("") }
    var existingVideoUrl by remember { mutableStateOf("") }


    var expanded by remember { mutableStateOf(false) }

    val viewModelErrorMessage by elementsViewModel.error.collectAsState()
    val isUpdating by elementsViewModel.isLoading.collectAsState() // Use general isLoading or a specific one for update
    var uiErrorMessage by remember { mutableStateOf<String?>(null) }
    val displayErrorMessage = uiErrorMessage ?: viewModelErrorMessage

    val stringCurrentImage = stringResource(id = R.string.current_image)
    val stringCurrentVideo = stringResource(id = R.string.current_video)
    val stringNewPictureSelected = stringResource(id = R.string.new_picture_selected)
    val stringNewVideoSelected = stringResource(id = R.string.new_video_selected)
    val stringErrorNameBlank = stringResource(id = R.string.name_cant_be_blank)
    val stringErrorPleasePickLevel = stringResource(id = R.string.please_pick_level)

    // --- Load existing element data when the screen is first composed or elementId changes ---
    LaunchedEffect(key1 = elementId) {
        elementsViewModel.fetchElementById(elementId)
    }

    // --- Populate form fields when existingElement data is available ---
    LaunchedEffect(existingElement) {
        existingElement?.let { element ->
            elementName = element.name
            elementDescription = element.description
            selectedLevelEnum = ElementLevel.values().find { it.displayName == element.level }
            existingImageUrl = element.image // Store existing image URL
            existingVideoUrl = element.video // Store existing video URL
            imageFileName = if (element.image.isNotEmpty()) stringCurrentImage else "" // Placeholder for existing
            videoFileName = if (element.video.isNotEmpty()) stringCurrentVideo else "" // Placeholder for existing
        }
    }

    // --- Image Picker ---
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri // This is for the NEW image
        imageFileName = uri?.lastPathSegment ?: stringNewPictureSelected
    }

    // --- Video Picker ---
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        videoUri = uri // This is for the NEW video
        videoFileName = uri?.lastPathSegment ?: stringNewVideoSelected
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        if (isLoadingElementDetails && existingElement == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = buttonColor)
        } else if (existingElement == null && !isLoadingElementDetails) {
            Text(
                stringResource(id = R.string.element_not_found_or_cant_be_loaded),
                color = Color.White,
                modifier = Modifier.align(Alignment.Center).padding(16.dp)
            )
        } else if (existingElement != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 100.dp) // For bottom nav bar
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- Top Bar with Back Button ---
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
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        stringResource(id = R.string.edit_element),
                        style = MaterialTheme.typography.titleLarge.copy(color = textColor, fontWeight = FontWeight.Bold),
                    )
                    Spacer(modifier = Modifier.weight(1f)) // Pushes title to center if back button is only item
                    Spacer(modifier = Modifier.size(24.dp)) // Placeholder for symmetry if needed
                }


                Spacer(modifier = Modifier.height(16.dp)) // Reduced spacer

                // --- Element Name ---
                Text(stringResource(id = R.string.new_element_title), color = textColor, fontSize = 16.sp, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), textAlign = TextAlign.Start)
                OutlinedTextField(
                    value = elementName,
                    onValueChange = { elementName = it },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    placeholder = { Text(stringResource(id = R.string.new_element_title_placeholder), color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, focusedBorderColor = buttonColor, unfocusedBorderColor = Color.Gray, cursorColor = buttonColor),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(20.dp))

                // --- Element Description ---
                Text(stringResource(id = R.string.new_element_description), color = textColor, fontSize = 16.sp, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), textAlign = TextAlign.Start)
                OutlinedTextField(
                    value = elementDescription,
                    onValueChange = { elementDescription = it },
                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 100.dp), // Allow multiline description
                    placeholder = { Text(stringResource(id = R.string.new_element_description_placeholder), color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, focusedBorderColor = buttonColor, unfocusedBorderColor = Color.Gray, cursorColor = buttonColor),
                    singleLine = false
                )
                Spacer(modifier = Modifier.height(20.dp))

                // --- Level Dropdown ---
                Text(stringResource(id = R.string.new_element_level), color = textColor, fontSize = 16.sp, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), textAlign = TextAlign.Start)
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedLevelEnum?.displayName ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text(stringResource(id = R.string.new_element_level_placeholder), color = Color.Gray) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, focusedBorderColor = buttonColor, unfocusedBorderColor = Color.Gray, cursorColor = buttonColor),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color(0xFF444444))) {
                        levelOptionsFromEnum.forEach { level ->
                            DropdownMenuItem(
                                text = { Text(text = level.displayName, color = textColor) },
                                onClick = { selectedLevelEnum = level; expanded = false },
                                colors = MenuDefaults.itemColors(textColor = textColor)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                // --- Image Picker/Display ---
                Text(stringResource(id = R.string.new_element_picture), color = textColor, fontSize = 16.sp, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), textAlign = TextAlign.Start)
                // Display current image if it exists and no new URI is selected
                if (imageUri == null && existingImageUrl.isNotEmpty()) {
                    AsyncImage(model = existingImageUrl, contentDescription = stringResource(id = R.string.current_image),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(3f/4f)
                            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Box(
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                        .border(width = 1.dp, color = if (imageUri != null) buttonColor else Color.Gray, shape = RoundedCornerShape(4.dp))
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = if (imageUri == null) (if(existingImageUrl.isNotEmpty()) stringResource(id = R.string.change_picture) else stringResource(id = R.string.new_element_picture_placeholder)) else imageFileName.ifEmpty { stringResource(id = R.string.new_picture_selected) },
                        color = if (imageUri == null && existingImageUrl.isEmpty()) Color.Gray else textColor,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))

                // --- Video Picker/Display ---
                Text(stringResource(id = R.string.new_element_video), color = textColor, fontSize = 16.sp, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), textAlign = TextAlign.Start)
                // Display current video placeholder if it exists and no new URI is selected
                if (videoUri == null && existingVideoUrl.isNotEmpty()) {
                    Text("${stringResource(id = R.string.current_video)}: ${existingVideoUrl.substringAfterLast('/').substringBefore('?')}", // Basic name extraction
                        color = textColor.copy(alpha=0.7f),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                }
                Box(
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                        .border(width = 1.dp, color = if (videoUri != null) buttonColor else Color.Gray, shape = RoundedCornerShape(4.dp))
                        .clickable { videoPickerLauncher.launch("video/*") },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = if (videoUri == null) (if(existingVideoUrl.isNotEmpty()) stringResource(id = R.string.change_video) else stringResource(id = R.string.new_element_video_placeholder)) else videoFileName.ifEmpty { stringResource(id = R.string.new_video_selected) },
                        color = if (videoUri == null && existingVideoUrl.isEmpty()) Color.Gray else textColor,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // --- Error Message Display ---
                displayErrorMessage?.let { message ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Spacer(modifier = Modifier.height(32.dp))

                // --- Save Button ---
                Button(
                    onClick = {
                        uiErrorMessage = null
                        if (elementName.isBlank()) {
                            uiErrorMessage = stringErrorNameBlank
                            return@Button
                        }
                        if (selectedLevelEnum == null) {
                            uiErrorMessage = stringErrorPleasePickLevel
                            return@Button
                        }
                        existingElement?.let { currentElement ->
                            elementsViewModel.updateElement(
                                elementId = currentElement.id,
                                name = elementName,
                                description = elementDescription,
                                level = selectedLevelEnum!!.displayName,
                                levelNumber = selectedLevelEnum!!.levelNumber,
                                newImageLocalUri = imageUri,      // Pass new image URI
                                currentImageUrl = currentElement.image, // Pass existing image URL (for potential deletion or keeping)
                                newVideoLocalUri = videoUri,      // Pass new video URI
                                currentVideoUrl = currentElement.video, // Pass existing video URL
                                onSuccess = {
                                    onElementUpdated() // Callback on success
                                },
                                onFailure = { errorMsg ->
                                    // uiErrorMessage = errorMsg // ViewModel handles _error state
                                    println("Error from VM update: $errorMsg")
                                }
                            )
                        }
                    },
                    enabled = !isUpdating,
                    modifier = Modifier.width(200.dp).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = textColor, strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(id = R.string.save_changes), color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp)) // Spacer at the bottom before nav bar
            }
        }

        // --- Bottom Navigation Bar ---
        Box(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(100.dp)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 0.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nav Icons (same as NewElementScreen)
                Image(painter = painterResource(id = R.drawable.account), contentDescription = stringResource(id = R.string.nav_icon_description_account), modifier = Modifier.size(24.dp).clickable { onNavigateToAccount() }, colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.60f)))
                Image(painter = painterResource(id = R.drawable.search), contentDescription = stringResource(id = R.string.nav_icon_description_search), modifier = Modifier.size(24.dp).clickable { onNavigateToSearch() }, colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.60f)))
                Image(painter = painterResource(id = R.drawable.checkmark), contentDescription = stringResource(id = R.string.nav_icon_description_checkmark), modifier = Modifier.size(24.dp).clickable { onNavigateToCheckmark() }, colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.60f)))
                Image(painter = painterResource(id = R.drawable.heart), contentDescription = stringResource(id = R.string.nav_icon_description_heart), modifier = Modifier.size(24.dp).clickable { onNavigateToHeart() }, colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.60f)))
                Image(painter = painterResource(id = R.drawable.star), contentDescription = stringResource(id = R.string.nav_icon_description_star), modifier = Modifier.size(24.dp).clickable { onNavigateToStar() }, colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.60f)))
            }
        }
    }
}