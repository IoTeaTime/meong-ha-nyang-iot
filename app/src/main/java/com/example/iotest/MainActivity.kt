package com.example.iotest

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import kotlin.concurrent.thread
import java.io.File
import java.security.KeyStore

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
                val iotClientHelper = IoTClientHelper(androidId)
                val keyStoreFile = File("${this@MainActivity.filesDir}/keystore.bks")
                val mqttHelper = MqttManagerHelper(androidId)


                // 1. keyStore가 존재하는지 확인
                if (keyStoreFile.exists()) {
                    try {
                        val keyStore = mqttHelper.getKeyStore(this@MainActivity)
                        awsMqttManager = mqttHelper.createMqttManager(this@MainActivity, keyStore)

                    } catch (e: Exception) {
                        // 3. keyStore 존재하지만 오류 발생 시 삭제 후 기존 순서 실행
                        Log.e(tag, "KeyStore access error: ${e.message}", e)
                        keyStoreFile.delete()
                        initializeWithNewKeyStore(iotClientHelper, mqttHelper)
                    }
                } else {
                    // 2. keyStore가 존재하지 않는 경우 기존 순서 실행
                    initializeWithNewKeyStore(iotClientHelper, mqttHelper)
                }

            } catch (e: Exception) {
                Log.e(tag, "Error occurred: ${e.message}", e)
            }
        }

        // 버튼 설정
        findViewById<Button>(R.id.pubbutton).setOnClickListener {
            Log.d("BUTTONS", "User tapped the PubButton")
            awsMqttManager?.let { mqttManager -> MqttPubSub().pub(mqttManager, "hello") }
        }
    }

    // 기존 순서를 통해 키 저장소 생성 및 MQTT 매니저 설정 함수
    private fun initializeWithNewKeyStore(
        iotClientHelper: IoTClientHelper,
        mqttHelper: MqttManagerHelper
    ) {
        // 새로운 키 생성
        val awsKeyAndCert = iotClientHelper.getKeyAndCert()

        // 디바이스 등록
        iotClientHelper.registerDevice(this@MainActivity, awsKeyAndCert)

        // 새로운 KeyStore 생성
        mqttHelper.createKeyStore(this@MainActivity, awsKeyAndCert)
        val keyStore = mqttHelper.getKeyStore(this@MainActivity)

        // MQTT 매니저 생성
        awsMqttManager = mqttHelper.createMqttManager(this@MainActivity, keyStore)
    }


}
