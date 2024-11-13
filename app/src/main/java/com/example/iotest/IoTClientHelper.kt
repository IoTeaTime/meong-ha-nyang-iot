package com.example.iotest

import android.content.Context
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Region
import com.amazonaws.services.iot.AWSIotClient
import com.amazonaws.services.iot.model.AttachPolicyRequest
import com.amazonaws.services.iot.model.CreateKeysAndCertificateRequest
import com.amazonaws.services.iot.model.CreateKeysAndCertificateResult
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.File
import java.io.FileOutputStream
import java.security.KeyStore
import java.security.Security

class IoTClientHelper(context: Context) {
    private val tag = "MqttHelper"
    private val client: AWSIotClient
    private val keyStoreFilePath = "${context.filesDir}/keystore.bks"
    private val keyStorePassword = "qwer"

    init {
        // BouncyCastle Provider를 추가
        Security.addProvider(BouncyCastleProvider())

        client = AWSIotClient(
            BasicAWSCredentials(
                "", // AWS 액세스 키를 여기에 입력하세요
                ""  // AWS 비밀 키를 여기에 입력하세요
            )
        )
        client.setRegion(Region.getRegion("")) // 지역을 입력

        val request = CreateKeysAndCertificateRequest()
        request.setAsActive = true
        val result: CreateKeysAndCertificateResult =
            client.createKeysAndCertificate(request)

        val attachPolicyRequest = AttachPolicyRequest().apply {
            policyName = ""  // 생성한 정책의 이름을 넣습니다.
            target = result.certificateArn  // 인증서 ARN
        }

        client.attachPolicy(attachPolicyRequest)

        MqttManagerHelper(context, result)
    }
}
