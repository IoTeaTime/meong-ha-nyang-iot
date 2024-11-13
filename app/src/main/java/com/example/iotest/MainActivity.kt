package com.example.iotest

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    @Volatile
    private var textView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.textView)

        thread {
            try {
                val awsClientHelper = IoTClientHelper(this@MainActivity)

            } catch (e: Exception) {
                Log.e(TAG, "Error occurred: ${e.message}", e)
                runOnUiThread {
                    textView?.text = "Error: ${e.message}"
                }
            }
        }
    }
}
