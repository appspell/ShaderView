#version 300 es

precision mediump float;

uniform vec2 uViewSize;

in vec2 textureCoord;
in mat3 matrixTBN;
out vec4 fragColor;

vec4 shapeHeightMap(vec2 pos, float frameThickness, float maxHeight) {
    float defaultHeight = 0.5;

    float s1 = pos.x * pos.y * (1.0-pos.x) * (1.0-pos.y);
    float s2 = pow(frameThickness,4.0);

    float shape = min(1.0, s2 / s1);

    float alpha  = smoothstep(1.0, 0.0, shape);
    float height = smoothstep(maxHeight, 0.0, shape);
    float normalizedHeight = height * defaultHeight;

    return vec4(normalizedHeight, normalizedHeight, normalizedHeight, alpha);
}

vec4 bumpFromDepth(vec2 pos, float frameThickness, float maxHeight, vec2 resolution, float scale) {
  vec2 step = 1.0 / resolution;

  vec4 height = shapeHeightMap(pos, frameThickness, maxHeight);

  vec2 dxy = height.r - vec2(
      shapeHeightMap(pos + vec2(step.x, 0.0), frameThickness, maxHeight).r,
      shapeHeightMap(pos + vec2(0.0, step.y), frameThickness, maxHeight).r
  );

  return vec4(normalize(vec3(dxy * scale / step, 1.)), height.a);
}

void main() {
    float frameThickness = 0.3; // TODO uniform
    float maxHeight = 0.5; // TODO uniform

    vec2 pos = gl_FragCoord.xy / uViewSize;

    vec4 normalMap = bumpFromDepth(pos, frameThickness, maxHeight, uViewSize, 0.1);

    vec3 uLightDirection = vec3(0.0, 1.0, 1.0);
    vec3 uEyeDirection = vec3(0.0, 0.0, 1.0);
    vec3 uVaryingColor = vec3(0.5, 0.5, 0.5);

    // process input parameters
    vec3 inverseLightDirection = uLightDirection * matrixTBN;
    vec3 inverseEyeDirection = uEyeDirection * matrixTBN;

    // diffuse component
    vec3 diffuseLightIntensity = vec3(0.5, 0.5, 0.5);
    float normalDotLight = max(0.0, dot(normalMap.rgb, inverseLightDirection));
    vec3 diffuseColor = normalDotLight * uVaryingColor * diffuseLightIntensity;

    fragColor = vec4(diffuseColor, 1);
}