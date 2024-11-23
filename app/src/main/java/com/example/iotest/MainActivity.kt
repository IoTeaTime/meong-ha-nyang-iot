package com.example.iotest

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.services.kinesisvideo.model.ChannelRole
import kotlin.concurrent.thread
import java.io.File

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
               initializeAwsMqttManager()
               startKvsClientHelper()
            } catch (e: Exception) {
                Log.e(tag, "Error occurred during initialization: ${e.message}", e)
            }
        }

        setupButtons()
    }

    private fun initializeAwsMqttManager() {
        val iotClientHelper = IoTClientHelper(androidId)
        val keyStoreFile = getKeyStoreFile()
        val mqttHelper = MqttManagerHelper(androidId)

        if (keyStoreFile.exists()) {
            try {
                connectWithExistingKeyStore(mqttHelper)
            } catch (e: Exception) {
                Log.e(tag, "KeyStore access error: ${e.message}", e)
                initializeWithNewKeyStore(iotClientHelper, mqttHelper)
            }
        } else {
            initializeWithNewKeyStore(iotClientHelper, mqttHelper)
        }
    }

    private fun startKvsClientHelper() {
        try {
            val kvsClientHelper = KvsClientHelper()
            Log.d(tag, "KvsClientHelper started successfully.")
            kvsClientHelper.initializeMasterSession("test-channel", ChannelRole.MASTER, true)
        } catch (e: Exception) {
            Log.e(tag, "Error occurred while starting KvsClientHelper: ${e.message}", e)
        }
    }

    private fun connectWithExistingKeyStore(mqttHelper: MqttManagerHelper) {
        val keyStore = mqttHelper.getKeyStore(this)
        awsMqttManager = mqttHelper.createMqttManager(keyStore)
    }

    private fun initializeWithNewKeyStore(
        iotClientHelper: IoTClientHelper,
        mqttHelper: MqttManagerHelper
    ) {
        val awsKeyAndCert = iotClientHelper.getKeyAndCert()
        iotClientHelper.registerDevice(this, awsKeyAndCert)
        mqttHelper.createKeyStore(this, awsKeyAndCert)
        connectWithExistingKeyStore(mqttHelper)
    }

    private fun getKeyStoreFile(): File {
        return File("${this.filesDir}/keystore.bks")
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.updatebutton).setOnClickListener {
            Log.d("BUTTONS", "Shadow Update Request")
            awsMqttManager?.let { mqttManager -> MqttPubSub().updateShadow(mqttManager, androidId) }
        }

        findViewById<Button>(R.id.getbutton).setOnClickListener {
            Log.d("BUTTONS", "Shadow Get Request")
            awsMqttManager?.let { mqttManager -> MqttPubSub().getShadow(mqttManager, androidId) }
        }
    }
}
