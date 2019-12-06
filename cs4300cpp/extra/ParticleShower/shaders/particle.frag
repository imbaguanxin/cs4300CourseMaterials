in vec4 color;
in vec4 fTexCoord;
uniform sampler2D sprite;
out vec4 fColor;
void main()
{
    fColor = color * texture(sprite,fTexCoord.st);
}
