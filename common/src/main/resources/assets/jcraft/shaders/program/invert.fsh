#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D EffectSampler;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 source = texture(DiffuseSampler, texCoord);
    float mask = texture(EffectSampler, texCoord).a;

    if (mask > 0.0) {
        fragColor = vec4(1.-source.rgb, 1.0);
        return;
    }
    discard;
}
