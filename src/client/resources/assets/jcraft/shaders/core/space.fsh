#version 150

#define time Time

uniform float     Time;                 // shader playback time (in seconds)

uniform mat4      InverseTransformMatrix;

in vec4 vPosition;
in vec2 texCoord;

out vec4 fragColor;

float hash(float x){
    return fract(sin(x) * 43758.5453);
}

float noise(vec3 pos){
    vec3 i = floor(pos);
    vec3 q = fract(pos);

    // Random values
    float a = hash(dot(i, vec3(1.0, 0.0, 0.0)));
    float b = hash(dot(i, vec3(0.0, 1.0, 0.0)));
    float c = hash(dot(i, vec3(0.0, 0.0, 1.0)));
    float d = hash(dot(i, vec3(1.0, 1.0, 0.0)));
    float e = hash(dot(i, vec3(0.0, 1.0, 1.0)));
    float f = hash(dot(i, vec3(1.0, 0.0, 1.0)));
    float g = hash(dot(i, vec3(1.0, 1.0, 1.0)));

    // Smoothstep interpolation
    vec3 u = q * q * (3.0 - 2.0 * q);

    // Interpolate the random values
    float x1 = mix(a, b, u.x);
    float x2 = mix(c, d, u.x);
    float x3 = mix(e, f, u.x);
    float y1 = mix(x1, x2, u.y);
    float y2 = mix(x3, g, u.y);
    return mix(y1, y2, u.z);
}

float fbm(vec3 pos)
{
    float v = 0.0;
    float a = 0.5;
    //vec3 shift = vec3(100.0);
    mat3 rot = mat3(cos(0.5), sin(0.5), 0.0, -sin(0.5), cos(0.5), 0.0, 0.0, 0.0, 1.0);
    for (int i = 0; i < 4; i++){
        v += a * noise(pos);
        pos = rot * pos * 2.0;// + shift;
        a *= 0.5;
    }
    return v;
}

void main(void) {
    vec3 p = vPosition.xyz / 100;

    float time2 = 0.6 * Time / 2.0;

    vec3 q = vec3(0.0);
    q.x = fbm(vec3(p + 0.30 * time2));
    q.y = fbm(vec3(p + vec3(1.0)));
    vec3 r = vec3(0.0);
    r.x = fbm(vec3(p + q + vec3(1.2, 3.2, 0.0) + 0.135 * time2));
    r.y = fbm(vec3(p + q + vec3(8.8, 2.8, 0.0) + 0.126 * time2));
    float noiseResult = fbm(vec3(p + r));
    vec3 color = mix(vec3(0.0, 0.0, 0.0), vec3(1.0, 0.0, 0.7), clamp((noiseResult * noiseResult) * 8.0, 0.0, 5.0));

    //color = mix(color, vec3(0.0, 0.0, 1.0), clamp(length(q), 0.0, 1.0));

    //color = mix(color, vec3(1.0, 1.0, 1.0), clamp(length(r.x), 0.0, 1.0));

    color = (noiseResult * noiseResult * noiseResult + 0.6 * noiseResult * noiseResult + 0.9 * noiseResult) * color;

    fragColor = vec4(color, 1.0);
}