package com.example.artitudo.view

import com.example.artitudo.ui.theme.backgroundColor
import com.example.artitudo.ui.theme.buttonColor
import com.example.artitudo.ui.theme.textColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import coil.compose.AsyncImage
import com.example.artitudo.R
import com.example.artitudo.viewmodel.AuthViewModel
import com.example.artitudo.viewmodel.ElementsViewModel
import com.example.artitudo.viewmodel.UserFolderNames

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElementDetailScreen(
    elementId: String,
    elementsViewModel: ElementsViewModel,
    authViewModel: AuthViewModel,

    onNavigateToAccount: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToCheckmark: () -> Unit = {},
    onNavigateToHeart: () -> Unit = {},
    onNavigateToStar: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    //admin actions
    onEditClick: (elementId: String) -> Unit = {},
    onElementDeletedSuccessfully: () -> Unit = {}
) {
    val element by elementsViewModel.selectedElement.collectAsState()
    val isLoadingElement by elementsViewModel.isLoading.collectAsState() // Or a specific loading state for detail
    val error by elementsViewModel.error.collectAsState() // Or a specific error state
    val errorElement by elementsViewModel.error.collectAsState() // Element specific error

    val authError by authViewModel.authError.collectAsState() // Auth specific error

    val isElementInMastered by authViewModel.isElementInMastered.collectAsState()
    val isElementInFavorites by authViewModel.isElementInFavorites.collectAsState()
    val isElementInWishlist by authViewModel.isElementInWishlist.collectAsState()

    val currentUser by authViewModel.currentUser.collectAsState() // To know if user is logged in
    // --- For Delete Confirmation Dialog ---
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isAdmin by authViewModel.isAdmin.collectAsState()

    LaunchedEffect(elementId) {
        elementsViewModel.fetchElementById(elementId)
    }
    LaunchedEffect(elementId, authViewModel.userData.collectAsState().value) {
        authViewModel.checkElementFolderStatus(elementId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        if (isLoadingElement && element == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = buttonColor)
        } else if (error != null && element == null) {
            Text(
                text = "${stringResource(id = R.string.error_error)} ${error}",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center).padding(16.dp)
            )
        } else if (element != null) {
            val currentElement = element!!
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

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = currentElement.name,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = Color.White,
                    disabledContentColor = Color.White,
                    disabledContainerColor = buttonColor
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(35.dp),
                enabled = false // non-clickable since it's just for display
            ) {
                Text(currentElement.level)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = currentElement.description,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = textColor,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

                if (currentUser != null) { // Only show if user is logged in
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(
                                id = if (isElementInMastered) R.drawable.checkmark_filled else R.drawable.checkmark
                            ),
                            contentDescription = stringResource(id = R.string.nav_icon_description_checkmark),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    authViewModel.toggleElementInFolder(
                                        currentElement.id,
                                        UserFolderNames.MASTERED
                                    )
                                },
                            colorFilter = ColorFilter.tint(if (isElementInMastered) buttonColor else Color.White)
                        )

                        Image(
                            painter = painterResource(
                                id = if (isElementInFavorites) R.drawable.heart_filled else R.drawable.heart
                            ),
                            contentDescription = stringResource(id = R.string.nav_icon_description_heart),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    authViewModel.toggleElementInFolder(
                                        currentElement.id,
                                        UserFolderNames.FAVORITES
                                    )
                                },
                            colorFilter = ColorFilter.tint(if (isElementInFavorites) buttonColor else Color.White)
                        )

                        Image(
                            painter = painterResource(
                                id = if (isElementInWishlist) R.drawable.star_filled else R.drawable.star
                            ),
                            contentDescription = stringResource(id = R.string.nav_icon_description_star),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    authViewModel.toggleElementInFolder(
                                        currentElement.id,
                                        UserFolderNames.WISHLIST
                                    )
                                },
                            colorFilter = ColorFilter.tint(if (isElementInWishlist) buttonColor else Color.White)
                        )
                    }
                }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f/5f),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF444444))
            ) {
                if (element!!.image.isNotEmpty()) {
                    AsyncImage(
                        model = currentElement.image,
                        contentDescription = element!!.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize() ,
                        placeholder = painterResource(id = R.drawable.logo), // Add placeholder
                        error = painterResource(id = R.drawable.logo) // Add error image
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.no_picture_text),
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isAdmin) {
                Button(
                    onClick = { onEditClick(currentElement.id) },
                    modifier = Modifier
                        .width(200.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.edit_text),
                        color = textColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        elementsViewModel.clearError() // Clear previous errors
                        showDeleteDialog = true // Show confirmation dialog
                    },
                    enabled = !isLoadingElement,
                    modifier = Modifier
                        .width(200.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F) // Red color for delete
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.delete_text),
                        color = textColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
        } else if (!isLoadingElement) { // Element not found, not loading, and no specific error yet
            Text(
                stringResource(id = R.string.element_not_found),
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // --- Confirmation Dialog for Delete ---
        if (showDeleteDialog && element != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(stringResource(id = R.string.confirm_delete)) },
                text = { Text(stringResource(id = R.string.are_you_sure) + " \"${element!!.name}\"? " + stringResource(id = R.string.action_cant_be_deleted)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            elementsViewModel.deleteElement(element!!.id){
                                onElementDeletedSuccessfully() // Navigate after deletion
                            }
                        }
                    ) {
                        Text(stringResource(id = R.string.delete_text), color = Color(0xFFD32F2F))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(stringResource(id = R.string.cancel_text))
                    }
                },
                containerColor = Color(0xFF444444), // Darker background for dialog
                titleContentColor = textColor,
                textContentColor = textColor.copy(alpha = 0.8f)
            )
        }

        if (error != null && !showDeleteDialog ) { // Don't show if dialog is up or if element load error already shown
            // You might want a more specific place for this error if it's not a screen-blocking one
            Box(modifier = Modifier.align(Alignment.Center).padding(16.dp)) { // Example placement
                Text(
                    text = "${stringResource(id = R.string.error_error)} $error",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
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