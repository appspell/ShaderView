package com.appspell.shaderview.simple

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appspell.shaderview.databinding.ActivitySimpleOnlyXmlShaderBinding

class SimpleOnlyXMLShaderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySimpleOnlyXmlShaderBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}