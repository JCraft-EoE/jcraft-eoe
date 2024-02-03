#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;
uniform sampler2D InvertSampler;
uniform sampler2D InvertDepthSampler;

in vec2 texCoord;
in vec4 vPosition;

out vec4 fragColor;


void main() {
    vec4 source = texture2D(DiffuseSampler, texCoord);
    vec4 invert = texture2D(InvertSampler, texCoord);
    if (invert.a > 0.0) {
        fragColor = vec4(1.0 - source.r, 1.0 - source.g, 1.0 - source.b, source.a);
        return;
    }

    fragColor = source;
}
