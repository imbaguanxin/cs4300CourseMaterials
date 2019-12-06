QT += core
QT += gui xml widgets

CONFIG += c++11

INCLUDEPATH += ../headers

TARGET = ScenegraphBasicRaytracer
CONFIG += console
CONFIG -= app_bundle

TEMPLATE = app

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
    sgraph/TransformNode.h \
    sgraph/HitRecord.h \
    sgraph/Ray.h \
    sgraph/RTScenegraphRenderer.h

DISTFILES += \
    shaders/lights-textures.frag \
    shaders/lights-textures.vert
