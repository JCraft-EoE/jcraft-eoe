#version 150

uniform sampler2D DiffuseSampler;
uniform float GameTime;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);

    vec3 dayTint   = vec3(1.0, 0.9, 0.3); // warm yellow
    vec3 nightTint = vec3(0.2, 0.2, 1.0); // cold blue

    float blendFactor = 0.0; // Default to Day (0.0)

    // sunset
    if (GameTime >= 0.45 && GameTime <= 0.55) {
        blendFactor = smoothstep(0.45, 0.55, GameTime);
    }
    // night cycle
    else if (GameTime > 0.55 && GameTime < 0.95) {
        blendFactor = 1.0;
    }
    // sunrise
    else if (GameTime >= 0.95 || GameTime <= 0.05) {
        // Adjust for the 1.0 to 0.0 loop boundary point
        float adjustedTime = (GameTime >= 0.95) ? GameTime : (GameTime + 1.0);
        blendFactor = 1.0 - smoothstep(0.95, 1.05, adjustedTime);
    }

    vec3 finalTint = mix(dayTint, nightTint, blendFactor);
    color.rgb *= finalTint;

    fragColor = color;
}
