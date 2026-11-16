#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:globals.glsl>

uniform sampler2D InSampler;
uniform sampler2D DepthSampler;
uniform sampler2D StateSampler;

#include <theabyss:fx_camera.glsl>

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

const vec3 CYAN = vec3(0.28, 0.9, 1.0);
const vec3 SOUL = vec3(0.86, 1.0, 1.0);
const int WAVES = 4;

float hash12(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

void main() {
    fxCameraSetup();
    vec3 body = vec3(fxFloat(160), fxFloat(161), fxFloat(162));
    float strength = fxFloat(163);
    float restore = fxFloat(164);
    float beat = fxFloat(173);
    float death = fxFloat(174);
    float t = GameTime * 1200.0;

    float depth = texture(DepthSampler, texCoord).r;
    bool sky = depth <= 1.0e-7;
    vec3 ray = fxViewRay(texCoord);
    vec3 dir = normalize(ray);
    vec3 pos = sky ? fxEye + dir * 4096.0 : fxPosition(texCoord, depth);
    float sceneDist = sky ? 1.0e6 : length(pos - fxEye);
    float dBody = sky ? 1.0e6 : length(pos - body);

    float band = 0.0;
    float front = 0.0;
    float shell = 0.0;
    vec3 oc = fxEye - body;
    for (int i = 0; i < WAVES; i++) {
        float radius = fxFloat(165 + i * 2);
        float k = fxFloat(166 + i * 2);
        if (radius <= 0.0 || k <= 0.0) {
            continue;
        }
        float fade = k * (1.0 - smoothstep(170.0, 260.0, radius)) * (0.55 + 0.45 * exp(-radius / 40.0));
        float x = dBody - radius;
        float sharp = exp(-x * x / 0.8);
        float trail = x < 0.0 ? exp(x / 5.0) * 0.32 : 0.0;
        band += (sharp + trail) * fade;
        front += sharp * fade;
        float b = dot(oc, dir);
        float c = dot(oc, oc) - radius * radius;
        float h = b * b - c;
        if (h > 0.0) {
            float s = sqrt(h);
            for (int j = 0; j < 2; j++) {
                float hit = j == 0 ? -b - s : -b + s;
                if (hit > 0.05 && hit < sceneDist) {
                    vec3 n = normalize(oc + dir * hit);
                    float graze = 1.0 - abs(dot(n, dir));
                    shell += fade * pow(graze, 2.6) * 0.55;
                }
            }
        }
    }

    vec2 wobble = vec2(sin(dBody * 1.7 - t * 6.0), cos(dBody * 1.3 + t * 5.0));
    vec2 uv = texCoord + wobble * front * 0.004 * strength;
    vec3 color = texture(InSampler, uv).rgb;

    float luma = dot(color, vec3(0.2126, 0.7152, 0.0722));
    luma = pow(clamp(luma, 0.0, 1.0), 1.08);
    vec3 grey = vec3(luma) * vec3(0.95, 1.0, 1.04);
    float restoreMask = restore > 0.0 ? 1.0 - smoothstep(restore - 7.0, restore, dBody) : 0.0;
    vec3 result = mix(color, grey, strength * (1.0 - restoreMask));

    float aspect = OutSize.x / max(OutSize.y, 1.0);
    vec2 centred = texCoord - 0.5;
    float r = length(vec2(centred.x * aspect, centred.y));
    float grain = (hash12(texCoord * OutSize + fract(t * 0.37) * 911.0) - 0.5) * 0.045;
    result += grain * strength * (1.0 - restoreMask);
    float vignette = smoothstep(0.32 - 0.07 * beat, 1.05, r);
    result *= 1.0 - vignette * (0.5 + 0.25 * beat) * strength * (1.0 - restoreMask);

    float w = clamp(band, 0.0, 1.6);
    vec3 waveColour = mix(CYAN, SOUL, smoothstep(0.55, 1.25, w));
    result += waveColour * (w * 0.6 + shell) * strength;

    if (restore > 0.0) {
        float edge = dBody - restore;
        result += SOUL * exp(-edge * edge / 5.0) * 0.7 * strength;
    }

    vec3 centre = body + vec3(0.0, 0.9, 0.0);
    float along = clamp(dot(centre - fxEye, dir), 0.0, sceneDist);
    float miss = length(fxEye + dir * along - centre);
    float halo = exp(-miss * miss / 1.6) * 0.55 + exp(-miss / 3.5) * 0.12;
    float glowOnSurface = sky ? 0.0 : exp(-pow(max(dBody - 0.5, 0.0), 2.0) / 1.4) * 0.45;
    float pulse = 0.65 + 0.35 * beat;
    result += mix(CYAN, SOUL, 0.6) * (halo + glowOnSurface) * pulse * strength;

    vec2 o2 = fxEye.xz - body.xz;
    vec2 d2 = dir.xz;
    float dd = dot(d2, d2);
    float lineT = dd > 1.0e-6 ? clamp(-dot(o2, d2) / dd, 0.0, sceneDist) : 0.0;
    vec3 onRay = fxEye + dir * lineT;
    float lateral = length(onRay.xz - body.xz);
    float height = onRay.y - body.y;
    float beam = exp(-lateral * lateral / 0.3) * smoothstep(-0.5, 1.5, height) * (1.0 - smoothstep(18.0, 96.0, height));
    beam += exp(-lateral / 1.6) * 0.18 * smoothstep(-0.5, 1.5, height) * (1.0 - smoothstep(10.0, 70.0, height));
    result += mix(CYAN, SOUL, 0.35) * beam * 0.38 * pulse * strength;

    vec3 bodyView = transpose(fxRotation) * (body - fxEye);
    vec2 towards = bodyView.xy;
    bool behind = bodyView.z > 0.0;
    if (behind) {
        towards = vec2(abs(bodyView.x) > 1.0e-3 ? sign(bodyView.x) : 1.0, 0.0);
    }
    towards = normalize(towards + vec2(1.0e-5, 0.0));
    vec2 projected = behind ? vec2(2.0) : bodyView.xy / max(-bodyView.z, 1.0e-3) * fxProjection;
    float offScreen = smoothstep(0.75, 1.15, max(abs(projected.x), abs(projected.y)));
    vec2 screenDir = normalize(vec2(centred.x * aspect, centred.y) + vec2(1.0e-5, 0.0));
    float side = pow(max(dot(screenDir, towards), 0.0), 3.0);
    float rim = smoothstep(0.42, 0.85, r);
    result += CYAN * rim * side * beat * offScreen * 0.75 * strength;

    vec3 flashColour = vec3(0.9, 1.0, 1.0);
    result = mix(result, flashColour, death * death * 0.7);

    fragColor = vec4(clamp(result, 0.0, 1.0), 1.0);
}
