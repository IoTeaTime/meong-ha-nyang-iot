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


    }
}