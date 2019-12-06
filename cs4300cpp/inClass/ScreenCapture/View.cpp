#define GLM_SWIZZLE
#include "View.h"
#include "VertexAttrib.h"
#include "PolygonMesh.h"
#include <glm/gtc/type_ptr.hpp>
#include <glm/gtc/matrix_transform.hpp>
#include <vector>
#include <map>
#include <string>
using namespace std;
#include <cmath>

View::View()
{   
  WINDOW_WIDTH = WINDOW_HEIGHT = 0;
  obj = NULL;
  center = glm::vec4(100,100,0,1);
  //default motion is speed 1 in direction (1,1)
  motion = glm::vec4(1,1,0,0); //last coordinate is 0 because this is a vector
}

View::~View()
{
  if (obj!=NULL)
    delete obj;
}

void View::init(util::OpenGLFunctions& gl) throw(runtime_error)
{
  //do this if your initialization throws an error (e.g. shader not found,
  //some model not found, etc.
  //  throw runtime_error("Some error happened!");

  //create the shader program
  program.createProgram(gl,
                        string("shaders/default.vert"),
                        string("shaders/default.frag"));

  //assuming it got created, get all the shader variables that it uses
  //so we can initialize them at some point
  shaderLocations = program.getAllShaderVariables(gl);





  /*
    Think of creating a circle as a pizza. We assemble the circle from pizza
    slices. The narrower the slices, the smoother will be the boundary of the
    circle.
     */

  vector<glm::vec4> positions;

  //create vertices here
  int SLICES = 50;
  float PI = 3.14159f;
  //the first vertex for the center

  positions.push_back(glm::vec4(0,0,0,1));
  for (int i=0;i<SLICES;i++)
    {
      float theta = i*2*PI/SLICES;

      positions.push_back(glm::vec4(
                            cos(theta),
                            sin(theta),
                            0,
                            1));
    }

  //the last vertex to make sure circle is watertight
  positions.push_back(glm::vec4(1,0,0,1));



  //set up vertex attributes (in this case we have only position)
  vector<VertexAttrib> vertexData;
  for (unsigned int i=0;i<positions.size();i++) {
      VertexAttrib v;
      vector<float> data;

      data.push_back(positions[i].x);
      data.push_back(positions[i].y);
      data.push_back(positions[i].z);
      data.push_back(positions[i].w);
      v.setData("position",data);



      vertexData.push_back(v);
    }



  /*
    We now generate a series of indices.
    Think about how you will specify the indices given the above ordering
    of vertices
     */
  vector<unsigned int> indices;

  for (int i=0;i<positions.size();i++)
    {
      indices.push_back(i);
    }


  //now we create a polygon mesh object. Think of this as literally a mesh
  //or network of polygons. There are vertices and they form polygons
  util::PolygonMesh<VertexAttrib> mesh;

  //give it the vertex data
  mesh.setVertexData(vertexData);
  //give it the index data that forms the polygons
  mesh.setPrimitives(indices);

  /*
    It turns out, there are several ways of
    reading the list of indices and interpreting
    them as triangles.

    We use GL_TRIANGLE_FAN because it seems tailormade to draw a circle
    this will form triangles using indices from the positions (0,1,2), (0,2,3),
    and so on.
     */

  mesh.setPrimitiveType(GL_TRIANGLE_FAN); //when rendering specify this to OpenGL
  mesh.setPrimitiveSize(3); //3 vertices per polygon

  /*
     * now we create an ObjectInstance for it.
     * The ObjectInstance encapsulates a lot of the OpenGL-specific code
     * to draw this object
     */

  /* so in the mesh, we have some attributes for each vertex. In the shader
     * we have variables for each vertex attribute. We have to provide a mapping
     * between attribute name in the mesh and corresponding shader variable name.
     *
     * This will allow us to use PolygonMesh with any shader program, without
     * assuming that the attribute names in the mesh and the names of
     * shader variables will be the same.

       We create such a shader variable -> vertex attribute mapping now
     */
  map<string,string> shaderVarsToVertexAttribs;

  //currently there are only two per-vertex attribute: position and color
  shaderVarsToVertexAttribs["vPosition"]="position";

  obj = new util::ObjectInstance(string("ball"));

  obj->initPolygonMesh<VertexAttrib>(
        gl, //the gl wrapper
        program, //the shader program
        shaderLocations, //the shader locations
        shaderVarsToVertexAttribs, //the shader variable -> attrib map
        mesh); //the actual mesh object

  //we will color this square red
  color = glm::vec4(1.0f,0.0f,0.0f,1.0f);
}

void View::draw(util::OpenGLFunctions& gl)
{
  float radius = 50;

  //move the circle along the motion vector
  center = center + motion;
  //if the center is out of bounds in X, reverse the motion direction in x
  if ((center.x<radius) || (center.x>(WINDOW_WIDTH - radius))) //using bounds from glOrtho2D
    {
      motion.x = -motion.x;
    }
  //if the center is out of bounds in Y, reverse the motion direction in y
  if ((center.y<radius) || (center.y>(WINDOW_HEIGHT - radius)))
    {
      motion.y = -motion.y;
    }

  //set the background color to be white
  gl.glClearColor(1.0f,1.0f,1.0f,1.0f);
  //clear the background
  gl.glClear(GL_COLOR_BUFFER_BIT);

  //enable the shader program
  program.enable(gl);

  modelview = glm::mat4(1.0);
  modelview = modelview *
      glm::translate(glm::mat4(1.0),center.xyz()) *
      glm::scale(glm::mat4(1.0),glm::vec3(radius,radius,radius));


  //pass the projection matrix to the shader
  gl.glUniformMatrix4fv( //projection matrix is a uniform variable in shader
                         //4f indicates 4x4 matrix,
                         //v indicates it will be given as an array
                         shaderLocations.getLocation("projection"), //location in shader
                         1, //only one matrix
                         false, //don't normalize the matrix (i.e. takes numbers as-is)
                         glm::value_ptr(proj)); //convenience function to convert
  //glm::mat4 to float array

  //pass the modelview matrix to the shader
  gl.glUniformMatrix4fv( //projection matrix is a uniform variable in shader
                         //4f indicates 4x4 matrix,
                         //v indicates it will be given as an array
                         shaderLocations.getLocation("modelview"), //location in shader
                         1, //only one matrix
                         false, //don't normalize the matrix (i.e. takes numbers as-is)
                         glm::value_ptr(modelview)); //convenience function to convert
  //glm::mat4 to float array

  gl.glUniform4fv( //the color is a uniform variable in the shader
                   //4f indicates this will be specified as 3 float values
                   //v means the three values will be specified in an array
                   shaderLocations.getLocation("vColor"), //location in shader
                   1, //only one value should be read from the array below
                   glm::value_ptr(color)); //convenience function to convert
  //glm::vec3 to float array


  //draw the object
  obj->draw(gl);

  gl.glPolygonMode(GL_FRONT_AND_BACK,GL_FILL);

  //opengl is a pipeline-based framework. Things are not drawn as soon as
  //they are supplied. glFlush flushes the pipeline and draws everything
  gl.glFlush();
  //disable the program
  program.disable(gl);
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

  proj = glm::ortho(0.0f,(float)width,0.0f,(float)height);

}

void View::dispose(util::OpenGLFunctions& gl)
{
  //clean up the OpenGL resources used by the object
  obj->cleanup(gl);
  //release the shader resources
  program.releaseShaders(gl);
}
