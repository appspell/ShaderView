package com.appspell.shaderview.demo.simple

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appspell.shaderview.demo.R
import com.appspell.shaderview.demo.databinding.ActivitySimpleShaderBinding
import com.appspell.shaderview.gl.params.ShaderParamsBuilder

class SimpleShaderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySimpleShaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.shaderView.apply {
            fragmentShaderRawResId = R.raw.color_frag // fragment shader file

            shaderParams = ShaderParamsBuilder()
                // send parameter (uniform) to shader
                .addColor("diffuseColor", R.color.teal_200, resources)
                .build()
        }
    }
}