package com.appspell.shaderview.list

import android.opengl.GLES30
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.appspell.shaderview.R
import com.appspell.shaderview.databinding.ItemShaderBinding
import com.appspell.shaderview.gl.ShaderParams
import java.lang.RuntimeException
import kotlin.math.cos
import kotlin.math.sin

enum class ItemType {
    COLOR, MULTIPLE_TEXTURES,
    NORMAL_MAP, NORMAL_MAP_2,
    SIMPLE_ANIMATION,
    COLOR_ANIMATED, ANIMATED_TEXTURES,
    BLUR
}

class ShaderListAdapter : RecyclerView.Adapter<ShaderListAdapter.BaseShaderView>() {

    var items: List<Int> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseShaderView {
        val inflater = LayoutInflater.from(parent.context)
        val view = ItemShaderBinding.inflate(inflater)
        return when (ItemType.values()[viewType]) {
            ItemType.COLOR -> ColorShaderViewHolder(view)
            ItemType.MULTIPLE_TEXTURES -> MultipleTexturesShaderViewHolder(view)
            ItemType.NORMAL_MAP -> NormalMapShaderViewHolder(view)
            ItemType.NORMAL_MAP_2 -> NormalMapShaderViewHolder2(view)
            ItemType.SIMPLE_ANIMATION -> SimpleAnimationShaderViewHolder(view)
            ItemType.COLOR_ANIMATED -> ColorAnimatedShaderViewHolder(view)
            ItemType.ANIMATED_TEXTURES -> AnimatedTexturesShaderViewHolder(view)
            ItemType.BLUR -> BlurShaderViewHolder(view)
            else -> throw RuntimeException("view holder type is not available")
        }
    }

    override fun onBindViewHolder(holder: BaseShaderView, position: Int) {
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = items[position]

    abstract class BaseShaderView(rootView: View) : RecyclerView.ViewHolder(rootView)

    class ColorShaderViewHolder(binding: ItemShaderBinding) : BaseShaderView(binding.root) {
        init {
            binding.name.text = "color_frag" // I'm sorry but it's too boring to move it to strings for such a demo
            binding.shaderView.apply {
                updateContinuously = false // DO NOT update each frame
                fragmentShaderRawResId = R.raw.color_frag
                shaderParams = ShaderParams.Builder()
                    .addColor("diffuseColor", R.color.teal_200, resources)
                    .build()
            }
        }
    }

    class MultipleTexturesShaderViewHolder(binding: ItemShaderBinding) : BaseShaderView(binding.root) {
        init {
            binding.name.text =
                "multiple_textures_frag" // I'm sorry but it's too boring to move it to strings for such a demo
            binding.shaderView.apply {
                updateContinuously = false // DO NOT update each frame
                fragmentShaderRawResId = R.raw.multiple_textures_frag
                shaderParams = ShaderParams.Builder()
                    .addTexture2D(
                        "uTextureSampler1",
                        R.drawable.bokeh,
                        GLES30.GL_TEXTURE0
                    )
                    .addTexture2D(
                        "uTextureSampler2",
                        R.drawable.normal_button,
                        GLES30.GL_TEXTURE1
                    )
                    .addTexture2D(
                        "uTextureSampler3",
                        R.drawable.test_texture,
                        GLES30.GL_TEXTURE2
                    )
                    .build()
            }
        }
    }

    class NormalMapShaderViewHolder(binding: ItemShaderBinding) : BaseShaderView(binding.root) {
        init {
            binding.name.text =
                "nomral_map" // I'm sorry but it's too boring to move it to strings for such a demo
            binding.shaderView.apply {
                updateContinuously = true // update each frame
                vertexShaderRawResId = R.raw.quad_tangent_space_vert
                fragmentShaderRawResId = R.raw.nomral_map
                shaderParams = ShaderParams.Builder()
                    .addTexture2D(
                        "uNormalTexture",
                        R.drawable.normal_button,
                        GLES30.GL_TEXTURE0
                    )
                    .addColor("uColor", R.color.grey, resources)
                    .addVec3f("uVaryingColor", floatArrayOf(0.5f, 0.5f, 0.5f))
                    .addVec3f("uLightDirection", floatArrayOf(-1.0f, 1.0f, 0.0f))
                    .addVec3f("uEyeDirection", floatArrayOf(0.0f, 0.0f, 0.0f))
                    .build()
                onDrawFrameListener = { shaderParams ->
                    val pos = (System.currentTimeMillis() % 2500L) / 500f
                    shaderParams.updateValue("uLightDirection", floatArrayOf(-1.0f + pos, 1.0f, 0.0f))
                }
            }
        }
    }

    class NormalMapShaderViewHolder2(binding: ItemShaderBinding) : BaseShaderView(binding.root) {
        init {
            binding.name.text =
                "nomral_map v2" // I'm sorry but it's too boring to move it to strings for such a demo
            binding.shaderView.apply {
                updateContinuously = true // update each frame
                vertexShaderRawResId = R.raw.quad_tangent_space_vert
                fragmentShaderRawResId = R.raw.nomral_map
                shaderParams = ShaderParams.Builder()
                    .addTexture2D(
                        "uNormalTexture",
                        R.drawable.normal_sphere,
                        GLES30.GL_TEXTURE0
                    )
                    .addVec4f("uColor", floatArrayOf(0.2f, 0.2f, 0.2f, 1f))
                    .addVec3f("uVaryingColor", floatArrayOf(0.6f, 0.6f, 0.6f))
                    .addVec3f("uLightDirection", floatArrayOf(0.0f, 1.0f, 0.0f))
                    .addVec3f("uEyeDirection", floatArrayOf(0.0f, 0.0f, 0.0f))
                    .build()
                onDrawFrameListener = { shaderParams ->
                    val pos = (System.currentTimeMillis() % 1500.0) / 1500.0
                    shaderParams.updateValue(
                        "uLightDirection",
                        floatArrayOf(
                            cos(pos * 10).toFloat(),
                            1.0f + sin(pos).toFloat(),
                            sin(pos * 10).toFloat()
                        )
                    )
                }
            }
        }
    }

    class SimpleAnimationShaderViewHolder(binding: ItemShaderBinding) : BaseShaderView(binding.root) {
        init {
            binding.name.text =
                "simple_animation_frag" // I'm sorry but it's too boring to move it to strings for such a demo
            binding.shaderView.apply {
                updateContinuously = true // update each frame
                fragmentShaderRawResId = R.raw.simple_animation_frag
                shaderParams = ShaderParams.Builder()
                    .addFloat("time", 1.0f)
                    .build()
                onDrawFrameListener = { shaderParams ->
                    shaderParams.updateValue("time", (System.currentTimeMillis() % 5000L) / 5000f)
                }
            }

        }
    }

    class ColorAnimatedShaderViewHolder(binding: ItemShaderBinding) : BaseShaderView(binding.root) {
        init {
            binding.name.text =
                "color animated" // I'm sorry but it's too boring to move it to strings for such a demo
            binding.shaderView.apply {
                updateContinuously = true // update each frame
                fragmentShaderRawResId = R.raw.color_frag
                shaderParams = ShaderParams.Builder()
                    .addVec4f("diffuseColor", floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f))
                    .build()

                val startTime = System.currentTimeMillis()
                onDrawFrameListener = { shaderParams ->
                    val r = (((System.currentTimeMillis() - startTime) / 10) % 254).toFloat() / 254.0f
                    val g = (((System.currentTimeMillis() - startTime) / 50) % 254).toFloat() / 254.0f
                    val b = (((System.currentTimeMillis() - startTime) / 100) % 254).toFloat() / 254.0f
                    shaderParams.updateValue("diffuseColor", floatArrayOf(r, g, b, 1.0f))
                }
            }

        }
    }

