#version 140

struct MaterialProperties
{
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    float shininess;
};

struct LightProperties
{
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    vec4 position;
    vec4 spotdirection;
    float cosSpotCutoff;
};


in vec3 fNormal;
in vec4 fPosition;
in vec4 fTexCoord;

const int MAXLIGHTS = 10;

uniform MaterialProperties material;
uniform LightProperties light[MAXLIGHTS];
uniform int numLights;

/* texture */
uniform sampler2D image;

out vec4 fColor;

void main()
{
    vec3 lightVec,viewVec,reflectVec;
    vec3 normalView;
    vec3 ambient,diffuse,specular;
    float nDotL,rDotV;


    fColor = vec4(0,0,0,1);

    for (int i=0;i<numLights;i++)
    {
        vec3 spotdirection;

        if (length(light[i].spotdirection.xyz)>0)
            spotdirection = normalize(light[i].spotdirection.xyz);

        if (light[i].position.w!=0)
            lightVec = normalize(light[i].position.xyz - fPosition.xyz);
        else
            lightVec = normalize(-light[i].position.xyz);

        /* if point is not in the light cone of this light, move on to next light */
        if (dot(-lightVec,spotdirection)<=light[i].cosSpotCutoff)
            continue;


        normalView = normalize(fNormal);
        nDotL = dot(normalView,lightVec);

        viewVec = -fPosition.xyz;
        viewVec = normalize(viewVec);

        reflectVec = reflect(-lightVec,normalView);
        reflectVec = normalize(reflectVec);

        rDotV = max(dot(reflectVec,viewVec),0.0);

        ambient = material.ambient * light[i].ambient;
        diffuse = material.diffuse * light[i].diffuse * max(nDotL,0);
        if (nDotL>0)
            specular = material.specular * light[i].specular * pow(rDotV,material.shininess);
        else
            specular = vec3(0,0,0);
        fColor = fColor + vec4(ambient+diffuse+specular,1.0);
    }
    fColor = fColor * texture(image,fTexCoord.st);
    //fColor = vec4(light[0].specular.rgb,1.0);
    //fColor = vec4(light[0].specular.rgb,1.0);
}
