#version 330

uniform float GameTime;
uniform vec2 ScreenSize;

// The Nightblade's presence (NightbladeAtmosphere): near him the picture turns colder and paler and its edges sink into
// deep blue; in phase 3 the edges pulse like a heartbeat; a new phase sends a shock wave over the screen.
// fx state pixel 0: B = presence, A = shock wave progress (0 = none); pixel 1: A = heartbeat.

uniform sampler2D InSampler;
uniform sampler2D StateSampler;

in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

void main() {
    vec4 state0 = texelFetch(StateSampler, ivec2(0, 0), 0);
    float presence = state0.b;
    float ripple = state0.a;
    float heart = texelFetch(StateSampler, ivec2(1, 0), 0).a;
    float aspect = ScreenSize.x / max(ScreenSize.y, 1.0);
    vec2 p = (texCoord - 0.5) * vec2(aspect, 1.0);
    float r = length(p);

    // the shock wave of a new phase: a ring rushing outwards that bends the picture
    vec2 uv = texCoord;
    float ring = 0.0;
    if (ripple > 0.0) {
        float radius = ripple * 1.3;
        float d = r - radius;
        ring = exp(-d * d / 0.003) * (1.0 - ripple);
        uv -= normalize(p + 1.0e-5) / vec2(aspect, 1.0) * ring * 0.025;
    }
    vec3 colour = texture(InSampler, uv).rgb;

    // colder and paler
    float luma = dot(colour, vec3(0.299, 0.587, 0.114));
    colour = mix(colour, vec3(luma) * vec3(0.86, 0.95, 1.08), 0.28 * presence);

    // heartbeat (about 66 per minute): two quick beats, then rest
    float t = GameTime * 1200.0 * 1.1;
    float beat = heart * (pow(max(sin(t * 6.2831853), 0.0), 12.0) + 0.6 * pow(max(sin(t * 6.2831853 - 0.9), 0.0), 12.0));

    // edges sink into deep blue
    float vignette = smoothstep(0.32, 0.95, r);
    colour *= 1.0 - vignette * (0.5 * presence + 0.3 * beat);
    colour = mix(colour, vec3(0.0, 0.025, 0.07), vignette * 0.35 * presence);

    // the shock wave's crest glows faintly in his blue
    colour += vec3(0.35, 0.75, 1.0) * ring * 0.25;
    fragColor = vec4(clamp(colour, 0.0, 1.0), 1.0);
}
