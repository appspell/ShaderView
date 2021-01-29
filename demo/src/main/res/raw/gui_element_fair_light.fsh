#version 300 es

precision mediump float;

uniform vec2 uViewSize; // size of view or screen
uniform float uCornerRadius;
uniform float uSmoothness;
uniform vec4 uColor; // diffuse color

uniform vec3 uLightDirection;

in vec2 textureCoord;
in mat3 matrixTBN;
out vec4 fragColor;

#define HALF_HEIGHT 0.5

float shapeHeightMap(vec2 uv, vec2 angularity) {
    vec2 smoothAngularity = vec2(pow(angularity.x, uSmoothness), pow(angularity.y, uSmoothness));
    // horizontal
    float height = pow(uv.x, smoothAngularity.x) + pow(1.0 - uv.x, smoothAngularity.x);
    // vertical
    height += pow(uv.y, smoothAngularity.y) + pow(1.0 - uv.y, smoothAngularity.y);
    return height;
}

vec2 combinedShapeHeightMap(vec2 uv, vec2 angularity) {
    // general shape (extrude)
    float heightMain = shapeHeightMap(uv, angularity);
    heightMain = smoothstep(1.0, 0.0, heightMain);

    // slightly squeeze the top of the shape
    float heightInner = shapeHeightMap(uv, angularity);
    heightInner = smoothstep(0.0, 1.0, heightInner) + 0.5;

    return vec2(min(1.0, heightMain * heightInner), heightMain);
}

vec4 heightMapToNormalMap(vec2 uv, vec2 angularity, float scale) {
    vec2 step = 1.0 / uViewSize;

    vec2 height = combinedShapeHeightMap(uv, angularity);
    float hx = combinedShapeHeightMap(uv + vec2(step.x, 0.0), angularity).x;
    float hy = combinedShapeHeightMap(uv + vec2(0.0, step.y), angularity).x;
    vec2 dxy = height.x - vec2(hx, hy);

    float alpha = 0.0;
    if(height.y > 0.0) alpha = 1.0;

    return vec4(normalize(vec3(dxy * scale / step, 1.0)), alpha);
}

void main() {
    vec2 uv = gl_FragCoord.xy / uViewSize.xy;
    vec2 angularity = uViewSize.xy / uCornerRadius;

    vec4 normalMap = heightMapToNormalMap(uv, angularity, 0.05);

    // process input parameters (better move it to vertex shader)
    vec3 inverseLightDirection = uLightDirection * matrixTBN;

    // diffuse component
    float normalDotLight = max(0.0, dot(normalMap.rgb, inverseLightDirection));
    vec3 diffuseColor = uColor.rgb * normalDotLight;

    fragColor = vec4(diffuseColor, normalMap.a);
}