#version 330

uniform vec2 ScreenSize;

// Impact frame of the Nightblade's blink strike (ImpactFrame, strength in fx state pixel 1 B):
// strength 1 = inverted stark black and white with speed lines (the first ~3 frames), 0.7 = stark black and white,
// below that it fades back to the normal image. The whites carry a breath of his blue. A parried strike (pixel 84 R)
// shows the frame in cyan instead of white.

uniform sampler2D InSampler;
uniform sampler2D StateSampler;

in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

float hash(float n) {
    return fract(sin(n * 12.9898) * 43758.5453);
}

void main() {
    vec3 colour = texture(InSampler, texCoord).rgb;
    float strength = texelFetch(StateSampler, ivec2(1, 0), 0).b;
    if (strength <= 0.0) {
        fragColor = vec4(colour, 1.0);
        return;
    }
    float luma = dot(colour, vec3(0.299, 0.587, 0.114));
    // stark: almost only black and white, the threshold a little above the dark Abyss average
    float ink = smoothstep(0.16, 0.24, luma);
    float parry = texelFetch(StateSampler, ivec2(84, 0), 0).r;
    vec3 light = mix(vec3(0.9, 0.97, 1.0), vec3(0.2, 0.95, 1.0), parry);
    vec3 frame = mix(vec3(0.0), light, ink);

    // speed lines rushing to the centre
    float aspect = ScreenSize.x / max(ScreenSize.y, 1.0);
    vec2 p = (texCoord - 0.5) * vec2(aspect, 1.0);
    float angle = atan(p.y, p.x);
    float sector = floor((angle + 3.14159265) / 6.28318531 * 90.0);
    float line = step(0.78, hash(sector)) * smoothstep(0.22, 0.75, length(p));

    bool inverted = strength > 0.85;
    if (inverted) {
        frame = mix(light, vec3(0.0), ink);
        frame = mix(frame, vec3(0.0), line * 0.9);
    } else {
        frame = mix(frame, light, line * 0.6);
    }
    float amount = inverted ? 1.0 : clamp(strength / 0.7, 0.0, 1.0);
    fragColor = vec4(mix(colour, frame, amount), 1.0);
}
