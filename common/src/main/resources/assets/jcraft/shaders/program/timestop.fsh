#version 330

uniform sampler2D DepthTexture;

layout (std140) uniform ShaderUniforms
{
    vec2 Viewport;
    float Time;
};

in vec2 texCoord;

out vec4 fragColor;

void main()
{
    float depth = pow(texture(DepthTexture, texCoord).r, 50.);
    fragColor = vec4(depth, depth, depth, 0.5);
}