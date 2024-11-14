package com.example.iotest

import android.util.Log
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos

class MqttPubSub {
    private var tag = "PublishTopic"

    fun pub(awsMqttManager: AWSIotMqttManager, payload: String, topic: String) {
        awsMqttManager.publishString(
            payload,
            topic,
            AWSIotMqttQos.QOS0
        )
        Log.d(tag, "Published to topic: $topic with payload: $payload")
    }

    fun sub(awsMqttManager: AWSIotMqttManager, topic: String) {
        awsMqttManager.subscribeToTopic(
            topic,
            AWSIotMqttQos.QOS0
        ) { resTopic, message ->
            val str = message.toString(Charsets.UTF_8)
            Log.d(tag, "$resTopic : $str")
        }
    }

    fun getShadow(awsMqttManager: AWSIotMqttManager) {
        val topic = "\$aws/things/8a1d1987b4231a1a/shadow/get"
        val payload = "{}"

        pub(awsMqttManager, payload, topic)
        Log.d(tag, "Shadow get request sent.")
    }
    fun updateShadow(awsMqttManager: AWSIotMqttManager) {
        val topic = "\$aws/things/8a1d1987b4231a1a/shadow/update"

        val batteryLevel = 89
        val availableMemory = 123
        val kvsChannelActive = true
        val kvsChannelDeleteRequested = false

        val payload = """
            {
                "state": {
                    "reported": {
                        "kvsChannelActive": $kvsChannelActive,
                        "batteryLevel": $batteryLevel,
                        "availableMemory": $availableMemory,
                        "kvsChannelDeleteRequested": $kvsChannelDeleteRequested
                    }
                }
            }
        """.trimIndent()
        pub(awsMqttManager, payload, topic)
        Log.d(tag, "Shadow update request sent with payload: $payload")
    }

    fun deleteShadow(awsMqttManager: AWSIotMqttManager) {
        val topic = "\$aws/things/8a1d1987b4231a1a/shadow/delete"
        val payload = "{}"

        pub(awsMqttManager, payload, topic)
        Log.d(tag, "Shadow get request sent.")
    }
}
