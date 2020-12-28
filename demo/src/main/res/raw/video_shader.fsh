#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require

precision mediump float;

uniform samplerExternalOES uVideoTexture;

in vec2 textureCoord;
out vec4 fragColor;

void main() {
    vec4 videoTex = texture(uVideoTexture, textureCoord);
    fragColor = vec4(videoTex.rrr, 1); // show b/w
}