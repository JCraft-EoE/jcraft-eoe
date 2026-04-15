#version 330

#define DISTORTION

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

    float outside = smoothstep(rad - 1., rad, pct);
    float inside = smoothstep(rad + 1., rad, pct);
    float outside2 = smoothstep(rad - 2.5, rad - 2., pct);
    float inside2 = smoothstep(rad - 1.5, rad - 2., pct);
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
        //Change this to modify the color of the "ring"
        color += pow(vec3(1.) * (outside * inside + outside2 * inside2), vec3(3.));
    }

    vec3 hsv = rgb2hsv(color);
    hsv[0] = mix(hsv[0], 1.0 - hsv[0], inside);
    hsv[1] = mix(hsv[1], hsv[1] * OuterSat, outside);
    color = hsv2rgb(hsv);

    fragColor  = vec4(color, 1.0);
}