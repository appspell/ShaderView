package com.appspell.shaderview.demo.simple

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appspell.shaderview.demo.databinding.ActivitySimpleOnlyXmlShaderBinding

class SimpleOnlyXMLShaderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySimpleOnlyXmlShaderBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}