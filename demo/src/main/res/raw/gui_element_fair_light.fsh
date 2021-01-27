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

vec4 shapeHeightMap(vec2 uv) {
    // original function you may find here https://www.shadertoy.com/view/tlsGWN

    // main shape
    vec2 angularity = uViewSize / uCornerRadius;
    float shape = pow(pow(uv.x, angularity.x) + pow(uv.y, angularity.y), uSmoothness);
    float height = mix(HALF_HEIGHT, 0.0, shape);

    // inner shape
    angularity = (uViewSize - vec2(uCornerRadius, uCornerRadius)) / (uCornerRadius * 0.5);
    shape = pow(pow(uv.x, angularity.x) + pow(uv.y, angularity.y), uSmoothness * 2.0);
    height *= shape;

    float alpha = 0.0;
    if(height > 0.0) alpha = 1.0;

    return vec4(height, height, height, alpha);
}

vec4 heightMapToNormalMap(vec2 uv, float scale) {
  vec2 step = 1.0 / uViewSize;

  vec4 height = shapeHeightMap(uv);

  vec2 dxy = height.r - vec2(
      shapeHeightMap(uv + vec2(step.x, 0.0)).r,
      shapeHeightMap(uv + vec2(0.0, step.y)).r
  );

  return vec4(normalize(vec3(dxy * scale / step, 1.)), height.a);
}

void main() {
    vec2 center = uViewSize * 0.5;
    vec2 pos = gl_FragCoord.xy - center;
    vec2 uv = pos / center;

    vec4 normalMap = heightMapToNormalMap(uv, 0.1);

    // process input parameters (better move it to vertex shader)
    vec3 inverseLightDirection = uLightDirection * matrixTBN;

    // diffuse component
    float normalDotLight = max(0.0, dot(normalMap.rgb, inverseLightDirection));
    vec3 diffuseColor = uColor.rgb * normalDotLight;

    fragColor = vec4(diffuseColor, normalMap.a);
}