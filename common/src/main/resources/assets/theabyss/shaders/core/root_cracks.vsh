#version 150

in vec3 Position;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 CrackOffset;
uniform vec3 ViewOffset;

out vec4 vertexColor;
out vec3 worldPos;
out vec3 viewPos;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color;
    worldPos = Position + CrackOffset;
    viewPos = Position + ViewOffset;
}
