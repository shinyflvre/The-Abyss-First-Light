#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:globals.glsl>

uniform sampler2D InSampler;
uniform sampler2D DepthSampler;
uniform sampler2D StateSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

const vec3 PALE = vec3(0.60, 0.66, 0.70);
const vec3 CRYSTAL = vec3(0.016, 0.031, 0.047);
const vec3 RIM = vec3(0.22, 0.77, 0.87);

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float facets(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    float best = 8.0;
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 o = vec2(float(x), float(y));
            vec2 c = o + vec2(hash(i + o), hash(i + o + 7.1));
            vec2 d = abs(f - c);
            best = min(best, max(d.x, d.y) + 0.5 * min(d.x, d.y));
        }
    }
    return best;
}

void main() {
    vec4 state = texelFetch(StateSampler, ivec2(93, 0), 0);
    float stage = state.r;
    float pulse = state.g;
    float collapse = state.b;
    if (stage <= 0.001 && collapse <= 0.001) {
        fragColor = texture(InSampler, texCoord);
        return;
    }
    vec2 uv = 0.5 + (texCoord - 0.5) * (1.0 - 0.012 * pulse * max(stage, 0.3));
    vec3 col = texture(InSampler, uv).rgb;
    float depth = texture(DepthSampler, uv).r;

    float lum = dot(col, vec3(0.299, 0.587, 0.114));
    vec3 pale = mix(vec3(lum), lum * vec3(0.80, 0.88, 0.95) + vec3(0.05, 0.06, 0.07), 0.8);
    float hi = max(col.g, col.b);
    float cyan = smoothstep(0.12, 0.32, hi - col.r) * smoothstep(0.3, 0.55, hi);
    float fade = clamp(stage * 0.85 + collapse * 0.15, 0.0, 1.0);
    vec3 world = mix(col, pale, fade * (1.0 - 0.85 * cyan));

    float dist = depth <= 1.0e-7 ? 1000.0 : 0.05 / depth;
    float reach = mix(140.0, 14.0, stage);
    float far = smoothstep(reach * 0.35, reach, dist) * stage;
    world = mix(world, PALE, far * 0.85 * (1.0 - 0.6 * cyan));

    vec2 p = (texCoord - 0.5) * vec2(OutSize.x / OutSize.y, 1.0) * 2.0;
    float r = length(p);
    float dark = smoothstep(0.75, 1.5, r) * (0.18 * stage + 0.22 * pulse * stage);
    world *= 1.0 - dark;

    if (collapse > 0.001) {
        float cell = max(floor(OutSize.y / 120.0), 1.0);
        vec2 pix = (floor(texCoord * OutSize / cell) + 0.5) * cell / OutSize;
        vec2 q = (pix - 0.5) * vec2(OutSize.x / OutSize.y, 1.0) * 2.0;
        float edge = length(q) + (facets(pix * vec2(OutSize.x / OutSize.y, 1.0) * 9.0) - 0.4) * 0.45;
        float front = 2.1 - collapse * 2.4;
        float inside = step(front, edge);
        float rim = step(front - 0.05, edge) - inside;
        world = mix(world, RIM * 0.8, rim * (1.0 - collapse * 0.5));
        world = mix(world, CRYSTAL, inside);
        world *= 1.0 - smoothstep(0.85, 1.0, collapse);
    }
    fragColor = vec4(world, 1.0);
}
