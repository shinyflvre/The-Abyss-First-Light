#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;
uniform sampler2D DepthSampler;
uniform sampler2D StateSampler;
uniform sampler2D LightsSampler;

#include <theabyss:fx_camera.glsl>

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

const vec3 LUMA = vec3(0.299, 0.587, 0.114);
const float STRENGTH = 1.25;

vec2 unpack16(vec4 texel) {
    vec4 b = floor(texel * 255.0 + 0.5);
    return vec2(b.r * 256.0 + b.g, b.b * 256.0 + b.a) / 65535.0;
}

vec3 positionAt(vec2 uv) {
    return fxPosition(uv, texture(DepthSampler, uv).r);
}

void main() {
    vec3 color = texture(InSampler, texCoord).rgb;
    float depth = texture(DepthSampler, texCoord).r;
    int count = int(texelFetch(LightsSampler, ivec2(0, 0), 0).r * 255.0 + 0.5);
    if (depth <= 1.0e-7 || count == 0) {
        fragColor = vec4(color, 1.0);
        return;
    }
    fxCameraSetup();
    vec3 position = fxPosition(texCoord, depth);
    vec2 texel = 1.0 / vec2(textureSize(DepthSampler, 0));
    vec3 left = positionAt(texCoord - vec2(texel.x, 0.0));
    vec3 right = positionAt(texCoord + vec2(texel.x, 0.0));
    vec3 down = positionAt(texCoord - vec2(0.0, texel.y));
    vec3 up = positionAt(texCoord + vec2(0.0, texel.y));
    vec3 dx = distance(right, position) < distance(position, left) ? right - position : position - left;
    vec3 dy = distance(up, position) < distance(position, down) ? up - position : position - down;
    vec3 normal = normalize(cross(dx, dy));
    if (dot(normal, position - fxEye) > 0.0) {
        normal = -normal;
    }
    vec3 light = vec3(0.0);
    for (int i = 0; i < count; i++) {
        int p = 1 + i * 3;
        vec2 xy = unpack16(texelFetch(LightsSampler, ivec2(p, 0), 0)) * 256.0 - 128.0;
        vec4 second = texelFetch(LightsSampler, ivec2(p + 1, 0), 0);
        float z = unpack16(vec4(second.rg, 0.0, 0.0)).x * 256.0 - 128.0;
        float reach = second.b * 255.0 / 8.0;
        vec3 to = vec3(xy, z) - position;
        float d = length(to);
        if (d >= reach) {
            continue;
        }
        float falloff = 1.0 - d / reach;
        float facing = clamp(dot(normal, to / max(d, 0.001)) * 0.75 + 0.25, 0.0, 1.0);
        light += texelFetch(LightsSampler, ivec2(p + 2, 0), 0).rgb * second.a * STRENGTH * falloff * falloff * facing;
    }
    float strength = max(light.r, max(light.g, light.b));
    if (strength > 0.002) {
        vec3 hue = clamp(light / strength, 0.0, 1.0);
        float luma = dot(color, LUMA);
        vec3 tinted = hue * (luma / max(dot(hue, LUMA), 0.3));
        float boost = strength * 1.25;
        color = mix(color, clamp(tinted, 0.0, 1.0), min(1.0, boost));
        color += color * hue * min(boost, 2.5) * 1.5 * (1.0 - color) + hue * min(boost, 2.5) * 0.06;
    }
    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
