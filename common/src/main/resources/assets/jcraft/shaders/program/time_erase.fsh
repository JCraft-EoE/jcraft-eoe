#version 330

#define RADIUS 100.
#define BLEND_DIST 45.

#define PI 3.14159

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;

uniform float Time;
uniform vec2 Viewport;
uniform mat4 InverseTransformMatrix;

in vec2 texCoord;

out vec4 fragColor;

float random (in vec2 st) {
    return fract(sin(dot(st.xy,
    vec2(12.9898,78.233)))*
    43758.5453123);
}

// Based on Morgan McGuire @morgan3d
// https://www.shadertoy.com/view/4dS3Wd
float noise (in vec2 st) {
    vec2 i = floor(st);
    vec2 f = fract(st);

    // Four corners in 2D of a tile
    float a = random(i);
    float b = random(i + vec2(1.0, 0.0));
    float c = random(i + vec2(0.0, 1.0));
    float d = random(i + vec2(1.0, 1.0));

    vec2 u = f * f * (3.0 - 2.0 * f);

    return mix(a, b, u.x) +
    (c - a)* u.y * (1.0 - u.x) +
    (d - b) * u.x * u.y;
}

#define OCTAVES 6
float fbm (in vec2 st) {
    // Initial values
    float value = 0.0;
    float amplitude = .5;
    float frequency = 0.;
    //
    // Loop of octaves
    for (int i = 0; i < OCTAVES; i++) {
        value += amplitude * noise(st);
        st *= 2.;
        amplitude *= .5;
    }
    return value;
}

float fbm3(in vec3 p)
{
    vec3 n = normalize(p);
    vec3 w = abs(n);

    w = pow(w, vec3(4.0));
    w /= (w.x + w.y + w.z);

    float xy = fbm(p.xy);
    float xz = fbm(p.xz);
    float yz = fbm(p.yz);

    return xy * w.z + xz * w.y + yz * w.x;
}

// Source - https://stackoverflow.com/a/51137756
// Posted by neXyon
// Retrieved 2026-04-25, License - CC BY-SA 4.0
float linearize_depth(float d, float zNear, float zFar)
{
    return zNear * zFar / (zFar + d * (zNear - zFar));
}

vec4 calcEyeFromWindow(in float depth)
{
    vec3 ndcPos;
    ndcPos.xy = (2.0 * gl_FragCoord.xy) / (Viewport) - 1;
    ndcPos.z = (2.0 * depth - gl_DepthRange.near - gl_DepthRange.far) / (gl_DepthRange.far - gl_DepthRange.near);
    vec4 clipPos = vec4(ndcPos, 1.);
    vec4 homogeneous = InverseTransformMatrix * clipPos;
    vec4 eyePos = vec4(homogeneous.xyz / homogeneous.w, homogeneous.w);
    return eyePos;
}

vec3 stars(in vec2 uv)
{
    vec3 col = vec3(0.);

    col += pow(noise(uv*500.), 100.);
    col += pow(noise(uv*125.), 100.)/2.;

    vec3 starTint = vec3(
        noise(uv*8.),
        noise(uv*4.),
        noise(uv*2.)
    );
    starTint = mix(starTint, vec3(1.), 0.5);

    col *= starTint;

    return col;
}

void main()
{
    vec3 base = texture(DiffuseSampler, texCoord).rgb;

    float depth = texture(DepthSampler, texCoord).r;
    vec3 viewPos = calcEyeFromWindow(depth).xyz;

    float dist = length(viewPos);
    if (dist < RADIUS-BLEND_DIST)
    {
        fragColor = vec4(base, 1.);
        return;
    }

    float m = clamp((dist-(RADIUS-BLEND_DIST))/BLEND_DIST, 0., 1.);

    vec3 rayDir = normalize(viewPos);
    rayDir = normalize(rayDir + vec3(sin(Time/240.)/5.));

    float u = atan(rayDir.x, rayDir.z) / (2.0 * PI) + 0.5;
    float v = asin(rayDir.y) / PI + 0.5;
    vec2 uv = vec2(u, v);

    vec3 col = vec3(pow(fbm3(rayDir*10.), 5.)/2.);
    col *= vec3(0.573,0.227,0.357);

    col += (pow(fbm3(rayDir*5.), 2.)/4.)*vec3(0.443,0.380,0.620);
    col += (pow(fbm3((rayDir*10.)+vec3(10., Time, 4.)), 4.)/4.)*vec3(0.384,0.616,0.529);

    col += stars(uv)*2.;

    fragColor = vec4(mix(base, col, m), 1.0);
}