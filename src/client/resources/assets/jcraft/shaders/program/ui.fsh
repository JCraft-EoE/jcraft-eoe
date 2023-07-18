#version 150

in vec2 texCoord;
in vec4 vPosition;

uniform sampler2D DiffuseSampler;
uniform sampler2D InputSampler;
uniform sampler2D OverlaySampler;

out vec4 fragColor;

vec4 blend(vec4 c1, vec4 c2) {
    return vec4((1 - c2.a) * c1.rgb + c2.a * c2.rgb, c1.a);
}

void main() {
    vec4 diffuseColor = texture(DiffuseSampler, texCoord);
    vec4 inputColor = texture(InputSampler, texCoord);
    vec4 overlayColor = texture(OverlaySampler, texCoord);

    vec4 hudColor = inputColor.a < 0.1 ? vec4(0.0) : blend(inputColor, overlayColor);

    fragColor = blend(diffuseColor, hudColor);
}
