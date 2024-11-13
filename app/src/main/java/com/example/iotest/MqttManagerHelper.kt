package com.example.iotest

import android.content.Context
import android.util.Log
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.services.iot.AWSIotClient
import com.amazonaws.services.iot.model.CreateKeysAndCertificateResult

class MqttManagerHelper(context: Context, result: CreateKeysAndCertificateResult) {
    private var tag = "MqttManagerHelper"

    init {

        // 인증서와 키를 저장
        AWSIotKeystoreHelper.saveCertificateAndPrivateKey(
            result.certificateId,
            result.certificatePem,
            result.keyPair.privateKey,
            "${context.filesDir}",
            "keystore.bks",  // .bks 파일 확장자 사용
            "qwer"
        )

        val awsMqttManager = AWSIotMqttManager(
            "cid_1",
            "" // IoT 엔드포인트 입력 
        )

        // Keystore 파일을 불러오기
        val keyStore = AWSIotKeystoreHelper.getIotKeystore(
            result.certificateId,
            "${context.filesDir}",
            "keystore.bks",  // .bks 파일 확장자 사용
            "qwer"
        )

        awsMqttManager.connect(keyStore,
            AWSIotMqttClientStatusCallback { status, throwable ->
                if (throwable != null) {
                    Log.e(tag, "Connection error: $throwable")
                    throwable.printStackTrace()
                }
                Log.d(tag, "Status: $status")
            })
    }
}