QT += core
QT += gui widgets

CONFIG += c++11

TARGET = Fire
CONFIG += console
CONFIG -= app_bundle

INCLUDEPATH += ../headers

TEMPLATE = app

SOURCES += main.cpp \
    OpenGLWindow.cpp \
    View.cpp \
    FireParticle.cpp

HEADERS += \
    OpenGLWindow.h \
    VertexAttrib.h \
    View.h \
    FireParticle.h

DISTFILES += \
    shaders/fire.frag \
    shaders/fire.vert


