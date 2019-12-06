#include "View.h"
#include <glm/gtc/matrix_transform.hpp>
#include <glm/gtc/type_ptr.hpp>
#include <map>
#include <string>
#include <vector>
#include "PolygonMesh.h"
#include "VertexAttrib.h"
using namespace std;
#include "OBJImporter.h"

View::View()
{
    WINDOW_WIDTH = WINDOW_HEIGHT = 0;
    proj = glm::mat4(1.0);
    time = 0.0f;
    cameraMode = GLOBAL;
}

View::~View()
{
    for (map<ObjectName, util::ObjectInstance*>::iterator it =
             meshObjects.begin();
         it != meshObjects.end(); it++)
    {
        delete it->second;
    }
}

void View::initObjects(util::OpenGLFunctions& gl) throw(runtime_error)
{
    util::PolygonMesh<VertexAttrib> mesh;
    util::ObjectInstance* o;
    glm::mat4 transform;
    util::Material mat;

    ifstream in;

    map<string, string> shaderToVertexAttribute;

    // currently there is only one per-vertex attribute: position
    shaderToVertexAttribute["vPosition"] = "position";

    // floor
    in.open("models/box.obj");
    mesh = util::ObjImporter<VertexAttrib>::importFile(in, false);
    o = new util::ObjectInstance(string("floor"));
    o->initPolygonMesh<VertexAttrib>(gl, program, shaderLocations,
                                     shaderToVertexAttribute, mesh);
    mat.setAmbient(0, 0,
                   1);  // only this one is used currently to determine color
    mat.setDiffuse(0, 0, 1);
    mat.setSpecular(0, 0, 1);

    meshObjects[FLOOR] = o;
    meshMaterials[FLOOR] = mat;

    transform = glm::translate(glm::mat4(1.0), glm::vec3(0.0f, -1.0f, 0.0f)) *
                glm::scale(glm::mat4(1.0), glm::vec3(500.0f, 2.0f, 500.0f));

    meshTransforms[FLOOR] = transform;
    animationTransforms[FLOOR] = glm::mat4(1.0);
    in.close();

    // back wall
    in.open("models/box.obj");
    mesh = util::ObjImporter<VertexAttrib>::importFile(in, true);
    o = new util::ObjectInstance(string("back wall"));
    o->initPolygonMesh<VertexAttrib>(gl, program, shaderLocations,
                                     shaderToVertexAttribute, mesh);
    mat.setAmbient(1, 0,
                   0);  // only this one is used currently to determine color
    mat.setDiffuse(1, 0, 0);
    mat.setSpecular(1, 0, 0);
    meshMaterials[WALL_BACK] = mat;
    meshObjects[WALL_BACK] = o;
    transform =
        glm::translate(glm::mat4(1.0), glm::vec3(0.0f, 150.0f, -250.0f)) *
        glm::scale(glm::mat4(1.0), glm::vec3(500.0f, 300.0f, 2.0f));
    meshTransforms[WALL_BACK] = transform;
    animationTransforms[WALL_BACK] = glm::mat4(1.0);
    in.close();

    // front wall
    in.open("models/box.obj");
    mesh = util::ObjImporter<VertexAttrib>::importFile(in, true);
    o = new util::ObjectInstance(string("front wall"));
    o->initPolygonMesh<VertexAttrib>(gl, program, shaderLocations,
                                     shaderToVertexAttribute, mesh);
    mat.setAmbient(0, 1,
                   0);  // only this one is used currently to determine color
    mat.setDiffuse(0, 1, 0);
    mat.setSpecular(0, 1, 0);
    meshMaterials[WALL_FRONT] = mat;
    meshObjects[WALL_FRONT] = o;
    transform =
        glm::translate(glm::mat4(1.0), glm::vec3(0.0f, 150.0f, 250.0f)) *
        glm::scale(glm::mat4(1.0), glm::vec3(500.0f, 300.0f, 2.0f));

    meshTransforms[WALL_FRONT] = transform;
    animationTransforms[WALL_FRONT] = glm::mat4(1.0);
    in.close();

    // left wall
    in.open("models/box.obj");
    mesh = util::ObjImporter<VertexAttrib>::importFile(in, true);
    o = new util::ObjectInstance(string("left wall"));
    o->initPolygonMesh<VertexAttrib>(gl, program, shaderLocations,
                                     shaderToVertexAttribute, mesh);
    mat.setAmbient(1, 0.5f,
                   0);  // only this one is used currently to determine color
    mat.setDiffuse(1, 0.5f, 0);
    mat.setSpecular(1, 0.5f, 0);
    meshMaterials[WALL_LEFT] = mat;
    meshObjects[WALL_LEFT] = o;
    transform =
        glm::translate(glm::mat4(1.0), glm::vec3(-250.0f, 150.0f, 0.0f)) *
        glm::scale(glm::mat4(1.0), glm::vec3(2.0f, 300.0f, 500.0f));
    meshTransforms[WALL_LEFT] = transform;
    animationTransforms[WALL_LEFT] = glm::mat4(1.0);
    in.close();

    // right wall
    in.open("models/box.obj");
    mesh = util::ObjImporter<VertexAttrib>::importFile(in, true);
    o = new util::ObjectInstance(string("right wall"));
    o->initPolygonMesh<VertexAttrib>(gl, program, shaderLocations,
                                     shaderToVertexAttribute, mesh);
    mat.setAmbient(1, 0,
                   0.5f);  // only this one is used currently to determine color
    mat.setDiffuse(1, 0, 0.5f);
    mat.setSpecular(1, 0, 0.5f);
    meshMaterials[WALL_RIGHT] = mat;
    meshObjects[WALL_RIGHT] = o;
    transform =
        glm::translate(glm::mat4(1.0), glm::vec3(250.0f, 150.0f, 0.0f)) *
        glm::scale(glm::mat4(1.0), glm::vec3(2.0f, 300.0f, 500.0f));
    meshTransforms[WALL_RIGHT] = transform;
    animationTransforms[WALL_RIGHT] = glm::mat4(1.0);
    in.close();

    // jack in the box face
    in.open("models/sphere.obj");
    mesh = util::ObjImporter<VertexAttrib>::importFile(in, false);
    o = new util::ObjectInstance(string("sphere"));
    o->initPolygonMesh<VertexAttrib>(gl, program, shaderLocations,
                                     shaderToVertexAttribute, mesh);
    mat.setAmbient(1, 0,
                   0);  // only this one is used currently to determine color
    mat.setDiffuse(1, 0, 0);
    mat.setSpecular(1, 0, 0);
    meshMaterials[JACK_BOX_FACE] = mat;
    meshObjects[JACK_BOX_FACE] = o;
    transform = glm::translate(glm::mat4(1.0), glm::vec3(0.0f, 30.0f, 0.0f)) *
                glm::scale(glm::mat4(1.0), glm::vec3(50.0f, 30.0f, 50.0f));
    meshTransforms[JACK_BOX_FACE] = transform;
    animationTransforms[JACK_BOX_FACE] = glm::mat4(1.0);
    in.close();

    // jack in the box cap
    in.open("models/cone.obj");
    mesh = util::ObjImporter<VertexAttrib>::importFile(in, false);
    o = new util::ObjectInstance(string("cone"));
    o->initPolygonMesh<VertexAttrib>(gl, program, shaderLocations,
                                     shaderToVertexAttribute, mesh);
    mat.setAmbient(0, 1,
                   0);  // only this one is used currently to determine color
    mat.setDiffuse(0, 1, 0);
    mat.setSpecular(0, 1, 0);
    meshMaterials[JACK_BOX_CAP] = mat;
    meshObjects[JACK_BOX_CAP] = o;
    transform = glm::translate(glm::mat4(1.0), glm::vec3(0.0f, 60.0f, 0.0f)) *
                glm::scale(glm::mat4(1.0), glm::vec3(20.0f, 100.0f, 20.0f));
    meshTransforms[JACK_BOX_CAP] = transform;
    animationTransforms[JACK_BOX_CAP] = glm::mat4(1.0);
    in.close();

    // aeroplane

    in.open("models/aeroplane.obj");
    mesh = util::ObjImporter<VertexAttrib>::importFile(in, true);
    glm::vec4 maxB = mesh.getMaximumBounds();
    o = new util::ObjectInstance(string("aeroplane"));
    o->initPolygonMesh<VertexAttrib>(gl, program, shaderLocations,
                                     shaderToVertexAttribute, mesh);
    mat.setAmbient(1, 1,
                   0);  // only this one is used currently to determine color
    mat.setDiffuse(1, 1, 0);
    mat.setSpecular(1, 1, 0);
    meshMaterials[AEROPLANE] = mat;
    meshObjects[AEROPLANE] = o;
    transform = glm::rotate(glm::mat4(1.0), glm::radians(90.0f),
                            glm::vec3(1.0f, 0.0f, 0.0f)) *
                glm::scale(glm::mat4(1.0), glm::vec3(100.0f, 100.0f, 100.0f)) *
                glm::rotate(glm::mat4(1.0), glm::radians(180.0f),
                            glm::vec3(0.0f, 1.0f, 0.0f)) *
                glm::translate(glm::mat4(1.0), glm::vec3(0.0f, -maxB.y, 0.0f));
    meshTransforms[AEROPLANE] = transform;
    animationTransforms[AEROPLANE] = glm::mat4(1.0);
    in.close();

    // yellow cube

    in.open("models/box.obj");
    mesh = util::ObjImporter<VertexAttrib>::importFile(in, false);
    o = new util::ObjectInstance(string("yellow box"));
    o->initPolygonMesh<VertexAttrib>(gl, program, shaderLocations,
                                     shaderToVertexAttribute, mesh);
    mat.setAmbient(1, 1,
                   0);  // only this one is used currently to determine color
    mat.setDiffuse(1, 1, 0);
    mat.setSpecular(1, 1, 0);
    meshMaterials[YELLOW_CUBE] = mat;
    meshObjects[YELLOW_CUBE] = o;
    transform = glm::scale(glm::mat4(1.0), glm::vec3(20.0f, 20.0f, 20.0f));
    meshTransforms[YELLOW_CUBE] = transform;
    animationTransforms[YELLOW_CUBE] = glm::mat4(1.0);
    in.close();

    // purple cube
    in.open("models/box.obj");
    mesh = util::ObjImporter<VertexAttrib>::importFile(in, false);
    o = new util::ObjectInstance(string("purple box"));
    o->initPolygonMesh<VertexAttrib>(gl, program, shaderLocations,
                                     shaderToVertexAttribute, mesh);
    mat.setAmbient(1, 0,
                   1);  // only this one is used currently to determine color
    mat.setDiffuse(1, 0, 1);
    mat.setSpecular(1, 0, 1);
    meshMaterials[PURPLE_CUBE] = mat;
    meshObjects[PURPLE_CUBE] = o;
    transform = glm::scale(glm::mat4(1.0), glm::vec3(20.0f, 20.0f, 20.0f));
    meshTransforms[PURPLE_CUBE] = transform;
    animationTransforms[PURPLE_CUBE] = glm::mat4(1.0);
    in.close();

    // red orb
    in.open("models/sphere.obj");
    mesh = util::ObjImporter<VertexAttrib>::importFile(in, false);
    o = new util::ObjectInstance(string("red orb"));
    o->initPolygonMesh<VertexAttrib>(gl, program, shaderLocations,
                                     shaderToVertexAttribute, mesh);
    mat.setAmbient(1, 0,
                   0);  // only this one is used currently to determine color
    mat.setDiffuse(1, 0, 0);
    mat.setSpecular(1, 0, 0);
    meshMaterials[RED_ORB] = mat;
    meshObjects[RED_ORB] = o;
    transform = glm::scale(glm::mat4(1.0), glm::vec3(20.0f, 20.0f, 20.0f));
    meshTransforms[RED_ORB] = transform;
    animationTransforms[RED_ORB] = glm::mat4(1.0);
    in.close();

    // neptune
    in.open("models/neptune.obj");
    mesh = util::ObjImporter<VertexAttrib>::importFile(in, true);
    o = new util::ObjectInstance(string("neptune"));
    o->initPolygonMesh<VertexAttrib>(gl, program, shaderLocations,
                                     shaderToVertexAttribute, mesh);
    mat.setAmbient(1, 0,
                   1);  // only this one is used currently to determine color
    mat.setDiffuse(1, 0, 1);
    mat.setSpecular(1, 0, 1);
    meshMaterials[NEPTUNE] = mat;
    meshObjects[NEPTUNE] = o;
    // the translation correction in y is because the base of the nepture model
    // is not exactly horizontal
    transform = glm::scale(glm::mat4(1.0), glm::vec3(80.0f, 80.0f, 80.0f)) *
                glm::translate(
                    glm::mat4(1.0),
                    glm::vec3(0.0f, -mesh.getMinimumBounds().y + 0.1f, 0.0f));
    meshTransforms[NEPTUNE] = transform;
    animationTransforms[NEPTUNE] = glm::mat4(1.0);
    in.close();
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

    initObjects(gl);
}

