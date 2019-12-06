#version 140


in vec4 outColor;
in vec4 fTexCoord;
out vec4 fColor;

uniform sampler2D sprite;

void main()
{
    vec4 c = texture(sprite,fTexCoord.st);
    fColor = outColor * c;

}
