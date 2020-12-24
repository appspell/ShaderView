#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vTextureCoord;
uniform vec3 myUniform;
//uniform int isEnabled;
void main() {
    gl_FragColor = vec4(vTextureCoord.x, vTextureCoord.y * myUniform.y, myUniform.x, myUniform.z);
}