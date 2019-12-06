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
    meshObject = NULL;
    proj = glm::mat4(1.0);
    modelview = glm::mat4(1.0);
    angleOfRotation = 0;
}

View::~View()
{
    if (meshObject!=NULL)
        delete meshObject;
}

void View::initObjects(util::OpenGLFunctions& gl) throw(runtime_error)
{
    util::PolygonMesh<VertexAttrib> tmesh;

    ifstream in("models/thomas-lyons-object.obj");

    tmesh = util::ObjImporter<VertexAttrib>::importFile(in,true);

    map<string,string> shaderToVertexAttrib;

    shaderToVertexAttrib["vPosition"] = "position";

    meshObject = new util::ObjectInstance("OBJ model");
    meshObject->initPolygonMesh(gl,
                                          program,
                                          shaderLocations,
                                          shaderToVertexAttrib,
                                          tmesh);




    material.setAmbient(1,1,1);
    material.setDiffuse(1,1,1);
    material.setSpecular(1,1,1);


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

    initObjects(gl);


}

void View::draw(util::OpenGLFunctions& gl)
{
    gl.glEnable(GL_LINE_SMOOTH);
    gl.glEnable(GL_BLEND);
    gl.glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
    gl.glHint(GL_LINE_SMOOTH_HINT,GL_NICEST);
    angleOfRotation = (angleOfRotation+1)%360;

    modelview = glm::mat4(1.0);
    modelview = modelview
                * glm::lookAt(
                              glm::vec3(0.0f,200.0f,200.0f),
                              glm::vec3(0.0f,0.0f,0.0f),
                              glm::vec3(0.0f,1.0f,0.0f))
                * glm::scale(glm::mat4(1.0),
                             glm::vec3(200.0f,200.0f,200.0f))
                * glm::rotate(glm::mat4(1.0),
                              glm::radians((float)angleOfRotation),
                              glm::vec3(0.0f,1.0f,0.0f));

    //set the background color to be white
    gl.glClearColor(0.0f,0.0f,0.0f,0.0f);
    //clear the background
    gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    gl.glEnable(GL_DEPTH_TEST);

    //enable the shader program
    program.enable(gl);


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
    gl.glUniformMatrix4fv( //modelview matrix is a uniform variable in shader
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
                glm::value_ptr(material.getAmbient())); //convenience function to convert
                                        //glm::vec4 to float array

    gl.glPolygonMode(GL_FRONT_AND_BACK,GL_LINE);

    //draw the object
    meshObject->draw(gl);

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

    proj = glm::perspective(glm::radians(120.0f),
                            (float)WINDOW_WIDTH/WINDOW_HEIGHT,
                            0.1f,
                            10000.0f);
    proj = glm::ortho(-400.0f,400.0f,-400.0f,400.0f,0.1f,10000.0f);
   // proj = glm::ortho(-150.0f,150.0f,-150.0f,150.0f);

}

void View::dispose(util::OpenGLFunctions& gl)
{
    //clean up the OpenGL resources used by the object
    meshObject->cleanup(gl);
    //release the shader resources
    program.releaseShaders(gl);
}
