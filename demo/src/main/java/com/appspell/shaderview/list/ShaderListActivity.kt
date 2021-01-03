package com.appspell.shaderview.list

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.appspell.shaderview.databinding.ActivityShaderListBinding
import com.appspell.shaderview.list.ItemType.COLOR
import com.appspell.shaderview.list.ItemType.COLOR_ANIMATED
import com.appspell.shaderview.list.ItemType.MULTIPLE_TEXTURES
import com.appspell.shaderview.list.ItemType.NORMAL_MAP
import com.appspell.shaderview.list.ItemType.NORMAL_MAP_2
import com.appspell.shaderview.list.ItemType.SIMPLE_ANIMATION
import com.appspell.shaderview.list.ItemType.ANIMATED_TEXTURES
import com.appspell.shaderview.list.ItemType.BLUR

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

