#version 300 es

precision mediump float;

uniform float time;

in vec2 textureCoord;
out vec4 fragColor;

void main() {
    fragColor = vec4(textureCoord.x * time, textureCoord.y * time, 1.0, 1.0);
}