QT += core
QT += gui widgets

CONFIG += c++11

TARGET = ObjViewer
CONFIG += console
CONFIG -= app_bundle

INCLUDEPATH += ../headers

TEMPLATE = app

SOURCES += main.cpp \
    OpenGLWindow.cpp \
    View.cpp

HEADERS += \
    OpenGLWindow.h \
    VertexAttrib.h \
    View.h

DISTFILES += \
    shaders/default.frag \
    shaders/default.vert
