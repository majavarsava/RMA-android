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
import androidx.compose.material3.TextFieldColors
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager


data class Element(
    val id: Int,
    val name: String,
    val image: String,
    val level: String
)

fun getFilteredElements(query: String, level: String): List<Element> {
    val allElements = listOf(
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

    return allElements
        .filter { level == "All" || it.level == level }
        .filter { it.name.contains(query, ignoreCase = true) }
}

@Composable
fun ElementsScreen(
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
    var searchQuery by remember { mutableStateOf("") }
    val levelOptions = listOf("All", "Spins", "Beginner", "Intermediate", "Advanced")

    val focusManager = LocalFocusManager.current


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
                .padding(horizontal = 24.dp)
                .padding(bottom = 100.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(30.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logotext),
                    contentDescription = "Name logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 8.dp),
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

            Spacer(modifier = Modifier.height(24.dp))

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
                    painter = painterResource(id = R.drawable.checkmark),
                    contentDescription = "Checkmark",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onNavigateToCheckmark() },
                    colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.60f))
                )

                Image(
                    painter = painterResource(id = R.drawable.heart),
                    contentDescription = "Heart",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onNavigateToHeart() },
                    colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.60f))
                )

                Image(
                    painter = painterResource(id = R.drawable.star),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    val purpleColor = Color(0xFF722F7F)
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) purpleColor else Color.Gray,
                shape = RoundedCornerShape(8.dp)
            ),
        placeholder = { Text("Pretraži element...") },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, "Clear", tint = Color.Gray)
                }
            }
        },
        leadingIcon = {
            Icon(Icons.Default.Search, "Search", tint = Color.Gray)
        },
        singleLine = true,
        shape = RoundedCornerShape(8.dp)
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
        modifier = Modifier.height(40.dp)
    ) {
        Text(text)
    }
}

