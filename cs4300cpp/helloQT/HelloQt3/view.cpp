#include "view.h"
#include "VertexAttribWithColor.h"
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
    obj = NULL;
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
    program.createProgram(gl,string("shaders/default.vert"),string("shaders/default.frag"));

    //assuming it got created, get all the shader variables that it uses
    //so we can initialize them at some point
    shaderLocations = program.getAllShaderVariables(gl);



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
    positions.push_back(glm::vec4(-100.0f,-100.0f,0.0f,1.0f));
    positions.push_back(glm::vec4( 100.0f,-100.0f,0.0f,1.0f));
    positions.push_back(glm::vec4( 100.0f, 100.0f,0.0f,1.0f));
    positions.push_back(glm::vec4(-100.0f, 100.0f,0.0f,1.0f));

    /*
     * we add a second attribute to each vertex: color
     * note that the shader variable has been changed to "in" as compared
     * to HelloQt because color is now a per-vertex attribute
     */

    vector<glm::vec4> colors;
    colors.push_back(glm::vec4(1.0f,0.0f,0.0f,1.0f)); //red
    colors.push_back(glm::vec4(0.0f,1.0f,0.0f,1.0f)); //green
    colors.push_back(glm::vec4(0.0f,0.0f,1.0f,1.0f)); //blue
    colors.push_back(glm::vec4(1.0f,1.0f,1.0f,1.0f)); //white


    //set up vertex attributes (in this case we have only position)
    vector<VertexAttribWithColor> vertexData;
    for (unsigned int i=0;i<positions.size();i++) {
        VertexAttribWithColor v;
        vector<float> data;

        data.push_back(positions[i].x);
        data.push_back(positions[i].y);
        data.push_back(positions[i].z);
        data.push_back(positions[i].w);
        v.setData("position",data);

        data.clear();
        data.push_back(colors[i].x);
        data.push_back(colors[i].y);
        data.push_back(colors[i].z);
        data.push_back(colors[i].w);
        v.setData("color",data);

        vertexData.push_back(v);
    }



    /*
    We now generate a series of indices.
    These indices will be for the above list
    of vertices. For example we want to use
    the above list to draw triangles.
    The first triangle will be created from
    vertex numbers 0, 1 and 2 in the list above
    (indices begin at 0 like arrays). The second
    triangle will be created from vertex numbers
    0, 2 and 3. Therefore we will create a list
    of indices {0,1,2,0,2,3}.

    What is the advantage of having a second
    list? Vertices are often shared between
    triangles, and having a separate list of
    indices avoids duplication of vertex data.
    In other words, we would have the following options:
    (a) no indices: 2 triangles = 6 vertices = 24 floats
        (96 bytes @4 bytes/float)
    (b) with indices: 4 vertices = 16 floats + 6 indices (integers)
        (64+24=88 bytes @ 4 bytes/float and 4 bytes/int assumed)
     */
    vector<unsigned int> indices;
    indices.push_back(0);
    indices.push_back(1);
    indices.push_back(2);

    indices.push_back(0); //vertex 0 is shared
    indices.push_back(2); //vertex 2 is shared
    indices.push_back(3);

    //now we create a polygon mesh object. Think of this as literally a mesh
    //or network of polygons. There are vertices and they form polygons
    util::PolygonMesh<VertexAttribWithColor> mesh;

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

    mesh.setPrimitiveType(GL_TRIANGLES); //when rendering specify this to OpenGL
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
    shaderVarsToVertexAttribs["vColor"]="color";

    obj = new util::ObjectInstance("triangles");
    obj->initPolygonMesh<VertexAttribWithColor>(
                gl, //the gl wrapper
                program, //the shader program
                shaderLocations, //the shader locations
                shaderVarsToVertexAttribs, //the shader variable -> attrib map
                mesh); //the actual mesh object
}

void View::draw(util::OpenGLFunctions& gl)
{
    //set the background color to be black
    gl.glClearColor(0.0f,0.0f,0.0f,1.0f);
    //clear the background
    gl.glClear(GL_COLOR_BUFFER_BIT);

    //enable the shader program
    program.enable(gl);

    //initialize the modelview matrix
    modelview = glm::mat4(1.0f);
    modelview = modelview * glm::translate(glm::mat4(1.0),glm::vec3(-120,-120,0));

    //pass the modelview matrix to the shader
    gl.glUniformMatrix4fv( //modelview matrix is a uniform variable in shader
                           //4f indicates 4x4 matrix,
                           //v indicates it will be given as an array
                shaderLocations.getLocation("modelview"), //location in shader
                1, //only one matrix
                false, //don't normalize the matrix (i.e. takes numbers as-is)
                glm::value_ptr(modelview)); //convenience function to convert
                                       //glm::mat4 to float array

    //pass the projection matrix to the shader
    gl.glUniformMatrix4fv( //projection matrix is a uniform variable in shader
                           //4f indicates 4x4 matrix,
                           //v indicates it will be given as an array
                shaderLocations.getLocation("projection"), //location in shader
                1, //only one matrix
                false, //don't normalize the matrix (i.e. takes numbers as-is)
                glm::value_ptr(proj)); //convenience function to convert
                                       //glm::mat4 to float array


    //draw the object
    obj->draw(gl);


    //draw the second object, which is another rectangle

    //we reset the modelview matrix, so that we can start from scratch
    modelview = glm::mat4(1.0f);
    modelview = modelview * glm::translate(glm::mat4(1.0),glm::vec3(120,120,0));

    //pass the modelview matrix to the shader
    gl.glUniformMatrix4fv( //modelview matrix is a uniform variable in shader
                           //4f indicates 4x4 matrix,
                           //v indicates it will be given as an array
                shaderLocations.getLocation("modelview"), //location in shader
                1, //only one matrix
                false, //don't normalize the matrix (i.e. takes numbers as-is)
                glm::value_ptr(modelview)); //convenience function to convert
                                       //glm::mat4 to float array

    //we do not pass the projection matrix to the shader again,
    //because it has not changed

    //draw the object. Note that we reuse the same object, because we are
    //drawing the same shape again
    obj->draw(gl);

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

    proj = glm::ortho(-300.0f,300.0f,-300.0f,300.0f);

}

void View::dispose(util::OpenGLFunctions& gl)
{
    //clean up the OpenGL resources used by the object
    obj->cleanup(gl);
    //release the shader resources
    program.releaseShaders(gl);
}
