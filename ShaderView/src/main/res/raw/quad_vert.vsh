#version 300 es

uniform mat4 uMVPMatrix;
uniform mat4 uSTMatrix;

in vec4 inPosition;
in vec4 inTextureCoord;

out vec2 textureCoord;
//out vec2 normalCoord; // TODO

void main() {
    gl_Position = uMVPMatrix * inPosition;
    textureCoord = (uSTMatrix * inTextureCoord).xy;
}