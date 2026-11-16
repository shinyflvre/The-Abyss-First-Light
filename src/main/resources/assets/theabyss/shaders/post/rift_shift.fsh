#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:globals.glsl>

// Rift of Sins transition (key O, both directions). Pixel 86 of the fx state texture: R = intensity, G = 0 leaving /
// 1 arriving. Darker sibling of the Dream Shifter: no colours at all.
// Leaving: every colour drains away, the picture is torn into ragged horizontal shreds that slide apart, black soot
// billows in from the edges with a slow heavy heartbeat and swallows everything.
// Arriving: out of the black the smoke is ripped open in tatters, ash-pale torn edges, the new world shows through
// grey and trembling, the colours come back last.

uniform sampler2D InSampler;
uniform sampler2D StateSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

const vec3 LUMA = vec3(0.299, 0.587, 0.114);

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
    for (int i = 0; i < 5; i++) {
        value += amplitude * noise(p);
        p *= 2.03;
        amplitude *= 0.5;
    }
    return value;
}

void main() {
    vec4 state = texelFetch(StateSampler, ivec2(86, 0), 0);
    float e = state.r;
    float arriving = state.g;
    float t = GameTime * 1200.0;
    float aspect = OutSize.x / max(OutSize.y, 1.0);

    vec2 c = texCoord - 0.5;
    vec2 p = vec2(c.x * aspect, c.y);
    float r = length(p);
    float angle = atan(p.y, p.x);

    // slow, heavy heartbeat
    float phase = fract(t * 0.8);
    float beat = exp(-phase * 11.0) + 0.6 * exp(-max(phase - 0.24, 0.0) * 11.0) * step(0.24, phase);

    // torn shreds: ragged horizontal strips that slide apart sideways
    float strip = floor(texCoord.y * (14.0 + 10.0 * noise(vec2(t * 0.5, 3.0))) + noise(vec2(texCoord.x * 6.0, t * 0.3)) * 1.5);
    float slide = (hash(vec2(strip, floor(t * 6.0))) - 0.5) * 0.06 * e * e;
    vec2 shake = (vec2(noise(vec2(t * 29.0, 1.7)), noise(vec2(4.3, t * 29.0))) - 0.5) * 0.01 * e;
    vec2 uv = texCoord + vec2(slide, 0.0) + shake;
    uv = 0.5 + (uv - 0.5) * (1.0 - 0.06 * e * beat);
    vec3 scene = texture(InSampler, clamp(uv, 0.0, 1.0)).rgb;
    float luma = dot(scene, LUMA);
    scene = mix(scene, vec3(luma) * 0.85, clamp(e * 1.3, 0.0, 1.0));
    scene *= 1.0 - 0.35 * e;

    vec3 color;
    if (arriving < 0.5) {
        // soot billowing in from the edges, pulsing with the heartbeat
        float billow = fbm(vec2(angle * 2.2, r * 2.6 - t * 0.25) + fbm(p * 3.0 + t * 0.1));
        float edge = 1.5 - e * 1.75 - beat * 0.08 * e;
        float dark = smoothstep(edge - 0.3, edge + 0.05, r + (billow - 0.5) * 0.6 * (0.3 + e));
        float ashBand = dark * (1.0 - dark) * 4.0;
        color = scene + ashBand * vec3(0.09) * e;
        color = mix(color, vec3(0.0), dark);
    } else {
        // the black is ripped open in tatters, ash-pale torn edges
        float tatters = fbm(p * 2.4 + vec2(0.0, t * 0.08)) + (noise(p * 22.0 + t * 0.4) - 0.5) * 0.08;
        float open = (1.0 - e) * 1.15 - 0.05;
        float reveal = smoothstep(1.0 - open, 1.0 - open + 0.03, tatters + (1.0 - r) * 0.35);
        float rim = exp(-abs(tatters + (1.0 - r) * 0.35 - (1.0 - open)) * 60.0) * e;
        color = mix(vec3(0.0), scene, reveal);
        color += rim * vec3(0.32, 0.32, 0.34) * (0.6 + 0.4 * beat);
    }

    color *= 1.0 - smoothstep(0.3, 1.05, r) * 0.7 * e;
    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
