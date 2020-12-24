#version 300 es

precision mediump float;

uniform vec3 myUniform;
uniform bool isEnabled;

in vec2 textCoord;
out vec4 fragColor;

void main() {
    if(isEnabled) {
        fragColor = vec4(textCoord.x, 1.0, 1.0, 1.0);
    } else {
        fragColor = vec4(textCoord.x, textCoord.y * myUniform.y, myUniform.x, myUniform.z);
    }

//    fragColor = vec4(textCoord.x, textCoord.y * myUniform.y, myUniform.x, myUniform.z);
}