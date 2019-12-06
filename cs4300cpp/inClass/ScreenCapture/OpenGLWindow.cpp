#include "openglwindow.h"
#include <iomanip>
using namespace std;
#include <QScreen>
#include <OpenGLFunctions.h>
#include <QMessageBox> //requires QT += widgets in .pro file
#include <QPainter>
#include <QDebug>
#include <QStaticText>



OpenGLWindow::OpenGLWindow(QWindow *parent)    
  :QOpenGLWindow(UpdateBehavior::NoPartialUpdate,parent)
{
  //changes the title shown on the window
  this->setTitle("Hello Qt!");
  //resize the window to (400,400). This will result in a call to resizeGL
  this->resize(400,400);

  //make sure we have OpenGL 3.1 (major.minor), with 16-bit buffers
  QSurfaceFormat format;
  format.setDepthBufferSize(24);
  format.setStencilBufferSize(8);
  //format.setVersion(3,3);
  format.setProfile(QSurfaceFormat::CoreProfile);

  this->setFormat(format);
  QSurfaceFormat::setDefaultFormat(format);

  isDragged = false;
  frames = 0;
  frameno=0;
  setAnimating(true);


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
  if (frameno<500)
    {
      stringstream filename;

      filename << "output/image" << setw(3) << setfill('0') << frameno << ".png";
      try {
        captureFrame(filename.str());
      }
      catch (exception& e)
      {
        QMessageBox msgBox;
        msgBox.setText(e.what());
        msgBox.exec();
        //assuming we cannot recover from this error, shut down the application
        exit(1);
      }
      frameno++;
    }


}

void OpenGLWindow::captureFrame(const string& filename) throw(runtime_error)
{
  QImage image = this->grabFramebuffer();
  image.save(QString(filename.c_str()));
}



void OpenGLWindow::resizeGL(int w,int h)
{
  //simply delegate to the view's reshape
  view.reshape(*gl,w,h);
}



/*
 * This function helps us to automatically start animating
 * When we call this function with "true", it sets up the window so
 * that it calls update() again and again automatically
 */

void OpenGLWindow::setAnimating(bool enabled)
{
  if (enabled) {
      // Animate continuously, throttled by the blocking swapBuffers() call the
      // QOpenGLWindow internally executes after each paint. Once that is done
      // (frameSwapped signal is emitted), we schedule a new update. This
      // obviously assumes that the swap interval (see
      // QSurfaceFormat::setSwapInterval()) is non-zero.
      connect(this, SIGNAL(frameSwapped()), this, SLOT(update()));
      update();
    } else {
      disconnect(this, SIGNAL(frameSwapped()), this, SLOT(update()));
    }
}




