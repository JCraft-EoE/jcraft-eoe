#version 150

in vec2 texCoord;
in vec4 vPosition;

uniform sampler2D DiffuseSampler;
uniform float Intensity;
uniform float Extend;

out vec4 fragColor;

const vec3 pink = vec3(167.0, 55.0, 75.0) / 255.0;

void main() {
    vec2 pos = texCoord;
    pos *= 1.0 - pos.yx;

    float vig = 1.0 - min(pow(pos.x * pos.y * Intensity, Extend), 1.0);

    fragColor = (1.0 - vig) * texture(DiffuseSampler, texCoord) + vig * vec4(pink, 0.0);
}
