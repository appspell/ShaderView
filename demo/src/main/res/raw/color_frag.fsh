#version 300 es

precision mediump float;

uniform vec4 diffuseColor;

in vec2 textureCoord;
out vec4 fragColor;

void main() {
    fragColor = diffuseColor;
}