package com.example.artitudo.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.artitudo.R

@Composable
fun ProfilePageScreen(
    username: String = "placeholder",
    completedElements: Int = 0,
    onLogoutClick: () -> Unit = {},
    onCheckLevelClick: () -> Unit = {},
    onAddElementClick: () -> Unit = {},
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(bottom = 100.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(60.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(50.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Korisničko ime: ",
                    color = textColor,
                    fontSize = 24.sp
                )
                Text(
                    text = username,
                    color = textColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Broj usavršenih elemenata: ",
                        color = textColor,
                        fontSize = 24.sp
                    )
                    Text(
                        text = completedElements.toString(),
                        color = textColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onLogoutClick,
                    modifier = Modifier
                        .width(200.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Odjava",
                        color = textColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Check level button
                Button(
                    onClick = onCheckLevelClick,
                    modifier = Modifier
                        .width(200.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Provjeri level šipke",
                        color = textColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Add element button
                Button(
                    onClick = onAddElementClick,
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