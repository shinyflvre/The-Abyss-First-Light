#version 330
#extension GL_ARB_separate_shader_objects : require

// Volumetric noise fog, composite: the scene seen through the fog (transmittance) plus the fog's own colour.
// The fog comes from a small target. Plain bilinear upscaling smeared it across depth edges - leaves in front of a far
// background got the background's fog in blocky seams and lost Minecraft's crisp edges (owner, 10.10.2026). So the
// upscale is depth aware (bilateral): of the 4 nearest fog texels, those whose scene depth matches this pixel count,
// those across an edge hardly at all. The depth of a fog texel is the one the march pass used: the full-resolution depth
// at the texel's centre. Costs 4 depth reads per pixel; the march stays small.

uniform sampler2D InSampler;
uniform sampler2D FogSampler;
uniform sampler2D DepthSampler;

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

// view distance from reversed-Z depth (sky = far)
float distanceAt(vec2 uv) {
    float depth = texture(DepthSampler, uv).r;
    return depth <= 1.0e-7 ? 4096.0 : min(0.05 / depth, 4096.0);
}

void main() {
    vec3 color = texture(InSampler, texCoord).rgb;
    float here = distanceAt(texCoord);

    vec2 size = vec2(textureSize(FogSampler, 0));
    vec2 st = texCoord * size - 0.5;
    vec2 base = floor(st);
    vec2 f = st - base;
    vec4 fog = vec4(0.0);
    float total = 0.0;
    for (int i = 0; i < 4; i++) {
        vec2 offset = vec2(float(i & 1), float(i >> 1));
        vec2 texel = clamp(base + offset, vec2(0.0), size - 1.0);
        float bilinear = (offset.x > 0.5 ? f.x : 1.0 - f.x) * (offset.y > 0.5 ? f.y : 1.0 - f.y);
        float there = distanceAt((texel + 0.5) / size);
        // relative depth difference: 1 on the same surface, ~0 across an edge
        float similar = 1.0 / (1.0 + 40.0 * abs(there - here) / max(here, 0.5));
        float weight = (bilinear + 1.0e-3) * similar;
        fog += texelFetch(FogSampler, ivec2(texel), 0) * weight;
        total += weight;
    }
    fog /= max(total, 1.0e-5);
    fragColor = vec4(clamp(color * (1.0 - fog.a) + fog.rgb, 0.0, 1.0), 1.0);
}
