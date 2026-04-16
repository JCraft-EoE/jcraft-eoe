#version 330

//#define DISTORTION
//#define RING_NOISE
#define RAYMARCHED_BUBBLE

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;

uniform vec3 CameraPosition;
uniform vec4 CameraRotation;
uniform vec3 Center;
#define MAX_RADIUS 100.
uniform float Radius;
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

void main(){
    float sceneDepth = texture(DepthSampler, texCoord).x;
    vec3 pixelPosition = calcEyeFromWindow(sceneDepth).xyz + CameraPosition;
    float aspect = Viewport.x/Viewport.y;

    float pct = distance(pixelPosition, Center);

    // TODO: Getting the time from the radius/max radius is garbage and needs to be replaced with a time or lifetime uniform.
    // Although in the code it looks like the radius can be at maximum 100.
    float t = clamp(Radius/MAX_RADIUS, 0., 1.);
    t = clamp(1-pow(2, -10*t), 0., 1.);
    float rad = MAX_RADIUS * t;

#ifdef RING_NOISE
    float noise = noise(pixelPosition.xz)*2.;
    rad += noise;
#endif

    float outside = smoothstep(rad - 1., rad, pct);
    float inside = smoothstep(rad + 1., rad, pct);

#ifdef RAYMARCHED_BUBBLE
    float bubbleMask = 0.;
    float fresnel = 0.;

    vec3 rayDir = normalize(pixelPosition - CameraPosition);
    vec3 rayPos = CameraPosition;

    for (int i = 0; i < 64; i++)
    {
        if (distance(rayPos, CameraPosition) >= distance(CameraPosition, pixelPosition))
        { break; }

        float d = max(length(rayPos-Center)-rad, 0.001);
        rayPos += rayDir*d;
        if (d < 0.01)
        {
            bubbleMask = 1.0;
            vec3 normal = normalize(Center - rayPos);
            if (distance(CameraPosition, Center) > rad)
            {
                // the fresnel effect looks weird in the bubble
                fresnel = pow(1.-clamp(dot(rayDir, normal), 0., 1.), 3.)*2.;
            }
            break;
        }
    }
#endif

    vec2 m = vec2(0.5, 0.5 / aspect);
    vec2 d = texCoord - m;
    float r = sqrt(dot(d, d));
    float power = ( 1.0 * 3.141592 / (2.0 * sqrt(dot(m, m))) ) * (inside * -0.2);
    float bind = (aspect < 1.0) ? m.x : m.y;

    vec2 uv = texCoord;
#ifdef DISTORTION
    if (power < 0.0 && rad > 0)
    {
        uv = m + normalize(d) * atan(r * -power * 10.0) * bind / atan(-power * bind * 10.0);
    }
#endif

    vec3 color = texture(DiffuseSampler, uv).rgb;
    if(rad > 0){
        // Change this to modify the color of the "ring"
        color += pow(vec3(1.) * (outside * inside), vec3(3.));
    }

    vec3 hsv = rgb2hsv(color);
#ifdef RAYMARCHED_BUBBLE
    hsv[0] = mix(hsv[0], 1.0 - hsv[0], bubbleMask);
    hsv.b += fresnel;
#else
    hsv[0] = mix(hsv[0], 1.0 - hsv[0], inside);
#endif
    float saturation = 1.;
    if (pct >= rad)
    { saturation = 0.3; }

    hsv[1] = mix(hsv[1], hsv[1] * saturation, outside);
    color = hsv2rgb(hsv);

    fragColor  = vec4(color, 1.0);
}