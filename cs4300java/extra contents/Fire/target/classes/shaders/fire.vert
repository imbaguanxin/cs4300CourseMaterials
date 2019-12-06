#version 140

in vec4 vPosition;
in vec4 vTexCoord;
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
