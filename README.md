# ShaderView

This library is the easiest way to use **OpenGL shaders** as an **[Android View](https://developer.android.com/reference/android/view/View)**. You just simply need to add **ShaderView** in your layout and set up shaders.
The advantage of this library that you can use ShaderView in your hierarchy as a regular View.

### Use cases:
- Shaders for video
- Advanced UI components (blur, shadow, lighting)
- UI effects and animation
- Realtime image animation

![shaders in RecyclerView](https://i.imgur.com/Iv1FLrg.gif)
![shader for video](https://i.imgur.com/znnJsQp.gif)
![custom UI](https://i.imgur.com/XAqSmP7.png)

## How to use it?

- [Add ShaderView to XML layout](https://github.com/appspell/ShaderView#option-1-add-shaderview-to-xml-layout)
- [Add ShaderView programmatically](https://github.com/appspell/ShaderView#option-2-add-shaderview-programmatically-or-configure-programmatically)
- [The full list of ShaderView properties](https://github.com/appspell/ShaderView#the-full-list-of-shaderview-properties)
- [How to send custom data to the shader](https://github.com/appspell/ShaderView#how-to-send-custom-data-to-the-shader)
- [How to add custom fragment shader using build-in vector shader](https://github.com/appspell/ShaderView#how-to-add-custom-fragment-shader-using-build-in-vector-shader)
- [How to add shaders for video playback](https://github.com/appspell/ShaderView#how-to-add-shaders-for-video-playback)
- [Example of shaders](https://github.com/appspell/ShaderView#example-of-shaders)

### Option #1. Add ShaderView to XML layout

1. Add ShaderView to the XML layout

```xml
<com.appspell.shaderview.ShaderView
        android:id="@+id/shaderView"
        android:layout_width="200dp"
        android:layout_height="200dp"
        android:layout_gravity="center"
        app:fragment_shader_raw_res_id="@raw/simple_frag" />
```

2. Set your fragment and vertex (if needed) shaders using the following attributes:

`app:fragment_shader_raw_res_id` - reference to the fragment shader file in RAW resource solder [example](https://github.com/appspell/ShaderView/blob/main/ShaderView/src/main/res/raw/default_frag.fsh)

`app:vertex_shader_raw_res_id` - reference to the vertex shader file in RAW resource solder [example](https://github.com/appspell/ShaderView/blob/main/ShaderView/src/main/res/raw/quad_vert.vsh)

### Option #2. Add ShaderView programmatically (or configure programmatically)

```kotlin
val shaderView = ShaderView(this)

with(shaderView) {
   fragmentShaderRawResId = R.raw.color_frag
   shaderParams = ShaderParams.Builder()
                .addColor("diffuseColor", R.color.teal_200, resources)
                .build()
}
```

### The full list of ShaderView properties:

`fragmentShaderRawResId` - reference to the vertex shader file in RAW resource solder [example]

`vertexShaderRawResId` - reference to the fragment shader file in RAW resource solder [example]

`shaderParams` - custom parameters that we're going to send to the shader (uniform)

`onViewReadyListener` - called when the view is created and ready to create a shader

`onDrawFrameListener` - called each frame

`updateContinuously` -  should we render the view each frame only when it's really needed by the system (default is "false")

`debugMode` - enable or disable debug logs

## How to send custom data to the shader

1. Create ShaderParams instance using ShaderParamsBuilder

```kotlin
ShaderParamsBuilder()
                    .addTexture2D(
                        "uNormalTexture", // name of `sampler2D` in the fragment shader
                        R.drawable.normal_button, // drawable that we use for such texture
                        GLES30.GL_TEXTURE0 // texture slot
                    )
                    .addColor("uColor", R.color.grey, resources) // send color as `uniform vec4`
                    .addVec4f("uColor2", floatArrayOf(0.5f, 0.5f, 0.5f, 1f))
                    .addVec3f("uVaryingColor", floatArrayOf(0.5f, 0.5f, 0.5f))
                    .addFloat("uTime", 1.0f)
                    .build()
```

2. Set ShaderParams from the previous step to the ShaderView

```kotlin
shaderView.shaderParams = shaderParams
```

*The full list of supported uniform types:
float, int, bool,
vec2f, vec3f, vec4f, vec2i, vec3i, vec4i,
mat3, mat4, mat3x4,
sampler2D, samplerExternalOES*

## How to add custom **fragment shader** using build-in vector shader

1. Set up version
2. Configure input and output. Buy default vertex shader sends texture coordinates using this field `in vec2 textureCoord`
3. add `main()` function and return the result color to `fragColor`

```glsl
#version 300 es

precision mediump float;

in vec2 textureCoord;
out vec4 fragColor;

void main() {
    fragColor = vec4(textureCoord.x, textureCoord.y, textureCoord.y, 1.0);
}
```

## How to add shaders for **video playback**

Full code of example using [ExoPlayer](https://github.com/google/ExoPlayer) you may find [here](https://github.com/appspell/ShaderView/blob/main/demo/src/main/java/com/appspell/shaderview/video/VideoActivity.kt) and [here](https://github.com/appspell/ShaderView/blob/main/demo/src/main/java/com/appspell/shaderview/video/VideoAdvancedActivity.kt)

1. Setup OES texture in fragment shader:
```glsl
#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require

uniform samplerExternalOES uVideoTexture;
```

2. Define it for ShaderParams
```glsl
shaderParams = ShaderParamsBuilder()
                .addTextureOES("uVideoTexture") // video texture input/output
                .build()
```
3. When `ShaderView` is ready, send `Surface` to the video player
```kotlin
shaderView.onViewReadyListener = { shader ->
                // get surface from shader params
                val surface = shader.params.getTexture2dOESSurface("uVideoTexture")

                // initialize video player with this surface
                initVideoPlayer(surface)
            }
```

## Example of shaders
- [simple shader](https://github.com/appspell/ShaderView/blob/main/demo/src/main/res/raw/simple_frag.fsh)
- [blur](https://github.com/appspell/ShaderView/blob/main/demo/src/main/res/raw/blur.fsh)
- normal map shader: [vertex](https://github.com/appspell/ShaderView/blob/main/ShaderView/src/main/res/raw/quad_tangent_space_vert.vsh), [fragment](https://github.com/appspell/ShaderView/blob/main/demo/src/main/res/raw/nomral_map.fsh)
- [color](https://github.com/appspell/ShaderView/blob/main/demo/src/main/res/raw/color_frag.fsh)
- [multiple textures](https://github.com/appspell/ShaderView/blob/main/demo/src/main/res/raw/multiple_textures_frag.fsh)
- [video](https://github.com/appspell/ShaderView/blob/main/demo/src/main/res/raw/video_shader.fsh)
- [advanced video shader](https://github.com/appspell/ShaderView/blob/main/demo/src/main/res/raw/video_advanced_shader.fsh)

In Android Demo Project code you may found it in ViewHolders [here](https://github.com/appspell/ShaderView/blob/main/demo/src/main/java/com/appspell/shaderview/list/ShaderListAdapter.kt)

## Additional information

Why we use [TextureView](https://developer.android.com/reference/android/view/TextureView) instead of [SurfaceView](https://developer.android.com/reference/android/view/SurfaceView) you can read [here](https://github.com/crosswalk-project/crosswalk-website/wiki/Android-SurfaceView-vs-TextureView). 

To be able to use OpenGL rendering for Android TextureView, we've created [GLTextureView.kt](https://github.com/appspell/ShaderView/blob/main/ShaderView/src/main/java/com/appspell/shaderview/gl/GLTextureView.kt)
