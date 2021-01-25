#version 300 es

precision mediump float;

uniform sampler2D uTexture;
uniform vec2 uScale;
uniform int uBlurSize;

in vec2 textureCoord;
out vec4 fragColor;

void main() {
    vec4 color = vec4(0.0);
    float halfOffset = float(uBlurSize) / 2.0;

    for (int x = 0; x < uBlurSize; x++) {
        for (int y = 0; y < uBlurSize; y++) {
            vec2 offset = vec2(float(x) - halfOffset, float(y) - halfOffset) * uScale;
            color += texture(uTexture, textureCoord + offset);
        }
    }

    color = color / float (uBlurSize * uBlurSize);

    fragColor = color;
}