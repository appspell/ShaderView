package com.appspell.shaderview.demo.viewpager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.appspell.shaderview.ShaderView
import com.appspell.shaderview.demo.BuildConfig
import com.appspell.shaderview.demo.R
import com.appspell.shaderview.demo.databinding.ActivityViewPagerBinding
import com.appspell.shaderview.ext.getTexture2dOESSurface
import com.appspell.shaderview.gl.params.ShaderParamsBuilder
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.util.Util

/**
 * Test example for this issue https://github.com/appspell/ShaderView/issues/7
 */
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
            return ShaderView(inflater.context)
                .apply {
                    updateContinuously = true // update the view each frame (do not forget set it "true")
                    debugMode = true
                    fragmentShaderRawResId = R.raw.video_shader // fragment shader for video frame processing
                    shaderParams = ShaderParamsBuilder()
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
            val userAgent: String = Util.getUserAgent(requireContext(), BuildConfig.APPLICATION_ID)
            val defDataSourceFactory = DefaultDataSourceFactory(requireContext(), userAgent)

            val uri = RawResourceDataSource.buildRawResourceUri(R.raw.video)
            val mediaItem: MediaItem = MediaItem.fromUri(uri)

            val mediaSource: MediaSource = ProgressiveMediaSource
                .Factory(defDataSourceFactory)
                .createMediaSource(mediaItem)

            val player = SimpleExoPlayer.Builder(requireContext()).build()
                .apply {
                    setMediaSource(mediaSource)
                    prepare()
                    setVideoSurface(surface)
                    playWhenReady = true
                    repeatMode = Player.REPEAT_MODE_ALL
                }

            lifecycle.addObserver(object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                fun onPause() {
                    player.pause()
                }

                @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
                fun onResume() {
                    player.playWhenReady = true
                }
            })
        }
    }
}