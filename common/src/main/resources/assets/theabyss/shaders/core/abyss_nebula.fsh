#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:globals.glsl>
#include <minecraft:fog.glsl>
#include <minecraft:dynamictransforms.glsl>

// Abyss nebula sky (owner request, config Visuals/AbyssNebulaSky): the dark sibling of the astral sky.
// A dark nebula flowing slowly over a near-black dome, faint aurora veils, twinkling stars and an eclipse instead of the
// moon (black orb with a glowing corona). All colours follow the biome fog (owner: the sky must match every biome).
// The horizon takes the current fog colour, so the terrain fog of every biome melts into the sky; below the horizon
// the fog sinks into the abyss. ColorModulator: xyz = direction of the eclipse, w = rain brightness; ModelOffset.x =
// red lightning flash.

layout(location = 0) in vec3 direction;

layout(location = 0) out vec4 fragColor;

float hash(vec3 p) {
    p = fract(p * 0.3183099 + 0.1);
    p *= 17.0;
    return fract(p.x * p.y * p.z * (p.x + p.y + p.z));
}

float noise(vec3 x) {
    vec3 i = floor(x);
    vec3 f = fract(x);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(mix(hash(i + vec3(0, 0, 0)), hash(i + vec3(1, 0, 0)), f.x),
                   mix(hash(i + vec3(0, 1, 0)), hash(i + vec3(1, 1, 0)), f.x), f.y),
               mix(mix(hash(i + vec3(0, 0, 1)), hash(i + vec3(1, 0, 1)), f.x),
                   mix(hash(i + vec3(0, 1, 1)), hash(i + vec3(1, 1, 1)), f.x), f.y), f.z);
}

float fbm(vec3 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 4; i++) {
        value += amplitude * noise(p);
        p = p * 2.03 + vec3(1.7, 9.2, 3.1);
        amplitude *= 0.5;
    }
    return value;
}

// 3 octaves: for the warp vectors, the detail and the veils the 4th octave is invisible (performance)
float fbm3(vec3 p) {
    float value = 0.5 * noise(p);
    p = p * 2.03 + vec3(1.7, 9.2, 3.1);
    value += 0.25 * noise(p);
    p = p * 2.03 + vec3(1.7, 9.2, 3.1);
    return value + 0.125 * noise(p);
}

float flow(vec3 p, float t) {
    vec3 q = vec3(fbm3(p + vec3(0.0, t * 0.02, 0.0)), fbm3(p + vec3(5.2, 1.3, 2.8) - t * 0.015), fbm3(p + vec3(1.7, 9.2, 3.1)));
    return fbm(p + 2.2 * q);
}

// palette derived from the biome fog colour (hue of the fog, from near-black to a bright accent), plus a slightly
// hue-shifted tone for depth - so the sky always belongs to the biome (purple biomes: violet nebula, teal ones: petrol)
vec3 fogTint() {
    vec3 f = FogColor.rgb;
    float m = max(f.r, max(f.g, f.b));
    return m > 0.002 ? f / m : vec3(0.3, 0.9, 1.0);
}

vec3 abyssEntry(float i, vec3 tint) {
    vec3 shifted = normalize(mix(tint, tint.brg, 0.3)) * 1.2;
    vec3 c = tint * 0.035 + 0.006;
    c = i > 0.5 ? tint * 0.11 : c;
    c = i > 1.5 ? tint * 0.20 : c;
    c = i > 2.5 ? mix(tint, vec3(1.0), 0.15) * 0.30 : c;
    c = i > 3.5 ? shifted * 0.14 : c;
    return c;
}

vec3 abyssColour(float h, vec3 tint) {
    float x = fract(h) * 5.0;
    float i = floor(x);
    float f = smoothstep(0.0, 1.0, x - i);
    return mix(abyssEntry(i, tint), abyssEntry(mod(i + 1.0, 5.0), tint), f);
}

void main() {
    // Minecraft like sky (config, ModelOffset.y): the sky in coarse pixels on the sky cube, like a sky texture (colours
    // are not reduced - that turned the dark sky into blotches of wrong colours)
    bool pixelStyle = ModelOffset.y > 0.5;
    vec3 d = normalize(pixelStyle ? (floor(direction / 1.6) + 0.5) * 1.6 : direction);
    float t = GameTime * 1200.0;
    float height = d.y;
    vec3 fog = FogColor.rgb;
    // below the horizon the fog covers the sky completely: no nebula needed (most of the screen when looking down)
    if (height < 0.0) {
        fragColor = vec4(mix(fog, fog * 0.18, smoothstep(0.0, -0.45, height)), 1.0);
        return;
    }
    vec3 tint = fogTint();

    // near-black dome in the fog's hue
    vec3 sky = mix(tint * 0.05 + 0.004, tint * 0.016 + 0.002, smoothstep(0.05, 0.9, height));

    // the nebula
    float n = flow(d * 1.8, t);
    float detail = fbm3(d * 4.5 + n * 1.5 + vec3(0.0, 0.0, t * 0.008));
    float density = smoothstep(0.38, 0.85, n) * (0.55 + 0.45 * detail);
    sky += abyssColour(n * 1.7 + t * 0.003, tint) * density * 1.15;

    // faint cold aurora veils
    if (height > 0.02) {
        float ridge = 1.0 - abs(fbm3(d * 2.6 + vec3(0.0, t * 0.01, 0.0)) * 2.0 - 1.0);
        sky += mix(tint, vec3(1.0), 0.1) * 0.25 * pow(ridge, 9.0) * 0.4 * smoothstep(0.02, 0.5, height);
    }

    // stars: small, twinkling, a few coloured, dimmed behind dense nebula
    vec3 cell = floor(d * 190.0);
    float pick = hash(cell);
    float star = step(0.9955, pick) * (0.55 + 0.45 * sin(t * (1.5 + hash(cell + 3.0) * 3.0) + pick * 60.0));
    vec3 starColour = mix(vec3(0.85, 0.9, 1.0), mix(tint, vec3(1.0), 0.45), step(0.6, hash(cell + 5.0)));
    sky += starColour * star * 0.85 * (1.0 - density * 0.7) * smoothstep(-0.02, 0.25, height) * ColorModulator.a;

    // eclipse: a black orb with a cyan corona
    vec3 eclipse = normalize(ColorModulator.xyz);
    float angle = acos(clamp(dot(d, eclipse), -1.0, 1.0));
    float radius = 0.075;
    float outside = max(angle - radius, 0.0);
    float corona = exp(-outside * 45.0) * 0.75 + exp(-outside * 10.0) * 0.18;
    corona *= 0.85 + 0.15 * sin(t * 0.7 + atan(d.z - eclipse.z, d.x - eclipse.x) * 7.0);
    sky += mix(tint, vec3(1.0), 0.2) * 0.9 * corona * ColorModulator.a;
    sky = mix(sky, vec3(0.0, 0.004, 0.006), 1.0 - smoothstep(radius - 0.003, radius + 0.003, angle));

    // lightning (ModelOffset.x, AbyssLightningClient): the sky takes the lightning colour, like The Midnight
    sky = mix(sky, vec3(1.0, 0.349, 0.349), ModelOffset.x); // #FF5959

    // the horizon melts into the fog of the biome, below it the fog sinks into the abyss
    sky = mix(sky, fog, (1.0 - smoothstep(-0.02, 0.3, height)) * 0.92);
    fragColor = vec4(sky, 1.0);
}
