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

  time = 0.0f;

  refreshRate = 1;
  fireSpeed = 1;
  defaultSize = 5;
  fireSource = glm::vec3(100, 0, 0);
  numParticles = 50000;
}

View::~View()
{
  delete quadObj;

  for (unsigned int i=0;i<fireTextures.size();i++)
    {
      delete fireTextures[i];
    }

}

void View::initObjects(util::OpenGLFunctions& gl) throw(runtime_error)
{
  util::PolygonMesh<VertexAttrib> tmesh;

  ifstream in("models/quad.obj");

  tmesh = util::ObjImporter<VertexAttrib>::importFile(in,true);

  map<string,string> shaderToVertexAttrib;

  shaderToVertexAttrib["vPosition"] = "position";
  shaderToVertexAttrib["vTexCoord"] = "texcoord";

  quadObj =
      new util::ObjectInstance(string(""));

  quadObj->initPolygonMesh<VertexAttrib>(gl,
                                            program,
                                            shaderLocations,
                                            shaderToVertexAttrib,
                                            tmesh);




  //textures
  util::TextureImage *textureImage;

  for (int i=0;i<4;i++)
    {
      stringstream str;
      str << "textures/gfire-" << (i+1) << ".png";

      textureImage = new util::TextureImage(str.str(),"fire");

      QOpenGLTexture * tex = textureImage->getTexture();



      tex->setWrapMode(QOpenGLTexture::Repeat);
      tex->setMinMagFilters(QOpenGLTexture::Nearest,QOpenGLTexture::Linear);

      fireTextures.push_back(textureImage);
    }
}



void View::init(util::OpenGLFunctions& gl) throw(runtime_error)
{
  //do this if your initialization throws an error (e.g. shader not found,
  //some model not found, etc.
  //  throw runtime_error("Some error happened!");

  //create the shader program
  program.createProgram(gl,
                        string("shaders/fire.vert"),
                        string("shaders/fire.frag"));

  //assuming it got created, get all the shader variables that it uses
  //so we can initialize them at some point
  shaderLocations = program.getAllShaderVariables(gl);

  initObjects(gl);
  initFireParticles();


}

void View::initFireParticles() {
    fireParticles.clear();
    for (int i = 0; i < numParticles; i++) {
      fireParticles.push_back(getRandomFireParticle());
    }
  }

FireParticle View::getRandomFireParticle() {
    glm::vec3 position;
    glm::vec3 dir;
    float temperature;
    float startTime;
    float lifetime;
    float size;

    position = glm::vec3(-50 + (int) (100 * (float)rand()/RAND_MAX), 0, 0);
    dir = glm::vec3(-0.4f + 0.8f * (float) rand()/RAND_MAX,
                       0.4f + 0.6f * (float) rand()/RAND_MAX,
                       0);
    dir = glm::normalize(dir);
    glm::vec3 randomVector = glm::vec3((float)rand()/RAND_MAX,
                                       (float)rand()/RAND_MAX,
                                       (float)rand()/RAND_MAX);
    dir = randomVector * dir;

    temperature = 1000;
    startTime = time + 5.0f * (float) rand()/RAND_MAX;
    lifetime = 5 * (float) rand()/RAND_MAX;
    size = defaultSize * (float) rand()/RAND_MAX;

    return FireParticle(position, dir,
                        size, temperature, startTime, lifetime);
  }

void View::animate()
{
    time += (float) refreshRate / 1000;
    for (int i = 0; i < fireParticles.size(); i++) {
      fireParticles[i].advance(time);
      if (fireParticles[i].isDead())
        {
        fireParticles[i] = getRandomFireParticle();
      }
    }
  }



void View::draw(util::OpenGLFunctions& gl)
{
  animate();

  //set the background color to be black
  gl.glClearColor(0.0f,0.0f,0.0f,0.0f);
  //clear the background
  gl.glClear(GL_COLOR_BUFFER_BIT);


  gl.glEnable(GL_BLEND);
  gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE);
  gl.glEnable(GL_TEXTURE_2D);
  gl.glActiveTexture(GL_TEXTURE0);

  //enable the shader program
  program.enable(gl);

  gl.glUniform1i(shaderLocations.getLocation("sprite"), 0);


  modelview = glm::mat4(1.0);
  modelview = modelview * glm::lookAt(glm::vec3(0.0f, 0.0f, 200.0f),
                                      glm::vec3(0.0f, 0.0f, 0.0f),
                                      glm::vec3(0.0f, 1.0f, 0.0f));

  //pass the projection matrix to the shader
  gl.glUniformMatrix4fv(shaderLocations.getLocation("projection"),
                        1,
                        false,
                        glm::value_ptr(proj));

  for (int i = 0; i < fireParticles.size(); i++) {
        if (fireParticles[i].hasStarted()) {
          glm::mat4 transformation =
              glm::translate(glm::mat4(1.0),fireSource) *
              glm::translate(glm::mat4(1.0),
                             glm::vec3(fireParticles[i].getPosition())) *
              glm::scale(glm::mat4(1.0),
                         glm::vec3(fireParticles[i].getSize(),
                                   fireParticles[i].getSize(),
                                   fireParticles[i].getSize()));
          gl.glUniformMatrix4fv(
                  shaderLocations.getLocation("modelview"),
                  1, false,
                  glm::value_ptr(transformation));
          glm::vec4 color = getColor(fireParticles[i].getTemperature());
          gl.glUniform4fv(
                  shaderLocations.getLocation("vColor"), 1,
                  glm::value_ptr(color));
          fireTextures[2 * (i % 2) + 1]->getTexture()->bind();
          quadObj->draw(gl);
        }
      }
      gl.glFlush();

      program.disable(gl);
}

glm::vec4 View::getColor(float temperature)
{
    float r, g, b;

    // return glm::vec4(1,1,1,1);

  /*  if (temperature>980)
    {
        r = 1;
        g = 1;
        b = (temperature-980)/20;
    }
    else*/
    if (temperature > 800) {
      r = 1;
      g = 0.5f + 0.5f * (temperature - 800) / 100;
      b = 0;
    } else {
      r = temperature / 1000;
      g = 0.5f * temperature / 1000;
      b = 0;
    }
    return glm::vec4(r, g, b, 1);
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

  proj = glm::ortho(-0.25f*WINDOW_WIDTH,
                    0.25f*WINDOW_WIDTH,
                    -0.25f*WINDOW_HEIGHT,
                    0.25f*WINDOW_HEIGHT,
                    -10000.0f,10000.0f);

}

void View::moveFireSource(int x, int y)
{
    fireSource = glm::vec3(0.5f * x - 0.25f * WINDOW_WIDTH,
                           0.5f * (WINDOW_HEIGHT - y) - 0.25f * WINDOW_HEIGHT,
                           0);
  }

void View::dispose(util::OpenGLFunctions& gl)
{

  //release the shader resources
  program.releaseShaders(gl);
}
