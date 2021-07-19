package com.appspell.shaderview.demo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appspell.shaderview.demo.databinding.ActivityMenuBinding
import com.appspell.shaderview.demo.list.ShaderListActivity
import com.appspell.shaderview.demo.simple.SimpleOnlyXMLShaderActivity
import com.appspell.shaderview.demo.simple.SimpleShaderActivity
import com.appspell.shaderview.demo.ui.UiActivity
import com.appspell.shaderview.demo.video.VideoActivity
import com.appspell.shaderview.demo.video.VideoAdvancedActivity
import com.appspell.shaderview.demo.viewpager.ViewPagerActivity

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.simpleShader.setOnClickListener { open(SimpleShaderActivity::class.java) }
        binding.simpleXmlOnlyShader.setOnClickListener { open(SimpleOnlyXMLShaderActivity::class.java) }
        binding.customUi.setOnClickListener { open(UiActivity::class.java) }
        binding.shaderList.setOnClickListener { open(ShaderListActivity::class.java) }
        binding.viewPager.setOnClickListener { open(ViewPagerActivity::class.java) }
        binding.simpleVideo.setOnClickListener { open(VideoActivity::class.java) }
        binding.advancedVideo.setOnClickListener { open(VideoAdvancedActivity::class.java) }
    }

    private fun open(cls: Class<*>) = startActivity(Intent(this, cls))
}