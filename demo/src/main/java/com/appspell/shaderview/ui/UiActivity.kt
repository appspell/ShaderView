package com.appspell.shaderview.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appspell.shaderview.R
import com.appspell.shaderview.databinding.ActivityUiBinding

class UiActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityUiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.shapeLayout1.apply {
            color = R.color.teal_200
            radius = resources.getDimension(R.dimen.shape_radius)
            smoothness = 2.0f
        }

        binding.shapeLayout2.apply {
            radius = resources.getDimension(R.dimen.shape_radius)
            smoothness = 2.0f
        }
    }
}