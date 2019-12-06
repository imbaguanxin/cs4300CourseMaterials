#include "openglwindow.h"
#include <OpenGLFunctions.h>
#include <QDebug>
#include <QMessageBox>  //requires QT += widgets in .pro file
#include <QPainter>
#include <QScreen>
#include <QStaticText>

OpenGLWindow::OpenGLWindow(QWindow* parent)
    : QOpenGLWindow(UpdateBehavior::NoPartialUpdate, parent)
{
    // changes the title shown on the window
    this->setTitle("Fans and Transformations");
    // resize the window to (600,600). This will result in a call to resizeGL
    this->resize(600, 600);

    // make sure we have OpenGL 3.3 (major.minor), with 16-bit buffers
    QSurfaceFormat format;
    format.setDepthBufferSize(24);
    format.setStencilBufferSize(8);
    format.setProfile(QSurfaceFormat::CoreProfile);
    format.setSwapInterval(0);

    this->setFormat(format);

    isDragged = false;
    frames = 0;
    setAnimating(true);
}

OpenGLWindow::~OpenGLWindow()
{
    // When this window is called, we must release all opengl resources
    view.dispose(*gl);
}

void OpenGLWindow::initializeGL()
{
    // create the opengl function wrapper class
    gl = new util::OpenGLFunctions();
    try
    {
        view.init(*gl);
    }
    catch (exception& e)
    {
        // if something goes wrong, show an error message in a popup.
        // look at the comment in the #include above for this to work correctly
        QMessageBox msgBox;
        msgBox.setText(e.what());
        msgBox.exec();
        // assuming we cannot recover from this error, shut down the application
        exit(1);
    }
}

void OpenGLWindow::paintGL()
{
    // simply delegate to the view's draw
    view.draw(*gl);

    // measure frame rate

    if (this->frames == 0)
    {
        this->timer.start();
    }
    this->frames++;
    if (this->frames > 100)
    {
        framerate = this->frames / ((float)this->timer.restart() / 1000.0f);
        this->frames = 0;
    }

    // display frame rate as text

    QPainter painter(this);
    // painter.fillRect(0, 0, width(), height(), Qt::white);
    painter.setPen(QColor(255, 0, 0));
    painter.setFont(QFont("Sans", 12));
    QStaticText text(QString("Frame rate: %1 fps").arg(framerate));
    painter.drawStaticText(20, 50, text);
}

void OpenGLWindow::resizeGL(int w, int h)
{
    // simply delegate to the view's reshape
    view.reshape(*gl, w, h);
}

/*
 * This function helps us to automatically start animating
 * When we call this function with "true", it sets up the window so
 * that it calls update() again and again automatically
 */

void OpenGLWindow::setAnimating(bool enabled)
{
    if (enabled)
    {
        // Animate continuously, throttled by the blocking swapBuffers() call
        // the QOpenGLWindow internally executes after each paint. Once that is
        // done (frameSwapped signal is emitted), we schedule a new update. This
        // obviously assumes that the swap interval (see
        // QSurfaceFormat::setSwapInterval()) is non-zero.

        connect(this, SIGNAL(frameSwapped()), this, SLOT(update()));
        update();
    }
    else
    {
        disconnect(this, SIGNAL(frameSwapped()), this, SLOT(update()));
    }
}
