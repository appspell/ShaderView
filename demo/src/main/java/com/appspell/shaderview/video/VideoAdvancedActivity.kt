package com.appspell.shaderview.video

import android.graphics.Outline
import android.os.Bundle
import android.view.Surface
import android.view.View
import android.view.ViewOutlineProvider
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.appspell.shaderview.BuildConfig
import com.appspell.shaderview.R
import com.appspell.shaderview.databinding.ActivityVideoBinding
import com.appspell.shaderview.ext.getTexture2dOESSurface
import com.appspell.shaderview.gl.params.ShaderParamsBuilder
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.util.Util
import kotlin.math.min
import kotlin.random.Random

class VideoAdvancedActivity : AppCompatActivity() {
    lateinit var binding: ActivityVideoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.shaderView.apply {
            // [ configure view ]
            // as far it is Android View we can set OutlineProvider to have round corners
            clipToOutline = true
            outlineProvider = RoundOutlineProvider()

            // [ configure shaders ]
            updateContinuously = true // update the view each frame (do not forget set it "true")
            fragmentShaderRawResId = R.raw.video_advanced_shader // fragment shader for video frame processing
            shaderParams = ShaderParamsBuilder()
                .addFloat("progress", 0.5f)
                .addFloat("fps", 30f)
                .addVec2f("resolution", floatArrayOf(1f, 1f))
                .addTextureOES("uVideoTexture") // video texture input/output
                .build()
            onViewReadyListener = { shader ->
                // get surface from shader params
                val surface = shader.params.getTexture2dOESSurface("uVideoTexture")

                // initialize video player when shader is ready
                binding.root.post {
                    initVideoPlayer(surface)
                }

            }
            onDrawFrameListener = { shaderParams ->
                shaderParams.updateValue("progress", Random.nextFloat())
            }

        }
    }

    /**
     * Initialize ExoPlayer
     *
     * example: https://exoplayer.dev/hello-world.html
     */
    private fun initVideoPlayer(surface: Surface?) {
        val uri = RawResourceDataSource.buildRawResourceUri(R.raw.video)

        val userAgent: String = Util.getUserAgent(this, BuildConfig.APPLICATION_ID)
        val defDataSourceFactory = DefaultDataSourceFactory(this, userAgent)
        val mediaSource: MediaSource = ProgressiveMediaSource
            .Factory(defDataSourceFactory)
            .createMediaSource(uri)

        val player = SimpleExoPlayer.Builder(this@VideoAdvancedActivity).build()
        player.prepare(mediaSource)
        player.setVideoSurface(surface)
        player.playWhenReady = true
        player.repeatMode = Player.REPEAT_MODE_ALL

        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            fun onPause() {
                player.stop()
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun onResume() {
                player.playWhenReady = true
            }
        })
    }

    /**
     * the custom Outline Provider to apply some shape to the ShaderView
     */
    class RoundOutlineProvider : ViewOutlineProvider() {
        override fun getOutline(view: View?, outline: Outline?) {
            val width = view?.width ?: 0
            val height = view?.height ?: 0
            val radius = min(width / 2f, height / 2f)
            outline?.setRoundRect(0, 0, width, height, radius)
        }
    }
}