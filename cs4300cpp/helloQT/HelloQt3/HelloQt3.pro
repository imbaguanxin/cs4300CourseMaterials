QT += core
QT += gui
QT += widgets

CONFIG += c++11

INCLUDEPATH += ../headers

TARGET = HelloQt3
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
