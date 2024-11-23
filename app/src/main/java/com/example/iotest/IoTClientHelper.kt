package com.example.iotest

import android.content.Context
import com.amazonaws.regions.Region
import com.amazonaws.services.iot.AWSIotClient
import com.amazonaws.services.iot.model.AttachPolicyRequest
import com.amazonaws.services.iot.model.AttachThingPrincipalRequest
import com.amazonaws.services.iot.model.CreateKeysAndCertificateRequest
import com.amazonaws.services.iot.model.CreateKeysAndCertificateResult
import com.amazonaws.services.iot.model.RegisterThingRequest
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

class IoTClientHelper(androidId: String) {
    private val tag = "MqttHelper"
    private val client: AWSIotClient
    private var androidId = ""

    init {
        this.androidId = androidId
        client = AWSIotClient(AWSCredentialConfig().basicCredential())
        client.setRegion(Region.getRegion(BuildConfig.AWS_REGION))
    }

    fun getKeyAndCert(): CreateKeysAndCertificateResult {
        val request = CreateKeysAndCertificateRequest()
            .apply { setAsActive = true }
        return client.createKeysAndCertificate(request)
    }

    fun registerDevice(context: Context, result: CreateKeysAndCertificateResult) {
        Security.addProvider(BouncyCastleProvider())

        val attachPolicyRequest = AttachPolicyRequest().apply {
            policyName = "certified_thing"
            target = result.certificateArn
        }

        client.attachPolicy(attachPolicyRequest)


        val templateBody = context.resources.openRawResource(R.raw.thing_template)
            .bufferedReader().use { it.readText() }

        val registerRequest = RegisterThingRequest().apply {
            this.templateBody = templateBody
            this.parameters = mapOf("DeviceSerialNumber" to androidId)
        }
        client.registerThing(registerRequest)


        val attachThingPrincipalRequest = AttachThingPrincipalRequest().apply {
            thingName = androidId
            principal = result.certificateArn
        }
        client.attachThingPrincipal(attachThingPrincipalRequest)
    }
}
