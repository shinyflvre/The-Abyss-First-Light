#version 330

uniform float GameTime;

// World-space effects of the rings (RingFxClient, owner 11.10.2026: beautiful, not cheap particles). Up to 8 slots in
// fx state pixels 96..159, 8 floats each: centre x, y, z (camera relative), radius, progress 0..1, kind, colour
// (0xRRGGBB as a float), param. Kinds:
//   1 WAVE     a shock wave running out over the surfaces, a ripple of heat in its band
//   2 DOME     a glowing sphere shell growing out of the centre (fresnel rim)
//   3 PILLAR   a column of light rising from the centre, param = width
//   4 BOLT     a jagged lightning beam from the sky onto the centre, param = width
//   5 IMPLODE  a wave running inwards to the centre
//   6 VORTEX   a turning spiral of light on the ground around the centre
//   7 RADIUS   a lasting circle on the terrain at radius around the centre (Ring of Invisibility: who is inside sees
//              you), param = strength
//   8 LENS     a black hole bends the picture behind it (thin lens: a pixel at impact b shows what lies at b - r*r/b),
//              radius = Einstein radius in blocks, nothing in front of the hole is bent

uniform sampler2D InSampler;
uniform sampler2D DepthSampler;

uniform float DepthFar;

float fxDepth(float depth) {
    if (depth >= 1.0) {
        return 0.0;
    }
    float far = DepthFar;
    float near = 0.05;
    float viewZ = 2.0 * near * far / (far + near - (2.0 * depth - 1.0) * (far - near));
    return near / viewZ;
}
uniform sampler2D StateSampler;

// The exact camera of this frame for world-space post effects (rebuilding positions from the depth buffer).
// Written by ScreenFx#onExtractLevel into fx state pixels 8..21 as 32-bit floats (bits in R,G,B,A, most significant
// first): the inverse of (view bobbing x hurt tilt x view rotation) - mat3 column-major + eye offset - and the
// projection scale (m00, m11). With view bobbing the level is rendered with a bobbing projection; rebuilding positions
// from yaw/pitch alone made light and noise slide over the blocks and flicker at block edges while walking.
// Needs `uniform sampler2D StateSampler;` declared before the include. Call fxCameraSetup() first.
// Positions are relative to the camera position (Globals: CameraBlockPos - CameraOffset). Reversed-Z depth.

mat3 fxRotation;
vec3 fxEye;
vec2 fxProjection;

float fxFloat(int index) {
    uvec4 b = uvec4(texelFetch(StateSampler, ivec2(index, 0), 0) * 255.0 + 0.5);
    return uintBitsToFloat(b.r << 24u | b.g << 16u | b.b << 8u | b.a);
}

void fxCameraSetup() {
    fxRotation = mat3(fxFloat(8), fxFloat(9), fxFloat(10), fxFloat(11), fxFloat(12), fxFloat(13), fxFloat(14), fxFloat(15), fxFloat(16));
    fxEye = vec3(fxFloat(17), fxFloat(18), fxFloat(19));
    fxProjection = vec2(fxFloat(20), fxFloat(21));
}

// view ray through a screen position, scaled so that one unit = one block of view depth
vec3 fxViewRay(vec2 uv) {
    return fxRotation * vec3((uv * 2.0 - 1.0) / fxProjection, -1.0);
}

// camera-relative position of the surface seen at uv (depth = raw reversed-Z depth, > 0)
vec3 fxPosition(vec2 uv, float depth) {
    return fxEye + fxViewRay(uv) * (0.05 / max(depth, 1.0e-7));
}

in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

const int SLOTS = 8;
const int FIRST = 96;

float hash(float n) {
    return fract(sin(n * 12.9898) * 43758.5453);
}

vec3 slotColour(float packedColour) {
    int c = int(packedColour); // exact: the colour is an integer below 2^24 (+0.5 rounded odd values up - every odd blue became 0)
    return vec3(float((c >> 16) & 255), float((c >> 8) & 255), float(c & 255)) / 255.0;
}

// band of a wave front at distance d from the centre: gaussian around the front
float band(float d, float front, float width) {
    float x = (d - front) / width;
    return exp(-x * x);
}

