#include "view.h"
#include <climits>
#include <glm/gtc/matrix_transform.hpp>
#include <glm/gtc/type_ptr.hpp>
#include <map>
#include <string>
#include <vector>
#include "PolygonMesh.h"
#include "VertexAttribWithColor.h"
using namespace std;

View::View()
{
    WINDOW_WIDTH = WINDOW_HEIGHT = 0;
    obj = NULL;
    timeclock = 0;
}

View::~View()
{
    if (obj != NULL) delete obj;
}

void View::init(util::OpenGLFunctions& gl) throw(runtime_error)
{
    // do this if your initialization throws an error (e.g. shader not found,
    // some model not found, etc.
    //  throw runtime_error("Some error happened!");

    // create the shader program
    program.createProgram(gl, string("shaders/default.vert"),
                          string("shaders/default.frag"));

    // assuming it got created, get all the shader variables that it uses
    // so we can initialize them at some point
    shaderLocations = program.getAllShaderVariables(gl);

    vector<glm::vec4> positions;
    positions.push_back(glm::vec4(0.0f, 1.0f, 0.0f, 1.0f));
    positions.push_back(glm::vec4(-0.5f, 0.0f, 0.0f, 1.0f));
    positions.push_back(glm::vec4(0.5f, 0.0f, 0.0f, 1.0f));

    vector<glm::vec4> colors;
    colors.push_back(glm::vec4(1.0f, 0.0f, 0.0f, 1.0f));  // red
    colors.push_back(glm::vec4(0.0f, 1.0f, 0.0f, 1.0f));  // green
    colors.push_back(glm::vec4(0.0f, 0.0f, 1.0f, 1.0f));  // blue

    // set up vertex attributes (in this case we have only position)
    vector<VertexAttribWithColor> vertexData;
    for (unsigned int i = 0; i < positions.size(); i++)
    {
        VertexAttribWithColor v;
        vector<float> data;

        data.push_back(positions[i].x);
        data.push_back(positions[i].y);
        data.push_back(positions[i].z);
        data.push_back(positions[i].w);
        v.setData("position", data);

        data.clear();
        data.push_back(colors[i].x);
        data.push_back(colors[i].y);
        data.push_back(colors[i].z);
        data.push_back(colors[i].w);
        v.setData("color", data);

        vertexData.push_back(v);
    }

    vector<unsigned int> indices;
    indices.push_back(0);
    indices.push_back(1);
    indices.push_back(2);

    util::PolygonMesh<VertexAttribWithColor> mesh;

    // give it the vertex data
    mesh.setVertexData(vertexData);
    // give it the index data that forms the polygons
    mesh.setPrimitives(indices);

    mesh.setPrimitiveType(
        GL_TRIANGLES);         // when rendering specify this to OpenGL
    mesh.setPrimitiveSize(3);  // 3 vertices per polygon

    /*
     * now we create an ObjectInstance for it.
     * The ObjectInstance encapsulates a lot of the OpenGL-specific code
     * to draw this object
     */

    /* so in the mesh, we have some attributes for each vertex. In the shader
     * we have variables for each vertex attribute. We have to provide a mapping
     * between attribute name in the mesh and corresponding shader variable
     name.
     *
     * This will allow us to use PolygonMesh with any shader program, without
     * assuming that the attribute names in the mesh and the names of
     * shader variables will be the same.

       We create such a shader variable -> vertex attribute mapping now
     */
    map<string, string> shaderVarsToVertexAttribs;

    // currently there are only two per-vertex attribute: position and color
    shaderVarsToVertexAttribs["vPosition"] = "position";
    shaderVarsToVertexAttribs["vColor"] = "color";

    obj = new util::ObjectInstance("triangles");
    obj->initPolygonMesh(
        gl,                         // the gl wrapper
        program,                    // the shader program
        shaderLocations,            // the shader locations
        shaderVarsToVertexAttribs,  // the shader variable -> attrib map
        mesh);                      // the actual mesh object
}

