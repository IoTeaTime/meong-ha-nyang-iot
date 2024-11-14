package com.example.iotest

import android.content.Context
import android.util.Log
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.services.iot.model.CreateKeysAndCertificateResult
import java.io.File
import java.security.KeyStore

class MqttManagerHelper(androidId: String) {
    private var tag = "MqttManagerHelper"
    private var clientId = androidId
    private lateinit var keyStoreFile:File

    companion object {
        private const val PREFS_NAME = "IoTPreferences"
        private const val CERTIFICATE_ID_KEY = "certificateId"
    }

    fun createKeyStore(context: Context, result: CreateKeysAndCertificateResult) {
        keyStoreFile = File("${context.filesDir}/keystore.bks")

        AWSIotKeystoreHelper.saveCertificateAndPrivateKey(
            result.certificateId,
            result.certificatePem,
            result.keyPair.privateKey,
            context.filesDir.absolutePath,
            "keystore.bks",
            BuildConfig.AWS_KEYSTORE_PW
        )
        Log.d(tag, "KeyStore created and saved.")
        saveCertificateId(context, result.certificateId)
    }

    fun getKeyStore(context: Context): KeyStore? {
        val certificateId = getStoredCertificateId(context)
        return if (certificateId != null) {
            AWSIotKeystoreHelper.getIotKeystore(
                certificateId,
                context.filesDir.absolutePath,
                "keystore.bks",
                BuildConfig.AWS_KEYSTORE_PW
            )
        } else {
            Log.e(tag, "No certificate ID found in storage.")
            null
        }
    }

    fun createMqttManager(context: Context, keyStore: KeyStore?): AWSIotMqttManager {
        val awsMqttManager = AWSIotMqttManager(
            clientId,
            BuildConfig.MQTT_END_POINT
        ).apply {
            isAutoReconnect = true
        }

        awsMqttManager.connect(keyStore) { status, throwable ->
            if (throwable != null) {
                Log.e(tag, "Connection error: $throwable")
                throwable.printStackTrace()

                val keyStoreFile = File("${context.filesDir}/keystore.bks")
                if (keyStoreFile.exists()) {
                    Log.d(tag, "Deleting existing keyStore.bks and creating a new keyStore.")
                    keyStoreFile.delete()
                } else {
                    Log.e(tag, "Failed to create a new KeyStore.")
                }
            } else {
                Log.d(tag, "Status: $status")
                if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connected) {
                    MqttPubSub().sub(awsMqttManager, "\$aws/things/8a1d1987b4231a1a/shadow/update/delta")
                    MqttPubSub().sub(awsMqttManager, "\$aws/things/8a1d1987b4231a1a/shadow/update/accepted")
                    MqttPubSub().sub(awsMqttManager, "\$aws/things/8a1d1987b4231a1a/shadow/update/rejected")
                    MqttPubSub().sub(awsMqttManager, "\$aws/things/8a1d1987b4231a1a/shadow/update/documents")
                    MqttPubSub().sub(awsMqttManager, "\$aws/things/8a1d1987b4231a1a/shadow/get/accepted")
                    MqttPubSub().sub(awsMqttManager, "\$aws/things/8a1d1987b4231a1a/shadow/get/rejected")

                    MqttPubSub().updateShadow(awsMqttManager)
                }
            }
        }
        return awsMqttManager
    }

    // SharedPreferences에 certificateId 저장
    fun saveCertificateId(context: Context, certificateId: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(CERTIFICATE_ID_KEY, certificateId)
            apply()
        }
        Log.d(tag, "Certificate ID saved to SharedPreferences: $certificateId")
    }

    // SharedPreferences에서 certificateId 불러오기
    fun getStoredCertificateId(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(CERTIFICATE_ID_KEY, null)
    }
}
