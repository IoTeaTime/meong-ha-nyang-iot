package com.example.iotest

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private val tag = "MainActivity"
    private var awsMqttManager: AWSIotMqttManager? = null
    private var androidId = ""

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        androidId = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)

        thread {
            try {
                val awsKeyAndCert = IoTClientHelper(androidId)
                    .registerDevice(this@MainActivity)
                awsMqttManager = MqttManagerHelper(androidId)
                    .createMqttManager(this@MainActivity, awsKeyAndCert)

            } catch (e: Exception) {
                Log.e(tag, "Error occurred: ${e.message}", e)
                runOnUiThread {
                }
            }
        }
        findViewById<Button>(R.id.pubbutton)
            .setOnClickListener {
                Log.d("BUTTONS", "User tapped the PubButton")
                awsMqttManager?.let { it1 -> MqttPubSub().pub(it1,"hello") }
            }


    }
}
