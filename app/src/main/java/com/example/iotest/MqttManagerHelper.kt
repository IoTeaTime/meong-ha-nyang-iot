package com.example.iotest

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos
import com.amazonaws.services.iot.model.CreateKeysAndCertificateResult

class MqttManagerHelper {
    private var tag = "MqttManagerHelper"

    fun createMqttManager(context: Context, result: CreateKeysAndCertificateResult)
    : AWSIotMqttManager {

        AWSIotKeystoreHelper.saveCertificateAndPrivateKey(
            result.certificateId,
            result.certificatePem,
            result.keyPair.privateKey,
            "${context.filesDir}",
            "keystore.bks",
            BuildConfig.AWS_KEYSTORE_PW
        )

        var clientId = getDeviceUuid(context)

        val awsMqttManager = AWSIotMqttManager(
            clientId,
            BuildConfig.MQTT_END_POINT // IoT 엔드포인트 입력
        ).apply {
            isAutoReconnect = true
        }


        // Keystore 파일을 불러오기
        val keyStore = AWSIotKeystoreHelper.getIotKeystore(
            result.certificateId,
            "${context.filesDir}",
            "keystore.bks",  // .bks 파일 확장자 사용
            BuildConfig.AWS_KEYSTORE_PW
        )

        awsMqttManager.connect(keyStore
        ) { status, throwable ->
            if (throwable != null) {
                Log.e(tag, "Connection error: $throwable")
                throwable.printStackTrace()
            }
            Log.d(tag, "Status: $status")
            // Connected 상태일 때만 구독
            if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connected) {
                awsMqttManager.subscribeToTopic(
                    "hello",
                    AWSIotMqttQos.QOS0
                ) { topic, message ->
                    val str = message.toString(Charsets.UTF_8)
                    Log.d(tag, "$topic : $str")
                }
                Log.d(tag, "Subscribed to topic: hello")
            }

        }

        return awsMqttManager
    }

    @SuppressLint("HardwareIds")
    private fun getDeviceUuid(context: Context): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

}