package com.appspell.shaderview.demo.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appspell.shaderview.demo.R
import com.appspell.shaderview.demo.databinding.ActivityUiBinding

class UiActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityUiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.shapeLayout1.apply {
            radius = resources.getDimension(R.dimen.shape_radius)
            smoothness = 2.0f
            scale = 0.05f
        }

        binding.button.apply {
            color = R.color.purple_200
            radius = resources.getDimension(R.dimen.shape_radius)
            smoothness = 1.5f
            scale = 0.07f
        }

        binding.shapeLayout3.apply {
            radius = resources.getDimension(R.dimen.shape_radius)
            smoothness = 1.5f
            scale = 0.01f
        }

        binding.innerShapeLayout.apply {
            radius = resources.getDimension(R.dimen.shape_radius)
            smoothness = 2.0f
            scale = -0.03f
        }

        binding.circle.apply {
            smoothness = 1.0f
            scale = -0.1f
        }
    }
}