#version 150

in vec2 texCoord;
in vec4 vPosition;

uniform sampler2D DiffuseSampler;
uniform sampler2D PredictionsSampler;

out vec4 fragColor;


vec4 blend(vec4 c1, vec4 c2) {
    return vec4((1 - c2.a) * c1.rgb + c2.a * c2.rgb, c1.a);
}

void main() {
    fragColor = blend(texture(DiffuseSampler, texCoord), texture(PredictionsSampler, texCoord) * vec4(1.0, 0.0, 0.0, 0.33));
}