void View::draw(util::OpenGLFunctions& gl)
{
    time += 0.01;
    animate();

    // set the background color to be white
    gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    // clear the background
    gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    gl.glEnable(GL_DEPTH_TEST);

    // enable the shader program
    program.enable(gl);

    while (!modelview.empty()) modelview.pop();

    modelview.push(glm::mat4(1.0));

    if (cameraMode == GLOBAL)
    {
        glm::vec4 camera = glm::vec4(0.0f, 600.0f, 600.0f, 1.0f);
        // transform the camera
        // camera = cameraRotation * camera;
        modelview.top() = modelview.top() *
                          glm::lookAt(glm::vec3(camera.x, camera.y, camera.z),
                                      glm::vec3(0.0f, 0.0f, 0.0f),
                                      glm::vec3(0.0f, 1.0f, 0.0f));
    }
    else
    {
        // modelview.top() = modelview.top() *
        // glm::rotate(glm::mat4(1.0),glm::radians(-90.0f),glm::vec3(1.0f,0.0f,0.0f))
        // * glm::inverse(animation_transform[3] *
        // objectsList[3]->getTransform()) ;
        modelview.top() = modelview.top() *
                          glm::lookAt(glm::vec3(0.0f, 0.0f, -50.0f),
                                      glm::vec3(0.0f, 0.0f, 500.0f),
                                      glm::vec3(0.0f, 1.0f, 0.0f)) *
                          glm::inverse(animationTransforms[AEROPLANE]);
    }

    // pass the projection matrix to the shader
    gl.glUniformMatrix4fv(  // projection matrix is a uniform variable in shader
                            // 4f indicates 4x4 matrix,
                            // v indicates it will be given as an array
        shaderLocations.getLocation("projection"),  // location in shader
        1,                                          // only one matrix
        false,  // don't normalize the matrix (i.e. takes numbers as-is)
        glm::value_ptr(proj));  // convenience function to convert
                                // glm::mat4 to float array

    gl.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

    for (map<ObjectName, util::ObjectInstance*>::iterator it =
             meshObjects.begin();
         it != meshObjects.end(); it++)
    {
        modelview.push(modelview.top());  // save the current modelview
        glm::mat4 transform =
            animationTransforms[it->first] * meshTransforms[it->first];
        modelview.top() = modelview.top() * transform;

        // The total transformation is whatever was passed to it, with its own
        // transformation
        gl.glUniformMatrix4fv(shaderLocations.getLocation("modelview"), 1,
                              false, glm::value_ptr(modelview.top()));
        // set the color for all vertices to be drawn for this object
        gl.glUniform4fv(shaderLocations.getLocation("vColor"), 1,
                        glm::value_ptr(meshMaterials[it->first].getAmbient()));
        it->second->draw(gl);
        modelview.pop();
    }
    modelview.pop();

    gl.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

    // opengl is a pipeline-based framework. Things are not drawn as soon as
    // they are supplied. glFlush flushes the pipeline and draws everything
    gl.glFlush();
    // disable the program
    program.disable(gl);
}

