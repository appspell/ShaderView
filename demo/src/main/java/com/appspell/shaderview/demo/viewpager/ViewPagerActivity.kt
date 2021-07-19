package com.appspell.shaderview.demo.viewpager

import android.opengl.GLES30
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.appspell.shaderview.ShaderView
import com.appspell.shaderview.demo.R
import com.appspell.shaderview.demo.databinding.ActivityViewPagerBinding
import com.appspell.shaderview.gl.params.ShaderParamsBuilder
import kotlin.random.Random

class ViewPagerActivity : AppCompatActivity() {
    lateinit var bindings: ActivityViewPagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindings = ActivityViewPagerBinding.inflate(layoutInflater)
        setContentView(bindings.root)

        bindings.pager.adapter = SampleAdapter(this)
    }

    class SampleAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 5

        override fun createFragment(position: Int): Fragment = PageFragment()
    }

    class PageFragment : Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
            return ShaderView(inflater.context).apply {
                updateContinuously = true // update each frame
                debugMode = true // to see logs

                fragmentShaderRawResId = R.raw.animated_texture

                val texture = when (Random.nextInt(5)) {
                    1 -> R.drawable.android
                    2 -> R.drawable.normal_button
                    3 -> R.drawable.bokeh
                    4 -> R.drawable.test_texture
                    else -> R.drawable.normal_sphere
                }

                shaderParams = ShaderParamsBuilder()
                    .addTexture2D(
                        "uTexture",
                        texture,
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
        }
    }
}