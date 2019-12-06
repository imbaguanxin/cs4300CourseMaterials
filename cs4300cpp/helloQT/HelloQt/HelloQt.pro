QT += core
QT += gui
QT += widgets

CONFIG += c++11

TARGET = HelloQt
CONFIG -= app_bundle

INCLUDEPATH += ../headers

TEMPLATE = app

SOURCES += main.cpp \
    View.cpp \
    OpenGLWindow.cpp

HEADERS += \
    VertexAttrib.h \
    View.h \
    View.h \
    OpenGLWindow.h

DISTFILES += \
    shaders/default.frag \
    shaders/default.vert
