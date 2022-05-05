# ShaderView
[![](https://jitpack.io/v/appspell/ShaderView.svg)](https://jitpack.io/#appspell/ShaderView)

This library is the easiest way to use **OpenGL shaders** as an **[Android View](https://developer.android.com/reference/android/view/View)**. You just simply need to add **ShaderView** in your layout and set up shaders.
The advantage of this library that you can use ShaderView in your hierarchy as a regular View.


### Use cases:

- Shaders for video
- Advanced UI components (blur, shadow, lighting, etc.)
- UI effects and animation
- Realtime image animation
- Shaders for a camera

<img src="https://i.imgur.com/bV8im18.gif" width="30%"><img src="https://i.imgur.com/zQa1uas.gif" width="30%">

## Table of content

- [How to use it](https://github.com/appspell/ShaderView#how-to-use-it)
   - [Add to the project](https://github.com/appspell/ShaderView#add-dependency-to-the-project)
   - [Add ShaderView to XML layout](https://github.com/appspell/ShaderView#add-shaderview-to-xml-layout)
   - [Add ShaderView programmatically](https://github.com/appspell/ShaderView#add-shaderview-programmatically-or-configure-programmatically)
- [The full list of ShaderView properties](https://github.com/appspell/ShaderView#the-full-list-of-shaderview-properties)
- [How to send custom data to the shader](https://github.com/appspell/ShaderView#how-to-send-custom-data-to-the-shader)
- [How to add custom fragment shader using build-in vector shader](https://github.com/appspell/ShaderView#how-to-add-custom-fragment-shader-using-build-in-vector-shader)
- [How to add shaders for video playback](https://github.com/appspell/ShaderView#how-to-add-shaders-for-video-playback)
- [Example of shaders](https://github.com/appspell/ShaderView#example-of-shaders)


## How to use it

### Add dependency to the project

**Gradle**

```gralde
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

```gradle
implementation 'com.github.appspell:ShaderView:[last-version]'
```

### Add ShaderView to XML layout

1. Add ShaderView to the XML layout

```xml
    <com.appspell.shaderview.ShaderView
        android:id="@+id/shaderView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:fragment_shader_raw_res_id="@raw/fragment_shader" />
```

2. Set your fragment and vertex (if needed) shaders using the following attributes:

`app:fragment_shader_raw_res_id` - reference to the fragment shader file in RAW resource solder [example](https://github.com/appspell/ShaderView/blob/main/lib/src/main/res/raw/default_frag.fsh)

`app:vertex_shader_raw_res_id` - reference to the vertex shader file in RAW resource solder [example](https://github.com/appspell/ShaderView/blob/main/lib/src/main/res/raw/quad_vert.vsh)

### Add ShaderView programmatically (or configure programmatically)

```kotlin
val shaderView = ShaderView(this)

with(shaderView) {
   fragmentShaderRawResId = R.raw.color_frag
   shaderParams = ShaderParams.Builder()
                .addColor("diffuseColor", R.color.teal_200, resources)
                .build()
}
```


## The full list of ShaderView properties:

`fragmentShaderRawResId` - reference to the vertex shader file in RAW resource solder [example]  
OR  
`fragmentShader` - a string of the fragment shader code

`vertexShaderRawResId` - reference to the fragment shader file in RAW resource solder [example]  
OR  
`vertexShader` - a string of the vertex shader code

`shaderParams` - custom parameters that we're going to send to the shader (uniform)

`onViewReadyListener` - called when the view is created and ready to create a shader

`onDrawFrameListener` - called each frame

`updateContinuously` -  should we render the view each frame (default is "false")

`fps` - At what framerate should the shader be drawn if updateContinuously set to true (0 or below means as quickly as the device can handle)

`debugMode` - enable or disable debug logs


## How to send custom data to the shader

Pass `ShaderParams` to the `ShaderView` if you need to set up some `uniform` attributes.

```kotlin
shaderView.shaderParams = ShaderParamsBuilder()
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

During execution, you may update this param:

```kotlin
shaderParams.updateValue("time", System.currentTimeMillis())
```

If you need to update `uniform` each frame, you may use `onDrawFrameListener`.

```kotlin
shaderView.onDrawFrameListener = { shaderParams ->
                    shaderParams.updateValue("time", System.currentTimeMillis())
                }
```

*The full list of supported uniform types:
float, int, bool,
vec2f, vec3f, vec4f, vec2i, vec3i, vec4i,
mat3, mat4, mat3x4,
sampler2D, samplerExternalOES*


## How to add custom **fragment shader** using build-in vector shader

1. Set up version
2. Configure input and output. By default vertex shader sends texture coordinates using this field `in vec2 textureCoord`
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

Full code of example using [ExoPlayer](https://github.com/google/ExoPlayer) you may find [here](https://github.com/appspell/ShaderView/blob/main/demo/src/main/java/com/appspell/shaderview/demo/video/VideoActivity.kt) and [here](https://github.com/appspell/ShaderView/blob/main/demo/src/main/java/com/appspell/shaderview/demo/video/VideoAdvancedActivity.kt)

1. Setup [OES texture](https://www.khronos.org/registry/OpenGL/extensions/OES/OES_EGL_image_external_essl3.txt) in [fragment shader](https://github.com/appspell/ShaderView/blob/main/demo/src/main/res/raw/video_shader.fsh):
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

In Android Demo Project code you may found it in ViewHolders [here](https://github.com/appspell/ShaderView/blob/main/demo/src/main/java/com/appspell/shaderview/demo/list/ShaderListAdapter.kt)


## Additional information

Why we use [TextureView](https://developer.android.com/reference/android/view/TextureView) instead of [SurfaceView](https://developer.android.com/reference/android/view/SurfaceView) you can read [here](https://github.com/crosswalk-project/crosswalk-website/wiki/Android-SurfaceView-vs-TextureView). 

To be able to use OpenGL rendering for Android TextureView, we've created [GLTextureView.kt](https://github.com/appspell/ShaderView/blob/main/lib/src/main/java/com/appspell/shaderview/gl/view/GLTextureView.kt)
