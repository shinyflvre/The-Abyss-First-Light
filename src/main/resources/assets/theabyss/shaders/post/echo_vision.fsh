#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:globals.glsl>

// Echo Vision (Ring of Silent Echoes): the player walks as the echo. fx state pixel 175: R intensity, G leash edge
// (how close the body's limit is), B the flash of crossing over.

uniform sampler2D InSampler;
uniform sampler2D StateSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

const vec3 PEARL = vec3(0.910, 0.886, 0.984);
const vec3 LILAC = vec3(0.706, 0.486, 1.000);
const vec3 DEEP = vec3(0.055, 0.035, 0.100);

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
    vec4 state = texelFetch(StateSampler, ivec2(175, 0), 0);
    float k = state.r;
    float edge = state.g;
    float flash = state.b;
    if (k <= 0.001 && flash <= 0.001) {
        fragColor = vec4(col, 1.0);
        return;
    }
    float t = GameTime * 1200.0;
    vec2 p = (texCoord - 0.5) * vec2(OutSize.x / max(OutSize.y, 1.0), 1.0);
    float r = length(p);

    // a faint second picture drifting beside the first: the world seen twice
    float drift = 0.0035 + 0.010 * edge;
    vec2 offset = vec2(cos(t * 0.6), sin(t * 0.45)) * drift;
    vec3 ghost = texture(InSampler, texCoord + offset).rgb;
    col = mix(col, max(col, ghost), 0.22 * k + 0.25 * edge);

    // silence: the colour drains, the shadows lean to lilac
    float lum = dot(col, vec3(0.299, 0.587, 0.114));
    vec3 still = mix(vec3(lum), lum * vec3(0.92, 0.88, 1.06), 0.7);
    col = mix(col, still, 0.38 * k);
    col += LILAC * (1.0 - smoothstep(0.0, 0.35, lum)) * 0.035 * k;

    // echo rings rolling out from the middle of the view, faster near the end of the leash
    float period = mix(2.6, 1.1, edge);
    float phase = fract(t / period);
    float front = phase * 1.1;
    float strength = (1.0 - phase) * (0.35 + 0.65 * edge);
    float ring = exp(-pow((r - front) / 0.012, 2.0)) + 0.5 * exp(-pow((r - front * 0.6) / 0.008, 2.0));
    col += PEARL * ring * strength * 0.10 * k;

    // the leash: the edges close in and fray as the body falls behind
    float cell = max(floor(OutSize.y / 150.0), 1.0);
    vec2 pixel = (floor(texCoord * OutSize / cell) + 0.5) * cell / OutSize;
    vec2 q = (pixel - 0.5) * 2.0;
    float rq = length(q);
    float ang = atan(q.y, q.x);
    vec2 dir = vec2(cos(ang), sin(ang));
    float n = fbm(dir * 2.6 + vec2(rq * 3.0 - t * 0.25, t * 0.07));
    float inner = mix(1.05, 0.55, edge);
    float veil = smoothstep(inner, 1.45, rq + (n - 0.5) * 0.5) * k;
    float band = floor(veil * 4.0 + 0.15) / 4.0;
    if (band > 0.0) {
        vec3 smoke = band <= 0.25 ? mix(DEEP, LILAC, 0.35) : (band <= 0.5 ? mix(DEEP, LILAC, 0.15) : DEEP);
        float alpha = band <= 0.25 ? 0.30 : (band <= 0.5 ? 0.55 : 0.82);
        col = mix(col, smoke, alpha * (0.55 + 0.45 * edge));
    }
    col *= 1.0 - 0.10 * edge * step(0.85, noise(vec2(t * 6.0, 1.7)));

    // the moment of crossing over: a pearl breath
    col = mix(col, PEARL, flash * flash * 0.45);
    fragColor = vec4(clamp(col, 0.0, 1.0), 1.0);
}
