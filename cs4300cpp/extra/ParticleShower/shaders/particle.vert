#version 330 core

layout(location=0)in vec4 vPosition;
layout(location=1) in vec4 vTexCoord;
uniform vec4 initialPosition;
uniform vec4 vColor;
uniform vec3 velocity;
uniform float startTime;

uniform float mass;


uniform mat4 projection;
uniform mat4 modelview;
uniform float time;
uniform vec3 force;
uniform vec3 background;
out vec4 color;
out vec4 fTexCoord;

void main()
{
    vec3 vert,acc;
    float t = time - startTime;

    acc = vec3(0,-9.81,0) + force/mass;

    if (t>=0)
    {
            vert = vPosition.xyz + initialPosition.xyz + velocity*t +
            0.5*acc*t*t;
            color = (10-t)*vColor/10 + t*vec4(background,1)/10;
    }
    else
    {
            vert = vPosition.xyz;
            color = vec4(background,1.0);
            color = vec4(1,1,1,1);
    }

    gl_Position = projection * modelview * vec4(vert,1.0);
    fTexCoord = vTexCoord;
}
