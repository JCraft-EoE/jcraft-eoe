#version 150

#moj_import <matrix.glsl>

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

uniform float GameTime;
uniform int SwirlLayers;

in vec4 texProj0;

const vec3[] COLORS = vec3[](
    vec3(0.2, 0.0, 0.0),
    vec3(0.24, 0.0, 0.0),
    vec3(0.28, 0.0, 0.0),
    vec3(0.32, 0.0, 0.0),
    vec3(0.36, 0.0, 0.0),
    vec3(0.2, 0.1, 0.1),
    vec3(0.24, 0.1, 0.1),
    vec3(0.28, 0.1, 0.1),
    vec3(0.32, 0.1, 0.1),
    vec3(0.36, 0.1, 0.1),
    vec3(0.2, 0.2, 0.2),
    vec3(0.24, 0.2, 0.2),
    vec3(0.28, 0.2, 0.2),
    vec3(0.32, 0.2, 0.2),
    vec3(0.36, 0.2, 0.2)
);

const mat4 SCALE_TRANSLATE = mat4(
    0.5, 0.0, 0.0, 0.25,
    0.0, 0.5, 0.0, 0.25,
    0.0, 0.0, 1.0, 0.0,
    0.0, 0.0, 0.0, 1.0
);

mat4 swirl_layer(float layer) {
    mat4 translate = mat4(
        1.0, 0.0, 0.0, 17.0 / layer,
        0.0, 1.0, 0.0, (2.0 + layer / 1.5) * (GameTime * 1.5),
        0.0, 0.0, 1.0, 0.0,
        0.0, 0.0, 0.0, 1.0
    );

    mat2 rotate = mat2_rotate_z(radians((layer * layer + GameTime * 60 * (layer - 20)) * 2.0));

    mat2 scale = mat2((4.5 - layer / 4.0) * 2.0);

    return mat4(scale * rotate) * translate * SCALE_TRANSLATE;
}

out vec4 fragColor;

void main() {
    vec3 color = textureProj(Sampler0, texProj0).rgb * COLORS[0];
    for (int i = 0; i < SwirlLayers; i++) {
        color += textureProj(Sampler1, texProj0 * swirl_layer(float(i + 1))).rgb * COLORS[i];
    }
    fragColor = vec4(color, 1.0);
}
