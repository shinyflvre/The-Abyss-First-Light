#version 330

uniform float GameTime;
uniform ivec3 CameraBlockPos;
uniform vec3 CameraOffset;

// Volumetric noise fog, pass 1 (small target, upscaled afterwards): marches along every view ray through drifting 3D
// noise anchored in the world - fog banks and wisps you walk through, lying on the terrain and thinning out upwards.
// Height: the smoothed ground height around the camera (fx state pixel 7, ScreenFx#updateFogGround) - full density up to
// a few blocks above it. Colour: the final fog colour of the frame (biome fog, pearl white on the astral plane, pixel 4)
// with its hue and saturation kept, only scaled up to at least MIST_BRIGHTNESS so dark fogs do not turn black (no grey).
// Steps are spaced quadratically: fine near the camera (wisps right in front of you), coarse in the distance.
// Camera from fx_camera.glsl + Globals; reversed-Z depth (0 = sky, distance = near / depth).
// rgb = in-scattered fog colour, a = 1 - transmittance.

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

const float MIST_BRIGHTNESS = 0.25;
const int STEPS = 20;
const float MAX_DISTANCE = 96.0;
const float LAYER = 7.0;     // full density up to this many blocks above the ground
const float FALLOFF = 9.0;   // ...thinning out above it
const float BANK_DENSITY = 0.15;
const float HAZE_DENSITY = 0.0025;

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

void main() {
    float t = GameTime * 1200.0;
    fxCameraSetup();
    vec3 ray = fxViewRay(texCoord);
    vec3 dir = normalize(ray);

    float depth = fxDepth(texture(DepthSampler, texCoord).r);
    float sceneDistance = depth <= 1.0e-7 ? 1.0e6 : length(ray * (0.05 / depth));
    float maxDistance = min(sceneDistance, MAX_DISTANCE);

    // camera: x/z kept small for float precision, y real (the fog depends on the height)
    ivec3 block = ((CameraBlockPos % 8192) + 8192) % 8192;
    vec3 camera = vec3(float(block.x), float(CameraBlockPos.y), float(block.z)) - CameraOffset + fxEye;
    float ground = unpack16(texelFetch(StateSampler, ivec2(7, 0), 0)).x * 2048.0 - 1024.0;
    float top = ground + LAYER;

    vec3 fogColour = texelFetch(StateSampler, ivec2(4, 0), 0).rgb;
    float strongest = max(max(fogColour.r, fogColour.g), max(fogColour.b, 1.0e-4));
    vec3 mist = fogColour * max(1.0, MIST_BRIGHTNESS / strongest);
    vec3 wind = vec3(t * 0.6, t * 0.05, t * 0.25);

    // stable per-pixel jitter (interleaved gradient noise): no banding, no crawling grain
    float jitter = fract(52.9829189 * fract(dot(gl_FragCoord.xy, vec2(0.06711056, 0.00583715))));

    // only march where there can be fog: below the height where it has faded out (performance - most sky rays end here)
    float ceiling = top + FALLOFF * 6.2;
    float start = 0.0;
    if (camera.y > ceiling) {
        if (dir.y >= -1.0e-4) {
            fragColor = vec4(0.0);
            return;
        }
        start = (camera.y - ceiling) / -dir.y;
    } else if (dir.y > 1.0e-4) {
        maxDistance = min(maxDistance, (ceiling - camera.y) / dir.y);
    }
    if (start >= maxDistance) {
        fragColor = vec4(0.0);
        return;
    }

    float transmittance = 1.0;
    vec3 scattered = vec3(0.0);
    float previous = start;
    float span = min(MAX_DISTANCE, maxDistance) - start;
    for (int i = 0; i < STEPS; i++) {
        float k = (float(i) + jitter) / float(STEPS);
        float travelled = start + span * k * k;
        if (travelled > maxDistance) {
            travelled = maxDistance;
        }
        float stepLength = travelled - previous;
        previous = travelled;
        vec3 p = camera + dir * travelled;
        float height = exp(-max(p.y - top, 0.0) / FALLOFF);
        if (height > 0.002 && stepLength > 0.0) {
            // big slow banks, broken up by finer wisps
            vec3 q = p + wind;
            float bank = smoothstep(0.44, 0.74, 0.6 * noise(q * 0.055) + 0.4 * noise(q * 0.12 + vec3(5.2, 1.3, 2.8)));
            // clear gaps between the banks (no wisp noise needed there), ragged wisps at their edges
            float wisp = bank > 0.0 ? noise(q * 0.21 + vec3(1.7, 9.2, 3.1) - wind * 0.4) : 0.5;
            float shape = bank * (0.3 + 1.4 * wisp * wisp);
            float density = (shape * BANK_DENSITY + HAZE_DENSITY) * height;
            float stepTransmittance = exp(-density * stepLength);
            vec3 colour = mist * (0.85 + 0.3 * wisp);
            scattered += colour * (1.0 - stepTransmittance) * transmittance;
            transmittance *= stepTransmittance;
            if (transmittance < 0.02) {
                break;
            }
        }
        if (travelled >= maxDistance) {
            break;
        }
    }
    fragColor = vec4(scattered, 1.0 - transmittance);
}
