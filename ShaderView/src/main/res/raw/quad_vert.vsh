#version 300 es

uniform mat4 uMVPMatrix;
uniform mat4 uSTMatrix;

in vec4 inPosition;
in vec4 inTextureCoord;
out vec2 textCoord;

void main() {
    gl_Position = uMVPMatrix * inPosition;
    textCoord = (uSTMatrix * inTextureCoord).xy;
}