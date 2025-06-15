package com.example.artitudo.view

import com.example.artitudo.ui.theme.backgroundColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.artitudo.R
import coil.compose.rememberAsyncImagePainter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.error
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.artitudo.model.Element
import com.example.artitudo.viewmodel.AuthViewModel
import com.example.artitudo.viewmodel.ElementsViewModel
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.res.stringResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElementsScreen(
    authViewModel: AuthViewModel,
    elementsViewModel: ElementsViewModel,

    onElementClick: (elementId: String) -> Unit,

    onNavigateToAccount: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToCheckmark: () -> Unit = {},
    onNavigateToHeart: () -> Unit = {},
    onNavigateToStar: () -> Unit = {}
) {
    var selectedLevel by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    val levelOptions = listOf("All", "Spins", "Beginner", "Intermediate", "Advanced", "Other")

    val focusManager = LocalFocusManager.current
    // Collect states from ElementsViewModel
    val filteredElements by elementsViewModel.filteredElements.collectAsState()
    val isLoading by elementsViewModel.isLoading.collectAsState()
    val error by elementsViewModel.error.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }

    // Apply filters whenever searchQuery or selectedLevel changes
    LaunchedEffect(searchQuery, selectedLevel) {
        elementsViewModel.updateFilters(searchQuery, selectedLevel)
    }

    LaunchedEffect(isLoading) {
        isRefreshing = isLoading
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            elementsViewModel.fetchAllElementsForFiltering()
        },
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .windowInsetsPadding(WindowInsets.statusBars)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }) {
                focusManager.clearFocus()
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logotext),
                    contentDescription = stringResource(id = R.string.logo_content_description),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it }
            )

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
            if (isLoading && filteredElements.isEmpty()) { // Show loading only if list is empty initially
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(color = Color(0xFF722F7F))
            } else if (error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error)
            } else if (filteredElements.isEmpty() && !isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(id=R.string.no_element_for_query), color = Color.White)
            } else {
                ElementGrid(elements = filteredElements, onElementClick = onElementClick)
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
                    painter = painterResource(id = R.drawable.search_filled),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    val purpleColor = Color(0xFF722F7F)
    var isFocused by remember { mutableStateOf(false) }

    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .height(48.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) purpleColor else Color.Gray,
                shape = RoundedCornerShape(8.dp)
            ),
        placeholder = { Text(stringResource(id = R.string.search_for_element), color = Color.Gray) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, stringResource(id = R.string.clear_icon), tint = Color.Gray)
                }
            }
        },
        leadingIcon = {
            Icon(Icons.Default.Search, stringResource(id = R.string.search_icon), tint = Color.Gray, modifier = Modifier.size(20.dp))
        },
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.colors(
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent
        )
    )
}

@Composable
fun LevelButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val purpleColor = Color(0xFF722F7F)

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) purpleColor else Color.LightGray,
            contentColor = if (isSelected) Color.White else Color.Black
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.height(35.dp)
    ) {
        Text(text)
    }
}

@Composable
fun ElementGrid(elements: List<Element>, onElementClick: (elementId: String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = elements, // The list of elements
            key = { element -> element.id } // The stable key for each element
        ) { element -> // The lambda directly gives you each element
            GridElementCard(element = element) {
                onElementClick(element.id) // Assuming Element has an 'id' property
            }
        }
    }
}

@Composable
fun GridElementCard(element: Element, onElementClick: () -> Unit) {
    Card(
        modifier = Modifier
            .clickable { onElementClick() }
            .fillMaxWidth()
            .aspectRatio(4f/5f),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF444444))
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AsyncImage(
                model = element.image,
                contentDescription = element.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                placeholder = painterResource(id = R.drawable.logo), // Add a placeholder
                error = painterResource(id = R.drawable.logo) // Add an error image
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
            ) {
                Text(
                    text = element.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

