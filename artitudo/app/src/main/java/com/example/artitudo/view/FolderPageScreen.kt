package com.example.artitudo.view

import com.example.artitudo.ui.theme.backgroundColor
import com.example.artitudo.ui.theme.buttonColor
import com.example.artitudo.ui.theme.textColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.artitudo.FolderNames
import com.example.artitudo.model.Element
import com.example.artitudo.viewmodel.AuthViewModel
import com.example.artitudo.viewmodel.ElementsViewModel
import com.example.artitudo.viewmodel.UserFolderNames
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderPageScreen(
    folderScreenIdentifier: String,
    elementsViewModel: ElementsViewModel, // Get ElementsViewModel
    authViewModel: AuthViewModel,
    onElementClick: (elementId: String) -> Unit = {},

    onNavigateToAccount: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToCheckmark: () -> Unit = {},
    onNavigateToHeart: () -> Unit = {},
    onNavigateToStar: () -> Unit = {}
) {
    val userData by authViewModel.userData.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    var selectedLevel by remember { mutableStateOf("All") }
    val levelOptions = listOf("All", "Spins", "Beginner", "Intermediate", "Advanced", "Other")

    val focusManager = LocalFocusManager.current
    val isLoading by elementsViewModel.isLoading.collectAsState()
    val error by elementsViewModel.error.collectAsState()
    val isLoadingAuth by authViewModel.isLoading.collectAsState()
    val errorAuth by authViewModel.authError.collectAsState()
    val displayError = error ?: errorAuth

    var isRefreshing = isLoading || isLoadingAuth

    val (folderKey, folderNameResId) = remember(folderScreenIdentifier) {
        when (folderScreenIdentifier) {
            FolderNames.FAVORITI -> UserFolderNames.FAVORITES to R.string.folder_name_favorites
            FolderNames.ZELJE -> UserFolderNames.WISHLIST to R.string.folder_name_wishlist
            FolderNames.USAVRSENI_ELEMENTI -> UserFolderNames.MASTERED to R.string.folder_name_mastered
            else -> "" to R.string.folder_name_other
        }
    }
    val displayFolderName = stringResource(id = folderNameResId)

    // For bottom bar active state (remains the same)
    val isFavoritiFolder = folderScreenIdentifier == FolderNames.FAVORITI
    val isZeljeFolder = folderScreenIdentifier == FolderNames.ZELJE
    val isUsavrseniFolder = folderScreenIdentifier == FolderNames.USAVRSENI_ELEMENTI

    LaunchedEffect(userData, folderKey) {
        userData?.let { user ->
            elementsViewModel.updateUserFolderIds(
                masteredIds = user.folders[UserFolderNames.MASTERED],
                favoritesIds = user.folders[UserFolderNames.FAVORITES],
                wishlistIds = user.folders[UserFolderNames.WISHLIST]
            )
        }
    }
    val elementsInThisFolder: List<Element> = remember(folderKey,
        elementsViewModel.elementsForUserFolders.collectAsState().value
    ) { // Recompute if allElements or folderKey changes
        elementsViewModel.getElementsForFolder(folderKey)
    }
    // Apply level filtering on top of the folder's elements
    val finalFilteredElements = remember(elementsInThisFolder, selectedLevel) {
        if (selectedLevel == "All") {
            elementsInThisFolder
        } else {
            elementsInThisFolder.filter { element ->
                element.level.equals(selectedLevel, ignoreCase = true)
            }
        }
    }
    LaunchedEffect(isLoading) { // Or a combined loading state if you add one for userData
        isRefreshing = isLoading
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            elementsViewModel.fetchAllElementsForFiltering() // Or your actual element fetch function name
            currentUser?.uid?.let { userId ->
                authViewModel.refreshUserData(userId) // Call the new function in AuthViewModel
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .windowInsetsPadding(WindowInsets.statusBars)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }) {
                focusManager.clearFocus() // Use LocalFocusManager.current
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
                .padding(bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(25.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = displayFolderName,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${stringResource(id = R.string.number_of_elements)} ${finalFilteredElements.size}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = textColor.copy(alpha = 0.8f),
                        fontSize = 16.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(levelOptions.size) { index ->
                    val level = levelOptions[index]
                    LevelButton(
                        text = level,
                        isSelected = selectedLevel == level,
                        onClick = { selectedLevel = level }
                    )
                }
            }

            // Content display logic
            if (isRefreshing && finalFilteredElements.isEmpty() && currentUser != null) {
                // Show loader if actively refreshing AND list is currently empty for a logged-in user
                CircularProgressIndicator(color = buttonColor, modifier = Modifier.padding(16.dp))
            } else if (displayError != null) {
                Text(
                    "${stringResource(id = R.string.error_error)} $displayError",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            } else if (currentUser == null && folderKey.isNotEmpty()) {
                Text(
                    stringResource(id = R.string.error_message_no_user),
                    color = Color.White,
                    modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally)
                )
            } else if (finalFilteredElements.isEmpty() && currentUser != null) {
                Text(
                    stringResource(id = R.string.error_message_no_elements),
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            } else if (finalFilteredElements.isNotEmpty()) {
                ElementGrid(elements = finalFilteredElements, onElementClick = onElementClick)
            } else if (currentUser == null && folderKey.isEmpty()) {
                Text(
                    stringResource(id = R.string.content_not_available),
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))


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
                    painter = painterResource(
                        id = if (isUsavrseniFolder) R.drawable.checkmark_filled else R.drawable.checkmark
                    ),
                    contentDescription = stringResource(id = R.string.nav_icon_description_checkmark),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onNavigateToCheckmark() },
                    colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.60f))
                )

                Image(
                    painter = painterResource(
                        id = if (isFavoritiFolder) R.drawable.heart_filled else R.drawable.heart
                    ),
                    contentDescription = stringResource(id = R.string.nav_icon_description_heart),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onNavigateToHeart() },
                    colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.60f))
                )

                Image(
                    painter = painterResource(
                        id = if (isZeljeFolder) R.drawable.star_filled else R.drawable.star
                    ),
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