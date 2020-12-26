#version 300 es

precision mediump float;

in vec2 textureCoord;
out vec4 fragColor;

void main() {
    fragColor = vec4(textureCoord.x, textureCoord.y, textureCoord.y, 1.0);
}