#version 150

uniform sampler2D DiffuseSampler;
uniform float GameTime;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);

    if (GameTime > 0.5) {
        color.rgb *= vec3(0.2, 0.2, 1.0); // tint blue
    }
    else {
        color.rgb *= vec3(1.0, 0.9, 0.3); // tint yellow
    }

    fragColor = color;
}
