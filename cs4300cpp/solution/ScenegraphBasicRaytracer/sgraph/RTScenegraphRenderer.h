#ifndef _RTSCENEGRAPHRENDERER_H_
#define _RTSCENEGRAPHRENDERER_H_

#include "INode.h"
#include "OpenGLFunctions.h"
#include "glm/glm.hpp"
#include <glm/gtc/type_ptr.hpp>
#include "Material.h"
#include "TextureImage.h"
#include "ObjectInstance.h"
#include "IVertexData.h"
#include "Ray.h"
#include "HitRecord.h"
#include "ShaderLocationsVault.h"
#include "Light.h"
#include <string>
#include <sstream>
#include <map>
#include <stack>
#include <cmath>
using namespace std;

namespace sgraph
{

  /**
 * This is a scene graph renderer implementation that works specifically
 * with the Qt library
 * It mandates OpenGL 3 and above.
 * \author Amit Shesh
 */
  class RTScenegraphRenderer
  {
  private:
    map<string, util::TextureImage *> textures;
    /**
     * A table of renderers for individual meshes
     */
    map<string, util::ObjectInstance *> meshRenderers;
    vector<util::Light> lights;

  public:
    RTScenegraphRenderer()
    {
    }



    void addTexture(const string& name,const string& path)
    {
      util::TextureImage *image = NULL;
      try {
        image = new util::TextureImage(path,name);
      } catch (runtime_error e) {
        throw runtime_error("Texture "+path+" cannot be read!");
      }

      textures[name]=image;
    }

    /**
     * Begin ray tracing this scene graph
     * \param root
     * \param modelView
     */
    void draw(INode *root, stack<glm::mat4>& modelView)
    {
      int i,j;
      int width = 800;
      int height = 800;
      float FOVY = 120.0f;
      Ray rayView;

      lights = root->getLightsInView(modelView);

      QImage output(width, height,QImage::Format_ARGB32);

      rayView.start = glm::vec4(0,0,0,1);
      for (i=0;i<width;i++)
        {
          for (j=0;j<height;j++)
            {
              /*
               create ray in view coordinates
               start point: 0,0,0 always!
               going through near plane pixel (i,j)
               So 3D location of that pixel in view coordinates is
               x = i-width/2
               y = j-height/2
               z = -0.5*height/tan(FOVY)
              */
              rayView.direction = glm::vec4(i-0.5f*width,
                                            j-0.5f*height,
                                            -0.5f*height/(float)tan(glm::radians(0.5*FOVY)),
                                            0.0f);

              HitRecord hitR;
              glm::vec3 color;
              raycast(rayView,root,modelView,hitR);
              color = getRaytracedColor(hitR);

              int r,g,b;

              r = (int)(color.r*255);
              g = (int)(color.g*255);
              b = (int)(color.b*255);

              output.setPixelColor(i, height-1-j, QColor(r, g, b));
            }
        }

      output.save(QString("output/raytrace.png"));
    }

    void dispose()
    {

    }




  private:
    void raycast(Ray& rayView,INode*& root,stack<glm::mat4>& modelView,HitRecord& hitRecord)
    {
      root->intersect(rayView,modelView,hitRecord);

    }

    glm::vec3 getRaytracedColor(HitRecord& hitRecord)
    {
      glm::vec3 color;
      if (hitRecord.intersected())
        {
        color = shade(hitRecord.point,hitRecord.normal,hitRecord.material,hitRecord.textureName,hitRecord.texcoord);
      }
      else
      {

          color = glm::vec3(0.0f,0.0f,0.0f);
      }
      return color;
    }

    glm::vec3 shade(glm::vec4& point,glm::vec4& normal,util::Material& material,string textureName,glm::vec2 texcoord)
    {
      glm::vec3 color = glm::vec3(0.0f,0.0f,0.0f);

      for (int i=0;i<lights.size();i++)
        {
          glm::vec3 lightVec;
          glm::vec3 spotdirection = glm::vec3(
                lights[i].getSpotDirection().x,
                lights[i].getSpotDirection().y,
                lights[i].getSpotDirection().z);


          if (spotdirection.length()>0)
            spotdirection = glm::normalize(spotdirection);

          if (lights[i].getPosition().w!=0) {
              lightVec = glm::vec3(
                    lights[i].getPosition().x,
                    lights[i].getPosition().y,
                    lights[i].getPosition().z) - glm::vec3(point.x,point.y,point.z);
            }
          else
            {
              lightVec = glm::vec3(
                    -lights[i].getPosition().x,
                    -lights[i].getPosition().y,
                    -lights[i].getPosition().z);
            }
          lightVec = glm::normalize(lightVec);


          /* if point is not in the light cone of this light, move on to next light */
//          if (glm::dot(-lightVec,spotdirection)<=cos(glm::radians(lights[i].getSpotCutoff())))
//            continue;


          glm::vec3 normalView = glm::normalize(glm::vec3(normal.x,normal.y,normal.z));

          float nDotL = glm::dot(normalView,lightVec);


          glm::vec3 viewVec = -glm::vec3(point.x,point.y,point.z);
          viewVec = glm::normalize(viewVec);

          glm::vec3 reflectVec = glm::reflect(-lightVec,normalView);
          reflectVec = glm::normalize(reflectVec);

          float rDotV = glm::max(glm::dot(reflectVec,viewVec),0.0f);

          glm::vec3 ambient = glm::vec3(material.getAmbient()) * lights[i].getAmbient();

          glm::vec3 diffuse = glm::vec3(material.getDiffuse()) * lights[i].getDiffuse() * glm::max(nDotL,0.0f);
          glm::vec3 specular;
          if (nDotL>0)
            {
              specular = glm::vec3(material.getSpecular()) * lights[i].getSpecular() * glm::pow(rDotV,material.getShininess());
            }
          else
            {
              specular = glm::vec3(0.0f,0.0f,0.0f);
            }
          color = color + ambient+diffuse+specular;
        }
      color = color * glm::vec3(this->textures[textureName]->getColor(texcoord.x,1-texcoord.y));
      color = glm::clamp(color,glm::vec3(0,0,0),glm::vec3(1,1,1));

      return color;
    }


    int getShaderLocation(const string& name)
    {
      throw runtime_error("Operation not supported on this renderer");
    }
  };
}
#endif
