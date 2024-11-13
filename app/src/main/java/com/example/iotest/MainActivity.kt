package com.example.iotest

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private val tag = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        thread {
            try {
                val awsClientHelper = IoTClientHelper(this@MainActivity)

            } catch (e: Exception) {
                Log.e(tag, "Error occurred: ${e.message}", e)
                runOnUiThread {
                }
            }
        }
    }
}
