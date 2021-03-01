package com.appspell.shaderview.demo.list

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appspell.shaderview.demo.databinding.ActivityShaderListBinding
import com.appspell.shaderview.demo.list.ItemType.*

class ShaderListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityShaderListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.list.adapter = ShaderListAdapter()
            .apply {
                items = mutableListOf(
                    COLOR, NORMAL_MAP_2,
                    BLUR, NORMAL_MAP,
                    COLOR_ANIMATED, MULTIPLE_TEXTURES,
                    SIMPLE_ANIMATION, ANIMATED_TEXTURES
                )
                    .repeat(10)
                    .map { it.ordinal }
            }
    }

    private fun <E> MutableList<E>.repeat(times: Int): MutableList<E> {
        for (i in 0..times) {
            this.addAll(this)
        }
        return this
    }
}

