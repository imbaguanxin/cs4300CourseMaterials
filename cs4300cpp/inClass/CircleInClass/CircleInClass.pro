QT += core
QT += gui widgets

CONFIG += c++11

TARGET = CircleInClass
CONFIG += console
CONFIG -= app_bundle

INCLUDEPATH += ../headers

TEMPLATE = app

SOURCES += main.cpp \
    OpenGLWindow.cpp \
    View.cpp

HEADERS += \
    openglwindow.h \
    VertexAttrib.h \
    View.h
