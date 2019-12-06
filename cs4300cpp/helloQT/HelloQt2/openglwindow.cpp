#include "openglwindow.h"
#include <QScreen>
#include <OpenGLFunctions.h>
#include <QMessageBox> //requires QT += widgets in .pro file



OpenGLWindow::OpenGLWindow(QWindow *parent)    
    :QOpenGLWindow(UpdateBehavior::NoPartialUpdate,parent)
{
    //changes the title shown on the window
    this->setTitle("Hello Qt!");
    //resize the window to (400,400). This will result in a call to resizeGL
    this->resize(400,400);

    //make sure we have OpenGL 3.3 (major.minor), with 16-bit buffers
    QSurfaceFormat format;
    format.setSamples(16);
    //format.setVersion(3,3); //this line should be uncommented for Mac OSX
    format.setProfile(QSurfaceFormat::CoreProfile);
    this->setFormat(format);

    isDragged = false;


}


OpenGLWindow::~OpenGLWindow()
{
    //When this window is called, we must release all opengl resources
    view.dispose(*gl);
}

void OpenGLWindow::initializeGL()
{
    //create the opengl function wrapper class
    gl = new util::OpenGLFunctions();
    try
    {
        view.init(*gl);
    }
    catch (exception& e)
    {
        //if something goes wrong, show an error message in a popup.
        //look at the comment in the #include above for this to work correctly
        QMessageBox msgBox;
        msgBox.setText(e.what());
        msgBox.exec();
        //assuming we cannot recover from this error, shut down the application
        exit(1);
    }
}

void OpenGLWindow::paintGL()
{
    //simply delegate to the view's draw
    view.draw(*gl);
}

void OpenGLWindow::resizeGL(int w,int h)
{
    //simply delegate to the view's reshape
    view.reshape(*gl,w,h);
}



