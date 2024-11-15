package com.example.iotest

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import kotlin.concurrent.thread
import java.io.File

class MainActivity : AppCompatActivity() {
    private val tag = "MainActivity"
    private var awsMqttManager: AWSIotMqttManager? = null
    private var androidId = ""

    @SuppressLint("HardwareIds", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        androidId = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)

        thread {
            try {
                val iotClientHelper = IoTClientHelper(androidId)
                val keyStoreFile = File("${this@MainActivity.filesDir}/keystore.bks")
                val mqttHelper = MqttManagerHelper(androidId)
                if (keyStoreFile.exists()) {
                    try {
                        val keyStore = mqttHelper.getKeyStore(this@MainActivity)
                        awsMqttManager = mqttHelper.createMqttManager(this@MainActivity, keyStore)
                    } catch (e: Exception) {
                        Log.e(tag, "KeyStore access error: ${e.message}", e)
                        initializeWithNewKeyStore(iotClientHelper, mqttHelper)
                    }
                } else {
                    initializeWithNewKeyStore(iotClientHelper, mqttHelper)
                }
            } catch (e: Exception) {
                Log.e(tag, "Error occurred: ${e.message}", e)
            }
        }
        findViewById<Button>(R.id.deletebutton).setOnClickListener {
            Log.d("BUTTONS", "Shadow Get Request")
            awsMqttManager?.let { mqttManager -> MqttPubSub().deleteShadow(mqttManager) }
        }
        findViewById<Button>(R.id.getbutton).setOnClickListener {
            Log.d("BUTTONS", "Shadow get Request")
            awsMqttManager?.let { mqttManager -> MqttPubSub().getShadow(mqttManager) }
        }
    }

    private fun initializeWithNewKeyStore(
        iotClientHelper: IoTClientHelper,
        mqttHelper: MqttManagerHelper
    ) {
        val awsKeyAndCert = iotClientHelper.getKeyAndCert()
        iotClientHelper.registerDevice(this@MainActivity, awsKeyAndCert)
        mqttHelper.createKeyStore(this@MainActivity, awsKeyAndCert)
        val keyStore = mqttHelper.getKeyStore(this@MainActivity)
        awsMqttManager = mqttHelper.createMqttManager(this@MainActivity, keyStore)
    }


}
