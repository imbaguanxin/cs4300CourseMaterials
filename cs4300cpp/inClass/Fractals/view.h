#ifndef VIEW_H
#define VIEW_H

#include <OpenGLFunctions.h>
#include <exception>
using namespace std;
#include <glm/glm.hpp>
#include "ShaderProgram.h"
#include "ShaderLocationsVault.h"
#include <ObjectInstance.h>
#include "VertexAttrib.h"

/*
 * This class encapsulates all our program-specific details. This makes our
 * design better if we wish to port it to another C++-based windowing
 * library
 */

class View
{
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
     * Function that tells the window the current part of the
     * fractal world it is showing
     */
    string getFrameInfoString();

    /*
     * Function that tells the window the number of iterations being
     * used for the fractal generation process
     */

    string getIterationInfoString();

    /*
     * GUI controls for the fractal process
     */
    void zoomIn();
    void zoomOut();
    void increaseMaxIterations();
    void decreaseMaxIterations();
    void translate(int x,int y);

    /*
     * This function is called whenever the window is being destroyed
     */
    void dispose(util::OpenGLFunctions& gl);

private:
    //record the current window width and height
    int WINDOW_WIDTH,WINDOW_HEIGHT;
    //the projection matrix
    glm::mat4 proj,modelview;
    //the object which we are rendering
    util::ObjectInstance *obj;
    //the list of shader variables and their locations within the shader program
    util::ShaderLocationsVault shaderLocations;
    int modelviewLocation;
    int projectionLocation;
    int vPositionLocation;
    int dimsLocation;
    int maxIterLocation;
    int centerLocation;
    int scaleLocation;
    //the GLSL shader
    util::ShaderProgram program;

    //fractal controlling variables
    glm::vec2 center;
    float scale,scalex,scaley;
    int MAX_ITERATIONS;
};

#endif // VIEW_H
