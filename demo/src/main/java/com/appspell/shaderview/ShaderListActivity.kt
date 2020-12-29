package com.appspell.shaderview

import android.opengl.GLES30
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.appspell.shaderview.databinding.ActivityShaderListBinding
import com.appspell.shaderview.gl.GLShader
import com.appspell.shaderview.gl.ShaderParams
import kotlin.math.cos
import kotlin.math.sin

class ShaderListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityShaderListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.texture.apply {
            updateContinuously = true
            vertexShaderRawResId = R.raw.quad_tangent_space_vert
            fragmentShaderRawResId = R.raw.nomral_map
            shaderParams = ShaderParams.Builder()
                .addTexture2D(
                    "uNormalTexture",
                    R.drawable.normal_button,
                    context.resources,
                    GLES30.GL_TEXTURE1
                )
                .addColor("uColor", R.color.grey, resources)
                .addVec3f("uVaryingColor", floatArrayOf(0.5f, 0.5f, 0.5f))
                .addVec3f("uLightDirection", floatArrayOf(-1.0f, 1.0f, 0.0f))
                .addVec3f("uEyeDirection", floatArrayOf(0.0f, 0.0f, 0.0f))
                .build()
            onDrawFrameListener = { shaderParams ->
                val pos = (System.currentTimeMillis() % 2500L) / 2500f
                shaderParams.updateValue("uLightDirection", floatArrayOf(-1.0f + pos, 1.0f, 0.0f))
            }
        }
        binding.texture2.apply {
            fragmentShaderRawResId = R.raw.color_frag
            shaderParams = ShaderParams.Builder()
                .addColor("diffuseColor", R.color.teal_200, resources)
                .build()
        }
        binding.texture3.apply {
            updateContinuously = true
            fragmentShaderRawResId = R.raw.simple_animation_frag
            shaderParams = ShaderParams.Builder()
                .addFloat("time", 1.0f)
                .build()
            onDrawFrameListener = { shaderParams ->
                shaderParams.updateValue("time", (System.currentTimeMillis() % 5000L) / 5000f)
            }
        }
        binding.texture4.apply {
            fragmentShaderRawResId = R.raw.simple_frag
        }
        binding.texture5.apply {
            updateContinuously = true
            fragmentShaderRawResId = R.raw.color_frag
            shaderParams = ShaderParams.Builder()
                .addVec4f("diffuseColor", floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f))
                .build()
            onDrawFrameListener = { shaderParams ->
                val r = (System.currentTimeMillis() % 50000L) / 50000f
                val g = (System.currentTimeMillis() % 10000L) / 10000f
                val b = (System.currentTimeMillis() % 5000L) / 5000f
                shaderParams.updateValue("diffuseColor", floatArrayOf(r, g, b, 1.0f))
            }
        }

        binding.texture6.apply {
            updateContinuously = true
            fragmentShaderRawResId = R.raw.animated_texture
            shaderParams = ShaderParams.Builder()
                .addTexture2D(
                    "uTexture",
                    R.drawable.normal_button,
                    context.resources,
                    GLES30.GL_TEXTURE0
                )
                .addVec2f("uOffset")
                .build()
            onDrawFrameListener = { shaderParams ->
                val u = (System.currentTimeMillis() % 5000L) / 5000f
                val v = (System.currentTimeMillis() % 1000L) / 1000f
                shaderParams.updateValue("uOffset", floatArrayOf(u, v))
            }
        }

        binding.texture7.apply {
            fragmentShaderRawResId = R.raw.multiple_textures_frag
            shaderParams = ShaderParams.Builder()
                .addTexture2D(
                    "uTextureSampler1",
                    R.drawable.bokeh,
                    context.resources,
                    GLES30.GL_TEXTURE0
                )
                .addTexture2D(
                    "uTextureSampler2",
                    R.drawable.normal_button,
                    context.resources,
                    GLES30.GL_TEXTURE1
                )
                .addTexture2D(
                    "uTextureSampler3",
                    R.drawable.test_texture,
                    context.resources,
                    GLES30.GL_TEXTURE2
                )
                .build()
        }

        binding.texture8.apply {
            updateContinuously = true
            fragmentShaderRawResId = R.raw.animated_texture
            shaderParams = ShaderParams.Builder()
                .addTexture2D(
                    "uTexture",
                    R.drawable.test_texture,
                    context.resources,
                    GLES30.GL_TEXTURE0
                )
                .addVec2f("uOffset")
                .build()
            onDrawFrameListener = { shaderParams ->
                val u = sin(System.currentTimeMillis() % 1000L / 5000f)
                val v = cos(System.currentTimeMillis() % 1000L / 1000f)
                shaderParams.updateValue("uOffset", floatArrayOf(u, v))
            }
        }
    }

}