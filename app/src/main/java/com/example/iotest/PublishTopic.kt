package com.example.iotest

import android.util.Log
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos
import org.bouncycastle.util.encoders.UTF8
import java.util.stream.Stream

class PublishTopic {
    private var tag = "PublishTopic"

    fun publishStatus(awsMqttManager: AWSIotMqttManager) {
        awsMqttManager.publishString(
            "Hello world",
            "hello",
            AWSIotMqttQos.QOS0
        )

        awsMqttManager.subscribeToTopic(
            "hello",
            AWSIotMqttQos.QOS0
        ) { topic, message ->
            val str = message.toString(Charsets.UTF_8)
            Log.d(tag, "$topic : $str")
        }
    }
}