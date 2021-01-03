package com.appspell.shaderview.simple

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.appspell.shaderview.R
import com.appspell.shaderview.databinding.ActivitySimpleOnlyXmlShaderBinding
import com.appspell.shaderview.databinding.ActivitySimpleShaderBinding
import com.appspell.shaderview.gl.ShaderParams

class SimpleOnlyXMLShaderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySimpleOnlyXmlShaderBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}