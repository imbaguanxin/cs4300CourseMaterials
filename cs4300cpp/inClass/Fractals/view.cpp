#include "view.h"
#include "VertexAttrib.h"
#include "PolygonMesh.h"
#include <glm/gtc/type_ptr.hpp>
#include <glm/gtc/matrix_transform.hpp>
#include <vector>
#include <map>
#include <string>
#include <iostream>
using namespace std;

View::View()
{   
    WINDOW_WIDTH = WINDOW_HEIGHT = 0;
    obj = NULL;
    //for mandelbrot, try the following points below for zoom without pan:
        // (0.0f,1.0f)
        //(-2.0f,0.0f)
        //(-0.77568377f, 0.13646737f)
        //(-1.54368901f, 0.0f)
    center = glm::vec2(0.0f,0.0f);
    scale = scalex = scaley = 1.0f;
    MAX_ITERATIONS = 50;
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
                          string("shaders/fractal.vert"),
                          string("shaders/fractal.frag"));

    //assuming it got created, get all the shader variables that it uses
    //so we can initialize them at some point
    shaderLocations = program.getAllShaderVariables(gl);

    //get input variables that need to be given to the shader program
    projectionLocation = shaderLocations.getLocation("projection");
    modelviewLocation = shaderLocations.getLocation("modelview");
    maxIterLocation = shaderLocations.getLocation( "maxiter");
    dimsLocation = shaderLocations.getLocation("dims");
    centerLocation = shaderLocations.getLocation("center");
    scaleLocation = shaderLocations.getLocation("scale");

    vPositionLocation = shaderLocations.getLocation("vPosition");


    /*
      Now we create a triangle mesh from these
      vertices.

      The mesh has vertex positions and indices for now.

     */

    /*
    Create the vertices of the two triangles to be
    drawn. Since we are drawing in 2D, z-coordinate
    of all points will be 0. The fourth number
    for each vertex is 1. This is the
    homogeneous coordinate, and "1" means this
    is a location and not a direction
     */

    vector<glm::vec4> positions;
    positions.push_back(glm::vec4(0.0f,0.0f,0.0f,1.0f));
    positions.push_back(glm::vec4(1.0f,0.0f,0.0f,1.0f));
    positions.push_back(glm::vec4(1.0f,1.0f,0.0f,1.0f));
    positions.push_back(glm::vec4(0.0f,1.0f,0.0f,1.0f));

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



    //draw a single quad
    vector<unsigned int> indices;
    indices.push_back(0);
    indices.push_back(1);
    indices.push_back(2);
    indices.push_back(3);

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

    The first, simplest (and the one we have
    assumed above) is to just read the list of
    indices 3 at a time, and use them as triangles.
    In OpenGL, this is the GL_TRIANGLES mode.

    If we wanted to draw lines by reading the indices
    two at a time, we would specify GL_LINES (try this).

    In any case, this "mode" and the actual list of
    indices are related. That is, decide which mode
    you want to use, and accordingly build the list
    of indices.
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

    //currently there is only one per-vertex attribute: position
    shaderVarsToVertexAttribs["vPosition"]="position";
    obj = new util::ObjectInstance("triangles");
    obj->initPolygonMesh<VertexAttrib>(
                gl, //the gl wrapper
                program, //the shader program
                shaderLocations, //the shader locations
                shaderVarsToVertexAttribs, //the shader variable -> attrib map
                mesh); //the actual mesh object


}

void View::draw(util::OpenGLFunctions& gl)
{
    //set the background color to be white
    gl.glClearColor(0.0f,0.0f,0.0f,1.0f);
    //clear the background
    gl.glClear(GL_COLOR_BUFFER_BIT);

    modelview = glm::mat4(1.0f);
    modelview = glm::scale
            (glm::mat4(1.0f),
             glm::vec3((float)WINDOW_WIDTH,WINDOW_HEIGHT,1));


    //enable the shader program
    program.enable(gl);


    //pass the projection matrix to the shader
    gl.glUniformMatrix4fv( //projection matrix is a uniform variable in shader
                           //4f indicates 4x4 matrix,
                           //v indicates it will be given as an array
                projectionLocation, //location in shader
                1, //only one matrix
                false, //don't normalize the matrix (i.e. takes numbers as-is)
                glm::value_ptr(proj)); //convenience function to convert
                                       //glm::mat4 to float array

    //pass the modelview matrix to the shader
    gl.glUniformMatrix4fv( //projection matrix is a uniform variable in shader
                           //4f indicates 4x4 matrix,
                           //v indicates it will be given as an array
                modelviewLocation, //location in shader
                1, //only one matrix
                false, //don't normalize the matrix (i.e. takes numbers as-is)
                glm::value_ptr(modelview)); //convenience function to convert
                                       //glm::mat4 to float array

    //send the fractal parameters to the shader
    gl.glUniform1i(maxIterLocation,MAX_ITERATIONS);
    gl.glUniform2f(dimsLocation,WINDOW_WIDTH,WINDOW_HEIGHT);
    gl.glUniform2f(centerLocation,center.x,center.y);
    gl.glUniform1f(scaleLocation,(float)scale);

    //draw the object
    obj->draw(gl);

    //opengl is a pipeline-based framework. Things are not drawn as soon as
    //they are supplied. glFlush flushes the pipeline and draws everything
    gl.glFlush();
    //disable the program
    program.disable(gl);
}

void View::zoomIn()
{
    scalex = 4*scalex;
    scaley = 5*scaley;

    if ((scalex>10000) || (scaley>10000))
    {
        scalex = scalex/10000;
        scaley = scaley/10000;
    }
    scale = scalex/scaley;
    cout << "Scalex: " << scalex << " and scaley: " << scaley << endl;
}

void View::zoomOut()
{
    scalex = 0.25f*scalex;
    scaley = 0.25f*scaley;

    if ((scalex<0.0001f) || (scaley<0.0001f))
    {
        scalex = scalex*10000;
        scaley = scaley*10000;
    }

    scale = scalex/scaley;
}

void View::increaseMaxIterations()
{
    MAX_ITERATIONS+=5;
}

void View::decreaseMaxIterations()
{
    if (MAX_ITERATIONS>5)
        MAX_ITERATIONS-=5;
}

void View::translate(int x,int y)
{
    center.x = center.x - (float)(scale*x/(WINDOW_WIDTH));
    center.y = center.y - (float)(scale*y/(WINDOW_HEIGHT));
}

string View::getFrameInfoString()
{
    stringstream str;

    str << "Range of set:("
        << (center.x - 0.5 * scale)
        << "," << (center.x+0.5*scale)
        << ") to " << (center.x+0.5*scale)
        << "," << (center.y + 0.5 * scale) << "\t";

    return str.str();

}

string View::getIterationInfoString()
{
    stringstream str;
    str << "Number of iterations: " << MAX_ITERATIONS;
    return str.str();
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

    proj = glm::ortho(0.0f,(float)WINDOW_WIDTH,0.0f,(float)WINDOW_HEIGHT);

}

void View::dispose(util::OpenGLFunctions& gl)
{
    //clean up the OpenGL resources used by the object
    obj->cleanup(gl);
    //release the shader resources
    program.releaseShaders(gl);
}
