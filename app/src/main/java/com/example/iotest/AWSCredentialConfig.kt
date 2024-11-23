package com.example.iotest

import com.amazonaws.auth.BasicAWSCredentials

class AWSCredentialConfig() {
    fun basicCredential() : BasicAWSCredentials {
        val awsCredentials = BasicAWSCredentials(
            BuildConfig.AWS_ACCESS_KEY,
            BuildConfig.AWS_PRIVATE_KEY
        )
        return awsCredentials
    }
}