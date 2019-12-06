#version 400 core

layout(location=0) in vec4 vPosition;
layout(location=1) in vec4 vTexCoord;
uniform mat4 projection;
uniform mat4 modelview;
uniform vec4 vColor;
out vec4 outColor;
out vec4 fTexCoord;

void main()
{
    gl_Position = projection * modelview * vec4(vPosition.xyzw);
    outColor = vColor;
    fTexCoord = vTexCoord;
}
