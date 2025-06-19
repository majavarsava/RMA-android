package com.example.artitudo.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.artitudo.ui.theme.backgroundColor // Your default background
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import com.example.artitudo.ui.theme.lightPurple

class LevelerViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationVectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val _pitch = MutableStateFlow(0f)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private val _roll = MutableStateFlow(0f)

    private val _isLevel = MutableStateFlow(false)
    val isLevel: StateFlow<Boolean> = _isLevel.asStateFlow()

    private val _levelerColor = MutableStateFlow(backgroundColor)
    val levelerColor: StateFlow<Color> = _levelerColor.asStateFlow()

    private val targetFlatPitchDegrees = 90.0f
    private val flatLevelThresholdDegrees = 2.0f

    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    init {
        startListening()
    }

    fun startListening() {
        rotationVectorSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

            val orientationAngles = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)

            val currentRawPitchDegrees = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
            val currentRawRollDegrees = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

            _pitch.value = currentRawPitchDegrees
            _roll.value = currentRawRollDegrees

            val wasLevel = _isLevel.value

            val isPitchNearTargetFlat = abs(abs(currentRawPitchDegrees) - targetFlatPitchDegrees) < flatLevelThresholdDegrees

            val nowLevel = isPitchNearTargetFlat

            _isLevel.value = nowLevel
            _levelerColor.value = if (nowLevel) lightPurple else backgroundColor

            if (nowLevel && !wasLevel) {
                triggerVibration()
            }
        }
    }

    private fun triggerVibration() {
        viewModelScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)) // 500ms
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}