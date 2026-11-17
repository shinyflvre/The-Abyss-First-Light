#version 330

// Dream Shifter, pass 1: the previous frame is pulled outwards a little and kept - the world smears into a tunnel.

uniform sampler2D InSampler;
uniform sampler2D PrevSampler;
uniform sampler2D StateSampler;

in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

void main() {
    float e = texelFetch(StateSampler, ivec2(1, 0), 0).r;
    vec3 now = texture(InSampler, texCoord).rgb;
    vec3 previous = texture(PrevSampler, 0.5 + (texCoord - 0.5) * 0.982).rgb;
    fragColor = vec4(mix(now, previous, 0.58 * e), 1.0);
}
