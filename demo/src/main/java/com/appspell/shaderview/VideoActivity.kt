package com.appspell.shaderview

import android.os.Bundle
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import com.appspell.shaderview.databinding.ActivityVideoBinding
import com.appspell.shaderview.ext.getTexture2dOESSurface
import com.appspell.shaderview.gl.ShaderParams
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.util.Util


class VideoActivity : AppCompatActivity() {
    lateinit var binding: ActivityVideoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.shaderView.apply {
            updateContinuously = true // update the view each frame
            fragmentShaderRawResId = R.raw.video_shader // fragment shader for video frame processing
            shaderParams = ShaderParams.Builder()
                .addTextureOES("uVideoTexture") // video texture input/output
                .build()
            onViewReadyListener = { shader ->
                // get surface from shader params
                val surface = shader.params.getTexture2dOESSurface("uVideoTexture")

                // initialize video player when shader is ready
                initVideoPlayer(surface)
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

        val player = SimpleExoPlayer.Builder(this@VideoActivity).build()
        player.prepare(mediaSource)
        player.setVideoSurface(surface)
        player.playWhenReady = true
        player.repeatMode = Player.REPEAT_MODE_ALL
    }
}