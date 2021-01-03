package com.appspell.shaderview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.appspell.shaderview.databinding.ActivityMenuBinding
import com.appspell.shaderview.list.ShaderListActivity
import com.appspell.shaderview.simple.SimpleOnlyXMLShaderActivity
import com.appspell.shaderview.simple.SimpleShaderActivity
import com.appspell.shaderview.video.VideoActivity
import com.appspell.shaderview.video.VideoAdvancedActivity

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.simpleShader.setOnClickListener { open(SimpleShaderActivity::class.java) }
        binding.simpleXmlOnlyShader.setOnClickListener { open(SimpleOnlyXMLShaderActivity::class.java) }
        binding.shaderList.setOnClickListener { open(ShaderListActivity::class.java) }
        binding.simpleVideo.setOnClickListener { open(VideoActivity::class.java) }
        binding.advancedVideo.setOnClickListener { open(VideoAdvancedActivity::class.java) }
    }

    private fun open(cls: Class<*>) = startActivity(Intent(this, cls))
}