    class AnimatedTexturesShaderViewHolder(val binding: ItemShaderBinding) : BaseShaderView(binding.root) {
        init {
            binding.name.text =
                "animated_texture" // I'm sorry but it's too boring to move it to strings for such a demo
            binding.shaderView.apply {
                updateContinuously = true // update each frame
                fragmentShaderRawResId = R.raw.animated_texture
                shaderParams = ShaderParams.Builder()
                    .addTexture2D(
                        "uTexture",
                        R.drawable.normal_sphere,
                        GLES30.GL_TEXTURE0
                    )
                    .addVec2f("uOffset")
                    .build()
                onDrawFrameListener = { shaderParams ->
                    val u = (System.currentTimeMillis() % 5000L) / 5000f
                    val v = (System.currentTimeMillis() % 1000L) / 1000f
                    shaderParams.updateValue("uOffset", floatArrayOf(u, v))
                }
                debugMode = true
            }
        }
    }

    class BlurShaderViewHolder(binding: ItemShaderBinding) : ShaderListAdapter.BaseShaderView(binding.root) {
        init {
            binding.name.text = "blur" // I'm sorry but it's too boring to move it to strings for such a demo
            binding.shaderView.apply {
                updateContinuously = true // update each frame
                fragmentShaderRawResId = R.raw.blur
                shaderParams = ShaderParams.Builder()
                    .addTexture2D(
                        "uTexture",
                        R.drawable.test_texture,
                        GLES30.GL_TEXTURE0
                    )
                    .addVec2f("uScale", floatArrayOf(0f, 0f))
                    .addInt("uBlurSize", 3)
                    .build()

                val startTime = System.currentTimeMillis()
                onDrawFrameListener = { shaderParams ->
                    val maxBlurSize = 25
                    val size = ((System.currentTimeMillis() - startTime) / 100) % maxBlurSize + 1
                    shaderParams.updateValue("uBlurSize", size.toInt())
                    shaderParams.updateValue("uScale", floatArrayOf(1.0f / width, 1.0f / height))
                }
            }
        }
    }
}