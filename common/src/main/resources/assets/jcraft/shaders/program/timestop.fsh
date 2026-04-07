#version 330

layout (std140) uniform ShaderUniforms
{
    vec3 camPos;
    float radius;
};

in vec2 texCoord;

out vec4 fragColor;

void main()
{
    fragColor = vec4(1., 0., 0., 1.0);
}