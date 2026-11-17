#version 330

uniform float GameTime;

// Astral plane, pass 2 (full resolution, cheap): composes the upscaled nacre / sky colours from astral_nacre.fsh with
// the frame. The blocks are pearl white (PearlWorld swaps the textures): bright, colourless pixels take the nacre colour
// with their own light and shade kept; coloured mobs stay as they are; the sky is replaced by the astral sky.
// Reversed-Z: depth 0 = sky. On top: lavender edges and the crossing flash (fx state pixel 0).

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
uniform sampler2D NacreSampler;
uniform sampler2D StateSampler;

in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

void main() {
    vec4 state = texelFetch(StateSampler, ivec2(0, 0), 0);
    float strength = state.r;
    float flash = state.g;
    float t = GameTime * 1200.0;

    vec3 color = texture(InSampler, texCoord).rgb;
    if (strength > 0.0) {
        vec3 nacre = texture(NacreSampler, texCoord).rgb;
        float depth = fxDepth(texture(DepthSampler, texCoord).r);
        vec3 astral;
        if (depth <= 1.0e-7) {
            astral = nacre;
        } else {
            float luma = dot(color, vec3(0.299, 0.587, 0.114));
            float saturation = max(color.r, max(color.g, color.b)) - min(color.r, min(color.g, color.b));
            float pearl = smoothstep(0.18, 0.55, luma) * (1.0 - smoothstep(0.1, 0.25, saturation));
            astral = mix(color, nacre * (luma * 1.08), 0.8 * pearl);
        }
        color = mix(color, astral, strength);
    }

    // lavender edges, softly breathing
    vec2 c = texCoord - 0.5;
    float edge = smoothstep(0.45, 0.95, length(c)) * (0.85 + 0.15 * sin(t * 1.3));
    color = mix(color, color * vec3(0.88, 0.82, 1.0), edge * 0.45 * strength);
    color = mix(color, vec3(0.97, 0.95, 1.0), flash * flash * 0.45);
    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
