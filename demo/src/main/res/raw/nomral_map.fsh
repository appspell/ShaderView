#version 300 es

precision mediump float;

uniform vec3 uColor;
uniform vec3 uVaryingColor;
uniform vec3 uLightDirection;
uniform vec3 uEyeDirection;
uniform sampler2D uNormalTexture;

in vec2 textureCoord;
in mat3 matrixTBN;

out vec4 fragColor;

void main() {
    // process input parameters
    vec3 inverseLightDirection = uLightDirection * matrixTBN;
    vec3 inverseEyeDirection = uEyeDirection * matrixTBN;

    vec3 normal = texture(uNormalTexture, textureCoord).xyz;
    normal = normalize(normal * 2.0 -1.0);

    // diffuse component
    vec3 diffuseLightIntensity = vec3(1.0, 1.0, 1.0);
    float normalDotLight = max(0.0, dot(normal, inverseLightDirection));
    vec3 diffuseColor = normalDotLight * uVaryingColor *diffuseLightIntensity;

    // ambient component
    vec3 ambientLightIntensity = vec3(0.1, 0.1, 0.1);
    vec3 ambientColor = ambientLightIntensity * uVaryingColor;

    // specular component
    vec3 specularLightIntensity = vec3(1.0, 1.0, 1.0);
    vec3 vertexSpecularReflectionConstant = vec3(1.0, 1.0, 1.0);
    float shininess = 2.0;// you can move it to uniform
    vec3 lightReflectionDirection = reflect(vec3(0) - inverseLightDirection, normal);
    float normalDotReflection = max(0.0, dot(inverseEyeDirection, lightReflectionDirection));
    vec3 specularColor = pow(normalDotReflection, shininess) * vertexSpecularReflectionConstant * specularLightIntensity;

    vec3 color = uColor + diffuseColor + ambientColor + specularColor;
    // output colour should be in between 0 and 1
    clamp(color, 0.0, 1.0);

    fragColor = vec4(color, 1.0);
}