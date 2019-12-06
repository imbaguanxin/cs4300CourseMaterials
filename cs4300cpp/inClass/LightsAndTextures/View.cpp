#include "View.h"
#include "VertexAttrib.h"
#include "PolygonMesh.h"
#include <glm/gtc/type_ptr.hpp>
#include <glm/gtc/matrix_transform.hpp>
#include <vector>
#include <map>
#include <string>
using namespace std;
#include "OBJImporter.h"

View::View()
{   
  WINDOW_WIDTH = WINDOW_HEIGHT = 0;
  proj = glm::mat4(1.0);
  modelview = glm::mat4(1.0);
  textureTransform = glm::mat4(1.0);
  trackballRadius = 300;
  trackballTransform = glm::mat4(1.0);
  mipmapped = false;
  time = 0.0f;
}

View::~View()
{
  for (unsigned int i=0;i<meshObjects.size();i++)
    {
      delete meshObjects[i];
    }
  for (unsigned int i=0;i<textures.size();i++)
    {
      delete textures[i];
    }

}

void View::initObjects(util::OpenGLFunctions& gl) throw(runtime_error)
{
  util::PolygonMesh<VertexAttrib> tmesh;

  ifstream in("models/sphere.obj");

  tmesh = util::ObjImporter<VertexAttrib>::importFile(in,true);

  map<string,string> shaderToVertexAttrib;

  shaderToVertexAttrib["vPosition"] = "position";
  shaderToVertexAttrib["vNormal"] = "normal";
  shaderToVertexAttrib["vTexCoord"] = "texcoord";

  util::ObjectInstance *meshObject =
      new util::ObjectInstance(string(""));

  meshObject->initPolygonMesh<VertexAttrib>(gl,
                                            program,
                                            shaderLocations,
                                            shaderToVertexAttrib,
                                            tmesh);

  meshObjects.push_back(meshObject);

  glm::mat4 t = glm::translate(glm::mat4(1.0),glm::vec3(0.0f,0.0f,0.0f)) *
      glm::scale(glm::mat4(1.0),glm::vec3(50.0f,50.0f,50.0f));

  transforms.push_back(t);

  //material
  util::Material mat;

  mat.setAmbient(0.3f, 0.3f, 0.3f);
  mat.setDiffuse(0.7f, 0.7f, 0.7f);
  mat.setSpecular(0.7f, 0.7f, 0.7f);
  mat.setShininess(100);
  materials.push_back(mat);

  //textures
  util::TextureImage *textureImage;

  textureImage = new util::TextureImage("textures/earthmap.png","earthmap");

  QOpenGLTexture * tex = textureImage->getTexture();



  tex->setWrapMode(QOpenGLTexture::Repeat);
  tex->setMinMagFilters(QOpenGLTexture::Linear,QOpenGLTexture::Linear);

  textures.push_back(textureImage);
}

void View::initLights()
{
  util::Light l;
  l.setAmbient(0.8f, 0.8f, 0.8f);
  l.setDiffuse(0.5f, 0.5f, 0.5f);
  l.setSpecular(0.5f, 0.5f, 0.5f);
  l.setPosition(00, 00, 100);
  lights.push_back(l);
  lightCoordinateSystems.push_back(meshObjects.size()); //in world

}

void View::initShaderVariables()
{
  //get input variables that need to be given to the shader program
  for (int i = 0; i < lights.size(); i++)
    {
      LightLocation ll;
      stringstream name;

      name << "light[" << i << "]";
      ll.ambient = shaderLocations.getLocation(name.str() + "" +".ambient");
      ll.diffuse = shaderLocations.getLocation(name.str() + ".diffuse");
      ll.specular = shaderLocations.getLocation(name.str() + ".specular");
      ll.position = shaderLocations.getLocation(name.str() + ".position");
      lightLocations.push_back(ll);
    }
}

void View::init(util::OpenGLFunctions& gl) throw(runtime_error)
{
  //do this if your initialization throws an error (e.g. shader not found,
  //some model not found, etc.
  //  throw runtime_error("Some error happened!");

  //create the shader program
  program.createProgram(gl,
                        string("shaders/gouraud-multiple.vert"),
                        string("shaders/gouraud-multiple.frag"));

  //assuming it got created, get all the shader variables that it uses
  //so we can initialize them at some point
  shaderLocations = program.getAllShaderVariables(gl);

  initObjects(gl);
  initLights();
  initShaderVariables();


}



