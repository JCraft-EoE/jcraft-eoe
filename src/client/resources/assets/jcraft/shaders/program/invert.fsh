#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D InvertSampler;

in vec2 texCoord;
in vec4 vPosition;

out vec4 fragColor;


void main() {
    vec4 source = texture(DiffuseSampler, texCoord);
    vec4 invert = texture(InvertSampler, texCoord);

    if (invert.a > 0.0) {
        fragColor = vec4(1.0 - source.r, 1.0 - source.g, 1.0 - source.b, 1.0);
        return;
    }

    fragColor = source;
}
