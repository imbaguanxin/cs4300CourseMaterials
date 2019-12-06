QT += core
QT += gui widgets

CONFIG += c++11

TARGET = CircleSolution
CONFIG += console
CONFIG -= app_bundle

INCLUDEPATH += ../headers

TEMPLATE = app

SOURCES += main.cpp \
    OpenGLWindow.cpp \
    View.cpp

HEADERS += \
    VertexAttrib.h \
    View.h \
    OpenGLWindow.h
