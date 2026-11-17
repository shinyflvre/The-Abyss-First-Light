#version 150

uniform sampler2D Sampler0;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    float depth = texture(Sampler0, texCoord).r;
    if (depth >= 1.0) {
        discard;
    }
    gl_FragDepth = depth;
    fragColor = vec4(0.0);
}
