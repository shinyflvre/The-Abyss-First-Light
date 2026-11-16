#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:globals.glsl>

// The Infected Gauntlet's spell wheel (client/fx/SpellWheelClient, owner 12.10.2026: "mystical, smoke, cooler than the
// reference"). Drawn on one quad around the wheel (texCoord 0..1 = 240 GUI units across), vanilla position_tex_color
// vertex shader. Five slices (slot 0 at 12 o'clock, clockwise):
// - a band of abyss smoke from radius 20 to 86 that swirls slowly round the centre (domain-warped noise), navy with
//   cyan and violet wisps, fraying outwards into loose smoke up to radius ~116,
// - a pulsing light circle round the hole, thin rays on the slice borders (owner: no rune ring outside),
// - the hovered slice lights up cyan from the inside out, smoke included; the selected slice keeps a violet ember.
// vertexColor: r = (hovered + 1) / 8 (0 = none), g = selected / 8, b = open progress 0..1, a = 1 -> wheel opening /
// open, a ~0.7 -> closing (blown outwards), a 0.25..0.6 -> one upgrade gauge (see gauge()), a < 0.25 -> screen vignette
// mode (the quad covers the screen, b = fade).
// Opening the smoke gathers out of loose shreds while the ring grows into place; closing it tears and blows away.

layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    mat4 TextureMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
};

uniform sampler2D Sampler0;

layout(location = 0) in vec2 texCoord0;
layout(location = 1) in vec4 vertexColor;

layout(location = 0) out vec4 fragColor;

const float PI = 3.14159265;
const float SLICE = 2.0 * PI / 5.0;
const vec3 NAVY = vec3(0.016, 0.047, 0.066);
const vec3 TEAL = vec3(0.06, 0.42, 0.44);
const vec3 CYAN = vec3(0.45, 0.98, 0.9);
const vec3 VIOLET = vec3(0.42, 0.2, 0.72);

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + vec2(1.0, 0.0)), f.x), mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 5; i++) {
        v += a * noise(p);
        p = p * 2.03 + vec2(17.1, 9.7);
        a *= 0.5;
    }
    return v;
}

vec2 rot(vec2 p, float a) {
    float c = cos(a);
    float s = sin(a);
    return vec2(c * p.x - s * p.y, s * p.x + c * p.y);
}

