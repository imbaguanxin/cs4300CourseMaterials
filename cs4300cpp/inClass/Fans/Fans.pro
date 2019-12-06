QT += core
QT += gui widgets

CONFIG += c++11

INCLUDEPATH += ../headers

TARGET = Fans
CONFIG += console
CONFIG -= app_bundle

TEMPLATE = app

SOURCES += main.cpp \
    openglwindow.cpp \
    view.cpp

HEADERS += \
    openglwindow.h \
    VertexAttribWithColor.h \
    View.h

DISTFILES += \
    shaders/default.frag \
    shaders/default.vert
