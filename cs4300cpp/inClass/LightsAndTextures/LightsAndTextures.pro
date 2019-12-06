QT += core
QT += gui widgets

CONFIG += c++11

TARGET = LightsAndTextures
CONFIG += console
CONFIG -= app_bundle

TEMPLATE = app

INCLUDEPATH += ../headers

SOURCES += main.cpp \
    OpenGLWindow.cpp \
    View.cpp

HEADERS += \
    OpenGLWindow.h \
    VertexAttrib.h \
    View.h

DISTFILES += \
    shaders/phong-multiple.frag \
    shaders/phong-multiple.vert \
    shaders/gouraud-multiple.vert \
    shaders/gouraud-multiple.frag