void View::draw(util::OpenGLFunctions& gl)
{
    glm::vec4 color;
    int NUM_BLADES = 4;
    int ORBIT_RADIUS = 50;
    int NUM_FANS = 8;
    int PENDULUM_RADIUS = 150;
    int PENDULUM_ANGLE = 45;

    // increment the time, looping back to avoid overflow
    timeclock = (timeclock + 1) % (INT_MAX - 1);

    // set the background color to be white
    gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    // clear the background
    gl.glClear(GL_COLOR_BUFFER_BIT);

    // enable the shader program
    program.enable(gl);

    // pass the projection matrix to the shader
    gl.glUniformMatrix4fv(shaderLocations.getLocation("projection"), 1, false,
                          glm::value_ptr(proj));

    modelview.push(glm::mat4(1.0));

    // move oscillating orbiting spinning fans
    modelview.push(modelview.top());
    modelview.top() = modelview.top() *
                      glm::translate(glm::mat4(1.0), glm::vec3(300, 300, 0));

    // pendulum motion, applied to orbiting spinning fans
    modelview.push(modelview.top());
    modelview.top() =
        modelview.top() *
        glm::rotate(glm::mat4(1.0),
                    glm::radians(PENDULUM_ANGLE * (float)sin(0.1f * timeclock)),
                    glm::vec3(0, 0, 1)) *
        glm::translate(glm::mat4(1.0), glm::vec3(0, -PENDULUM_RADIUS, 0));

    // the above transformation is applied to everything drawn in the loop below

    for (int j = 0; j < NUM_FANS; j++)
    {
        // put into orbit
        modelview.push(modelview.top());
        modelview.top() =
            modelview.top() *
            glm::rotate(glm::mat4(1.0), (float)glm::radians(1.0f * timeclock),
                        glm::vec3(0, 0, 1)) *
            glm::rotate(glm::mat4(1.0), glm::radians(j * 360.0f / NUM_FANS),
                        glm::vec3(0, 0, 1)) *
            glm::translate(glm::mat4(1.0), glm::vec3(ORBIT_RADIUS, 0, 0));

        // spinning motion
        modelview.push(modelview.top());
        modelview.top() =
            modelview.top() *
            glm::rotate(glm::mat4(1.0), glm::radians(10.0f * timeclock),
                        glm::vec3(0, 0, 1)) *
            glm::scale(glm::mat4(1.0), glm::vec3(0.125f, 0.125f, 0.125f));

        for (int i = 0; i < NUM_BLADES; i++)
        {
            // push a copy of modelview onto stack
            modelview.push(modelview.top());
            modelview.top() =
                modelview.top() *
                glm::rotate(glm::mat4(1.0),
                            glm::radians(i * 360.0f / NUM_BLADES),
                            glm::vec3(0, 0, 1)) *
                glm::scale(glm::mat4(1.0), glm::vec3(50, 100, 1.0f)) *
                glm::translate(glm::mat4(1.0), glm::vec3(0.0f, -1.0f, 0.0f));

            // pass the modelview matrix to the shader
            gl.glUniformMatrix4fv(shaderLocations.getLocation("modelview"), 1,
                                  false, glm::value_ptr(modelview.top()));

            // draw the object
            obj->draw(gl);
            // revert to the original modelview
            modelview.pop();
        }
        // undo spinning motion
        modelview.pop();

        // undo orbit
        modelview.pop();
    }

    // undo pendulum motion
    modelview.pop();
    // opengl is a pipeline-based framework. Things are not drawn as soon as
    // they are supplied. glFlush flushes the pipeline and draws everything
    gl.glFlush();
    // disable the program
    program.disable(gl);

    modelview.pop();
}

void View::reshape(util::OpenGLFunctions& gl, int width, int height)
{
    // record the new width and height
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

    proj = glm::ortho(0.0f, 500.0f, 0.0f, 500.0f);
}

void View::dispose(util::OpenGLFunctions& gl)
{
    // clean up the OpenGL resources used by the object
    obj->cleanup(gl);
    // release the shader resources
    program.releaseShaders(gl);
}
