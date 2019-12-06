QT += core
QT += gui widgets

CONFIG += c++11

TARGET = ParticleShower


INCLUDEPATH += ../headers

TEMPLATE = app

SOURCES += main.cpp \
    OpenGLWindow.cpp \
    View.cpp

HEADERS += \
    OpenGLWindow.h \
    VertexAttrib.h \
    View.h \
    Particle.h

DISTFILES += \
    shaders/particle.frag \
    shaders/particle.vert


