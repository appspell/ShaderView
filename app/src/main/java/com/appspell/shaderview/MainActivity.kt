package com.appspell.shaderview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<ShaderView>(R.id.texture).updateContinuously = true
        findViewById<ShaderView>(R.id.texture2).updateContinuously = true
        findViewById<ShaderView>(R.id.texture3).updateContinuously = true
        findViewById<ShaderView>(R.id.texture4).updateContinuously = false
        findViewById<ShaderView>(R.id.texture5).updateContinuously = true
    }

}