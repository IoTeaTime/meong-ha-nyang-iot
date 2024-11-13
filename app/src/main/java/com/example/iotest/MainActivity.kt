package com.example.iotest

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private val tag = "MainActivity"
    private var awsMqttManager: AWSIotMqttManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        thread {
            try {
                val awsKeyAndCert = IoTClientHelper()
                    .registerDevice(this@MainActivity)
                awsMqttManager = MqttManagerHelper()
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
                awsMqttManager?.let { it1 -> PublishTopic().publishStatus(it1) }
            }


    }
}
