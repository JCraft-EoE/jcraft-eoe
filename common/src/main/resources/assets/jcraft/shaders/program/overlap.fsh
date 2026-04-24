#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D EffectSampler;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 source = texture(DiffuseSampler, texCoord);
    vec4 overlap = texture(EffectSampler, texCoord);

    float a = overlap.a;
    fragColor = vec4(a * overlap.rgb + (1 - a) * source.rgb, 1.0);
}
