#version 150

// The Reflection's skill tree (client/gui/SkillTreeScreen, owner 23.10.2026: "like our spell wheel, abyss blue / cyan").
// Same vertex layout as the GUI (position_tex_color); the vertex colour picks the mode:
// a < 0.2  background over the whole screen: r = aspect / 4, g = tree centre (uv y), b = fade
// a < 0.5  a link between two gifts (texCoord x along, y across): r = state (0 locked, 0.5 open, 1 learned),
//          g = seed, b = length / 256
// else     the halo of a gift: r = state, g = hovered, b = keystone

uniform sampler2D Sampler0;
uniform float GameTime;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

const vec3 NAVY = vec3(0.016, 0.047, 0.066);
const vec3 TEAL = vec3(0.06, 0.42, 0.44);
const vec3 CYAN = vec3(0.45, 0.98, 0.9);
const vec3 PALE = vec3(0.706, 0.992, 0.918);
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
    for (int i = 0; i < 4; i++) {
        v += a * noise(p);
        p = p * 2.03 + vec2(17.1, 9.7);
        a *= 0.5;
    }
    return v;
}

vec4 background(vec2 uv, float aspect, float cy, float fade, float t) {
    vec2 p = vec2((uv.x - 0.5) * aspect, uv.y - cy);
    float r = length(p);
    vec2 q = p * 3.0;
    vec2 warp = vec2(fbm(q + vec2(0.0, t * 0.03)), fbm(q + vec2(5.2, -t * 0.025)));
    float smoke = fbm(q * 1.3 + warp * 1.6 + vec2(t * 0.02, 0.0));
    vec3 col = mix(NAVY, TEAL * 0.55, smoothstep(0.2, 0.7, smoke));
    col += CYAN * smoothstep(0.6, 0.82, fbm(q * 2.1 - warp + vec2(-t * 0.04, t * 0.03))) * 0.16;
    col += VIOLET * smoothstep(0.64, 0.86, fbm(q * 1.7 + warp * 2.0 + 7.0)) * 0.18;
    col += CYAN * 0.12 * exp(-r * r * 7.0);
    for (int k = 1; k <= 3; k++) {
        float rr = 0.15 * float(k);
        float band = exp(-pow((r - rr) * 120.0, 2.0));
        col += PALE * band * 0.07 * (0.6 + 0.4 * sin(t * 0.7 + float(k) * 2.1));
    }
    vec2 g = vec2(uv.x * aspect, uv.y) * 46.0 + vec2(0.0, t * 0.5);
    vec2 cell = floor(g);
    float h = hash(cell);
    if (h > 0.955) {
        vec2 spot = vec2(hash(cell + 1.3), hash(cell + 2.7)) - 0.5;
        float d = length(fract(g) - 0.5 - spot * 0.6);
        col += CYAN * smoothstep(0.09, 0.0, d) * (0.35 + 0.35 * sin(t * 2.0 + h * 40.0));
    }
    float vig = smoothstep(0.2, 1.05, length(vec2((uv.x - 0.5) * aspect, uv.y - 0.5)));
    col *= 1.0 - 0.5 * vig;
    return vec4(col, (0.84 + 0.14 * vig) * fade);
}

vec4 link(vec2 uv, float state, float seed, float len, float t) {
    float across = abs(uv.y - 0.5) * 2.0;
    float core = exp(-across * across * 28.0);
    float glow = exp(-across * across * 4.0);
    vec3 col;
    float a;
    if (state > 0.75) {
        float flow = fract(uv.x * len * 0.05 - t * 0.8 + seed);
        float pulse = smoothstep(0.0, 0.12, flow) * smoothstep(0.42, 0.12, flow);
        col = mix(TEAL, PALE, core) + CYAN * pulse * 0.8;
        a = core * 0.95 + glow * (0.3 + 0.35 * pulse);
    } else if (state > 0.25) {
        float breathe = 0.5 + 0.5 * sin(t * 2.0 + seed * 6.28);
        col = mix(TEAL, CYAN, core * 0.6);
        a = core * (0.4 + 0.3 * breathe) + glow * 0.1;
    } else {
        float dash = step(0.45, fract(uv.x * len * 0.09));
        col = vec3(0.08, 0.22, 0.26);
        a = core * 0.6 * dash;
    }
    a *= smoothstep(0.0, 0.05, uv.x) * smoothstep(1.0, 0.95, uv.x);
    return vec4(col, a);
}

vec4 halo(vec2 uv, float state, float hover, float keystone, float t) {
    vec2 p = (uv - 0.5) * 2.0;
    float r = length(p);
    float ang = atan(p.y, p.x);
    float ring = exp(-pow((r - 0.55) * 7.0, 2.0));
    float soft = exp(-r * r * 3.0);
    vec3 col = state > 0.75 ? CYAN : state > 0.25 ? TEAL * 1.5 : vec3(0.05, 0.15, 0.18);
    float k = state > 0.75 ? 1.0 : state > 0.25 ? 0.55 + 0.25 * sin(t * 2.2) : 0.2;
    float a = (soft * 0.35 + ring * 0.5) * k;
    if (state > 0.75) {
        float spark = pow(max(0.0, cos(ang * 3.0 - t * 1.6)), 14.0) * exp(-pow((r - 0.62) * 10.0, 2.0));
        col += PALE * spark;
        a += spark * 0.8;
    }
    if (keystone > 0.5) {
        float runes = exp(-pow((r - 0.86) * 16.0, 2.0)) * (0.5 + 0.5 * sin(ang * 8.0 + t * 0.9));
        col = mix(col, VIOLET * 1.4, 0.3 * runes + 0.12);
        a += runes * 0.45 * max(k, 0.4);
    }
    if (hover > 0.5) {
        col = mix(col, mix(CYAN, VIOLET * 1.4, 0.4), 0.5);
        a = a * 1.4 + soft * 0.25;
    }
    a *= smoothstep(1.0, 0.85, r);
    return vec4(col, clamp(a, 0.0, 1.0));
}

void main() {
    vec4 unused = texture(Sampler0, texCoord0) * 0.0;
    float t = GameTime * 1200.0;
    vec4 c = vertexColor;
    vec4 outColour;
    if (c.a < 0.2) {
        outColour = background(texCoord0, c.r * 4.0, c.g, c.b, t);
    } else if (c.a < 0.5) {
        outColour = link(texCoord0, c.r, c.g, c.b * 256.0, t);
    } else {
        outColour = halo(texCoord0, c.r, c.g, c.b, t);
    }
    if (outColour.a < 0.004) {
        discard;
    }
    fragColor = outColour + unused;
}
