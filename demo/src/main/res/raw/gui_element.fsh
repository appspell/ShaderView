#version 300 es

precision mediump float;

uniform vec2 uViewSize;

in vec2 textureCoord;
out vec4 fragColor;

vec4 shapeHeightMap(in vec2 pos, in float frameThickness, in float maxHeight) {
    float defaultHeight = 0.5;

    float s1 = pos.x * pos.y * (1.0-pos.x) * (1.0-pos.y);
    float s2 = pow(frameThickness,4.0);

    float shape = min(1.0, s2 / s1);

    float alpha  = smoothstep(1.0, 0.0, shape);
    float height = smoothstep(maxHeight, 0.0, shape); // you may use mix() for better performance
    float normalizedHeight = normalize(height) * defaultHeight;

    return vec4(normalizedHeight, normalizedHeight, normalizedHeight, alpha);
}

void main()
{
    float frameThickness = 0.3; // TODO uniform
    float maxHeight = 0.5; // TODO uniform

    vec2 pos = gl_FragCoord.xy / uViewSize;

    vec4 heightMapColor = shapeHeightMap(pos, frameThickness, maxHeight);

    fragColor = heightMapColor;
}