#version 330

in vec4 Position;
in vec2 TexCoord;

out vec2 texCoord;

void main()
{
    gl_Position = vec4(Position.xy, 0.0, 1.0);

    texCoord = TexCoord;
}