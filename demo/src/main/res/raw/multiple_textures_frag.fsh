#version 300 es

precision mediump float;

uniform sampler2D uTextureSampler1;
uniform sampler2D uTextureSampler2;
uniform sampler2D uTextureSampler3;

in vec2 textureCoord;
out vec4 fragColor;

void main() {
    vec4 texColor1 = texture(uTextureSampler1, textureCoord);
    vec4 texColor2 = texture(uTextureSampler2, textureCoord);
    vec4 texColor3 = texture(uTextureSampler3, textureCoord);

    fragColor = texColor1 * texColor2 * texColor3;
}