#version 150

uniform vec4 ColorModulator;
uniform vec4 FogColor;
uniform float SkyRenderDistance;

in float vertexDistance;

out vec4 fragColor;

void main() {
    // The Abyss used foggy dimension effects in 1.20.1, so its sky got the "thick" sky fog of FogRenderer:
    // start = 5 % of the render distance, end = half the render distance (capped at 192 blocks), blended with smoothstep.
    float fogStart = SkyRenderDistance * 0.05;
    float fogEnd = min(SkyRenderDistance, 192.0) * 0.5;
    float fogValue = 0.0;
    if (vertexDistance > fogStart) {
        fogValue = vertexDistance < fogEnd ? smoothstep(fogStart, fogEnd, vertexDistance) : 1.0;
    }

    fragColor = vec4(mix(ColorModulator.rgb, FogColor.rgb, fogValue * FogColor.a), ColorModulator.a);
}