void main() {
    fxCameraSetup();
    float t = GameTime * 1200.0;
    float depth = fxDepth(texture(DepthSampler, texCoord).r);
    vec3 ray = fxViewRay(texCoord);
    vec3 dir = normalize(ray);
    bool sky = depth <= 1.0e-7;
    float sceneDistance = sky ? 400.0 : length(ray * (0.05 / depth));
    vec3 position = fxEye + dir * sceneDistance;

    // pass 1: heat ripple - the waves bend the picture a little
    vec2 offset = vec2(0.0);
    for (int i = 0; i < SLOTS; i++) {
        int base = FIRST + i * 8;
        float kind = fxFloat(base + 5);
        if (kind < 0.5 || sky) {
            continue;
        }
        if (kind < 1.5 || (kind > 4.5 && kind < 5.5)) {
            vec3 centre = vec3(fxFloat(base), fxFloat(base + 1), fxFloat(base + 2));
            float radius = fxFloat(base + 3);
            float progress = fxFloat(base + 4);
            float front = kind < 1.5 ? progress * radius : (1.0 - progress) * radius;
            float d = distance(position, centre);
            float b = band(d, front, 1.6);
            offset += vec2(sin(d * 3.0 - t * 9.0), cos(d * 2.6 - t * 8.0)) * b * (1.0 - progress) * 0.004;
        }
    }
    vec2 lens = vec2(0.0);
    float shadow = 0.0;
    for (int i = 0; i < SLOTS; i++) {
        int base = FIRST + i * 8;
        float kind = fxFloat(base + 5);
        if (kind < 7.5 || kind > 8.5) {
            continue;
        }
        vec3 view = transpose(fxRotation) * (vec3(fxFloat(base), fxFloat(base + 1), fxFloat(base + 2)) - fxEye);
        if (view.z > -0.5) {
            continue;
        }
        float holeDepth = -view.z;
        vec2 hole = (view.xy / holeDepth * fxProjection) * 0.5 + 0.5;
        vec2 rel = (texCoord - hole) * 2.0 / fxProjection;
        float impact = length(rel) * holeDepth;
        float progress = clamp(fxFloat(base + 4), 0.0, 1.0);
        float strength = smoothstep(0.0, 0.14, progress) * (1.0 - smoothstep(0.9, 1.0, progress));
        float einstein = fxFloat(base + 3) * strength;
        float seen = sky ? 1.0e6 : dot(ray * (0.05 / depth), fxRotation * vec3(0.0, 0.0, -1.0));
        if (einstein < 0.01 || impact < 0.001 || seen < holeDepth - 0.4) {
            continue;
        }
        float horizon = einstein / 0.7;
        float edge = 0.5 * (horizon + sqrt(horizon * horizon + 4.0 * einstein * einstein));
        shadow = max(shadow, 1.0 - smoothstep(edge - 0.05, edge + 0.05, impact));
        float reach = einstein * 7.0;
        float bend = einstein * einstein / max(impact, einstein * 0.4) * (1.0 - smoothstep(einstein * 2.5, reach, impact));
        lens -= normalize(rel) * (bend / holeDepth) * fxProjection * 0.5;
    }
    vec3 colour = vec3(
        texture(InSampler, texCoord + offset + lens * 1.03).r,
        texture(InSampler, texCoord + offset + lens).g,
        texture(InSampler, texCoord + offset + lens * 0.97).b
    ) * (1.0 - shadow);
    vec3 light = vec3(0.0);

    for (int i = 0; i < SLOTS; i++) {
        int base = FIRST + i * 8;
        float kind = fxFloat(base + 5);
        if (kind < 0.5 || kind > 7.5) {
            continue;
        }
        vec3 centre = vec3(fxFloat(base), fxFloat(base + 1), fxFloat(base + 2));
        float radius = max(fxFloat(base + 3), 0.01);
        float progress = clamp(fxFloat(base + 4), 0.0, 1.0);
        vec3 tint = slotColour(fxFloat(base + 6));
        float param = fxFloat(base + 7);
        float fade = 1.0 - progress;

        if (kind < 1.5 || (kind > 4.5 && kind < 5.5)) {
            // WAVE / IMPLODE: a bright band on every surface it passes, a faint wash inside
            if (sky) {
                continue;
            }
            bool inward = kind > 4.5;
            float eased = inward ? progress * progress : 1.0 - (1.0 - progress) * (1.0 - progress);
            float front = inward ? (1.0 - eased) * radius : eased * radius;
            float d = distance(position, centre);
            float width = max(param, 0.6) * (inward ? 1.0 : 0.7 + 0.6 * progress);
            float b = band(d, front, width);
            float inside = (inward ? step(front, d) : step(d, front)) * (1.0 - smoothstep(0.0, radius, d)) * 0.12;
            light += tint * (b * 1.4 + inside) * fade * (inward ? progress * 1.5 + 0.2 : 1.0);
        } else if (kind < 2.5) {
            // DOME: sphere shell, grows fast then lingers and fades; fresnel rim, faint fill
            float r = radius * (1.0 - pow(1.0 - min(progress * 2.5, 1.0), 3.0));
            vec3 oc = fxEye - centre;
            float bq = dot(oc, dir);
            float cq = dot(oc, oc) - r * r;
            float h = bq * bq - cq;
            if (h > 0.0) {
                h = sqrt(h);
                float tNear = -bq - h;
                float tFar = -bq + h;
                float alpha = fade * (0.6 + 0.4 * sin(t * 4.0 + float(i)));
                for (int s = 0; s < 2; s++) {
                    float tHit = s == 0 ? tNear : tFar;
                    if (tHit > 0.0 && tHit < sceneDistance) {
                        vec3 p = fxEye + dir * tHit;
                        vec3 n = normalize(p - centre);
                        float rim = pow(1.0 - abs(dot(n, dir)), 3.0);
                        // flowing lines over the shell
                        float lines = 0.5 + 0.5 * sin((n.y * 9.0 + atan(n.z, n.x) * 3.0) + t * 2.0);
                        light += tint * (rim * 1.1 + 0.06 + lines * rim * 0.4) * alpha * (s == 0 ? 1.0 : 0.6);
                    }
                }
            }
        } else if (kind < 4.5) {
            // PILLAR / BOLT: closest approach of the view ray to the vertical line through the centre
            vec2 d2 = dir.xz;
            float denom = max(dot(d2, d2), 1.0e-5);
            float along = dot(centre.xz - fxEye.xz, d2) / denom;
            float y = fxEye.y + dir.y * along;
            float height = radius;
            if (along > 0.0 && along < sceneDistance + 0.3 && y > centre.y - 0.3 && y < centre.y + height) {
                float off = length(fxEye.xz + d2 * along - centre.xz);
                float width = max(param, 0.05);
                float strength;
                if (kind < 3.5) {
                    float rise = smoothstep(0.0, 0.25, progress);
                    float top = centre.y + height * rise;
                    strength = (y < top ? 1.0 : 0.0) * (1.0 - smoothstep(centre.y + height * 0.6, top, y) * 0.7);
                    float core = exp(-off * off / (width * width * 0.25));
                    float glow = exp(-off * off / (width * width * 4.0));
                    light += (tint * glow * 0.8 + mix(tint, vec3(1.0), 0.6) * core) * strength * fade;
                } else {
                    float jag = (hash(floor(y * 1.4) + float(i) * 13.0 + floor(t * 20.0)) - 0.5) * 0.45;
                    float o = abs(off + jag);
                    float flash = fade * fade;
                    float core = exp(-o * o / (width * width * 0.3));
                    float glow = exp(-o * o / (width * width * 6.0));
                    light += (tint * glow + mix(tint, vec3(1.0), 0.7) * core * 1.6) * flash;
                }
            }
            // light pool on the ground around the foot
            if (!sky) {
                float pool = 1.0 - smoothstep(0.0, max(param * 8.0, 2.0), distance(position, centre));
                light += tint * pool * fade * 0.35;
            }
        } else if (kind > 6.5) {
            // RADIUS: a thin glowing line where the terrain crosses the circle, a soft wash just inside it
            if (sky) {
                continue;
            }
            vec3 rel = position - centre;
            float d = length(rel.xz);
            float vertical = 1.0 - smoothstep(2.5, 5.0, abs(rel.y));
            float line = exp(-pow((d - radius) / 0.09, 2.0));
            float wash = smoothstep(radius - 1.6, radius, d) * step(d, radius) * 0.18;
            float angle = atan(rel.z, rel.x);
            float pulse = 0.75 + 0.25 * sin(angle * 6.0 - t * 2.0);
            light += tint * (line * pulse + wash) * vertical * clamp(param, 0.0, 1.0);
        } else if (kind < 6.5) {
            // VORTEX: spiral arms on the ground turning around the centre
            if (sky) {
                continue;
            }
            vec3 rel = position - centre;
            float d = length(rel.xz);
            if (d < radius && abs(rel.y) < 3.0) {
                float angle = atan(rel.z, rel.x);
                float arms = max(param, 2.0);
                float spiral = 0.5 + 0.5 * sin(angle * arms - d * 0.9 + t * 5.0);
                float edge = (1.0 - smoothstep(radius * 0.6, radius, d)) * (1.0 - smoothstep(1.5, 3.0, abs(rel.y)));
                float envelope = smoothstep(0.0, 0.15, progress) * (1.0 - smoothstep(0.75, 1.0, progress));
                light += tint * pow(spiral, 4.0) * edge * envelope * 0.9;
            }
        }
    }
    fragColor = vec4(colour + light, 1.0);
}