void View::animate()
{
    // the air plane: index 3
    animationTransforms[AEROPLANE] =
        glm::rotate(glm::mat4(1.0), glm::radians(-50.0f * time),
                    glm::vec3(0.0f, 1.0f, 0.0f)) *
        glm::translate(glm::mat4(1.0), glm::vec3(200.0f, 50.0f, 0.0f)) *
        glm::rotate(glm::mat4(1.0), glm::radians(150.0f * time),
                    glm::vec3(0.0f, 0.0f, 1.0f));

    // the cube: index 4
    animationTransforms[YELLOW_CUBE] =
        glm::translate(glm::mat4(1.0), glm::vec3(-200.0f, 50.0f, -200.0f)) *
        glm::rotate(glm::mat4(1.0), glm::radians(-time * 50.0f),
                    glm::vec3(0.0f, 1.0f, 0.0f));

    // the cube: index 5
    animationTransforms[PURPLE_CUBE] =
        glm::translate(glm::mat4(1.0), glm::vec3(200.0f, 50.0f, 200.0f)) *
        glm::rotate(glm::mat4(1.0), glm::radians(time * 75.0f),
                    glm::vec3(1.0f, 1.0f, 0.0f));

    // the sphere: index 6
    animationTransforms[RED_ORB] = glm::translate(
        glm::mat4(1.0), glm::vec3(100.0f, 80.0f, (float)(100 * sin(time))));

    // the neptune model: index 7
    animationTransforms[NEPTUNE] =
        glm::translate(glm::mat4(1.0), glm::vec3(-200.0f, 0.0f, 200.0f)) *
        glm::rotate(glm::mat4(1.0), glm::radians(time * 50.0f),
                    glm::vec3(0.0f, 1.0f, 0.0f));
}

void View::setFPS() { cameraMode = FPS; }

void View::setGlobal() { cameraMode = GLOBAL; }

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

    proj =
        glm::perspective(glm::radians(60.0f),
                         (float)WINDOW_WIDTH / WINDOW_HEIGHT, 0.1f, 10000.0f);
    // proj = glm::ortho(-400.0f,400.0f,-400.0f,400.0f,0.1f,10000.0f);
    // proj = glm::ortho(-150.0f,150.0f,-150.0f,150.0f);
}

void View::dispose(util::OpenGLFunctions& gl)
{
    // clean up the OpenGL resources used by the object
    for (map<ObjectName, util::ObjectInstance*>::iterator it =
             meshObjects.begin();
         it != meshObjects.end(); it++)
    {
        it->second->cleanup(gl);
    }
    // release the shader resources
    program.releaseShaders(gl);
}
