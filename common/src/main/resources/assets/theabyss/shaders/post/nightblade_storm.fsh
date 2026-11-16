#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:globals.glsl>

// The Nightblade's lightning storm (NightbladeStormClient): the world sinks into darkness - only the space around the
// player, the boss and the glowing circles stay lit. A circle crackles brighter while it charges, then lightning
// strikes: a jagged beam from the sky that lights up the dark around it for a moment.
// fx state (32-bit floats): 32..34 boss (camera relative), 35 darkness, 36.. twelve circles: x, y, z (ground), progress
// (0..1 charging, 1..2 strike flash, < 0 empty; +10 = red: phase 3 uses the Abyss's red lightning, #FF5959 / #FF5E5E;
// +20 = quiet: Ring of Thunder - the charging circle and the light of the strike, but no beam: it draws its own bolt;
// +40 = lantern: a creature bound by the Ring of Telekinetic, x y z = its centre, progress - 40 = how strongly it is lit).
// Also draws the Shadow Arena floor (88..92, see shadowArena).

uniform sampler2D InSampler;
uniform sampler2D DepthSampler;
uniform sampler2D StateSampler;

#include <theabyss:fx_camera.glsl>

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

const vec3 CYAN = vec3(0.45, 0.85, 1.0);
const vec3 BEAM = vec3(0.85, 0.95, 1.0);
const vec3 RED_LIGHT = vec3(1.0, 0.369, 0.369); // #FF5E5E, the light of the Abyss's red lightning
const vec3 VIOLET = vec3(0.64, 0.28, 1.0);       // the Ring of Telekinetic's chains
const vec3 RED_SKY = vec3(1.0, 0.349, 0.349);   // #FF5959, its sky flash
const float RING_RADIUS = 1.8;

float hash(float n) {
    return fract(sin(n * 12.9898) * 43758.5453);
}

vec2 hash2(vec2 p) {
    p = vec2(dot(p, vec2(127.1, 311.7)), dot(p, vec2(269.5, 183.3)));
    return fract(sin(p) * 43758.5453);
}

// Cellular pattern: distance to the nearest cell border (F2 - F1); the cell points drift slowly, so the cracks shift.
float cellEdge(vec2 p, float time) {
    vec2 cell = floor(p);
    vec2 f = fract(p);
    float f1 = 8.0;
    float f2 = 8.0;
    for (int j = -1; j <= 1; j++) {
        for (int i = -1; i <= 1; i++) {
            vec2 g = vec2(float(i), float(j));
            vec2 o = 0.5 + 0.38 * sin(time * 0.6 + 6.2831 * hash2(cell + g));
            float d = length(g + o - f);
            if (d < f1) {
                f2 = f1;
                f1 = d;
            } else if (d < f2) {
                f2 = d;
            }
        }
    }
    return f2 - f1;
}

// Distance along the view ray to the Shadow Arena floor, if it is hit in front of the scene (else -1). The floor is
// invisible blocks without depth, so the storm circles and beams use this as their ground on the platform.
float arenaFloor(vec3 dir, float sceneDistance) {
    float radius = fxFloat(91);
    if (radius < 0.05 || abs(dir.y) < 1.0e-4) {
        return -1.0;
    }
    vec3 centre = vec3(fxFloat(88), fxFloat(89), fxFloat(90));
    float s = (centre.y - fxEye.y) / dir.y;
    if (s <= 0.0 || s > sceneDistance + 0.02) {
        return -1.0;
    }
    vec3 p = fxEye + dir * s;
    return distance(p.xz, centre.xz) <= radius ? s : -1.0;
}

