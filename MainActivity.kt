package hr.ferit.varsava_lv3

import android.os.Bundle
import android.widget.Space
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.ferit.varsava_lv3.ui.theme.Varsava_lv3Theme
import kotlin.math.pow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Varsava_lv3Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    UserPreview(
                        modifier = Modifier.padding(innerPadding),
                        heightCm = 158,
                        weightKg = 55
                    )
                }
            }
        }
    }
}

@Composable
fun UserPreview(heightCm: Int, weightKg: Int,modifier: Modifier = Modifier) {
    val bmi : Double
    val bmiStatus: String
    bmi = weightKg.toDouble() / (heightCm.toDouble()/100).pow(2)

    if(bmi<18.5){
        bmiStatus = stringResource(id=R.string.low)
    }
    else if (bmi>= 25){
        bmiStatus = stringResource(id=R.string.high)
    }
    else{
        bmiStatus = stringResource(id=R.string.middle)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ){
        Image(
            painter = painterResource(id = R.drawable.backgroundpic), // ime tvoje slike
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.1f
        )
        Column (
            modifier = modifier.fillMaxSize()
        )
        {
            Row (
                modifier = modifier.padding(16.dp).height(64.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            )
            {
                Image(
                    painter = painterResource(id =  R.drawable.profile),
                    contentDescription = "Profilna slika",
                    Modifier.size(64.dp).clip(CircleShape)
                )
                Spacer(modifier= Modifier.width(16.dp))

                Column(
                    horizontalAlignment = Alignment.Start
                )
                {
                    Text(
                        text = stringResource(id=R.string.hello_miljenko),
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = bmiStatus,
                        modifier = Modifier
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Varsava_lv3Theme {

    }
}