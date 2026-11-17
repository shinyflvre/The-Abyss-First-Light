#version 150

uniform vec4 FogColor;
uniform float GameTime;

// Rift of Sins sky (always on, no config): no sun, moon, stars or colours. A near-black void filled with smoke that
// rolls slowly and is torn into ragged shreds: dark rips with slightly paler edges, thin ash filaments between the
// banks; the horizon melts into the fog, below it everything sinks into black.
// Performance (owner 16.10.2026, the sky fills most of the screen in the rift): 8 noise lookups per pixel
// (first version 22): warp 2, smoke 3, rip 2, shreds 1; the smoke value itself shades the banks.

in vec3 direction;

out vec4 fragColor;

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

float fbm3(vec3 p) {
    float value = 0.5 * noise(p);
    p = p * 2.03 + vec3(1.7, 9.2, 3.1);
    value += 0.25 * noise(p);
    p = p * 2.03 + vec3(1.7, 9.2, 3.1);
    return (value + 0.125 * noise(p)) / 0.875;
}

void main() {
    vec3 d = normalize(direction);
    float height = d.y;
    vec3 fog = FogColor.rgb;
    if (height < -0.02) {
        fragColor = vec4(mix(fog, vec3(0.0), smoothstep(-0.02, -0.4, height)), 1.0);
        return;
    }
    float t = GameTime * 1200.0;
    float breath = 0.5 + 0.5 * sin(t * 0.21);

    vec3 sky = mix(vec3(0.022, 0.022, 0.025), vec3(0.004, 0.004, 0.005), smoothstep(0.0, 0.85, height));

    vec3 p = d * 1.9;
    vec2 q = vec2(noise(p + vec3(0.0, t * 0.010, 0.0)), noise(p + vec3(5.2, 1.3, 2.8) - t * 0.008)) - 0.5;
    vec3 warped = p + vec3(q.x, q.y, -q.x) * 1.8 + vec3(t * 0.005, 0.0, -t * 0.004);
    float smoke = fbm3(warped);
    float density = smoothstep(0.36 - breath * 0.04, 0.78, smoke);

    vec3 r = d * vec3(4.2, 2.4, 4.2) + vec3(q * 2.4, 0.0) + vec3(0.0, -t * 0.012, t * 0.004);
    float ridge = abs((0.65 * noise(r) + 0.35 * noise(r * 2.07 + 3.3)) * 2.0 - 1.0);
    float rip = 1.0 - smoothstep(0.02, 0.11, ridge);
    float ripEdge = smoothstep(0.03, 0.08, ridge) * (1.0 - smoothstep(0.08, 0.17, ridge));
    density *= 1.0 - rip * 0.92;

    float shade = smoothstep(0.5, 0.9, smoke);
    sky = mix(sky, mix(vec3(0.040, 0.040, 0.044), vec3(0.105, 0.104, 0.112), shade), density);
    sky += vec3(0.07, 0.07, 0.075) * ripEdge * density * (0.6 + 0.4 * breath);
    sky = mix(sky, vec3(0.0), rip * 0.85);

    float shreds = noise(vec3(d.x * 7.0, d.y * 2.2, d.z * 7.0) + vec3(q * 3.0, 0.0) + vec3(t * 0.018, 0.0, t * 0.011));
    sky += vec3(0.045, 0.045, 0.048) * smoothstep(0.66, 0.85, shreds) * (1.0 - density * 0.6);

    sky = mix(sky, fog, (1.0 - smoothstep(-0.02, 0.32, height)) * 0.9);
    fragColor = vec4(sky, 1.0);
}
