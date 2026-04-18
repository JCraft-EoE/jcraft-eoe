#version 330

//#define DISTORTION
//#define RING_NOISE

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;

uniform vec3 CameraPosition;
uniform vec4 CameraRotation;
uniform vec3 Center;
#define MAX_RADIUS 100.
uniform float DesatRadius;
uniform float Radius;
uniform float HueOffset;
uniform float OuterSat;

uniform mat4 InverseTransformMatrix;
uniform vec2 Viewport;

in vec2 texCoord;

out vec4 fragColor;

#ifdef RING_NOISE
// 2D Random
float random (in vec2 st) {
    return fract(sin(dot(st.xy,
                         vec2(12.9898,78.233)))
                 * 43758.5453123);
}

// 2D Noise based on Morgan McGuire @morgan3d
// https://www.shadertoy.com/view/4dS3Wd
float noise (in vec2 st) {
    vec2 i = floor(st);
    vec2 f = fract(st);

    // Four corners in 2D of a tile
    float a = random(i);
    float b = random(i + vec2(1.0, 0.0));
    float c = random(i + vec2(0.0, 1.0));
    float d = random(i + vec2(1.0, 1.0));

    // Smooth Interpolation

    // Cubic Hermine Curve.  Same as SmoothStep()
    vec2 u = f*f*(3.0-2.0*f);
    // u = smoothstep(0.,1.,f);

    // Mix 4 coorners percentages
    return mix(a, b, u.x) +
    (c - a)* u.y * (1.0 - u.x) +
    (d - b) * u.x * u.y;
}
#endif

vec4 calcEyeFromWindow(in float depth){
    vec3 ndcPos;
    ndcPos.xy = ((2.0 * gl_FragCoord.xy)) / (Viewport) - 1;
    ndcPos.z = (2.0 * depth - gl_DepthRange.near - gl_DepthRange.far) / (gl_DepthRange.far - gl_DepthRange.near);
    vec4 clipPos = vec4(ndcPos, 1.);
    vec4 homogeneous = InverseTransformMatrix * clipPos;
    vec4 eyePos = vec4(homogeneous.xyz / homogeneous.w, homogeneous.w);
    return eyePos;
}

vec3 rgb2hsv(vec3 c){
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = c.g < c.b ? vec4(c.bg, K.wz) : vec4(c.gb, K.xy);
    vec4 q = c.r < p.x ? vec4(p.xyw, c.r) : vec4(c.r, p.yzx);

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 hsv2rgb(vec3 c){
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

vec3 quat_transform(in vec4 q, in vec3 v)
{
    return v + 2.*cross( q.xyz, cross( q.xyz, v ) + q.w*v );
}

vec2 getUV(in vec3 viewDir, in float aspect)
{
    return (vec2(viewDir.x/viewDir.z, viewDir.y/viewDir.z)/vec2(aspect, 1.))+0.5;
}

// Created by wwwtyro from https://gist.github.com/wwwtyro/beecc31d65d1004f5a9d
float raySphereIntersect(vec3 r0, vec3 rd, vec3 s0, float sr) {
    // - r0: ray origin
    // - rd: normalized ray direction
    // - s0: sphere center
    // - sr: sphere radius
    // - Returns distance from r0 to first intersecion with sphere,
    //   or -1.0 if no intersection.
    float a = dot(rd, rd);
    vec3 s0_r0 = r0 - s0;
    float b = 2.0 * dot(rd, s0_r0);
    float c = dot(s0_r0, s0_r0) - (sr * sr);
    if (b*b - 4.0*a*c < 0.0) {
        return -1.0;
    }
    return (-b - sqrt((b*b) - 4.0*a*c))/(2.0*a);
}

void main()
{
    float sceneDepth = texture(DepthSampler, texCoord).x;
    vec3 pixelPosition = calcEyeFromWindow(sceneDepth).xyz + CameraPosition;
    float aspect = Viewport.x/Viewport.y;
    vec2 cUV = texCoord-0.5;
    cUV.x *= aspect;

    float pct = distance(pixelPosition, Center);

//#ifdef RING_NOISE
//    float noise = noise(pixelPosition.xz)*2.;
//    rad += noise;
//#endif

    float outside = smoothstep(Radius - 1., Radius, pct);
    float inside = smoothstep(Radius + 1., Radius, pct);

    float desatOutside = smoothstep(DesatRadius - 1., DesatRadius, pct);
    float desatInside = smoothstep(DesatRadius + 1., DesatRadius, pct);

#ifdef DISTORTION
    vec3 viewDir = normalize(vec3(cUV.xy, 1.));
    viewDir = quat_transform(CameraRotation, viewDir);
#endif

    float desatBubbleMask = 0.;
    float satBubbleMask = 0.;
    float fresnel = 0.;

    vec3 rayDir = normalize(pixelPosition - CameraPosition);
    vec3 rayPos = CameraPosition;

    float desatIntersect = raySphereIntersect(rayPos, rayDir, Center, DesatRadius);
    float satIntersect = raySphereIntersect(rayPos, rayDir, Center, Radius);

    float pixCamDist = distance(pixelPosition, CameraPosition);

    if (desatIntersect >= 0.0 && desatIntersect < pixCamDist)
    {
        desatBubbleMask = 1.0;
        vec3 normal = normalize(Center - (rayPos + (rayDir*desatIntersect)));
        fresnel += clamp(pow(1.-clamp(dot(rayDir, normal), 0., 1.), 3.)*2., 0., 1.);
    }

    if (satIntersect >= 0.0 && satIntersect < pixCamDist)
    {
        satBubbleMask = 1.0;
        vec3 normal = normalize(Center - (rayPos + (rayDir*satIntersect)));
        fresnel += clamp(pow(1.-clamp(dot(rayDir, normal), 0., 1.), 3.)*2., 0., 1.);
    }

    vec2 m = vec2(0.5, 0.5 / aspect);
    vec2 d = texCoord - m;
    float r = sqrt(dot(d, d));
    float power = ( 1.0 * 3.141592 / (2.0 * sqrt(dot(m, m))) ) * (inside * -0.2);
    float bind = (aspect < 1.0) ? m.x : m.y;

#ifdef DISTORTION
    viewDir = quat_transform(vec4(-CameraRotation.xyz, CameraRotation.w), viewDir);
    vec2 uv = getUV(viewDir, aspect);
#else
    vec2 uv = texCoord;
#endif

    vec3 color = texture(DiffuseSampler, uv).rgb;
    if(DesatRadius > 0)
    {
        // Change this to modify the color of the "ring"
        color += pow(vec3(1.) * (outside * inside + desatOutside * desatInside), vec3(3.));
    }

    vec3 hsv = rgb2hsv(color);
    // if we hit the saturated bubble, use satHSV
    // otherwise, use desatHSV if we hit the desaturated bubble
    if (satBubbleMask > 0. || (pct <= Radius))
    {
        hsv.r += HueOffset;
        hsv.g = 1.-hsv.g;
    }
    else {
        if (desatBubbleMask > 0. || (pct > Radius && pct <= DesatRadius))
        {
            hsv.g = 0.1;
        }
    }

    color = hsv2rgb(hsv)+fresnel;

    fragColor  = vec4(color, 1.0);
}