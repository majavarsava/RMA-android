package com.example.artitudo.view

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.artitudo.R
import kotlin.math.exp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewElementScreen(
    onNavigateToAccount: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToCheckmark: () -> Unit = {},
    onNavigateToHeart: () -> Unit = {},
    onNavigateToStar: () -> Unit = {},
    onAddElement: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    var elementName by remember { mutableStateOf("") }
    var elementDescription by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf("") }
    var imageFile by remember { mutableStateOf("") }
    var videoFile by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val levelOptions = listOf("Okreti", "Beginner", "Intermediate", "Advanced")

    // Color definitions
    val backgroundColor = Color(0xFF333333)
    val buttonColor = Color(0xFF722F7F)
    val textColor = Color.White

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
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onNavigateBack() },
                    colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.60f))
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Ime elementa:",
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
                        text = "Unesite ime elementa...",
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
                text = "Opis:",
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
                        text = "Unesite opis elementa...",
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
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = false
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Level:",
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
                    value = selectedLevel,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = {
                        Text(
                            text = "Izaberite level elementa...",
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
                    levelOptions.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option,
                                    color = textColor
                                )
                            },
                            onClick = {
                                selectedLevel = option
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
                text = "Slika:",
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
                        color = if (imageFile.isNotEmpty()) buttonColor else Color.Gray,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clickable {
                        // TODO: Implement image picker
                        imageFile = "selected_image.jpg" // Placeholder
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = if (imageFile.isEmpty()) "Izaberite sliku..." else imageFile,
                    color = if (imageFile.isEmpty()) Color.Gray else textColor,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Video:",
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
                        color = if (videoFile.isNotEmpty()) buttonColor else Color.Gray,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clickable {
                        // TODO: Implement video picker
                        videoFile = "selected_video.mp4" // Placeholder
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = if (videoFile.isEmpty()) "Izaberite video..." else videoFile,
                    color = if (videoFile.isEmpty()) Color.Gray else textColor,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button( // elementName, elementDescription, selectedLevel, imageFile, videoFile
                onClick = { onAddElement() },
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Dodaj novi element",
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
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