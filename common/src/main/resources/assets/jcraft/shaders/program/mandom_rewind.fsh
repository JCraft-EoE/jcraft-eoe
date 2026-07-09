#version 150

uniform sampler2D DiffuseSampler;
uniform float Intensity;
uniform float Time;
uniform vec3 Color;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    // Subtle wave distortion: horizontal ripple + gentle vertical roll
    vec2 coord = texCoord;
    coord.x += sin(texCoord.y * 14.0 + Time * 5.0) * 0.009 * Intensity;
    coord.y += cos(texCoord.x * 10.0 + Time * 4.0) * 0.005 * Intensity;

    vec4 scene = texture(DiffuseSampler, coord);

    // Full-screen overlay rectangle with the stand's aura color and alpha
    vec4 overlay = vec4(Color, Intensity * 0.5);

    // Porter-Duff "over": overlay on top of scene
    fragColor = vec4(overlay.rgb * overlay.a + scene.rgb * (1.0 - overlay.a), scene.a);
}
