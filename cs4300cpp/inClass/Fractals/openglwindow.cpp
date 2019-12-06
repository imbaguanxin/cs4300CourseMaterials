#include "openglwindow.h"
#include <QScreen>
#include <OpenGLFunctions.h>
#include <QMouseEvent>
#include <QKeyEvent>
#include <QPainter>
#include <QStaticText>
#include <QMessageBox> //requires QT += widgets in .pro file



OpenGLWindow::OpenGLWindow(QWindow *parent)    
    :QOpenGLWindow(UpdateBehavior::NoPartialUpdate,parent)
{
    //changes the title shown on the window
    this->setTitle("Fractals!");
    //resize the window to (400,400). This will result in a call to resizeGL
    this->resize(400,400);

    //make sure we have OpenGL 3.3 (major.minor), with 16-bit buffers
    QSurfaceFormat format;
    format.setSamples(16);
 //   format.setVersion(3,3); //this line should be uncommented for MacOSX
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

    //display frame rate as text

    QPainter painter(this);
   // painter.fillRect(0, 0, width(), height(), Qt::white);
    painter.setPen(QColor(255, 255, 0));
    painter.setFont(QFont("Sans", 12));
    QStaticText text1(QString(view.getFrameInfoString().c_str()));
    painter.drawStaticText(10, 50, text1);
    QStaticText text2(QString(view.getIterationInfoString().c_str()));
    painter.drawStaticText(10, 80, text2);
}

void OpenGLWindow::resizeGL(int w,int h)
{
    //simply delegate to the view's reshape
    view.reshape(*gl,w,h);
}

void OpenGLWindow::mousePressEvent(QMouseEvent *e)
{
    isDragged = true;
    mouseX = e->x();
    mouseY = this->height() - e->y();
}


void OpenGLWindow::mouseMoveEvent(QMouseEvent *e)
{
    //for now, simply re-render if it is a drag
    if (isDragged)
    {
        view.translate(e->x()-mouseX,this->height()-e->y()-mouseY);
        mouseX = e->x();
        mouseY = this->height() - e->y();
        this->update();
    }

}

void OpenGLWindow::mouseReleaseEvent(QMouseEvent *e)
{
    isDragged = false;
}

void OpenGLWindow::keyPressEvent(QKeyEvent *e)
{
    switch (e->key())
    {
    case Qt::Key_Plus:
    case Qt::Key_Equal:
        view.increaseMaxIterations();
        this->update();
        break;
    case Qt::Key_Minus:
    case Qt::Key_Underscore:
        view.decreaseMaxIterations();
        this->update();
        break;
    case Qt::Key_Up:
        view.zoomIn();
        this->update();
        break;
    case Qt::Key_Down:
        view.zoomOut();
        this->update();
        break;
    case Qt::Key_Left:
        view.translate(-5,0);
        this->update();
        break;
    case Qt::Key_Right:
        view.translate(5,0);
        this->update();
        break;
    }
}




