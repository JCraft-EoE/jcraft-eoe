#version 150

// 'uniform' variables are inputs passed directly from Minecraft's engine
uniform sampler2D Sampler0; // the block textures (the atlas map)
uniform vec4 ColorModulator; // system-wide color multiplier

uniform float GameTime; // dynamic: server-synced time clock (loops 0.0 to 1.0 every 24k ticks)

// fog information
uniform vec4 FogColor;
uniform float FogStart;
uniform float FogEnd;

in vec2 texCoord0;
in vec4 vertexColor;
in float vertexDistance;

out vec4 fragColor;

bool isFogColorCloseTo(vec3 targetColor, float threshold) {
    return distance(FogColor.rgb, targetColor) < threshold;
}

void main() {
    // fetch the original pixel color of the block texture
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;

    // safety check for empty pixels (like gaps in leaves or air)
    if (color.a < 0.1) {
        discard;
    }

    vec3 devilsPalmFog = vec3(0.97, 0.93, 0.79);

    // only apply if in devil's palm biome
    if (isFogColorCloseTo(devilsPalmFog, 0.005)) {
        if (GameTime > 0.5) {
            color.rgb *= vec3(0.2, 0.2, 1.0); // tint blue
        }
        else {
            color.rgb *= vec3(1.0, 0.9, 0.3); // tint yellow
        }
    }

    // standard vanilla fog calculations (ensures blocks still fade smoothly into the distance)
    float fogFactor = smoothstep(FogStart, FogEnd, vertexDistance);
    color.rgb = mix(color.rgb, FogColor.rgb, fogFactor);

    fragColor = color;
}
