package com.appspell.shaderview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.appspell.shaderview.databinding.ActivitySimpleShaderBinding
import com.appspell.shaderview.gl.ShaderParams

class SimpleShaderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySimpleShaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.shaderView.apply {
            updateContinuously = true // disable redraw every frame, draw it only if needed
            fragmentShaderRawResId = R.raw.color_frag // fragment shader file

            shaderParams = ShaderParams.Builder()
                // send parameter (uniform) to shader
                .addColor("diffuseColor", R.color.teal_200, resources)
                .build()
        }
    }
}