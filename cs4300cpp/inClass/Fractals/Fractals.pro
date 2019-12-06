QT += core
QT += gui
QT += widgets

CONFIG += c++11

INCLUDEPATH += ../headers

TARGET = Fractals
CONFIG += console
CONFIG -= app_bundle

TEMPLATE = app

SOURCES += main.cpp \
    openglwindow.cpp \
    view.cpp

HEADERS += \
    openglwindow.h \
    VertexAttrib.h \
    view.h

DISTFILES += \
    shaders/fractal.frag \
    shaders/fractal.vert
