#version 330

layout (std140) uniform ShaderUniforms
{
    vec2 Viewport;
    float Time;
};

in vec2 texCoord;

out vec4 fragColor;

void main()
{
    fragColor = vec4(mod(texCoord + vec2(Time/60.), 1.0), 0., 1.);
}