void View::draw(util::OpenGLFunctions& gl)
{
  time +=0.1f;

  //set the background color to be black
  gl.glClearColor(0.0f,0.0f,0.0f,1.0f);
  //clear the background
  gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
  gl.glEnable(GL_DEPTH_TEST);

  //enable the shader program
  program.enable(gl);

  modelview = glm::mat4(1.0);
  modelview = modelview * glm::lookAt(glm::vec3(0.0f, 0.0f, 60.0f),
                                      glm::vec3(0.0f, 0.0f, 0.0f),
                                      glm::vec3(0.0f, 1.0f, 0.0f));

  //modelview currently represents world-to-view transformation
  //transform all lights into the view coordinate system before passing to
  //shaders. That way everything will be in one coordinate system in the shader
  //(the view) and the math will be correct

  for (int i = 0; i < lights.size(); i++)
    {
      glm::vec4 pos = lights[i].getPosition();
      glm::mat4 lightTransformation;

      if (lightCoordinateSystems[i]==meshObjects.size())
        {
          lightTransformation = modelview;
        }
      else if (lightCoordinateSystems[i]==meshObjects.size()+1)
        {
          lightTransformation = glm::mat4(1.0);
        }
      else
        {
          lightTransformation = modelview * trackballTransform * transforms[i];
        }
      pos = lightTransformation * pos;
      gl.glUniform4fv(lightLocations[i].position, 1, glm::value_ptr(pos));
    }





  //pass the projection matrix to the shader
  gl.glUniformMatrix4fv(shaderLocations.getLocation("projection"),
                        1,
                        false,
                        glm::value_ptr(proj));

  //pass light color properties to shader
  gl.glUniform1i(shaderLocations.getLocation("numLights"),lights.size());

  for (int i = 0; i < lights.size(); i++)
    {
      gl.glUniform3fv(lightLocations[i].ambient, 1, glm::value_ptr(lights[i].getAmbient()));
      gl.glUniform3fv(lightLocations[i].diffuse, 1, glm::value_ptr(lights[i].getAmbient()));
      gl.glUniform3fv(lightLocations[i].specular, 1,glm::value_ptr(lights[i].getSpecular()));
    }

  //textures
  //enable texture mapping
  gl.glEnable(GL_TEXTURE_2D);
  //whatever we will bind as texture, we will bind it to GL_TEXTURE0
  //OpenGL supports up to 8 textures per object, and so we have GL_TEXTURE0..7
  //here we are using just one texture


  gl.glActiveTexture(GL_TEXTURE0);
  //tell the shader to look for GL_TEXTURE"0"
  gl.glUniform1i(shaderLocations.getLocation("texture"), 0);

  for (int i = 0; i < meshObjects.size(); i++) {
      glm::mat4 transformation = modelview * trackballTransform * transforms[i];
      //the matrix applied to the normal should be the inverse transpose of
      //whatever is applied to the object
      glm::mat4 normalmatrix = glm::inverse(glm::transpose((transformation)));

      gl.glUniformMatrix4fv(shaderLocations.getLocation("modelview"), 1, false, glm::value_ptr(transformation));
      gl.glUniformMatrix4fv(shaderLocations.getLocation("normalmatrix"), 1, false,glm::value_ptr(normalmatrix));

      textureTransform = glm::mat4(1.0);

      gl.glUniformMatrix4fv(shaderLocations.getLocation("texturematrix"), 1, false, glm::value_ptr(textureTransform));
      gl.glUniform3fv(shaderLocations.getLocation("material.ambient"), 1, glm::value_ptr(materials[i].getAmbient()));
      gl.glUniform3fv(shaderLocations.getLocation("material.diffuse"), 1, glm::value_ptr(materials[i].getDiffuse()));
      gl.glUniform3fv(shaderLocations.getLocation("material.specular"), 1,glm::value_ptr(materials[i].getSpecular()));
      gl.glUniform1f(shaderLocations.getLocation("material.shininess"), materials[i].getShininess());

      if (mipmapped)
        textures[i]->getTexture()->setMinMagFilters(QOpenGLTexture::LinearMipMapLinear,
                                                    QOpenGLTexture::LinearMipMapLinear);
      else
        textures[i]->getTexture()->setMinMagFilters(QOpenGLTexture::Linear,
                                                    QOpenGLTexture::Linear);
      textures[i]->getTexture()->bind();
      meshObjects[i]->draw(gl);
    }

  //opengl is a pipeline-based framework. Things are not drawn as soon as
  //they are supplied. glFlush flushes the pipeline and draws everything
  gl.glFlush();
  //disable the program
  program.disable(gl);
}

void View::mousePressed(int x,int y)
{
  mousePos = glm::vec2(x,y);
}

void View::mouseReleased(int x,int y)
{

}

void View::mouseDragged(int x,int y)
{
  glm::vec2 newM = glm::vec2((float)x,(float)y);

  glm::vec2 delta = glm::vec2((float)(newM.x-mousePos.x),(float)(newM.y-mousePos.y));
  mousePos = newM;

  trackballTransform =
      glm::rotate(glm::mat4(1.0),delta.x/trackballRadius,glm::vec3(0.0f,1.0f,0.0f)) *
      glm::rotate(glm::mat4(1.0),delta.y/trackballRadius,glm::vec3(1.0f,0.0f,0.0f)) *
      trackballTransform;
}

void View::reshape(util::OpenGLFunctions& gl,int width,int height)
{
  //record the new width and height
  WINDOW_WIDTH = width;
  WINDOW_HEIGHT = height;

  /*
     * The viewport is the portion of the screen window where the drawing
     * would be placed. We want it to take up the entire area of the window
     * so we set the viewport to be the entire window.
     * Look at documentation of glViewport
     */

  gl.glViewport(0, 0, width, height);

  /*
     * This sets up the part of our virtual world that will be visible in
     * the screen window. Since this program is drawing 2D, the virtual world
     * is 2D. Thus this window can be specified in terms of a rectangle
     * Look at the documentation of glOrtho2D, which glm::ortho implements
     */

 /* proj = glm::perspective(glm::radians(120.0f),
                          (float)WINDOW_WIDTH/WINDOW_HEIGHT,
                          0.1f,
                          10000.0f); */
  //proj = glm::ortho(-400.0f,400.0f,-400.0f,400.0f,0.1f,10000.0f);
   proj = glm::ortho(-50.0f,50.0f,-50.0f,50.0f,0.1f,10000.0f);

}

void View::dispose(util::OpenGLFunctions& gl)
{
  //clean up the OpenGL resources used by the object
  for (int i=0;i<meshObjects.size();i++)
    {
      meshObjects[i]->cleanup(gl);
    }
  //release the shader resources
  program.releaseShaders(gl);
}