// One upgrade gauge (owner: "rounded bars that follow the circle of the wheel", then "much thinner, smaller, and in the
// corners, not like a crosshair"): a thin arc of five capsules round the wheel in a corner - damage top left, cooldown top
// right, usage bottom right, regen bottom left - filled up to the level in the potion's colour, and a hairline capsule
// arc outside it for the progress towards the next level (owner: "the capsules are the level, the line the progress").
vec4 gauge(vec2 uv, int index, int level, float left, float fade) {
    vec3 colours[4] = vec3[4](vec3(1.0, 0.71, 0.22), vec3(1.0, 0.42, 0.9), vec3(0.37, 0.94, 1.0), vec3(0.72, 0.48, 1.0));
    vec3 colour = index == 0 ? colours[0] : index == 1 ? colours[1] : index == 2 ? colours[2] : colours[3];
    float r = length(uv) * 120.0;
    float ang = atan(uv.x, -uv.y);
    // corners, clockwise from the top: top left 315, top right 45, bottom right 135, bottom left 225 degrees
    float centre = index == 0 ? PI * 1.75 : index == 1 ? PI * 0.25 : index == 2 ? PI * 0.75 : PI * 1.25;
    float d = mod(ang - centre + PI, 2.0 * PI) - PI;      // signed angle from the gauge centre
    // every arc fills left -> right: clockwise does that on the top corners, the bottom ones are flipped
    if (index == 2 || index == 3) {
        d = -d;
    }
    const float SPAN = 0.36;                              // half the gauge, radians (~21 degrees)
    const float MID = 93.0;
    const float HALF = 1.0;                               // half the thickness of a capsule: thin
    float s = d * MID;                                    // arc length from the centre (GUI units)
    float total = SPAN * 2.0 * MID;
    float gap = 2.4;
    float seg = (total - gap * 4.0) / 5.0;
    vec4 out_ = vec4(0.0);
    for (int k = 0; k < 5; k++) {
        float s0 = -total * 0.5 + float(k) * (seg + gap) + HALF;
        float s1 = s0 + seg - HALF * 2.0;
        float dx = max(max(s0 - s, 0.0), s - s1);
        float dist = length(vec2(dx, r - MID)) - HALF;
        float shape = 1.0 - smoothstep(-0.6, 0.6, dist);
        float halo = exp(-max(dist, 0.0) * 0.9) * 0.25;
        if (shape <= 0.0 && halo <= 0.01) {
            continue;
        }
        vec4 c;
        if (k < level) {
            float a = 1.0;
            vec3 lit = mix(colour, vec3(1.0), 0.35 * smoothstep(HALF, -HALF, r - MID + 1.0));
            c = vec4(mix(colour, lit, 0.6) * a + colour * halo * a, max(shape, halo * a) * (0.35 + 0.65 * a));
        } else {
            c = vec4(vec3(0.03, 0.09, 0.12), shape * 0.55);
        }
        out_ = c.a > out_.a ? c : out_;
    }
    // progress towards the next level (potions drunk / needed): a thin capsule arc outside, filled from the left end
    {
        const float MID2 = 97.5;
        float s2 = d * MID2;
        float total2 = SPAN * 2.0 * MID2;
        float e0 = -total2 * 0.5 + 1.0;
        float e1 = e0 + (total2 - 2.0) * clamp(left, 0.0, 1.0);
        float dx2 = max(max(e0 - s2, 0.0), s2 - e1);
        float dist2 = length(vec2(dx2, r - MID2)) - 0.45;
        float back = 1.0 - smoothstep(-0.5, 0.5, length(vec2(max(max(e0 - s2, 0.0), s2 - (total2 * 0.5 - 1.0)), r - MID2)) - 0.45);
        float fill = 1.0 - smoothstep(-0.5, 0.5, dist2);
        vec4 c2 = fill > 0.0 ? vec4(colour, fill) : vec4(vec3(0.03, 0.09, 0.12), back * 0.5);
        out_ = c2.a > out_.a ? c2 : out_;
    }
    out_.a *= fade;
    return out_;
}

