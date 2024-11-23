package com.example.iotest

import android.util.Log
import android.widget.CheckBox
import android.widget.EditText
import com.amazonaws.regions.Region.getRegion
import com.amazonaws.services.kinesisvideo.AWSKinesisVideoClient
import com.amazonaws.services.kinesisvideo.model.ChannelRole
import com.amazonaws.services.kinesisvideo.model.ChannelType
import com.amazonaws.services.kinesisvideo.model.CreateSignalingChannelRequest
import com.amazonaws.services.kinesisvideo.model.DescribeMediaStorageConfigurationRequest
import com.amazonaws.services.kinesisvideo.model.DescribeSignalingChannelRequest
import com.amazonaws.services.kinesisvideo.model.GetSignalingChannelEndpointRequest
import com.amazonaws.services.kinesisvideo.model.ResourceEndpointListItem
import com.amazonaws.services.kinesisvideo.model.ResourceNotFoundException
import com.amazonaws.services.kinesisvideo.model.SingleMasterChannelEndpointConfiguration
import com.amazonaws.services.kinesisvideo.model.SingleMasterConfiguration
import com.amazonaws.services.kinesisvideo.model.Tag
import com.amazonaws.services.kinesisvideosignaling.AWSKinesisVideoSignalingClient
import com.amazonaws.services.kinesisvideosignaling.model.GetIceServerConfigRequest
import com.amazonaws.services.kinesisvideosignaling.model.IceServer
import com.example.iotest.BuildConfig.AWS_REGION


class KvsClientHelper() {
    private val tag = "KvsClientHelper"
    private val kvsClient: AWSKinesisVideoClient
    private val awsSignalingClient: AWSKinesisVideoSignalingClient
    private var mChannelArn: String? = null
    private var mStreamArn: String? = null
    private lateinit var mChannelName: EditText
    private lateinit var mIngestMedia: CheckBox
    private val mEndpointList = mutableListOf<ResourceEndpointListItem>()
    private val mIceServerList = mutableListOf<IceServer>()

    init {
        kvsClient = AWSKinesisVideoClient(AWSCredentialConfig().basicCredential())
        kvsClient.setRegion(getRegion(AWS_REGION))

        awsSignalingClient = AWSKinesisVideoSignalingClient(AWSCredentialConfig().basicCredential())
        awsSignalingClient.setRegion(getRegion(AWS_REGION))
    }

    fun startMasterSession() {
        val channelName = mChannelName.text.toString()
        val role = ChannelRole.MASTER
        val result = initializeMasterSession(channelName, role, mIngestMedia.isChecked)
        result?.let { Log.e(tag, it) } ?: Log.i(tag, "Master session started successfully.")
    }

    fun startViewerSession() {
        Log.i(tag, "Viewer session logic to be implemented.")
    }

    fun initializeMasterSession(
        channelName: String,
        role: ChannelRole,
        ingestMedia: Boolean
    ): String {
        try {
            val describeSignalingChannelResult = kvsClient.describeSignalingChannel(
                DescribeSignalingChannelRequest().withChannelName(channelName)
            )

                mChannelArn = describeSignalingChannelResult.channelInfo.channelARN
            Log.i(tag, "Channel ARN: $mChannelArn")
        } catch (e: ResourceNotFoundException) {
            if (role == ChannelRole.MASTER) {
                try {

                    // CreateSignalingChannelRequest 설정
                    val request = CreateSignalingChannelRequest()
                        .withChannelName("test-channel")
                        .withChannelType(ChannelType.SINGLE_MASTER)
                        .withSingleMasterConfiguration(
                            SingleMasterConfiguration()
                                .withMessageTtlSeconds(100)
                        )
                        .withTags(
                            Tag().withKey("Project").withValue("Demo")
                        )


                    // 채널 생성
                    val result = kvsClient.createSignalingChannel(request)
                    println("Created Channel ARN: " + result.channelARN)
                } catch (ex: Exception) {
                    return "Create Signaling Channel failed: ${ex.localizedMessage}"
                }
            } else {
                return "Signaling Channel $channelName doesn't exist!"
            }
        } catch (ex: Exception) {
            return "Describe Signaling Channel failed: ${ex.localizedMessage}"
        }
        if (role == ChannelRole.MASTER && ingestMedia) {
            try {
                val describeMediaStorageConfigurationResult = kvsClient.describeMediaStorageConfiguration(
                    DescribeMediaStorageConfigurationRequest().withChannelARN(mChannelArn)
                )
                if (describeMediaStorageConfigurationResult.mediaStorageConfiguration.status != "ENABLED") {
                    return "Media Storage is DISABLED for this channel!"
                }
                mStreamArn = describeMediaStorageConfigurationResult.mediaStorageConfiguration.streamARN
                Log.i(tag, "Stream ARN: $mStreamArn")
            } catch (ex: Exception) {
                return "Describe Media Storage Configuration failed: ${ex.localizedMessage}"
            }
        }

        val protocols = if (ingestMedia) arrayOf("WSS", "HTTPS", "WEBRTC") else arrayOf("WSS", "HTTPS")
        return try {
            val getSignalingChannelEndpointResult = kvsClient.getSignalingChannelEndpoint(
                GetSignalingChannelEndpointRequest()
                    .withChannelARN(mChannelArn)
                    .withSingleMasterChannelEndpointConfiguration(
                        SingleMasterChannelEndpointConfiguration()
                            .withProtocols(*protocols)
                            .withRole(role)
                    )
            )
            mEndpointList.addAll(getSignalingChannelEndpointResult.resourceEndpointList)
            val dataEndpoint = mEndpointList.firstOrNull { it.protocol == "HTTPS" }?.resourceEndpoint
            dataEndpoint ?: "HTTPS Endpoint not found!"
        } catch (e: Exception) {
            "Get Signaling Endpoint failed with Exception: ${e.localizedMessage}"
        }.also {
            setupIceServerConfiguration()
        }
    }

    private fun setupIceServerConfiguration() {
        val dataEndpoint = mEndpointList.firstOrNull { it.protocol == "HTTPS" }?.resourceEndpoint
        if (dataEndpoint != null) {
            try {
                // AWS Signaling Client 초기화
                awsSignalingClient.endpoint = dataEndpoint
                val getIceServerConfigResult = awsSignalingClient.getIceServerConfig(
                    GetIceServerConfigRequest()
                        .withChannelARN(mChannelArn)
                        .withClientId("MASTER")
                )
                mIceServerList.addAll(getIceServerConfigResult.iceServerList)
                Log.i(tag, "Ice Server Configuration: $mIceServerList")
            } catch (e: Exception) {
                Log.e(tag, "Get Ice Server Config failed with Exception: ${e.localizedMessage}")
            }
        } else {
            Log.e(tag, "Data Endpoint not found for Ice Server Configuration")
        }
    }

}
