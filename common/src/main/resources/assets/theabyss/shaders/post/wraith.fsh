#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:globals.glsl>

uniform sampler2D InSampler;
uniform sampler2D StateSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

const vec3 SHADE = vec3(0.020, 0.013, 0.034);
const vec3 DUSK = vec3(0.075, 0.055, 0.115);
const vec3 VIOLET = vec3(0.205, 0.160, 0.300);

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + vec2(1.0, 0.0)), u.x), mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), u.x), u.y);
}

float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 4; i++) {
        value += amplitude * noise(p);
        p *= 2.03;
        amplitude *= 0.5;
    }
    return value;
}

void main() {
    vec3 col = texture(InSampler, texCoord).rgb;
    vec4 state = texelFetch(StateSampler, ivec2(87, 0), 0);
    float k = state.r;
    float pulse = state.g;
    if (k <= 0.001 && pulse <= 0.001) {
        fragColor = vec4(col, 1.0);
        return;
    }
    float t = GameTime * 1200.0;

    float lum = dot(col, vec3(0.299, 0.587, 0.114));
    vec3 cool = mix(vec3(lum), lum * vec3(0.80, 0.76, 1.0), 0.65);
    vec3 world = mix(col, cool, 0.5 * k) * mix(1.0, 0.84, k);
    world *= 1.0 - 0.22 * pulse;

    float cell = max(floor(OutSize.y / 150.0), 1.0);
    vec2 pixel = (floor(texCoord * OutSize / cell) + 0.5) * cell / OutSize;
    vec2 p = (pixel - 0.5) * 2.0;
    float r = length(p);
    float ang = atan(p.y, p.x);
    vec2 dir = vec2(cos(ang), sin(ang));
    float n1 = fbm(dir * 2.4 + vec2(r * 3.2 - t * 0.22, t * 0.05));
    float n2 = fbm(dir * 4.6 + vec2(-t * 0.09, r * 5.5 - t * 0.38) + 7.3);
    float inner = mix(0.78, 0.30, pulse * pulse);
    float density = smoothstep(inner, 1.38, r + (n1 - 0.5) * 0.55 + (n2 - 0.5) * 0.25) * max(k, pulse);
    float band = floor(density * 4.0 + 0.15) / 4.0;
    if (band > 0.0) {
        vec3 smoke = band <= 0.25 ? VIOLET : (band <= 0.5 ? DUSK : SHADE);
        float alpha = band <= 0.25 ? 0.38 : (band <= 0.5 ? 0.66 : 0.9);
        world = mix(world, smoke, alpha);
    }
    fragColor = vec4(world, 1.0);
}
