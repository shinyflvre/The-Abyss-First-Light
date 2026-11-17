#version 330


// Coloured light engine, pass 1: the light arriving at every visible surface, read from the light volume of ColoredLight.
// Volume: 96x48x96 cells, slice y stored as the 96x96 tile (y % 8, y / 8) of a 768x576 texture; RGB = light colour x
// brightness, A = open sky (254) | light source cell (bit 0). Two volume slots (A, B): each new volume fades in over the
// older one (no popping). Corners relative to the camera in fx state pixels 5/6 (A) and 24/25 (B) (16 bit, -128..128),
// daylight in pixel 6 BA, weight of B in pixel 25 BA. Positions from the depth buffer (reversed-Z: distance = near / depth).
// The surface normal is rebuilt from the neighbouring depth values and snapped to the block axes, then the volume is read
// half a block in front of the surface - the air cell the face looks into, like Minecraft's own lighting - with
// trilinear filtering (smooth, darker in corners). Pixels of a light source cell (the plant itself) get no light.
// rgb = light (already faded towards the volume border and under the open daytime sky).

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
uniform sampler2D VolumeASampler;
uniform sampler2D VolumeBSampler;

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

const vec3 SIZE = vec3(96.0, 48.0, 96.0);
const vec2 ATLAS = vec2(768.0, 576.0);

vec2 unpack16(vec4 texel) {
    vec4 b = floor(texel * 255.0 + 0.5);
    return vec2(b.r * 256.0 + b.g, b.b * 256.0 + b.a) / 65535.0;
}

vec3 positionAt(vec2 uv) {
    return fxPosition(uv, fxDepth(texture(DepthSampler, uv).r));
}

vec3 corner(int pixel) {
    vec2 xy = unpack16(texelFetch(StateSampler, ivec2(pixel, 0), 0)) * 256.0 - 128.0;
    float z = unpack16(texelFetch(StateSampler, ivec2(pixel + 1, 0), 0)).x * 256.0 - 128.0;
    return vec3(xy, z);
}

// one horizontal slice, bilinear inside the tile (the coordinate is kept away from the tile border)
vec4 slice(sampler2D volume, float y, vec2 xz) {
    if (y < 0.0 || y > SIZE.y - 1.0) {
        return vec4(0.0);
    }
    vec2 tile = vec2(mod(y, 8.0), floor(y / 8.0)) * SIZE.xz;
    vec2 inside = clamp(xz, vec2(0.5), SIZE.xz - 0.5);
    return texture(volume, (tile + inside) / ATLAS);
}

// is the surface point inside a light source cell (the glowing plant itself)?
bool isSource(sampler2D volume, vec3 local) {
    ivec3 own = ivec3(floor(local));
    if (any(lessThan(own, ivec3(0))) || any(greaterThanEqual(own, ivec3(SIZE)))) {
        return false;
    }
    ivec2 texelPos = ivec2(own.y % 8, own.y / 8) * ivec2(SIZE.xz) + own.xz;
    return (int(texelFetch(volume, texelPos, 0).a * 255.0 + 0.5) & 1) == 1;
}

// light half a block in front of the surface (trilinear), faded at the volume border and under the open daytime sky
vec3 lightAt(sampler2D volume, vec3 cell, float daylight) {
    float y = cell.y - 0.5;
    float y0 = floor(y);
    vec4 light = mix(slice(volume, y0, cell.xz), slice(volume, y0 + 1.0, cell.xz), y - y0);
    // the volume follows the camera: its border must never show
    vec3 border = min(cell, SIZE - cell);
    float fade = smoothstep(0.0, 8.0, min(min(border.x, border.z), border.y * 2.0));
    float sky = clamp(light.a * 255.0 / 254.0, 0.0, 1.0);
    return light.rgb * fade * (1.0 - 0.85 * sky * daylight);
}

void main() {
    float depth = fxDepth(texture(DepthSampler, texCoord).r);
    if (depth <= 1.0e-7) {
        fragColor = vec4(0.0);
        return;
    }
    fxCameraSetup();

    vec3 cornerA = corner(5);
    vec3 cornerB = corner(24);
    float daylight = unpack16(texelFetch(StateSampler, ivec2(6, 0), 0)).y;
    float weightB = unpack16(texelFetch(StateSampler, ivec2(25, 0), 0)).y;

    vec3 ray = fxViewRay(texCoord);
    vec3 dir = normalize(ray);
    vec3 position = fxEye + ray * (0.05 / depth);

    // surface normal from the neighbouring pixels (the closer neighbour on each axis, so edges stay clean)
    vec2 texel = 1.0 / vec2(textureSize(DepthSampler, 0));
    vec3 left = positionAt(texCoord - vec2(texel.x, 0.0));
    vec3 right = positionAt(texCoord + vec2(texel.x, 0.0));
    vec3 down = positionAt(texCoord - vec2(0.0, texel.y));
    vec3 up = positionAt(texCoord + vec2(0.0, texel.y));
    vec3 dx = distance(right, position) < distance(position, left) ? right - position : position - left;
    vec3 dy = distance(up, position) < distance(position, down) ? up - position : position - down;
    vec3 normal = cross(dx, dy);
    if (dot(normal, dir) > 0.0) {
        normal = -normal;
    }
    // blocky world: snap to the dominant axis
    vec3 a = abs(normal);
    normal = a.x > a.y && a.x > a.z ? vec3(sign(normal.x), 0.0, 0.0) : (a.y > a.z ? vec3(0.0, sign(normal.y), 0.0) : vec3(0.0, 0.0, sign(normal.z)));

    // the light source itself (the glowing plant) stays as it is; tested a bit behind the surface along the snapped
    // normal, so faces on a block border (the ground under a plant) are never caught by accident
    vec3 inside = position - normal * 0.05;
    if ((weightB < 0.99 && isSource(VolumeASampler, inside - cornerA)) || (weightB > 0.01 && isSource(VolumeBSampler, inside - cornerB))) {
        fragColor = vec4(0.0);
        return;
    }

    vec3 front = position + normal * 0.5;
    vec3 light = vec3(0.0);
    if (weightB < 0.999) {
        light += lightAt(VolumeASampler, front - cornerA, daylight) * (1.0 - weightB);
    }
    if (weightB > 0.001) {
        light += lightAt(VolumeBSampler, front - cornerB, daylight) * weightB;
    }
    fragColor = vec4(light, 1.0);
}
