#version 150

in vec4 Position;
in vec2 UV0;

uniform mat4 ProjMat;
uniform mat4 ModelViewMat;
uniform mat4 InverseTransformMatrix;

out vec4 vPosition;
out vec2 texCoord;

void main()
{
    gl_Position = ProjMat * ModelViewMat * vec4(Position);

    vPosition = InverseTransformMatrix * vec4(Position);
    texCoord = UV0;
}