// The Shadow Arena (NightbladeArenaClient, fx state 88..92: centre camera relative with y = the floor's top, radius,
// red): a floating floor of shadow glass - it darkens what lies beneath, glowing energy cracks shift through it, pulses
// run outwards, the rim burns and the floor lights up under the player's feet. Invisible blocks carry the weight.
vec3 shadowArena(vec3 colour, vec3 dir, float sceneDistance, float t) {
    float radius = fxFloat(91);
    if (radius < 0.05 || abs(dir.y) < 1.0e-4) {
        return colour;
    }
    vec3 centre = vec3(fxFloat(88), fxFloat(89), fxFloat(90));
    float s = (centre.y - fxEye.y) / dir.y;
    if (s <= 0.0 || s > sceneDistance + 0.02) {
        return colour; // behind the camera, or something stands in front of the floor
    }
    vec3 p = fxEye + dir * s;
    vec2 q = p.xz - centre.xz;
    float r = length(q);
    if (r > radius) {
        return colour;
    }
    vec3 tint = fxFloat(92) > 0.5 ? RED_LIGHT : CYAN;
    float edgeFade = smoothstep(radius, radius - 0.5, r);
    // shadow glass: more opaque when looked at flat
    float opacity = mix(0.55, 0.88, 1.0 - abs(dir.y)) * edgeFade;
    vec3 col = mix(colour, vec3(0.012, 0.014, 0.035), opacity);
    // energy cracks on two scales
    float cracks = smoothstep(0.09, 0.0, cellEdge(q * 0.55, t)) + 0.5 * smoothstep(0.06, 0.0, cellEdge(q * 1.4 + 17.0, t * 1.3));
    // pulses running outwards from the centre
    float ripple = 0.35 + 0.65 * pow(0.5 + 0.5 * sin(r * 1.3 - t * 2.4), 6.0);
    // the burning rim, and light under the player's feet
    float rim = exp(-pow(radius - r, 2.0) / 0.06) + 0.35 * exp(-(radius - r) / 1.2);
    vec2 feet = p.xz - fxEye.xz;
    float underFoot = exp(-dot(feet, feet) / 6.0);
    float glow = cracks * ripple * (0.55 + 0.7 * underFoot) + rim * 1.2 + underFoot * 0.12;
    return col + tint * glow * 0.9 * edgeFade;
}

