#version 330 core

in vec4 fPosition;
in vec4 fTexCoord;
in vec4 fColor;



out vec4 outColor;

void main()
{
    outColor = fColor;
}
