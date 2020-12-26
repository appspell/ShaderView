#version 300 es

precision mediump float;

uniform vec3 diffuseColor;

in vec2 textureCoord;
out vec4 fragColor;

void main() {
    fragColor = vec4(diffuseColor.xyz, 1.0);
}