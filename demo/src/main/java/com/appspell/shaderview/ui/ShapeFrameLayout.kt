package com.appspell.shaderview.ui

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.annotation.StyleRes
import androidx.core.view.children
import com.appspell.shaderview.R
import com.appspell.shaderview.ShaderView
import com.appspell.shaderview.gl.ShaderParams

private const val SHADER_UNIFORM_VIEW_SIZE = "uViewSize"
private const val SHADER_UNIFORM_CORNER_RADIUS = "uCornerRadius"
private const val SHADER_UNIFORM_SMOOTHNESS = "uSmoothness"
private const val SHADER_UNIFORM_COLOR = "uColor"
private const val SHADER_UNIFORM_LIGHT_DIRECTION = "uLightDirection"

/**
 * This class is made just only as a proof of concept
 * don't judge the code
 */
class ShapeFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        var lightDirection = floatArrayOf(0.0f, 1.0f, 1.0f)
    }

    var radius: Float = resources.getDimension(R.dimen.corner_radius)
        set(value) {
            field = value

            // update shape
            shape?.shaderParams = shape?.shaderParams
                ?.newBuilder()
                ?.addFloat(SHADER_UNIFORM_CORNER_RADIUS, value)
                ?.build()

            invalidate()
        }

    var smoothness: Float = 3.0f
        set(value) {
            field = value
            shape?.shaderParams = shape?.shaderParams
                ?.newBuilder()
                ?.addFloat(SHADER_UNIFORM_SMOOTHNESS, value)
                ?.build()
        }

    @ColorRes
    var color: Int = R.color.background
        set(value) {
            field = value
            shape?.shaderParams = shape?.shaderParams
                ?.newBuilder()
                ?.addColor(SHADER_UNIFORM_COLOR, value, resources)
                ?.build()
        }

    private var shape: ShaderView? = null

    init {
        createBackground(attrs)
    }

    private fun createBackground(attrs: AttributeSet?) {

        shape = ShaderView(context, attrs).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)

            updateContinuously = false
            vertexShaderRawResId = R.raw.quad_tangent_space_vert
            fragmentShaderRawResId = R.raw.gui_element_fair_light

            shaderParams = ShaderParams.Builder()
                .addVec2f(SHADER_UNIFORM_VIEW_SIZE, floatArrayOf(0f, 0f))
                .addFloat(SHADER_UNIFORM_CORNER_RADIUS, radius)
                .addFloat(SHADER_UNIFORM_SMOOTHNESS, smoothness)
                .addColor(SHADER_UNIFORM_COLOR, color, resources)
                .addVec3f(SHADER_UNIFORM_LIGHT_DIRECTION, lightDirection)
                .build()
            debugMode = true
        }
        addView(shape)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        shape?.apply {
            shaderParams = shaderParams
                ?.newBuilder()
                ?.addVec2f(SHADER_UNIFORM_VIEW_SIZE, floatArrayOf(width.toFloat(), height.toFloat()))
                ?.build()
            requestRender()
        }

        val offset = radius.toInt()
        children.forEach { view ->
            if (view != shape) {
                // just small correction to show that this approach can work
                view.left += offset
                view.top += offset

//                view.measure(view.measuredWidth - offset, view.measuredHeight - offset)
            }
        }
    }
}