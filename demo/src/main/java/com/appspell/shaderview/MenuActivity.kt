package com.appspell.shaderview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.appspell.shaderview.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.simpleShader.setOnClickListener { open(SimpleShaderActivity::class.java) }
        binding.shaderList.setOnClickListener { open(ShaderListActivity::class.java) }
        binding.simpleVideo.setOnClickListener { open(VideoActivity::class.java) }
        binding.advancedVideo.setOnClickListener { open(VideoAdvancedActivity::class.java) }
    }

    private fun open(cls: Class<*>) = startActivity(Intent(this, cls))
}