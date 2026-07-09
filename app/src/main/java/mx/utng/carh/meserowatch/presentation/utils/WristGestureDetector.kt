package mx.utng.carh.meserowatch.presentation.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class WristGestureDetector(
    context: Context,
    private val onGiroArriba: () -> Unit,
    private val onGiroAbajo: () -> Unit
) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val giroscopio: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private var anguloAcumulado = 0f
    private val UMBRAL_GIRO = 2.5f
    private var ultimoTimestamp = 0L

    fun iniciar() {
        giroscopio?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun detener() {
        sensorManager.unregisterListener(this)
        anguloAcumulado = 0f
        ultimoTimestamp = 0L
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_GYROSCOPE) return

        if (ultimoTimestamp == 0L) {
            ultimoTimestamp = event.timestamp
            return
        }
        val dt = (event.timestamp - ultimoTimestamp) / 1_000_000_000f
        ultimoTimestamp = event.timestamp

        anguloAcumulado += event.values[2] * dt

        when {
            anguloAcumulado > UMBRAL_GIRO -> {
                onGiroArriba()
                anguloAcumulado = 0f
            }
            anguloAcumulado < -UMBRAL_GIRO -> {
                onGiroAbajo()
                anguloAcumulado = 0f
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}