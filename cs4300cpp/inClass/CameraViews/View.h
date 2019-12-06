#ifndef VIEW_H
#define VIEW_H

#include <OpenGLFunctions.h>
#include <exception>
using namespace std;
#include <glm/glm.hpp>
#include "ShaderProgram.h"
#include "ShaderLocationsVault.h"
#include <ObjectInstance.h>
#include <ObjExporter.h>
#include "VertexAttrib.h"
#include "Material.h"
#include <stack>
#include <string>
#include <map>
using namespace std;

/*
 * This class encapsulates all our program-specific details. This makes our
 * design better if we wish to port it to another C++-based windowing
 * library
 */

class View
{
  typedef enum {GLOBAL,FPS} TypeOfCamera;
  typedef enum {JACK_BOX_FACE,JACK_BOX_CAP,FLOOR,WALL_BACK,WALL_FRONT,WALL_LEFT,
                WALL_RIGHT,RED_ORB,YELLOW_CUBE,PURPLE_CUBE,NEPTUNE,AEROPLANE}
               ObjectName;
public:
    View();
    ~View();
    /*
     * This is called when the application is being initialized. We should
     * do all our initializations here. This is also the first function where
     * OpenGL commands will work (i.e. don't do any OpenGL related stuff in the
     * constructor!)
     */
    void init(util::OpenGLFunctions& e) throw(runtime_error);

    /*
     * This function is called whenever the window is to be redrawn
     */
    void draw(util::OpenGLFunctions& e);

    /*
     * This function is called whenever the window is reshaped
     */
    void reshape(util::OpenGLFunctions& gl,int width,int height);



    /*
     * This function is called whenever the window is being destroyed
     */
    void dispose(util::OpenGLFunctions& gl);

    void setFPS();
    void setGlobal();

protected:
    void initObjects(util::OpenGLFunctions& gl) throw(runtime_error);
private:
    void animate();

private:
    //record the current window width and height
    int WINDOW_WIDTH,WINDOW_HEIGHT;
    //the projection matrix
    glm::mat4 proj;
    //the modelview matrix
    stack<glm::mat4> modelview;
    //the objects which we are rendering
    map<ObjectName,util::ObjectInstance *> meshObjects;
    map<ObjectName,glm::mat4> meshTransforms,animationTransforms;
    map<ObjectName,util::Material> meshMaterials;

    util::ShaderLocationsVault shaderLocations;
    //the GLSL shader
    util::ShaderProgram program;
    float time;
    TypeOfCamera cameraMode;
};

#endif // VIEW_H
