package com.example.iotest

import android.util.Log
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos

class MqttPubSub {
    private var tag = "PublishTopic"

    fun pub(awsMqttManager: AWSIotMqttManager,payload: String, topic: String) {
        awsMqttManager.publishString(
            payload,
            topic,
            AWSIotMqttQos.QOS0
        )
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
}