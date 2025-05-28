package com.example.artitudo.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.artitudo.R
import coil.compose.rememberAsyncImagePainter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import coil.compose.AsyncImage
import com.example.artitudo.FolderNames

fun getFilteredFolder(elements: List<Element>, level: String): List<Element> {
    val elements = listOf(
        Element(
            1,
            "Fireman Spin",
            "https://images.pexels.com/photos/6252554/pexels-photo-6252554.jpeg",
            "Spins"
        ),
        Element(
            2,
            "Butterfly",
            "https://images.pexels.com/photos/6253088/pexels-photo-6253088.jpeg",
            "Beginner"
        ),
        Element(
            3,
            "Crucifix",
            "https://images.pexels.com/photos/6604211/pexels-photo-6604211.jpeg",
            "Intermediate"
        ),
        Element(
            4,
            "Ayesha",
            "https://images.pexels.com/photos/6253100/pexels-photo-6253100.jpeg",
            "Advanced"
        ),
        Element(
            5,
            "Cross Ankle Release",
            "https://images.pexels.com/photos/6253074/pexels-photo-6253074.jpeg",
            "Beginner"
        ),
        Element(
            6,
            "Gemini",
            "https://images.pexels.com/photos/6253069/pexels-photo-6253069.jpeg",
            "Intermediate"
        ),
        Element(
            7,
            "Brass Monkey",
            "https://images.pexels.com/photos/6253022/pexels-photo-6253022.jpeg",
            "Advanced"
        ),
        Element(
            8,
            "Martini",
            "https://images.pexels.com/photos/6330756/pexels-photo-6330756.jpeg",
            "Spins"
        )
    )

    return elements.filter { level == "All" || it.level == level }
}

@Composable
fun FolderPageScreen(
    folderName: String = "Folder Name",
    levels: List<String> = listOf("All", "Spins", "Beginner", "Intermediate", "Advanced"),
    onLevelChange: (Int) -> Unit = {},
    elements: List<Element> = listOf(), // Replace with real image resources
    onElementClick: (Int) -> Unit = {},

    onNavigateToAccount: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToCheckmark: () -> Unit = {},
    onNavigateToHeart: () -> Unit = {},
    onNavigateToStar: () -> Unit = {}
) {
    // Color definitions
    val backgroundColor = Color(0xFF333333)
    val buttonColor = Color(0xFF722F7F)
    val textColor = Color.White
    val inactiveButtonColor = Color.LightGray

    var selectedLevel by remember { mutableStateOf("All") }
    val levelOptions = listOf("All", "Spins", "Beginner", "Intermediate", "Advanced")

    val focusManager = LocalFocusManager.current
    val filteredFolder by remember(selectedLevel, elements) {
        derivedStateOf { getFilteredFolder(elements, selectedLevel) }
    }

    val isFavoritiFolder = folderName == FolderNames.FAVORITI
    val isZeljeFolder = folderName == FolderNames.ZELJE
    val isUsavrseniFolder = folderName == FolderNames.USAVRSENI_ELEMENTI

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .windowInsetsPadding(WindowInsets.statusBars)
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = folderName,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Broj elemenata: ${filteredFolder.size}",
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

            ElementGrid(elements = filteredFolder, onElementClick = onElementClick)
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
                    contentDescription = "Account",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onNavigateToAccount() },
                    colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.60f))
                )

                Image(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = "Search",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onNavigateToSearch() },
                    colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.60f))
                )

                Image(
                    painter = painterResource(
                        id = if (isUsavrseniFolder) R.drawable.checkmark_filled else R.drawable.checkmark
                    ),
                    contentDescription = "Checkmark",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onNavigateToCheckmark() },
                    colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.60f))
                )

                Image(
                    painter = painterResource(
                        id = if (isFavoritiFolder) R.drawable.heart_filled else R.drawable.heart
                    ),
                    contentDescription = "Heart",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onNavigateToHeart() },
                    colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.60f))
                )

                Image(
                    painter = painterResource(
                        id = if (isZeljeFolder) R.drawable.star_filled else R.drawable.star
                    ),
                    contentDescription = "Star",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onNavigateToStar() },
                    colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.60f))
                )
            }
        }
    }
}
