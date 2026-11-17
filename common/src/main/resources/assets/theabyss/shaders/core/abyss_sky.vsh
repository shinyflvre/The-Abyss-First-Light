#version 150

in vec3 Position;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out float vertexDistance;

void main() {
    vec4 viewPosition = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * viewPosition;

    // 1.20.1 spherical fog distance, measured in view space (includes the translation of the abyss disc)
    vertexDistance = length(viewPosition.xyz);
}
