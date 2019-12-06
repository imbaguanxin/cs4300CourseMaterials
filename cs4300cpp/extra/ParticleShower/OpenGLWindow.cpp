#include "OpenGLWindow.h"
#include <OpenGLFunctions.h>
#include <QDebug>
#include <QMessageBox>  //requires QT += widgets in .pro file
#include <QPainter>
#include <QScreen>
#include <QStaticText>

OpenGLWindow::OpenGLWindow(QWindow *parent)
    : QOpenGLWindow(UpdateBehavior::NoPartialUpdate, parent)
{
    // changes the title shown on the window
    this->setTitle("Particle shower!");
    // resize the window to (900,900). This will result in a call to resizeGL
    this->resize(900, 900);

    // make sure we have OpenGL 3.1 (major.minor), with 16-bit buffers
    QSurfaceFormat format;
    format.setDepthBufferSize(24);
    format.setStencilBufferSize(8);
    format.setProfile(QSurfaceFormat::CoreProfile);

    this->setFormat(format);
    QSurfaceFormat::setDefaultFormat(format);

    isDragged = false;
    frames = 0;
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
    catch (exception &e)
    {
        // if something goes wrong, show an error message in a popup.
        // look at the comment in the #include above for this to work correctly
        QMessageBox msgBox;
        msgBox.setText(e.what());
        msgBox.exec();
        // assuming we cannot recover from this error, shut down the application
        exit(1);
    }
    setAnimating(true);
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
    painter.drawStaticText(5, 20, text);
}

void OpenGLWindow::resizeGL(int w, int h)
{
    // simply delegate to the view's reshape
    view.reshape(*gl, w, h);
}

void OpenGLWindow::mousePressEvent(QMouseEvent *e) { isDragged = true; }

void OpenGLWindow::mouseMoveEvent(QMouseEvent *e)
{
    // for now, simply re-render if it is a drag
    if (isDragged)
    {
        view.setStartPosition(e->x(), e->y());
        this->update();
    }
}

void OpenGLWindow::mouseReleaseEvent(QMouseEvent *e) { isDragged = false; }

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

void OpenGLWindow::keyPressEvent(QKeyEvent *e)
{
    switch (e->key())
    {
        case Qt::Key_Up:
            view.forceUp();
            break;
        case Qt::Key_Down:
            view.forceDown();
            break;
        case Qt::Key_Left:
            view.forceLeft();
            break;
        case Qt::Key_Right:
            view.forceRight();
            break;
    }
}
