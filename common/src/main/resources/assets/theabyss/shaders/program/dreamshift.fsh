#version 330

uniform float GameTime;

// Dream Shifter, final pass. Pixel 1 of the fx state texture: R = intensity, G = 0 leaving / 1 arriving.
// Leaving: the colour drains, the world is pulled and twisted inwards, black veins and tendrils creep in from the edges
// with a heartbeat until nothing is left. Arriving: the new world tears open along a jagged rift that widens until it
// fills the screen, still trembling and cold for a moment. Colours: deep blue and cyan (owner request).

uniform sampler2D InSampler;
uniform sampler2D StateSampler;

uniform vec2 OutSize;

in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + vec2(1.0, 0.0)), u.x), mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), u.x), u.y);
}

float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 5; i++) {
        value += amplitude * noise(p);
        p *= 2.03;
        amplitude *= 0.5;
    }
    return value;
}

vec3 bleed(vec3 color, float amount) {
    float luma = dot(color, vec3(0.299, 0.587, 0.114));
    vec3 cold = vec3(luma) * vec3(0.55, 0.78, 0.92);
    return mix(color, cold, amount);
}

void main() {
    vec4 state = texelFetch(StateSampler, ivec2(1, 0), 0);
    float e = state.r;
    float arriving = state.g;
    float t = GameTime * 1200.0;
    float aspect = OutSize.x / max(OutSize.y, 1.0);

    vec2 c = texCoord - 0.5;
    vec2 p = vec2(c.x * aspect, c.y);
    float r = length(p);
    float angle = atan(p.y, p.x);

    // heartbeat: lub-dub, then silence
    float phase = fract(t * 1.15);
    float beat = exp(-phase * 16.0) + 0.7 * exp(-max(phase - 0.2, 0.0) * 16.0) * step(0.2, phase);

    // the image trembles and, while leaving, is pulled towards the middle and twisted
    vec2 shake = (vec2(noise(vec2(t * 37.0, 1.7)), noise(vec2(4.3, t * 37.0))) - 0.5) * 0.014 * e * e;
    float leaving = 1.0 - arriving;
    float twist = e * e * 1.1 * (1.0 - smoothstep(0.0, 0.9, r)) * leaving;
    mat2 rot = mat2(cos(twist), -sin(twist), sin(twist), cos(twist));
    vec2 uv = 0.5 + rot * (c * (1.0 - 0.2 * e * e * leaving)) + shake;

    vec2 split = (r > 0.0 ? p / r : vec2(0.0)) * 0.016 * e * r;
    split.x /= aspect;
    vec3 scene = vec3(texture(InSampler, uv + split).r, texture(InSampler, uv).g, texture(InSampler, uv - split).b);
    scene = bleed(scene, e * 0.9);

    vec3 color;
    if (arriving < 0.5) {
        // darkness creeping in from the edges in tendrils, pulsing with the heartbeat, veins spreading over the image
        float tendrils = fbm(vec2(angle * 2.6, r * 3.2 - t * 0.35));
        float edge = 1.45 - e * 1.7 - beat * 0.07 * e;
        float dark = smoothstep(edge - 0.28, edge + 0.04, r + (tendrils - 0.5) * 0.5 * (0.25 + e));
        float veins = smoothstep(0.035, 0.0, abs(fbm(p * 5.0 + vec2(0.0, t * 0.15)) - 0.5)) * smoothstep(0.15, 0.6, e);
        color = scene * (1.0 - veins * 0.75);
        float band = dark * (1.0 - dark) * 4.0;
        color += band * vec3(0.0, 0.14, 0.24) * e;
        color = mix(color, vec3(0.0), dark);
    } else {
        // the new world tears open along a jagged rift that widens until it fills the screen
        float jag = (fbm(vec2(texCoord.y * 6.0, 2.0 + t * 0.25)) - 0.5) * 0.3 + (noise(vec2(texCoord.y * 48.0, 7.0)) - 0.5) * 0.035;
        float crack = abs(p.x - jag) + (fbm(p * 4.0 + t * 0.2) - 0.5) * 0.12 * e;
        float open = (1.0 - e) * 1.25;
        float reveal = 1.0 - smoothstep(max(open - 0.05, 0.0), open + 0.004, crack);
        float rim = exp(-abs(crack - open) * 70.0) * e;
        vec3 inside = scene + vec3(0.0, 0.07, 0.12) * e;
        color = mix(vec3(0.0), inside, reveal);
        color += rim * vec3(0.30, 0.85, 1.0) * (0.6 + 0.4 * beat);
    }

    // a vignette that never quite lets go while the effect runs
    color *= 1.0 - smoothstep(0.35, 1.05, r) * 0.65 * e;
    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
