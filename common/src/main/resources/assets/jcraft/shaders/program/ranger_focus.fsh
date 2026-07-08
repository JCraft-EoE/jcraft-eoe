#version 150

in vec2 texCoord;
in vec4 vPosition;

uniform sampler2D DiffuseSampler;
uniform float Fade;

out vec4 fragColor;

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);

    float grey = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    vec3 desaturated = mix(color.rgb, vec3(grey), 0.2 * Fade);
    vec3 warmed = desaturated * mix(vec3(1.0), vec3(1.08, 1.0, 0.88), Fade);

    vec2 pos = texCoord;
    pos *= 1.0 - pos.yx;
    float vig = 1.0 - min(pow(pos.x * pos.y * 25.0, 0.5), 1.0);

    fragColor = vec4(mix(warmed, vec3(0.0), vig * 0.35 * Fade), color.a);
}
