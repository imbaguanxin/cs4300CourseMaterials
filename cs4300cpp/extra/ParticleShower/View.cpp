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
  :numParticles(50000)
{   
  WINDOW_WIDTH = WINDOW_HEIGHT = 0;
  proj = glm::mat4(1.0);
  modelview = glm::mat4(1.0);

  time = 0.0f;

  startPosition = glm::vec4(0,0,0,1);
}

View::~View()
{
  delete pointObj;
  delete pointTexture;

}

void View::initObjects(util::OpenGLFunctions& gl) throw(runtime_error)
{
  util::PolygonMesh<VertexAttrib> tmesh;

  ifstream in("models/quad.obj");

  tmesh = util::ObjImporter<VertexAttrib>::importFile(in,true);

  map<string,string> shaderToVertexAttrib;

  shaderToVertexAttrib["vPosition"] = "position";
  shaderToVertexAttrib["vTexCoord"] = "texcoord";

  pointObj =
      new util::ObjectInstance(string(""));

  pointObj->initPolygonMesh<VertexAttrib>(gl,
                                            program,
                                            shaderLocations,
                                            shaderToVertexAttrib,
                                            tmesh);




  //textures
  pointTexture = new util::TextureImage("textures/circle.png","sprite");

      QOpenGLTexture * tex = pointTexture->getTexture();



      tex->setWrapMode(QOpenGLTexture::Repeat);
      tex->setMinMagFilters(QOpenGLTexture::Nearest,QOpenGLTexture::Linear);
}



void View::init(util::OpenGLFunctions& gl) throw(runtime_error)
{
  //do this if your initialization throws an error (e.g. shader not found,
  //some model not found, etc.
  //  throw runtime_error("Some error happened!");

  //create the shader program
  program.createProgram(gl,
                        string("shaders/particle.vert"),
                        string("shaders/particle.frag"));

  //assuming it got created, get all the shader variables that it uses
  //so we can initialize them at some point
  shaderLocations = program.getAllShaderVariables(gl);

  initObjects(gl);
  initParticles();


}

void View::initParticles() {
    particles.clear();
    for (int i = 0; i < numParticles; i++) {
      particles.push_back(getRandomParticle());
    }
  }

Particle View::getRandomParticle() {
    glm::vec4 position,color,velocity;
    glm::vec3 dir;
    float temperature;
    float startTime;
    float mass;

    position = glm::vec4(0, 0, 0, 1);
            color = glm::vec4(0.5f+0.5f*(float)rand()/RAND_MAX,0.5f+0.5f*(float)rand()/RAND_MAX,0.5f+0.5f*(float)rand()/RAND_MAX,1);
            velocity = glm::vec4(20*(float)rand()/RAND_MAX-10,40*(float)rand()/RAND_MAX+10,20*(float)rand()/RAND_MAX-10,0.0f);

            startTime = 20*(float)rand()/RAND_MAX+1;
            mass = 5 * (float) rand()/RAND_MAX+1;

    return Particle(position,velocity,color,mass,startTime);
  }

void View::animate()
{
    time += 0.1;
  }



void View::draw(util::OpenGLFunctions& gl)
{
  animate();

  //set the background color to be black
  gl.glClearColor(backgroundColor.r,backgroundColor.g,backgroundColor.b,backgroundColor.a);
  //clear the background
  gl.glClear(GL_COLOR_BUFFER_BIT);


  gl.glEnable(GL_BLEND);
  gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
  gl.glEnable(GL_TEXTURE_2D);
  gl.glActiveTexture(GL_TEXTURE0);

  //enable the shader program
  program.enable(gl);

  gl.glUniform1i(shaderLocations.getLocation("sprite"), 0);


  modelview = glm::mat4(1.0);
  modelview = modelview * glm::lookAt(glm::vec3(0.0f, 0.0f, 70.0f),
                                      glm::vec3(0.0f, 0.0f, 0.0f),
                                      glm::vec3(0.0f, 1.0f, 0.0f));

  //pass the projection matrix to the shader
  gl.glUniformMatrix4fv(shaderLocations.getLocation("projection"),
                        1,
                        false,
                        glm::value_ptr(proj));

  //pass the projection matrix to the shader
  gl.glUniformMatrix4fv(shaderLocations.getLocation("modelview"),
                        1,
                        false,
                        glm::value_ptr(modelview));

  gl.glUniform3f(shaderLocations.getLocation("force"),
              force.x,
              force.y,
              force.z);

  gl.glUniform1f(shaderLocations.getLocation("time"), time);
  gl.glEnable(GL_TEXTURE_2D);
  gl.glActiveTexture(GL_TEXTURE0);

  gl.glUniform1i(shaderLocations.getLocation("sprite"), 0);
  pointTexture->getTexture()->bind();

  for (int i = 0; i < particles.size(); i++) {
        Particle particle = particles[i];

        if (time > particle.getStartTime()) {


          gl.glUniform4fv(shaderLocations.getLocation
                  ("initialPosition"), 1,glm::value_ptr(particle.getPosition() + startPosition));
          gl.glUniform4fv(
                  shaderLocations.getLocation("vColor"),1,glm::value_ptr(particle.getColor()));
          gl.glUniform3fv(
                  shaderLocations.getLocation("velocity"), 1,glm::value_ptr(particle.getVelocity()));
          gl.glUniform1f(
                  shaderLocations.getLocation("startTime"), particle.getStartTime());
          gl.glUniform1f(
                  shaderLocations.getLocation("mass"), particle.getMass());

          pointObj->draw(gl);
        }

      }
  //    gl.glFlush();

      program.disable(gl);
}

void View::forceRight() {
    force.x += 1;
  }

  void View::forceLeft() {
    force.x -= 1;
  }

  void View::forceUp() {
    force.y += 1;
  }

  void View::forceDown() {
    force.y -= 1;
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

 /* proj = glm::ortho(-0.25f*WINDOW_WIDTH,
                    0.25f*WINDOW_WIDTH,
                    -0.25f*WINDOW_HEIGHT,
                    0.25f*WINDOW_HEIGHT,
                    -10000.0f,10000.0f); */
  proj = glm::perspective(
              (float) glm::radians(120.0f),
              (float) width / height,
              0.1f,
              10000.0f);
}

void View::setStartPosition(int x, int y) {
  float actualY = WINDOW_HEIGHT - y - 0.5f * WINDOW_HEIGHT;
  float actualX = x - 0.5f * WINDOW_WIDTH;
  actualY = 70.0f*actualY/(0.5*WINDOW_HEIGHT/tan(glm::radians(120.0f)/2));
  actualX = 70.0f*actualX/(0.5*WINDOW_HEIGHT/tan(glm::radians(120.0f)/2));

    startPosition = glm::vec4(actualX,
            actualY, 0, 1);
  }

void View::dispose(util::OpenGLFunctions& gl)
{

  //release the shader resources
  program.releaseShaders(gl);
}
