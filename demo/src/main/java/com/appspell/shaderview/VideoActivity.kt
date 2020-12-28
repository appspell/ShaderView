package com.appspell.shaderview

import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES
import android.opengl.GLES30
import android.opengl.Matrix
import android.os.Bundle
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import com.appspell.shaderview.databinding.ActivityVideoBinding
import com.appspell.shaderview.ext.createExternalTexture
import com.appspell.shaderview.gl.ShaderParams
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.util.GlUtil
import com.google.android.exoplayer2.util.Util


class VideoActivity : AppCompatActivity() {
    lateinit var binding: ActivityVideoBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initVideoPlayer()
    }

    private fun initVideoPlayer() {
        val uri = RawResourceDataSource.buildRawResourceUri(R.raw.video)

        val userAgent: String = Util.getUserAgent(this, BuildConfig.APPLICATION_ID)
        val defDataSourceFactory = DefaultDataSourceFactory(this, userAgent)
        val mediaSource: MediaSource = ProgressiveMediaSource
            .Factory(defDataSourceFactory)
            .createMediaSource(uri)

        binding.shaderView.apply {
            // TODO move to proper place
            var surfaceTexture : SurfaceTexture? = null

            updateContinuously = true
            fragmentShaderRawResId = R.raw.video_shader
            shaderParams = ShaderParams.Builder()
                    // todo add external shader texture
                .build()
            onViewReadyListener = { shader ->
                val textureId = createExternalTexture()
                surfaceTexture = SurfaceTexture(textureId)
                val surface = Surface(surfaceTexture)

                val player = SimpleExoPlayer.Builder(this@VideoActivity).build()
                player.setVideoSurface(surface)
                player.prepare(mediaSource)
                player.playWhenReady = true
                player.repeatMode = Player.REPEAT_MODE_ALL
            }

            val matrixSTM = FloatArray(16)
            Matrix.setIdentityM(matrixSTM, 0)
            onDrawFrameListener = { shaderParams ->
                synchronized(this) {
                    // TODO move to proper place
//                    if (updateSurface) {
                    surfaceTexture?.updateTexImage();
                    surfaceTexture?.getTransformMatrix(matrixSTM);
//                        updateSurface = false;
//                    }
                }
            }
        }
    }
}