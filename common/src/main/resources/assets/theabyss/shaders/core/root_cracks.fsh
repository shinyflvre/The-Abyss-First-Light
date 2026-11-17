#version 150

#moj_import <fog.glsl>

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform int FogShape;
uniform float GameTime;
uniform int Pass;

in vec4 vertexColor;
in vec3 worldPos;
in vec3 viewPos;

out vec4 fragColor;

const vec3 CORE = vec3(0.86, 1.0, 0.98);
const vec3 GLOW = vec3(0.16, 0.88, 1.0);
const vec3 DEEP = vec3(0.30, 0.38, 1.0);

float hash(ivec2 c, uint seed) {
    uint h = uint(c.x) * 0x8da6b343u ^ uint(c.y) * 0xd8163841u ^ seed * 0xcb1ab31fu;
    h ^= h >> 16u;
    h *= 0x7feb352du;
    h ^= h >> 15u;
    h *= 0x846ca68bu;
    h ^= h >> 16u;
    return float(h) / 4294967295.0;
}

float vnoise(vec2 p, uint seed) {
    ivec2 i = ivec2(floor(p));
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i, seed), hash(i + ivec2(1, 0), seed), u.x), mix(hash(i + ivec2(0, 1), seed), hash(i + ivec2(1, 1), seed), u.x), u.y);
}

void main() {
    vec2 texel = (floor(worldPos.xz * 16.0) + 0.5) / 16.0;
    float t = GameTime * 1200.0;
    float flow = vnoise(texel * 0.22 + vec2(t * 0.35, -t * 0.21), 41u);
    float energy = (0.62 + 0.55 * flow) * (0.92 + 0.08 * sin(t * 1.1));
    vec3 light = mix(DEEP, GLOW, 0.65 + 0.35 * flow) * energy;
    float fog = (1.0 - linear_fog_fade(fog_distance(viewPos, FogShape), FogStart, FogEnd)) * FogColor.a;
    float k = ColorModulator.a * (1.0 - fog);
    float value = vertexColor.r;

    if (Pass == 0) {
        fragColor = vec4(0.0, 0.0, 0.0, 0.6 * k);
    } else if (Pass == 1) {
        fragColor = vec4(light * 1.7 * value * k, 1.0);
    } else if (Pass == 2) {
        fragColor = vec4(light * 0.22 * value * k, 1.0);
    } else {
        vec3 inner;
        float alpha;
        if (vertexColor.b > 0.5) {
            inner = GLOW * (0.55 + 0.45 * energy);
            alpha = 0.85 * value;
        } else {
            inner = mix(GLOW, CORE, value * energy) * (0.8 + 0.5 * energy);
            alpha = 1.0;
        }
        inner = mix(inner, FogColor.rgb, fog);
        fragColor = vec4(inner, alpha * ColorModulator.a);
    }
}
