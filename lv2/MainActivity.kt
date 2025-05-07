package com.example.predlozak_1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.predlozak_1.ui.theme.Predlozak_1Theme
import kotlinx.coroutines.*
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.input.KeyboardType
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Predlozak_1Theme {
                Scaffold( modifier = Modifier.fillMaxSize() ) { innerPadding ->
                    UserPreview(191,100,

                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}


@Composable
fun UserPreview(heightCm: Int, weightKg: Int, modifier: Modifier = Modifier) {
    // Izračun BMI-a
    val heightMeters = heightCm / 100f
    var newWeight by remember { mutableStateOf("60") }
    var bmi by remember { mutableStateOf(weightKg / (heightMeters * heightMeters)) }

    // Određivanje poruke na temelju BMI-a
    val bmiStatus = when {
        bmi < 18.5 -> "Prenizak BMI"
        bmi in 18.5..24.9 -> "Idealan BMI"
        else -> "Previsok BMI"
    }

    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var isLoading2 by remember { mutableStateOf(false) }
    var rezultat by remember { mutableStateOf("") }
    var napredak by remember { mutableStateOf("")}
    var progressPercentage by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {

        Image(
            painter = painterResource(id = R.drawable.fitness),
            contentDescription = "Pozadinska slika",
            contentScale = ContentScale.Crop,
            alpha = 0.1f,
            modifier = Modifier.fillMaxSize()
        )


        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Profilna slika
            Image(
                painter = painterResource(id = R.drawable.profile_pic),
                contentDescription = "Profilna slika",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))


            Column {
                Text(
                    text = "Pozdrav, Miljenko",
                    fontSize = 18.sp
                )
                Text(
                    text = bmiStatus,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Button(onClick = {
                        scope.launch {
                            isLoading = true
                            delay(2000L) // simulacija "računanja"
                            val idealBmi = 21.7
                            val razlika = (bmi - idealBmi).let { kotlin.math.abs(it) }
                            rezultat = "Udaljeni ste %.1f od idealnog BMI-a.".format(razlika)
                            isLoading = false
                        }
                    }) {
                        Text("Izračunaj razliku od idealnog BMI")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Text(text = rezultat)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = newWeight,
                        onValueChange = { newWeight = it },
                        label = { Text("Unos nove težine u kg:") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Button(onClick = {
                        scope.launch {
                            isLoading2 = true
                            delay(1000) // simulacija "računanja"
                            val newBmi = newWeight.toFloat() / (heightMeters * heightMeters)
                            val idealBmi = 21.7
                            napredak = when {
                                newBmi > bmi -> "Nema napretka :("
                                newBmi <=  idealBmi -> "Napredak 100%"
                                else -> "Pomak bmi-ja od: %.2f".format((abs(bmi - idealBmi))-(abs(newBmi-idealBmi)))
                            }
                            bmi = newWeight.toFloat() / (heightMeters * heightMeters)

                            val progress = ((idealBmi - abs(newBmi - idealBmi)) / idealBmi).toFloat()
                            progressPercentage = progress.coerceIn(0f, 1f)

                            isLoading2 = false
                        }
                    }) {
                        Text("Izračunaj napredak prema idealnom BMI-ju")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isLoading2) {
                        CircularProgressIndicator()
                    } else {
                        Text(text = napredak)

                        LinearProgressIndicator(
                            progress = progressPercentage,
                            modifier = Modifier.fillMaxWidth().padding(top=8.dp)
                        )
                        Text(
                            text = "${(progressPercentage * 100).toInt()}%",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                }
            }
        }

    }
}

