#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:fog.glsl>

uniform sampler2D Sampler0;

layout(location = 0) in vec2 texCoord0;
layout(location = 1) in vec4 vertexColor;
layout(location = 2) in float sphericalVertexDistance;
layout(location = 3) in float cylindricalVertexDistance;

layout(location = 0) out vec4 fragColor;

vec3 toLinear(vec3 c) {
    return mix(c / 12.92, pow((c + 0.055) / 1.055, vec3(2.4)), step(0.04045, c));
}

vec3 toGamma(vec3 c) {
    return mix(c * 12.92, 1.055 * pow(c, vec3(1.0 / 2.4)) - 0.055, step(0.0031308, c));
}

void main() {
    if (texCoord0.x < 0.0 || texCoord0.x > 1.0 || texCoord0.y < 0.0 || texCoord0.y > 1.0) {
        discard;
    }
    vec4 texel = texture(Sampler0, texCoord0);
    float alpha = texel.a * vertexColor.a * vertexColor.a;
    if (alpha <= 0.0) {
        discard;
    }
    vec3 emitted = texel.rgb * toLinear(vertexColor.rgb) * float(TINT) * alpha;
    float cover = 1.0 - pow(1.0 - alpha, 1.0 / 2.2);
    float fog = 1.0 - total_fog_value(sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd,
        FogRenderDistanceStart, FogRenderDistanceEnd) * FogColor.a;
    fragColor = vec4(toGamma(min(emitted, vec3(1.0))) * fog, cover * fog);
}
