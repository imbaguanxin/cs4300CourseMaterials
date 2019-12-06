#ifndef VIEW_H
#define VIEW_H

#include <OpenGLFunctions.h>
#include <exception>
using namespace std;
#include <glm/glm.hpp>
#include "ShaderProgram.h"
#include "ShaderLocationsVault.h"
#include "ObjectInstance.h"
#include "TextureImage.h"
#include "Light.h"
#include "VertexAttrib.h"
#include "Material.h"

/*
 * This class encapsulates all our program-specific details. This makes our
 * design better if we wish to port it to another C++-based windowing
 * library
 */

class View
{
  class LightLocation
  {
  public:
    int ambient,diffuse,specular,position;
    LightLocation()
    {
      ambient = diffuse = specular = position = -1;
    }

  };

public:
  View();
  ~View();
  /*
     * This is called when the application is being initialized. We should
     * do all our initializations here. This is also the first function where
     * OpenGL commands will work (i.e. don't do any OpenGL related stuff in the
     * constructor!)
     */
  void init(util::OpenGLFunctions& e) throw(runtime_error);

  /*
     * This function is called whenever the window is to be redrawn
     */
  void draw(util::OpenGLFunctions& e);

  /*
     * This function is called whenever the window is reshaped
     */
  void reshape(util::OpenGLFunctions& gl,int width,int height);

  /*
     * This function is called whenever the window is being destroyed
     */
  void dispose(util::OpenGLFunctions& gl);
  void mousePressed(int x,int y);
  void mouseReleased(int x,int y);
  void mouseDragged(int x,int y);
  void toggleMipmapping() {mipmapped = !mipmapped;}

protected:
  void initObjects(util::OpenGLFunctions& gl) throw(runtime_error);
  vector<glm::vec4> getLightPositions(const glm::mat4& transformation);
  void initLights();
  void initShaderVariables();


private:
  //record the current window width and height
  int WINDOW_WIDTH,WINDOW_HEIGHT;
  //the projection matrix
  glm::mat4 proj;
  //the modelview matrix
  glm::mat4 modelview;
  //the objects which we are rendering
  vector<util::ObjectInstance *> meshObjects;
  //the textures we are using
  vector<util::TextureImage *> textures;
  //materials for our objects
  vector<util::Material> materials;
  //object-to-world transformations
  vector<glm::mat4> transforms;
  //the lights in our scene
  vector<util::Light> lights;
  //if 0-meshObjects.size()-1, then light[i] is in the coordinate system of that
  //object. if meshObject.size() it is in world and if meshObject.size()+1 it is
  //in view
  vector<int> lightCoordinateSystems;
  //the texture matrix
  glm::mat4 textureTransform;
  //the trackball transform
  glm::mat4 trackballTransform;
  //the radius of the virtual trackball
  float trackballRadius;
  //the mouse position
  glm::vec2 mousePos;
  //the list of shader variables and their locations within the shader program
  util::ShaderLocationsVault shaderLocations;

  //shader variables
  vector<LightLocation> lightLocations;

  //the GLSL shader
  util::ShaderProgram program;
  bool mipmapped;

  //animation
  float time;
};

#endif // VIEW_H
