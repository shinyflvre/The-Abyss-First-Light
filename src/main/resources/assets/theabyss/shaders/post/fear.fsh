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

float hash(float n) {
    return fract(sin(n * 127.1) * 43758.5453);
}

vec3 sampleSplit(vec2 uv, vec2 dir, float amount) {
    return vec3(texture(InSampler, uv + dir * amount).r, texture(InSampler, uv).g, texture(InSampler, uv - dir * amount).b);
}

void main() {
    vec4 state = texelFetch(StateSampler, ivec2(94, 0), 0);
    float intensity = state.r;
    float beat = state.g;
    float glitch = state.b;
    float hunt = state.a;
    float flash = texelFetch(StateSampler, ivec2(95, 0), 0).r;
    float t = GameTime * 1200.0;
    float aspect = OutSize.x / max(OutSize.y, 1.0);

    vec2 c = texCoord - 0.5;
    float r = length(vec2(c.x * aspect, c.y));

    vec2 uv = texCoord;
    float squeeze = (0.012 * beat + 0.02 * hunt * beat) * intensity;
    uv = 0.5 + c * (1.0 - squeeze * (1.0 - r));

    if (glitch > 0.01) {
        float band = floor(uv.y * 48.0);
        float frame = floor(t * 24.0);
        float tear = step(0.62, hash(band + frame * 7.3)) * (hash(band * 3.1 + frame) - 0.5);
        uv.x += tear * 0.08 * glitch;
    }

    vec2 dir = normalize(c + 1.0e-5) * r;
    float split = (0.002 + 0.007 * beat + 0.012 * hunt) * intensity + 0.025 * glitch;
    vec3 color = sampleSplit(uv, dir, split);

    float luma = dot(color, vec3(0.299, 0.587, 0.114));
    vec3 cold = vec3(luma) * vec3(0.82, 0.92, 1.08);
    color = mix(color, cold, 0.65 * intensity);
    float veil = texelFetch(StateSampler, ivec2(95, 0), 0).g;
    color = mix(color, cold, 0.35 * veil);
    color *= 1.0 - smoothstep(0.55, 1.15, r) * 0.2 * veil;

    float inner = mix(0.95, 0.42, intensity) - 0.07 * beat * intensity - 0.06 * hunt;
    float vignette = smoothstep(inner, inner + 0.6, r);
    color *= 1.0 - vignette * (0.6 + 0.3 * hunt) * intensity;

    float pulse = 0.55 + 0.45 * beat;
    color *= 1.0 - 0.45 * smoothstep(0.35, 1.0, vignette) * hunt * pulse * intensity;

    if (flash > 0.01) {
        vec3 flashColor = mix(vec3(0.55, 0.8, 0.86), vec3(0.92, 0.98, 1.0), smoothstep(0.75, 1.0, flash));
        color = mix(color, flashColor, flash * 0.45 * smoothstep(0.2, 0.75, r + 0.3 * flash));
        color = mix(color, vec3(dot(color, vec3(0.33))), flash * 0.3);
    }

    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
