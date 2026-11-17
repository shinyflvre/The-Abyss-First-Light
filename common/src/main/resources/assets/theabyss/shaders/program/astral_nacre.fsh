#version 330

uniform float GameTime;
uniform ivec3 CameraBlockPos;
uniform vec3 CameraOffset;

// Astral plane, pass 1 (small 1280x720 target, upscaled afterwards - the patterns are soft, so this costs nothing in
// look and saves most of the GPU time): the nacre colour of every pixel in WORLD space and the astral sky.
// rgb = nacre colour (world) or sky colour (sky). 26.3 uses reversed-Z: depth 0 = sky, distance = near / depth.
// Camera of this frame (incl. view bobbing) from fx_camera.glsl, the position from Globals.

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

const float NEAR = 0.05;

vec2 unpack16(vec4 texel) {
    vec4 b = floor(texel * 255.0 + 0.5);
    return vec2(b.r * 256.0 + b.g, b.b * 256.0 + b.a) / 65535.0;
}

float hash(vec3 p) {
    p = fract(p * 0.3183099 + 0.1);
    p *= 17.0;
    return fract(p.x * p.y * p.z * (p.x + p.y + p.z));
}

float noise(vec3 x) {
    vec3 i = floor(x);
    vec3 f = fract(x);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(mix(hash(i + vec3(0, 0, 0)), hash(i + vec3(1, 0, 0)), f.x),
                   mix(hash(i + vec3(0, 1, 0)), hash(i + vec3(1, 1, 0)), f.x), f.y),
               mix(mix(hash(i + vec3(0, 0, 1)), hash(i + vec3(1, 0, 1)), f.x),
                   mix(hash(i + vec3(0, 1, 1)), hash(i + vec3(1, 1, 1)), f.x), f.y), f.z);
}

float fbm(vec3 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 3; i++) {
        value += amplitude * noise(p);
        p = p * 2.03 + vec3(1.7, 9.2, 3.1);
        amplitude *= 0.5;
    }
    return value;
}

// one level of domain warping: soft, flowing streaks
float flow(vec3 p, float t) {
    vec3 q = vec3(fbm(p + vec3(0.0, t * 0.04, 0.0)), fbm(p + vec3(5.2, 1.3, 2.8) - t * 0.03), fbm(p + vec3(1.7, 9.2, 3.1)));
    return fbm(p + 2.0 * q);
}

// pearl palette: cream, blush, lavender, mint, sky blue, champagne (cyclic), 20% more saturated than plain pastels
vec3 paletteEntry(float i) {
    vec3 c = vec3(1.00, 0.97, 0.93);
    c = i > 0.5 ? vec3(1.00, 0.84, 0.91) : c;
    c = i > 1.5 ? vec3(0.87, 0.82, 1.00) : c;
    c = i > 2.5 ? vec3(0.80, 0.97, 0.92) : c;
    c = i > 3.5 ? vec3(0.82, 0.91, 1.00) : c;
    c = i > 4.5 ? vec3(1.00, 0.93, 0.80) : c;
    return clamp(mix(vec3(1.0), c, 1.2), 0.0, 1.0);
}

vec3 pearlColour(float h) {
    float x = fract(h) * 6.0;
    float i = floor(x);
    float f = smoothstep(0.0, 1.0, x - i);
    return mix(paletteEntry(i), paletteEntry(mod(i + 1.0, 6.0)), f);
}

void main() {
    float t = GameTime * 1200.0;
    float depth = fxDepth(texture(DepthSampler, texCoord).r);

    fxCameraSetup();
    vec3 ray = fxViewRay(texCoord);
    vec3 dir = normalize(ray);

    vec3 colour;
    if (depth <= 1.0e-7) {
        // astral sky: pearl nebula flowing across the dome, cream-blush at the horizon, lavender-blue above
        float height = dir.y;
        vec3 sky = height >= 0.0
            ? mix(vec3(1.00, 0.90, 0.92), vec3(0.70, 0.70, 0.95), smoothstep(0.0, 0.85, height))
            : mix(vec3(1.00, 0.90, 0.92), vec3(0.78, 0.74, 0.90), smoothstep(0.0, 0.4, -height));
        float n = flow(dir * 2.2, t);
        sky = mix(sky, pearlColour(n * 1.8 + t * 0.01), 0.5 * smoothstep(0.25, 0.75, n));
        // faint twinkling lights
        vec3 cell = floor(dir * 140.0);
        float star = step(0.9965, hash(cell)) * (0.6 + 0.4 * sin(t * 3.0 + hash(cell + 7.0) * 40.0));
        colour = sky + star * 0.5 * smoothstep(-0.05, 0.3, height);
    } else {
        // world position (camera block position kept small so the float stays precise)
        float z = NEAR / max(depth, 1.0e-7);
        ivec3 block = ((CameraBlockPos % 8192) + 8192) % 8192;
        vec3 world = vec3(block) - CameraOffset + fxEye + ray * z;
        float n = flow(world * 0.12, t);
        vec3 nacre = pearlColour(n * 2.4 + dir.y * 0.3 + t * 0.012);
        // a soft sheen gliding across the world
        float wave = 0.5 + 0.5 * sin(dot(world, vec3(0.06, 0.02, -0.045)) - t * 0.5 + n * 2.5);
        colour = nacre * (1.0 + pow(wave, 10.0) * 0.15);
    }
    fragColor = vec4(clamp(colour, 0.0, 1.0), 1.0);
}
