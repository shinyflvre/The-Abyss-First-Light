#version 330

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
uniform sampler2D StateSampler;
uniform sampler2D LightsSampler;
uniform sampler2D HandSampler;

// The exact camera of this frame for world-space post effects (rebuilding positions from the depth buffer).
// Written by ScreenFx#onExtractLevel into fx state pixels 8..21 as 32-bit floats (bits in R,G,B,A, most significant
// first): the inverse of (view bobbing x hurt tilt x view rotation) - mat3 column-major + eye offset - and the
// projection scale (m00, m11). With view bobbing the level is rendered with a bobbing projection; rebuilding positions
// from yaw/pitch alone made light and noise slide over the blocks and flicker at block edges while walking.
// Needs `uniform sampler2D StateSampler;` declared before the include. Call fxCameraSetup() first.
// Positions are relative to the camera position (Globals: CameraBlockPos - CameraOffset). Reversed-Z depth.

mat3 fxRotation;
vec3 fxEye;
vec2 fxProjection;

float fxFloat(int index) {
    uvec4 b = uvec4(texelFetch(StateSampler, ivec2(index, 0), 0) * 255.0 + 0.5);
    return uintBitsToFloat(b.r << 24u | b.g << 16u | b.b << 8u | b.a);
}

void fxCameraSetup() {
    fxRotation = mat3(fxFloat(8), fxFloat(9), fxFloat(10), fxFloat(11), fxFloat(12), fxFloat(13), fxFloat(14), fxFloat(15), fxFloat(16));
    fxEye = vec3(fxFloat(17), fxFloat(18), fxFloat(19));
    fxProjection = vec2(fxFloat(20), fxFloat(21));
}

// view ray through a screen position, scaled so that one unit = one block of view depth
vec3 fxViewRay(vec2 uv) {
    return fxRotation * vec3((uv * 2.0 - 1.0) / fxProjection, -1.0);
}

// camera-relative position of the surface seen at uv (depth = raw reversed-Z depth, > 0)
vec3 fxPosition(vec2 uv, float depth) {
    return fxEye + fxViewRay(uv) * (0.05 / max(depth, 1.0e-7));
}

in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

const vec3 LUMA = vec3(0.299, 0.587, 0.114);
const float STRENGTH = 1.25;

vec2 unpack16(vec4 texel) {
    vec4 b = floor(texel * 255.0 + 0.5);
    return vec2(b.r * 256.0 + b.g, b.b * 256.0 + b.a) / 65535.0;
}

vec3 positionAt(vec2 uv) {
    return fxPosition(uv, fxDepth(texture(DepthSampler, uv).r));
}

void main() {
    vec3 color = texture(InSampler, texCoord).rgb;
    float depth = fxDepth(texture(DepthSampler, texCoord).r);
    int count = int(texelFetch(LightsSampler, ivec2(0, 0), 0).r * 255.0 + 0.5);
    if (depth <= 1.0e-7 || count == 0 || texture(HandSampler, texCoord).r < 1.0) {
        fragColor = vec4(color, 1.0);
        return;
    }
    fxCameraSetup();
    vec3 position = fxPosition(texCoord, depth);
    vec2 texel = 1.0 / vec2(textureSize(DepthSampler, 0));
    vec3 left = positionAt(texCoord - vec2(texel.x, 0.0));
    vec3 right = positionAt(texCoord + vec2(texel.x, 0.0));
    vec3 down = positionAt(texCoord - vec2(0.0, texel.y));
    vec3 up = positionAt(texCoord + vec2(0.0, texel.y));
    vec3 dx = distance(right, position) < distance(position, left) ? right - position : position - left;
    vec3 dy = distance(up, position) < distance(position, down) ? up - position : position - down;
    vec3 normal = normalize(cross(dx, dy));
    if (dot(normal, position - fxEye) > 0.0) {
        normal = -normal;
    }
    vec3 light = vec3(0.0);
    for (int i = 0; i < count; i++) {
        int p = 1 + i * 3;
        vec2 xy = unpack16(texelFetch(LightsSampler, ivec2(p, 0), 0)) * 256.0 - 128.0;
        vec4 second = texelFetch(LightsSampler, ivec2(p + 1, 0), 0);
        float z = unpack16(vec4(second.rg, 0.0, 0.0)).x * 256.0 - 128.0;
        float reach = second.b * 255.0 / 8.0;
        vec3 to = vec3(xy, z) - position;
        float d = length(to);
        if (d >= reach) {
            continue;
        }
        float falloff = 1.0 - d / reach;
        float facing = clamp(dot(normal, to / max(d, 0.001)) * 0.75 + 0.25, 0.0, 1.0);
        light += texelFetch(LightsSampler, ivec2(p + 2, 0), 0).rgb * second.a * STRENGTH * falloff * falloff * facing;
    }
    float strength = max(light.r, max(light.g, light.b));
    if (strength > 0.002) {
        vec3 hue = clamp(light / strength, 0.0, 1.0);
        float luma = dot(color, LUMA);
        vec3 tinted = hue * (luma / max(dot(hue, LUMA), 0.3));
        float boost = strength * 1.25;
        color = mix(color, clamp(tinted, 0.0, 1.0), min(1.0, boost));
        color += color * hue * min(boost, 2.5) * 1.5 * (1.0 - color) + hue * min(boost, 2.5) * 0.06;
    }
    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
