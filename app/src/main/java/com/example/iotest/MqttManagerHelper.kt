package com.example.iotest

import android.content.Context
import android.util.Log
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
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

        val awsMqttManager = AWSIotMqttManager(
            "client_1",
            BuildConfig.MQTT_END_POINT // IoT 엔드포인트 입력
        )

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
        }

        return awsMqttManager
    }
}