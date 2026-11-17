#version 330
#extension GL_ARB_separate_shader_objects : require

// Coloured light engine, composite: surfaces in coloured light take on its colour (keeping their brightness, so the
// texture stays readable - the warm vanilla block light turns into the plant's colour) and get a little brighter.
// Light source pixels come in as 0 - the plants never brighten themselves.

uniform sampler2D InSampler;
uniform sampler2D SurfaceSampler;
uniform sampler2D StateSampler;
uniform sampler2D HandSampler;

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

const vec3 LUMA = vec3(0.299, 0.587, 0.114);

// config sliders LightSaturation / LightBrightness (1 = 100%), fx state pixel 22
vec2 lightLook() {
    vec4 b = floor(texelFetch(StateSampler, ivec2(22, 0), 0) * 255.0 + 0.5);
    return vec2(b.r * 256.0 + b.g, b.b * 256.0 + b.a) / 65535.0 * 4.0;
}

void main() {
    vec3 color = texture(InSampler, texCoord).rgb;
    if (texture(HandSampler, texCoord).r > 0.0) {
        fragColor = vec4(color, 1.0);
        return;
    }
    vec4 surface = texture(SurfaceSampler, texCoord);
    vec2 look = lightLook();
    vec3 light = surface.rgb * surface.a;
    float strength = max(light.r, max(light.g, light.b));
    if (strength > 0.002) {
        // +25% saturation of the light colour (owner: colours must be more present)
        vec3 hue = clamp(1.0 - (1.0 - light / strength) * 1.25 * look.x, 0.0, 1.0);
        float luma = dot(color, LUMA);
        vec3 tinted = hue * (luma / max(dot(hue, LUMA), 0.3));
        float boost = strength * 1.25 * look.y;
        float amount = min(1.0, boost);
        color = mix(color, clamp(tinted, 0.0, 1.0), amount);
        color += color * hue * min(boost, 2.5) * 1.5 * (1.0 - color) + hue * boost * 0.03;
    }
    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
