#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;
uniform vec2 OutSize;

uniform vec3 CameraPosition;
uniform vec3 Center;
uniform float Radius;
uniform float OuterSat;

uniform mat4 InverseTransformMatrix;
uniform ivec4 ViewPort;

in vec2 texCoord;
in vec4 vPosition;

out vec4 fragColor;

vec4 CalcEyeFromWindow(in float depth){
    vec3 ndcPos;
    ndcPos.xy = ((2.0 * gl_FragCoord.xy) - (2.0 * ViewPort.xy)) / (ViewPort.zw) - 1;
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
    vec3 ndc = vPosition.xyz / vPosition.w;
    vec2 viewportCoord = ndc.xy * 0.5 + 0.5;

    float sceneDepth = texture(DepthSampler, viewportCoord).x;
    vec3 pixelPosition = CalcEyeFromWindow(sceneDepth).xyz + CameraPosition;

    float pct = distance(pixelPosition, Center);

    float outside = smoothstep(Radius - 1., Radius, pct);
    float inside = smoothstep(Radius + 1., Radius, pct);
    float outside2 = smoothstep(Radius - 2.5, Radius - 2., pct);
    float inside2 = smoothstep(Radius - 1.5, Radius - 2., pct);
    vec2 p = gl_FragCoord.xy / OutSize.x;
    float prop = OutSize.x / OutSize.y;
    vec2 m = vec2(0.5, 0.5 / prop);
    vec2 d = p - m;
    float r = sqrt(dot(d, d));
    float power = ( 1.0 * 3.141592 / (2.0 * sqrt(dot(m, m))) ) *
    (inside * -0.2);
    float bind = (prop < 1.0) ? m.x : m.y;


    vec2 uv;
    if (power < 0.0)
    uv = m + normalize(d) * atan(r * -power * 10.0) * bind / atan(-power * bind * 10.0);
    else
    uv = p;
    vec3 color = texture(DiffuseSampler, vec2(uv.x, uv.y * prop)).rgb;
    color += pow(vec3(1.) * (outside * inside + outside2 * inside2), vec3(3.));

    vec3 hsv = rgb2hsv(color);
    hsv[0] = mix(hsv[0], 1.0 - hsv[0], inside);
    hsv[1] = mix(hsv[1], hsv[1] * OuterSat, outside);
    color = hsv2rgb(hsv);

    fragColor  = vec4(color, 1.0);
}