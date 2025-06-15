package com.example.artitudo.view

import androidx.compose.animation.core.copy
import com.example.artitudo.viewmodel.LevelerViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.artitudo.ui.theme.backgroundColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.artitudo.R
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.artitudo.ui.theme.lightPurple
import com.example.artitudo.ui.theme.textColor
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelerScreen(
    levelerViewModel: LevelerViewModel = viewModel(),
    onNavigateToAccount: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToCheckmark: () -> Unit = {},
    onNavigateToHeart: () -> Unit = {},
    onNavigateToStar: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val pitch by levelerViewModel.pitch.collectAsState()
    val roll by levelerViewModel.roll.collectAsState()
    val isLevel by levelerViewModel.isLevel.collectAsState()
    val currentBackgroundColor by levelerViewModel.levelerColor.collectAsState()

    // Manage sensor listener lifecycle with screen lifecycle
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> levelerViewModel.startListening()
                Lifecycle.Event.ON_PAUSE ->  levelerViewModel.stopListening()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            // ViewModel's onCleared will also call stopListening, but this is good practice
            levelerViewModel.stopListening()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(currentBackgroundColor)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(bottom = 100.dp),
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
                    colorFilter = ColorFilter.tint(if(isLevel) Color.Black else Color.White.copy(alpha = 0.60f))
                )
            }

            LevelerDisplay(pitch = pitch, isLevel = isLevel, modifier = Modifier
                .weight(1f)
                .fillMaxWidth())
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

@Composable
fun LevelerDisplay(pitch: Float, isLevel: Boolean, modifier: Modifier = Modifier) {
    val angleTextSize = 24.sp
    val angleTextColor =  if (isLevel) Color.Green else Color.LightGray

    Box(
        modifier = modifier.clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        // Box to hold the Logo and Text, this Box will be rotated
        Box(
            modifier = Modifier
                .graphicsLayer(
                    rotationZ = pitch + 90.0f // Rotate this Box (and its contents) by the pitch angle
                    // rotation around Z-axis
                )
                .wrapContentSize(), // Important so it doesn't try to fill max size before rotation
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = stringResource(id = R.string.logo_content_description),
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.Center),
                contentScale = ContentScale.Fit
            )

            Text(
                text = "%.1f°".format(pitch),
                color = angleTextColor,
                fontSize = angleTextSize,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 300.dp / 2 + angleTextSize.value.dp / 2 + 8.dp)
            )
        }
    }
}

