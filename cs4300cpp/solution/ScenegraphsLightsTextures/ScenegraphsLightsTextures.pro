QT += core
QT += gui widgets xml

CONFIG += c++11

TARGET = ScenegraphsLightsTextures
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
    View.h \
    sgraph/AbstractNode.h \
    sgraph/GLScenegraphRenderer.h \
    sgraph/GroupNode.h \
    sgraph/INode.h \
    sgraph/IScenegraph.h \
    sgraph/LeafNode.h \
    sgraph/Scenegraph.h \
    sgraph/scenegraphinfo.h \
    sgraph/SceneXMLReader.h \
    sgraph/TransformNode.h

DISTFILES += \
    shaders/phong-multiple.frag \
    shaders/phong-multiple.vert
