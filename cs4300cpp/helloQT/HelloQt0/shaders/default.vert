#version 140

in vec4 vPosition;
uniform vec4 vColor;
uniform mat4 projection;
out vec4 outColor;

void main()
{
    gl_Position = projection * vPosition;
    outColor = vColor;
}
