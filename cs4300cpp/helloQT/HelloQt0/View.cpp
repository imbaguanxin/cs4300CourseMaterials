#include "View.h"
#include "VertexAttrib.h"
#include "PolygonMesh.h"
#include <glm/gtc/type_ptr.hpp>
#include <glm/gtc/matrix_transform.hpp>
#include <vector>
#include <map>
#include <string>
using namespace std;

View::View()
{   
    WINDOW_WIDTH = WINDOW_HEIGHT = 0;
}

View::~View()
{

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



    //BEGIN: uses vertices directly and glDrawArrays to draw

        float vertexDataAsFloats[] = {-100,-100,100,-100,100,100,-100,-100,100,
                100,-100,100};





        program.enable(gl);
        gl.glGenBuffers(1, vbo);
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        gl.glBufferData(GL_ARRAY_BUFFER, 12 * sizeof(float), vertexDataAsFloats, GL_STATIC_DRAW);

        gl.glVertexAttribPointer(shaderLocations.getLocation("vPosition")
                ,2
                , GL_FLOAT
                , false
                , 0
                , 0);
        //enable this attribute so that when rendered, this is sent to the vertex shader
        gl.glEnableVertexAttribArray(shaderLocations.getLocation("vPosition"));


        //END: uses vertices directly and glDrawArrays to draw


    /*
     //BEGIN: using vertices and indices, uses glDrawElements to draw

        float vertexDataAsFloats[] = {-100,-100,100,-100,100,100,-100,100};
        int indices[] = {0,1,2,0,2,3};


        program.enable(gl);
        gl.glGenBuffers(2,vbo);
        gl.glBindBuffer(GL_ARRAY_BUFFER,vbo[0]);
        gl.glBufferData(GL_ARRAY_BUFFER,
                8*sizeof(float),
                vertexDataAsFloats,GL_STATIC_DRAW);

        gl.glVertexAttribPointer(shaderLocations.getLocation("vPosition")
                ,2
                , GL_FLOAT
                , false
                , 0
                , 0);
        //enable this attribute so that when rendered, this is sent to the vertex shader
        gl.glEnableVertexAttribArray(shaderLocations.getLocation("vPosition"));

        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,vbo[1]);
        gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER,
                6*sizeof(int),
                indices,GL_STATIC_DRAW);

        //END: using vertices and indices, uses glDrawElements to draw
*/
}

void View::draw(util::OpenGLFunctions& gl)
{
    //set the background color to be white
    gl.glClearColor(1.0f,1.0f,1.0f,1.0f);
    //clear the background
    gl.glClear(GL_COLOR_BUFFER_BIT);

    //enable the shader program
    program.enable(gl);

    //initialize the color

    color = glm::vec4(1.0f,0.0f,0.0f,1.0f);


    //pass the projection matrix to the shader
    gl.glUniformMatrix4fv( //projection matrix is a uniform variable in shader
                           //4f indicates 4x4 matrix,
                           //v indicates it will be given as an array
                shaderLocations.getLocation("projection"), //location in shader
                1, //only one matrix
                false, //don't normalize the matrix (i.e. takes numbers as-is)
                glm::value_ptr(proj)); //convenience function to convert
                                       //glm::mat4 to float array

    gl.glUniform4fv( //the color is a uniform variable in the shader
                     //4f indicates this will be specified as 3 float values
                     //v means the three values will be specified in an array
                shaderLocations.getLocation("vColor"), //location in shader
                1, //only one value should be read from the array below
                glm::value_ptr(color)); //convenience function to convert
                                        //glm::vec3 to float array


    //use this if using only vertices
        //total 6 vertices that form 2 triangles
        gl.glDrawArrays(GL_TRIANGLES,0,6);

    //use this if using vertices and indices
    //total 6 indices that form 2 triangles (GL_TRIANGLES = take 3 indices at
    // a time
    //gl.glDrawElements(GL_TRIANGLES, 6,GL_UNSIGNED_INT, 0);


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

    proj = glm::ortho(-150.0f,150.0f,-150.0f,150.0f);

}

void View::dispose(util::OpenGLFunctions& gl)
{
    //release the shader resources
    program.releaseShaders(gl);
}