void main() {
    fxCameraSetup();
    float t = GameTime * 1200.0;
    float darkness = fxFloat(35);
    vec3 boss = vec3(fxFloat(32), fxFloat(33), fxFloat(34));

    float depth = texture(DepthSampler, texCoord).r;
    vec3 ray = fxViewRay(texCoord);
    vec3 dir = normalize(ray);
    bool sky = depth <= 1.0e-7;
    float sceneDistance = sky ? 300.0 : length(ray * (0.05 / depth));
    vec3 position = fxEye + dir * sceneDistance;
    vec3 colour = texture(InSampler, texCoord).rgb;
    // standing on the Shadow Arena: its floor is the ground here (circles and beams land on it)
    float floorAt = arenaFloor(dir, sceneDistance);
    if (floorAt > 0.0) {
        sky = false;
        sceneDistance = floorAt;
        position = fxEye + dir * floorAt;
    }

    // what stays lit: around the player and around the boss
    float keep = 1.0 - smoothstep(3.0, 7.0, length(position));
    keep = max(keep, 1.0 - smoothstep(2.5, 5.0, distance(position, boss)));
    vec3 glow = vec3(0.0);

    for (int i = 0; i < 12; i++) {
        int base = 36 + i * 4;
        float progress = fxFloat(base + 3);
        if (progress < 0.0) {
            continue;
        }
        if (progress >= 35.0) {
            // +40: a creature bound by the Ring of Telekinetic - it stays lit in the dark, a faint violet glow around it
            float held = progress - 40.0;
            vec3 at = vec3(fxFloat(base), fxFloat(base + 1), fxFloat(base + 2));
            float d = distance(position, at);
            // lit only on a body-sized ellipsoid: a bigger sphere let the bright sky shine through the curse smoke
            // around the creature (translucent puffs write depth, so the sky behind them counted as "kept")
            vec3 q = (position - at) / vec3(0.55, 1.2, 0.55);
            keep = max(keep, (1.0 - smoothstep(0.9, 1.25, length(q))) * held);
            glow += VIOLET * exp(-d * d / 3.0) * 0.07 * held;
            continue;
        }
        bool quiet = progress >= 15.0;
        progress -= quiet ? 20.0 : 0.0;
        bool red = progress >= 5.0;
        progress -= red ? 10.0 : 0.0;
        vec3 tint = red ? RED_LIGHT : CYAN;
        vec3 beamColour = red ? mix(RED_SKY, vec3(1.0), 0.25) : BEAM;
        vec3 centre = vec3(fxFloat(base), fxFloat(base + 1), fxFloat(base + 2));
        float r = distance(position.xz, centre.xz);
        float onGround = sky ? 0.0 : 1.0 - smoothstep(0.6, 2.2, abs(position.y - centre.y));
        if (progress <= 1.0) {
            // charging: a crackling ring that brightens and fills towards the strike
            float angle = atan(position.z - centre.z, position.x - centre.x);
            float crackle = 0.65 + 0.35 * hash(floor(angle * 12.0) + floor(t * 14.0));
            float edge = exp(-pow(r - RING_RADIUS, 2.0) / 0.012) * onGround * crackle;
            float fill = (1.0 - smoothstep(RING_RADIUS - 0.3, RING_RADIUS, r)) * onGround * progress * progress;
            glow += tint * (edge * (0.7 + 0.8 * progress) + fill * 0.35);
            keep = max(keep, (1.0 - smoothstep(RING_RADIUS, RING_RADIUS + 2.5, r)) * onGround * 0.75);
        } else {
            // the strike: a jagged beam from the sky, and the dark around it lights up for a moment
            float flash = 2.0 - progress;
            vec2 d2 = dir.xz;
            float along = dot(centre.xz - fxEye.xz, d2) / max(dot(d2, d2), 1.0e-5);
            float y = fxEye.y + dir.y * along;
            if (!quiet && along > 0.0 && along < sceneDistance + 0.5 && y > centre.y - 0.5 && y < centre.y + 90.0) {
                float jag = (hash(floor(y * 1.3) + float(i) * 17.0) - 0.5) * 0.35;
                float off = length(fxEye.xz + d2 * along - centre.xz) + jag;
                float beam = exp(-off * off / 0.012);
                glow += beamColour * beam * 2.2 * flash;
            }
            float near = 1.0 - smoothstep(1.0, 9.0, distance(position, centre));
            keep = max(keep, near * flash);
            glow += tint * near * flash * 0.35;
        }
    }

    // what glows stays bright: very bright, saturated colours in the hues of the Abyss's glow - cyan, blue, violet (his
    // eyes and lines, his blades, glowing Abyss plants). Sunlit flowers and grass (yellow, red, green) and the sky are
    // darkened like everything else (owner, 10.10.2026: dandelions and poppies must not shine).
    float peak = max(colour.r, max(colour.g, colour.b));
    float low = min(colour.r, min(colour.g, colour.b));
    float saturation = peak > 0.001 ? (peak - low) / peak : 0.0;
    float hue = 0.0;
    if (peak - low > 0.001) {
        float range = peak - low;
        if (peak == colour.r) {
            hue = mod((colour.g - colour.b) / range, 6.0);
        } else if (peak == colour.g) {
            hue = (colour.b - colour.r) / range + 2.0;
        } else {
            hue = (colour.r - colour.g) / range + 4.0;
        }
        hue *= 60.0;
    }
    float abyssHue = smoothstep(160.0, 180.0, hue) * (1.0 - smoothstep(290.0, 310.0, hue));
    float glowing = sky ? 0.0 : smoothstep(0.78, 0.95, peak) * smoothstep(0.4, 0.65, saturation) * abyssHue;
    keep = max(keep, glowing);
    colour *= mix(1.0, 0.025, darkness * (1.0 - clamp(keep, 0.0, 1.0)));
    colour = shadowArena(colour, dir, sceneDistance, t); // glows on its own, untouched by the darkness
    fragColor = vec4(clamp(colour + glow, 0.0, 1.0), 1.0);
}
