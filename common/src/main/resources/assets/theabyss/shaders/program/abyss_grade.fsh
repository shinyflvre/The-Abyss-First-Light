#version 330

// Colour grading of The Abyss (config Visuals/AbyssColors), like Photoshop's Hue/Saturation adjustment:
// hue rotates every colour around the wheel, saturation pulls towards grey (< 0) or towards the pure colour (> 0),
// lightness pulls towards black (< 0) or white (> 0). Sliders -50..50 arrive in fx state pixels 23 (hue | saturation)
// and 31 (lightness), 16 bit each, as 0..1.

uniform sampler2D InSampler;
uniform sampler2D StateSampler;

in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

vec2 unpack16(vec4 texel) {
    vec4 b = floor(texel * 255.0 + 0.5);
    return vec2(b.r * 256.0 + b.g, b.b * 256.0 + b.a) / 65535.0;
}

vec3 rgbToHsl(vec3 c) {
    float high = max(c.r, max(c.g, c.b));
    float low = min(c.r, min(c.g, c.b));
    float l = (high + low) * 0.5;
    float d = high - low;
    if (d < 1.0e-5) {
        return vec3(0.0, 0.0, l);
    }
    float s = l > 0.5 ? d / (2.0 - high - low) : d / (high + low);
    float h = high == c.r ? (c.g - c.b) / d + (c.g < c.b ? 6.0 : 0.0) : (high == c.g ? (c.b - c.r) / d + 2.0 : (c.r - c.g) / d + 4.0);
    return vec3(h / 6.0, s, l);
}

float hueToRgb(float p, float q, float t) {
    t = fract(t);
    if (t < 1.0 / 6.0) return p + (q - p) * 6.0 * t;
    if (t < 0.5) return q;
    if (t < 2.0 / 3.0) return p + (q - p) * (2.0 / 3.0 - t) * 6.0;
    return p;
}

vec3 hslToRgb(vec3 hsl) {
    if (hsl.y < 1.0e-5) {
        return vec3(hsl.z);
    }
    float q = hsl.z < 0.5 ? hsl.z * (1.0 + hsl.y) : hsl.z + hsl.y - hsl.z * hsl.y;
    float p = 2.0 * hsl.z - q;
    return vec3(hueToRgb(p, q, hsl.x + 1.0 / 3.0), hueToRgb(p, q, hsl.x), hueToRgb(p, q, hsl.x - 1.0 / 3.0));
}

void main() {
    vec3 colour = texture(InSampler, texCoord).rgb;
    vec2 hueSaturation = unpack16(texelFetch(StateSampler, ivec2(23, 0), 0)) * 2.0 - 1.0;
    float lightness = unpack16(texelFetch(StateSampler, ivec2(31, 0), 0)).x * 2.0 - 1.0;

    vec3 hsl = rgbToHsl(colour);
    hsl.x = fract(hsl.x + hueSaturation.x * 0.5);
    float saturation = hueSaturation.y;
    hsl.y = saturation < 0.0 ? hsl.y * (1.0 + saturation) : hsl.y + (1.0 - hsl.y) * saturation * hsl.y;
    colour = hslToRgb(clamp(hsl, 0.0, 1.0));
    colour = lightness < 0.0 ? colour * (1.0 + lightness) : colour + (1.0 - colour) * lightness;
    fragColor = vec4(clamp(colour, 0.0, 1.0), 1.0);
}
