#version 150

in vec2 texCoord;
in vec4 vPosition;

uniform sampler2D DiffuseSampler;
uniform float HeatTime;
uniform float Intensity;

out vec4 fragColor;

void main() {
    vec2 uv = texCoord;

    uv.x += (sin(uv.y * 18.0 + HeatTime * 2.8) + sin(uv.y * 7.3 + HeatTime * 1.1)) * 0.00125 * Intensity;
    uv.y += (sin(uv.x * 12.0 + HeatTime * 2.0) + sin(uv.x * 5.7 + HeatTime * 0.7)) * 0.00075 * Intensity;

    vec4 color = texture(DiffuseSampler, uv);

    float lum = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    color.rgb = mix(color.rgb, vec3(lum * 1.08, lum * 0.96, lum * 0.88), 0.18 * Intensity);

    // White flash
    float flash = pow(max(0.0, sin(HeatTime * 3.7) * sin(HeatTime * 2.3)), 3.0) * 0.8 * Intensity;
    color.rgb = mix(color.rgb, vec3(1.0), flash);

    // Eyelids closing from top and bottom
    float blink = pow(max(0.0, sin(HeatTime * 1.0)), 4.0) * Intensity;
    float lid = max(
        1.0 - smoothstep(blink * 0.5 - 0.02, blink * 0.5, uv.y),
        smoothstep(1.0 - blink * 0.5, 1.0 - blink * 0.5 + 0.02, uv.y)
    );
    color.rgb = mix(color.rgb, vec3(0.0), lid);

    fragColor = color;
}