void main() {
    float t = GameTime * 1200.0;
    if (vertexColor.a > 0.24 && vertexColor.a < 0.62) {
        // gauge mode: r = index / 8, g = level / 8, b = progress to the next level, a = 0.25 + 0.35 x fade
        vec4 c = gauge(texCoord0 * 2.0 - 1.0, int(vertexColor.r * 8.0 + 0.5), int(vertexColor.g * 8.0 + 0.5), vertexColor.b,
            clamp((vertexColor.a - 0.25) / 0.35, 0.0, 1.0));
        if (c.a <= 0.003) {
            discard;
        }
        fragColor = c * ColorModulator;
        return;
    }
    if (vertexColor.a < 0.5) {
        // vignette over the screen while the wheel is open: the world darkens towards the edges
        vec2 q = texCoord0 * 2.0 - 1.0;
        float v = smoothstep(0.35, 1.35, length(q * vec2(1.0, 0.8)));
        fragColor = vec4(NAVY * 0.5, (0.18 + 0.5 * v) * vertexColor.b);
        return;
    }
    vec2 uv = texCoord0 * 2.0 - 1.0;
    float open = vertexColor.b;
    bool closing = vertexColor.a < 0.9;
    // opening: the smoke gathers and the ring grows into place; closing: it is blown outwards
    float ease = open * open * (3.0 - 2.0 * open);
    float spread = closing ? 1.35 - 0.35 * ease : 0.55 + 0.45 * ease;
    uv /= spread;
    float r = length(uv) * 120.0;
    float ang = atan(uv.x, -uv.y);                        // 0 up, clockwise
    float a01 = mod(ang + PI * 2.0, PI * 2.0);
    int hovered = int(vertexColor.r * 8.0 + 0.5) - 1;
    int selected = int(vertexColor.g * 8.0 + 0.5);

    // swirling smoke: the field turns slowly, faster near the hole, and is warped by itself
    vec2 p = rot(uv, t * 0.12 + 0.9 / (0.3 + length(uv)));
    vec2 warp = vec2(fbm(p * 2.2 + t * 0.07), fbm(p * 2.2 - t * 0.05 + 7.3));
    float smoke = fbm(p * 3.1 + warp * 1.6 - t * 0.04);
    float wisps = fbm(rot(uv, -t * 0.08) * 5.5 + warp * 2.0);

    // the band, soft inside and out; loose smoke fraying beyond it
    float band = smoothstep(18.0, 24.0, r) * (1.0 - smoothstep(78.0, 90.0, r));
    float fray = smoothstep(18.0, 30.0, r) * (1.0 - smoothstep(86.0, 118.0, r)) * smoothstep(0.52, 0.78, smoke);
    // thin where the smoke thins: the world shows through between the swirls
    float density = max(band * (0.18 + 0.82 * pow(smoke, 1.6)), fray * 0.8);

    vec3 col = NAVY;
    col = mix(col, TEAL * 0.7, smoothstep(0.42, 0.8, smoke) * 0.85);
    col = mix(col, VIOLET * 0.75, smoothstep(0.5, 0.85, wisps) * 0.6);
    // bright streaks where the swirl folds over itself
    float streak = smoothstep(0.62, 0.78, wisps) * smoothstep(0.5, 0.75, smoke) * band;
    col = mix(col, CYAN * 0.85, streak * 0.7);
    float alpha = max(density * 0.78, streak * 0.85);

    // which slice, and how far from its borders
    float sliceIndex = floor(mod(a01 + SLICE * 0.5, PI * 2.0) / SLICE);
    float within = mod(a01 + SLICE * 0.5, SLICE);
    float border = min(within, SLICE - within) * r;        // arc distance (GUI units) to the nearest border

    // hovered slice: cyan light from the hole outwards, the smoke in it lit up
    if (int(sliceIndex) == hovered) {
        float lit = pow(1.0 - smoothstep(20.0, 96.0, r), 0.6) * smoothstep(0.0, 3.0, border) * smoothstep(18.0, 23.0, r);
        col = mix(col, CYAN * (0.55 + 0.6 * smoke), lit * 0.9);
        alpha = max(alpha, lit * (0.55 + 0.4 * smoke));
        // a bright arc along the slice's outer edge
        float arc = exp(-pow(r - 84.0, 2.0) / 3.0) * smoothstep(0.0, 6.0, border);
        col = mix(col, CYAN, arc);
        alpha = max(alpha, arc * 0.9);
    }
    // the selected slice keeps a violet ember near the outer edge
    if (int(sliceIndex) == selected && hovered != selected) {
        float ember = exp(-pow(r - 82.0, 2.0) / 5.0) * smoothstep(0.0, 6.0, border) * (0.7 + 0.3 * sin(t * 2.5));
        col = mix(col, VIOLET * 1.4, ember);
        alpha = max(alpha, ember * 0.85);
    }

    // rays on the slice borders, fading outwards
    float ray = (1.0 - smoothstep(0.0, 1.3, border)) * smoothstep(20.0, 24.0, r) * (1.0 - smoothstep(24.0, 112.0, r));
    col = mix(col, CYAN, ray * 0.7);
    alpha = max(alpha, ray * 0.75);

    // the light circle round the hole, breathing
    float pulse = 0.75 + 0.25 * sin(t * 2.2);
    float rim = exp(-pow(r - 21.0, 2.0) / 1.4);
    col = mix(col, CYAN, rim);
    alpha = max(alpha, rim * pulse);
    // a faint glow inside the hole
    float hole = (1.0 - smoothstep(0.0, 20.0, r)) * 0.25 * pulse;
    col = mix(col, TEAL, hole * (1.0 - rim));
    alpha = max(alpha, hole);

    // the smoke condenses out of loose shreds (opening) or tears into shreds and is gone (closing)
    float shreds = fbm(uv * 3.4 + vec2(t * 0.35, -t * 0.22));
    float keep = smoothstep(shreds - 0.1, shreds + 0.1, open * 1.3 - 0.15);
    fragColor = vec4(col, clamp(alpha, 0.0, 1.0) * keep) * ColorModulator;
    if (fragColor.a <= 0.003) {
        discard;
    }
}
