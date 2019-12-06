#version 140

in vec4 vPosition;
in vec4 vColor;
uniform mat4 projection;
uniform mat4 modelview;
out vec4 outColor;

void main()
{
    gl_Position = projection * modelview * vPosition;
    outColor = vColor;
}
