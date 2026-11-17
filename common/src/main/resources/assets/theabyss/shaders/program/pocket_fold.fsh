#version 330

uniform float GameTime;

uniform sampler2D InSampler;
uniform sampler2D StateSampler;

uniform vec2 OutSize;

in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

const vec3 ORANGE = vec3(1.0, 0.604, 0.235);
const vec3 PALE = vec3(1.0, 0.878, 0.69);
const vec3 EMBER = vec3(0.36, 0.11, 0.02);

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
    for (int i = 0; i < 4; i++) {
        value += amplitude * noise(p);
        p *= 2.07;
        amplitude *= 0.5;
    }
    return value;
}

vec3 radialBlur(vec2 uv, vec2 centre, float strength, float split) {
    vec3 sum = vec3(0.0);
    vec2 dir = uv - centre;
    for (int i = 0; i < 12; i++) {
        float s = 1.0 - strength * float(i) / 11.0;
        vec2 at = centre + dir * s;
        sum.r += texture(InSampler, centre + (at - centre) * (1.0 + split)).r;
        sum.g += texture(InSampler, at).g;
        sum.b += texture(InSampler, centre + (at - centre) * (1.0 - split)).b;
    }
    return sum / 12.0;
}

void main() {
    vec4 state = texelFetch(StateSampler, ivec2(85, 0), 0);
    float e = state.r;
    bool arriving = state.g > 0.5;
    float t = GameTime * 1200.0;
    float aspect = OutSize.x / max(OutSize.y, 1.0);

    vec2 c = texCoord - 0.5;
    vec2 p = vec2(c.x * aspect, c.y);
    float r = length(p);
    float angle = atan(p.y, p.x);

    float k = arriving ? e : e * e * (3.0 - 2.0 * e);
    float flow = arriving ? 1.0 : -1.0;

    vec2 shimmer = vec2(fbm(p * 6.0 + vec2(t * 0.9 * flow, 0.0)), fbm(p * 6.0 + vec2(3.1, -t * 0.9 * flow))) - 0.5;
    float pinch = arriving ? 1.0 + 0.35 * k * k : 1.0 - 0.32 * k * k * (1.0 - smoothstep(0.0, 0.9, r));
    vec2 uv = 0.5 + c * pinch + shimmer * 0.018 * k;
    vec3 scene = radialBlur(uv, vec2(0.5), 0.42 * k * k, 0.012 * k);

    float luma = dot(scene, vec3(0.299, 0.587, 0.114));
    vec3 amber = mix(EMBER, PALE, smoothstep(0.05, 0.9, luma)) * (0.55 + luma);
    vec3 color = mix(scene, amber, 0.55 * k);

    float swirl = fbm(vec2(angle * 2.2 + r * 4.0 * flow - t * 1.6 * flow, r * 3.5 - t * 0.8));
    float strand = smoothstep(0.045, 0.0, abs(swirl - 0.5)) * smoothstep(0.08, 0.5, r);
    float reach = arriving ? smoothstep(0.2, 1.0, k) : smoothstep(1.4 - k * 1.5, 1.4 - k * 1.5 + 0.35, r + 0.25);
    color += ORANGE * strand * reach * (0.9 + 0.6 * k);
    color += PALE * strand * reach * smoothstep(0.6, 1.0, k) * 0.4;

    float sparks = step(0.9965, hash(floor(vec2(angle * 40.0, r * 60.0 - t * 9.0 * flow)))) * smoothstep(0.15, 0.7, r);
    color += ORANGE * sparks * k * 1.5;

    float edge = smoothstep(0.35, 1.05, r);
    color = mix(color, color * 0.25 + EMBER * 0.6, edge * 0.75 * k);

    float bloom = arriving ? smoothstep(0.55, 1.0, k) : smoothstep(0.62, 1.0, k);
    float core = exp(-r * mix(9.0, 0.6, bloom));
    color += PALE * core * bloom * 1.4 + ORANGE * exp(-r * 3.0) * k * 0.35;
    color = mix(color, PALE * 0.92 + ORANGE * 0.08, smoothstep(0.82, 1.0, k));

    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
