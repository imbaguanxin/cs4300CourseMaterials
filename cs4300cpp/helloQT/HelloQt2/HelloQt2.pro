QT += core
QT += gui
QT += widgets


CONFIG += c++11

TARGET = HelloQt2
CONFIG += console
CONFIG -= app_bundle

INCLUDEPATH += ../headers

TEMPLATE = app

SOURCES += main.cpp \
    openglwindow.cpp \
    view.cpp

HEADERS += \
    openglwindow.h \
    view.h \
    VertexAttribWithColor.h
