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
#include "VertexAttrib.h"
#include "FireParticle.h"

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
  void moveFireSource(int x, int y);

protected:
  void initObjects(util::OpenGLFunctions& gl) throw(runtime_error);
  void initFireParticles();
  void animate();
  FireParticle getRandomFireParticle();
  glm::vec4 getColor(float temperature);


private:
  //record the current window width and height
  int WINDOW_WIDTH,WINDOW_HEIGHT;
  //the projection matrix
  glm::mat4 proj;
  //the modelview matrix
  glm::mat4 modelview;
  util::ObjectInstance *quadObj;
  vector<util::TextureImage *> fireTextures;
  vector<FireParticle> fireParticles;
  float time, refreshRate, fireSpeed, defaultSize;
  glm::vec3 fireSource;
  int numParticles;
  //the list of shader variables and their locations within the shader program
  util::ShaderLocationsVault shaderLocations;


  //the GLSL shader
  util::ShaderProgram program;
};

#endif // VIEW_H
