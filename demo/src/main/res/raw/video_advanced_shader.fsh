#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require

precision mediump float;

in vec2 textureCoord;
out vec4 fragColor;

/**
 original code from here https://gist.github.com/maxatwork/e28c442c436fc7793945d8ee8c063b6a
*/

uniform samplerExternalOES uVideoTexture;
uniform float progress;
uniform vec2 resolution;
uniform float fps;

//
// GLSL textureless classic 3D noise "cnoise",
// with an RSL-style periodic variant "pnoise".
// Author:  Stefan Gustavson (stefan.gustavson@liu.se)
// Version: 2011-10-11
//
// Many thanks to Ian McEwan of Ashima Arts for the
// ideas for permutation and gradient selection.
//
// Copyright (c) 2011 Stefan Gustavson. All rights reserved.
// Distributed under the MIT license. See LICENSE file.
// https://github.com/stegu/webgl-noise

vec3 mod289(vec3 x)
{
    return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 mod289(vec4 x)
{
    return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 permute(vec4 x)
{
    return mod289(((x*34.0)+1.0)*x);
}

vec4 taylorInvSqrt(vec4 r)
{
    return 1.79284291400159 - 0.85373472095314 * r;
}

vec3 fade(vec3 t) {
    return t*t*t*(t*(t*6.0-15.0)+10.0);
}

// Classic Perlin noise
float cnoise(vec3 P)
{
    vec3 Pi0 = floor(P);// Integer part for indexing
    vec3 Pi1 = Pi0 + vec3(1.0);// Integer part + 1
    Pi0 = mod289(Pi0);
    Pi1 = mod289(Pi1);
    vec3 Pf0 = fract(P);// Fractional part for interpolation
    vec3 Pf1 = Pf0 - vec3(1.0);// Fractional part - 1.0
    vec4 ix = vec4(Pi0.x, Pi1.x, Pi0.x, Pi1.x);
    vec4 iy = vec4(Pi0.yy, Pi1.yy);
    vec4 iz0 = Pi0.zzzz;
    vec4 iz1 = Pi1.zzzz;

    vec4 ixy = permute(permute(ix) + iy);
    vec4 ixy0 = permute(ixy + iz0);
    vec4 ixy1 = permute(ixy + iz1);

    vec4 gx0 = ixy0 * (1.0 / 7.0);
    vec4 gy0 = fract(floor(gx0) * (1.0 / 7.0)) - 0.5;
    gx0 = fract(gx0);
    vec4 gz0 = vec4(0.5) - abs(gx0) - abs(gy0);
    vec4 sz0 = step(gz0, vec4(0.0));
    gx0 -= sz0 * (step(0.0, gx0) - 0.5);
    gy0 -= sz0 * (step(0.0, gy0) - 0.5);

    vec4 gx1 = ixy1 * (1.0 / 7.0);
    vec4 gy1 = fract(floor(gx1) * (1.0 / 7.0)) - 0.5;
    gx1 = fract(gx1);
    vec4 gz1 = vec4(0.5) - abs(gx1) - abs(gy1);
    vec4 sz1 = step(gz1, vec4(0.0));
    gx1 -= sz1 * (step(0.0, gx1) - 0.5);
    gy1 -= sz1 * (step(0.0, gy1) - 0.5);

    vec3 g000 = vec3(gx0.x, gy0.x, gz0.x);
    vec3 g100 = vec3(gx0.y, gy0.y, gz0.y);
    vec3 g010 = vec3(gx0.z, gy0.z, gz0.z);
    vec3 g110 = vec3(gx0.w, gy0.w, gz0.w);
    vec3 g001 = vec3(gx1.x, gy1.x, gz1.x);
    vec3 g101 = vec3(gx1.y, gy1.y, gz1.y);
    vec3 g011 = vec3(gx1.z, gy1.z, gz1.z);
    vec3 g111 = vec3(gx1.w, gy1.w, gz1.w);

    vec4 norm0 = taylorInvSqrt(vec4(dot(g000, g000), dot(g010, g010), dot(g100, g100), dot(g110, g110)));
    g000 *= norm0.x;
    g010 *= norm0.y;
    g100 *= norm0.z;
    g110 *= norm0.w;
    vec4 norm1 = taylorInvSqrt(vec4(dot(g001, g001), dot(g011, g011), dot(g101, g101), dot(g111, g111)));
    g001 *= norm1.x;
    g011 *= norm1.y;
    g101 *= norm1.z;
    g111 *= norm1.w;

    float n000 = dot(g000, Pf0);
    float n100 = dot(g100, vec3(Pf1.x, Pf0.yz));
    float n010 = dot(g010, vec3(Pf0.x, Pf1.y, Pf0.z));
    float n110 = dot(g110, vec3(Pf1.xy, Pf0.z));
    float n001 = dot(g001, vec3(Pf0.xy, Pf1.z));
    float n101 = dot(g101, vec3(Pf1.x, Pf0.y, Pf1.z));
    float n011 = dot(g011, vec3(Pf0.x, Pf1.yz));
    float n111 = dot(g111, Pf1);

    vec3 fade_xyz = fade(Pf0);
    vec4 n_z = mix(vec4(n000, n100, n010, n110), vec4(n001, n101, n011, n111), fade_xyz.z);
    vec2 n_yz = mix(n_z.xy, n_z.zw, fade_xyz.y);
    float n_xyz = mix(n_yz.x, n_yz.y, fade_xyz.x);
    return 2.2 * n_xyz;
}

vec4 grayscale(vec4 c) {
    float aColor = 0.21 * c.r + 0.72 * c.g + 0.07 * c.b;
    return vec4(aColor, aColor, aColor, c.a);
}

vec4 adjust(vec4 c, float brightness, float contrast) {
    vec4 result = c.rgba;
    result.rgb /= result.a;
    result.rgb = ((result.rgb - 0.5) * max(contrast, .0)) + 0.5;
    result.rgb += brightness;
    result.rgb *= result.a;
    result.r = min(result.r, 1.);
    result.g = min(result.g, 1.);
    result.b = min(result.b, 1.);

    return result;
}

vec3 blendMultiply(vec3 base, vec3 blend) {
    return base*blend;
}

vec3 blendMultiply(vec3 base, vec3 blend, float opacity) {
    return (blendMultiply(base, blend) * opacity + base * (1.0 - opacity));
}

float rand(vec2 co){
    return fract(sin(dot(co.xy, vec2(12.9898, 78.233))) * 43758.5453);
}

vec4 vignette(vec4 c, float radius, float softness) {
    vec4 result = c;
    vec2 position = (textureCoord.xy / resolution.xy) - vec2(0.5);
    float len = length(position);
    float vignette = smoothstep(radius, radius - softness, len);
    result.rgb = mix(c.rgb, c.rgb * vignette, 0.5);
    return result;
}

void main() {
    float spf = 1000. / (fps == 0. ? 18. : fps);
    float real_time = float(int(progress * 1000.));
    float time = float(int(real_time / spf)) * spf;
    vec2 p = textureCoord.xy / resolution.xy;

    float brightness = .2 - rand(vec2(time, 0)) * 0.1;
    float contrast = 2.;

    vec4 pixelColor = texture(uVideoTexture, p);

    fragColor = grayscale(adjust(pixelColor, brightness, contrast));

    float noise = .5 - cnoise(vec3(p.x*100., p.y*.01, time));
    vec4 scratchColor = adjust(vec4(noise, noise, noise, 1.), 1., .9);

    noise = cnoise(vec3(p * 8., time*rand(vec2(time, 0))));
    noise = noise > .8 ? 1. : 0.;
    vec4 dustColor = adjust(vec4(noise, noise, noise, noise), -1.0, .5);

    vec4 overlayColor = mix(dustColor, scratchColor, .8);

    fragColor = vec4(blendMultiply(adjust(vignette(grayscale(texture(uVideoTexture, p)), 0.9, 0.5), brightness, contrast).rgb, overlayColor.rgb, .9), 1.);
}