package com.appspell.shaderview.demo.ui

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.annotation.StyleRes
import androidx.core.view.children
import com.appspell.shaderview.ShaderView
import com.appspell.shaderview.demo.R
import com.appspell.shaderview.gl.params.ShaderParamsBuilder

private const val SHADER_UNIFORM_VIEW_SIZE = "uViewSize"
private const val SHADER_UNIFORM_CORNER_RADIUS = "uCornerRadius"
private const val SHADER_UNIFORM_SMOOTHNESS = "uSmoothness"
private const val SHADER_UNIFORM_SCALE = "uScale"
private const val SHADER_UNIFORM_COLOR = "uColor"
private const val SHADER_UNIFORM_LIGHT_DIRECTION = "uLightDirection"

/**
 * This class is made just only as a proof of concept
 * don't judge the code
 */
class ShapeLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        var lightDirection = floatArrayOf(-0.5f, 1.0f, 1.0f)
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

    var smoothness: Float = 1.5f
        set(value) {
            field = value
            shape?.shaderParams = shape?.shaderParams
                ?.newBuilder()
                ?.addFloat(SHADER_UNIFORM_SMOOTHNESS, value)
                ?.build()
        }

    var scale: Float = 0.01f
        set(value) {
            field = value
            shape?.shaderParams = shape?.shaderParams
                ?.newBuilder()
                ?.addFloat(SHADER_UNIFORM_SCALE, value)
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

    private val offset
        get() = (radius * 0.5f).toInt()

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

            shaderParams = ShaderParamsBuilder()
                .addVec2f(SHADER_UNIFORM_VIEW_SIZE, floatArrayOf(0f, 0f))
                .addFloat(SHADER_UNIFORM_CORNER_RADIUS, radius)
                .addFloat(SHADER_UNIFORM_SMOOTHNESS, smoothness)
                .addFloat(SHADER_UNIFORM_SCALE, scale)
                .addColor(SHADER_UNIFORM_COLOR, color, resources)
                .addVec3f(SHADER_UNIFORM_LIGHT_DIRECTION, lightDirection)
                .build()
            debugMode = true
        }
        addView(shape)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        setMeasuredDimension(width, height)

        children.forEach { view ->
            if (view !is ShaderView) {
                val newWidth = MeasureSpec.makeMeasureSpec(width - offset, MeasureSpec.EXACTLY)
                val newHeight = MeasureSpec.makeMeasureSpec(height - offset, MeasureSpec.EXACTLY)
                view.measure(newWidth, newHeight)
            } else {
                val newWidth = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
                val newHeight = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
                view.measure(newWidth, newHeight)
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        children.forEach { view ->
            val width: Int = view.measuredWidth
            val height: Int = view.measuredHeight

            if (view !is ShaderView) {
                view.layout(
                    offset,
                    offset,
                    width,
                    height
                )
            } else {
                view.layout(0, 0, width, height)

                // apply new size for shaders
                updateShadersViewSize(width, height)
            }
        }
    }

    private fun updateShadersViewSize(width: Int, height: Int) {
        shape?.apply {
            shaderParams = shaderParams
                ?.newBuilder()
                ?.addVec2f(SHADER_UNIFORM_VIEW_SIZE, floatArrayOf(width.toFloat(), height.toFloat()))
                ?.build()
            requestRender()
        }
    }
}