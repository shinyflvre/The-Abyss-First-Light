#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:globals.glsl>

// Stopped time (Ring/Totem of Time, values from TimeStopClient via fx state pixels 26..30).
// A sphere of stopped time around the place of the spell: inside it the world loses its colour (cold, still, a breath of
// blue left), dust hangs motionless in the air and a frozen ring of clock marks rests at the edge of the view. Its front
// is a glassy shell - visible where it cuts through the air, bending the image and leaving a cold rim where it sweeps
// over the ground. The moment of the stop is a short jolt (chromatic split).
// World positions from the depth buffer with the exact camera (fx_camera.glsl), reversed-Z depth.

uniform sampler2D InSampler;
uniform sampler2D DepthSampler;
uniform sampler2D StateSampler;

#include <theabyss:fx_camera.glsl>

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

const vec3 LUMA = vec3(0.299, 0.587, 0.114);
const vec3 COLD = vec3(0.62, 0.86, 1.0);
const float SKY_DISTANCE = 290.0;

vec2 unpack16(vec4 texel) {
    vec4 b = floor(texel * 255.0 + 0.5);
    return vec2(b.r * 256.0 + b.g, b.b * 256.0 + b.a) / 65535.0;
}

float hash(vec3 p) {
    p = fract(p * 0.3183099 + 0.1);
    p *= 17.0;
    return fract(p.x * p.y * p.z * (p.x + p.y + p.z));
}

// the colour of stopped time: desaturated, cold, slightly harder
vec3 stillColour(vec3 c) {
    float l = dot(c, LUMA);
    vec3 grey = vec3(l) * vec3(0.90, 0.96, 1.07);
    vec3 still = mix(grey, c, 0.10);
    return clamp((still - 0.3) * 1.03 + 0.3, 0.0, 1.0);
}

void main() {
    fxCameraSetup();
    vec2 radiusStrength = unpack16(texelFetch(StateSampler, ivec2(26, 0), 0));
    vec2 frontShock = unpack16(texelFetch(StateSampler, ivec2(27, 0), 0));
    float radius = radiusStrength.x * 512.0;
    float strength = radiusStrength.y;
    float front = frontShock.x;
    float shock = frontShock.y;
    vec3 centre = vec3(fxFloat(28), fxFloat(29), fxFloat(30));
    float aspect = ScreenSize.x / max(ScreenSize.y, 1.0);

    float depth = texture(DepthSampler, texCoord).r;
    vec3 ray = fxViewRay(texCoord);
    vec3 dir = normalize(ray);
    bool sky = depth <= 1.0e-7;
    float sceneDistance = sky ? SKY_DISTANCE : length(ray * (0.05 / depth));
    vec3 position = fxEye + dir * sceneDistance;

    // ---- the shell of the sphere along the view ray (ray/sphere intersection in front of the scene)
    vec3 oc = fxEye - centre;
    float b = dot(oc, dir);
    float h = b * b - (dot(oc, oc) - radius * radius);
    float shell = 0.0;
    if (h > 0.0 && front > 0.0) {
        float root = sqrt(h);
        for (int i = 0; i < 2; i++) {
            float t = i == 0 ? -b - root : -b + root;
            if (t > 0.3 && t < sceneDistance) {
                vec3 normal = normalize(oc + dir * t);
                // like a soap bubble: bright where the shell is seen edge-on
                shell += pow(1.0 - abs(dot(normal, dir)), 4.0);
            }
        }
    }

    // ---- where the front sweeps over surfaces
    float edge = distance(position, centre) - radius;
    float sweep = exp(-edge * edge / 2.0) * front;
    float inside = 1.0 - smoothstep(-1.5, 1.5, edge);

    // ---- bend the image at the front, split the colours at the moment of the stop
    vec2 fromCentre = texCoord - 0.5;
    vec2 bend = fromCentre * (sweep * 0.012 + shell * front * 0.008);
    vec2 split = fromCentre * shock * 0.014;
    vec3 colour = vec3(
        texture(InSampler, texCoord - bend + split).r,
        texture(InSampler, texCoord - bend).g,
        texture(InSampler, texCoord - bend - split).b
    );

    // ---- inside: stopped time
    colour = mix(colour, stillColour(colour), inside * strength);

    // ---- dust hanging still in the air (world anchored, only inside the sphere, only in front of the scene)
    if (strength > 0.01) {
        ivec3 block = ((CameraBlockPos % 4096) + 4096) % 4096;
        vec3 cameraWorld = vec3(block) - CameraOffset + fxEye;
        float dust = 0.0;
        for (int i = 0; i < 14; i++) {
            float t = 0.9 + float(i) * 0.85;
            if (t > sceneDistance) {
                break;
            }
            vec3 world = cameraWorld + dir * t;
            vec3 cell = floor(world * 1.2);
            float pick = hash(cell);
            if (pick > 0.86) {
                vec3 speck = (cell + vec3(hash(cell + 1.7), hash(cell + 4.1), hash(cell + 8.3))) / 1.2;
                vec3 toSpeck = speck - cameraWorld;
                float along = dot(toSpeck, dir);
                if (along > 0.4 && along < sceneDistance) {
                    float across = length(toSpeck - dir * along);
                    float size = 0.004 * along + 0.006;
                    float inSphere = 1.0 - smoothstep(-1.0, 1.0, distance(speck - cameraWorld + fxEye, centre) - radius);
                    dust += smoothstep(size, 0.0, across) * (0.35 + 0.65 * fract(pick * 37.0)) * inSphere * smoothstep(12.5, 6.0, along);
                }
            }
        }
        colour += COLD * min(dust, 1.0) * 0.55 * strength;
    }

    // ---- the front: a cold rim on the surfaces and the glassy shell in the air
    colour += COLD * (sweep * 0.16 + shell * front * 0.2) * (1.0 - 0.5 * dot(colour, LUMA));

    // ---- frozen clock marks and a quiet vignette at the edge of the view
    vec2 p = fromCentre * vec2(aspect, 1.0);
    float r = length(p);
    float angle = atan(p.y, p.x) / 6.28318530718 + 0.5;
    float minute = abs(fract(angle * 60.0 + 0.5) - 0.5) / 60.0 * 6.28318530718 * r;
    float hour = abs(fract(angle * 12.0 + 0.5) - 0.5) / 12.0 * 6.28318530718 * r;
    float ringRadius = 0.47;
    float minuteMark = (1.0 - smoothstep(0.0012, 0.0024, minute)) * (1.0 - smoothstep(0.006, 0.008, abs(r - ringRadius)));
    float hourMark = (1.0 - smoothstep(0.0022, 0.0036, hour)) * (1.0 - smoothstep(0.016, 0.019, abs(r - ringRadius - 0.008)));
    float marks = max(minuteMark * 0.55, hourMark);
    float vignette = smoothstep(0.38, 0.95, r);
    colour *= 1.0 - 0.25 * vignette * strength;
    colour += COLD * marks * 0.16 * strength;

    // ---- the jolt of the moment
    colour += COLD * shock * 0.10;

    fragColor = vec4(clamp(colour, 0.0, 1.0), 1.0);
}
