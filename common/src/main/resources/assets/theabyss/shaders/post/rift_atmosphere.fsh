#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:globals.glsl>

// Rift of Sins world look (always on in the dimension, no config). One cheap pass, no noise (owner 16.10.2026: the
// structure is tiny, nobody walks through fog there - the smoke lives in the sky): drained ashen colours, soot haze
// with distance, heavy vignette, a little grain. Very bright, strongly saturated pixels keep their colour: the cyan
// outline of the Nightblade figure is drawn before this pass and must stay cyan (owner 17.10.2026).

uniform sampler2D InSampler;
uniform sampler2D DepthSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

const vec3 LUMA = vec3(0.299, 0.587, 0.114);
const vec3 SOOT = vec3(0.016, 0.016, 0.018);

void main() {
    float t = GameTime * 1200.0;
    vec3 color = texture(InSampler, texCoord).rgb;
    float depth = texture(DepthSampler, texCoord).r;

    float luma = dot(color, LUMA);
    float peak = max(color.r, max(color.g, color.b));
    float saturation = peak > 0.0 ? (peak - min(color.r, min(color.g, color.b))) / peak : 0.0;
    float keep = smoothstep(0.55, 0.75, saturation) * smoothstep(0.5, 0.7, peak);
    color = mix(color, vec3(luma) * vec3(0.97, 0.97, 1.0), 0.82 * (1.0 - keep));
    color *= mix(0.9, 1.0, keep);

    float haze = depth <= 1.0e-7 ? 0.0 : 1.0 - exp(-min(0.05 / depth, 4096.0) * 0.02);
    color = mix(color, SOOT, haze * 0.75);

    vec2 c = texCoord - 0.5;
    c.x *= OutSize.x / max(OutSize.y, 1.0);
    color *= 1.0 - smoothstep(0.32, 1.0, length(c)) * 0.6;
    color += (fract(sin(dot(gl_FragCoord.xy + fract(t) * 211.0, vec2(12.9898, 78.233))) * 43758.5453) - 0.5) * 0.014;
    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
