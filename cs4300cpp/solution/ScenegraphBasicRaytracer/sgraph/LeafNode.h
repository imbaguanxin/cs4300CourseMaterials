#ifndef _LEAFNODE_H_
#define _LEAFNODE_H_

#include <map>
#include <stack>
#include <string>
#include "AbstractNode.h"
#include "Material.h"
#include "OpenGLFunctions.h"
#include "glm/glm.hpp"
using namespace std;

namespace sgraph
{
/**
 * This node represents the leaf of a scene graph. It is the only type of node
 * that has actual geometry to render. \author Amit Shesh
 */
class LeafNode : public AbstractNode
{
    /**
     * The name of the object instance that this leaf contains. All object
     * instances are stored in the scene graph itself, so that an instance can
     * be reused in several leaves
     */
   protected:
    string objInstanceName;
    /**
     * The material associated with the object instance at this leaf
     */
    util::Material material;

    string textureName;

   public:
    LeafNode(const string& instanceOf, sgraph::Scenegraph* graph,
             const string& name)
        : AbstractNode(graph, name)
    {
        this->objInstanceName = instanceOf;
    }

    ~LeafNode() {}

    /*
     *Set the material of each vertex in this object
     */
    void setMaterial(const util::Material& mat) throw(runtime_error)
    {
        material = mat;
    }

    /**
     * Set texture ID of the texture to be used for this leaf
     * \param name
     */
    void setTextureName(const string& name) throw(runtime_error)
    {
        textureName = name;
    }

    /*
     * gets the material
     */
    util::Material getMaterial() { return material; }

    INode* clone()
    {
        LeafNode* newclone =
            new LeafNode(this->objInstanceName, scenegraph, name);
        newclone->setMaterial(this->getMaterial());
        return newclone;
    }

    /**
     * Delegates to the scene graph for rendering. This has two advantages:
     * <ul>
     *     <li>It keeps the leaf light.</li>
     *     <li>It abstracts the actual drawing to the specific implementation of
     * the scene graph renderer</li>
     * </ul>
     * \param context the generic renderer context {@link
     * sgraph.IScenegraphRenderer} \param modelView the stack of modelview
     * matrices \throws runtime_error
     */
    void draw(GLScenegraphRenderer& context,
              stack<glm::mat4>& modelView) throw(runtime_error)
    {
        if (objInstanceName.length() > 0)
        {
            context.drawMesh(objInstanceName, material, textureName,
                             modelView.top());
        }
    }

    void intersect(Ray& rayView, stack<glm::mat4>& modelView,
                   HitRecord& hitRecord)
    {
        Ray rayObject;
        glm::mat4 leafToView(modelView.top());
        glm::mat4 viewToLeaf = glm::inverse(leafToView);
        rayObject.start = glm::vec4(rayView.start);
        rayObject.direction = glm::vec4(rayView.direction);

        rayObject.start = viewToLeaf * rayObject.start;
        rayObject.direction = viewToLeaf * rayObject.direction;

        if (objInstanceName.compare("sphere") == 0)
        {
            float a, b, c;

            a = glm::length(rayObject.direction) *
                glm::length(rayObject.direction);
            b = 2 * glm::dot(rayObject.start, rayObject.direction);
            // the extra -1 because this is a vec4 with 1 in the w, so
            // length is one more than what we want
            c = glm::length(rayObject.start) * glm::length(rayObject.start) -
                1 - 1;

            if ((b * b - 4 * a * c) >= 0)
            {
                float t1 = (-b + (float)sqrt(b * b - 4 * a * c)) / (2 * a);
                float t2 = (-b - (float)sqrt(b * b - 4 * a * c)) / (2 * a);

                float t;
                if (t1 >= 0)
                {
                    if (t2 >= 0)
                    {
                        t = glm::min(t1, t2);
                    }
                    else
                    {
                        t = t1;
                    }
                }
                else
                {
                    if (t2 >= 0)
                        t = t2;
                    else
                        return;
                }

                if (t < hitRecord.time)
                {
                    hitRecord.time = t;
                    hitRecord.point = rayView.start + rayView.direction * t;
                    hitRecord.normal =
                        rayObject.start + rayObject.direction * t;
                    hitRecord.normal.w = 0;
                    hitRecord.texcoord = glm::vec2(
                        (float)((M_PI + (float)atan2(-hitRecord.normal.z,
                                                     hitRecord.normal.x)) /
                                (2 * M_PI)),
                        (float)((M_PI / 2 + (float)asin(hitRecord.normal.y)) /
                                M_PI));
                    hitRecord.texcoord.x = (hitRecord.texcoord.x + 0.5f);

                    hitRecord.textureName = this->textureName;

                    hitRecord.normal =
                        glm::transpose(viewToLeaf) * hitRecord.normal;
                    hitRecord.normal =
                        glm::vec4(glm::normalize(glm::vec3(hitRecord.normal.x,
                                                           hitRecord.normal.y,
                                                           hitRecord.normal.z)),
                                  0);

                    hitRecord.material = this->material;
                    hitRecord.textureName = this->textureName;
                }
            }
        }
        else if (objInstanceName.compare("box") == 0)
        {
            float tmaxX, tmaxY, tmaxZ;
            float tminX, tminY, tminZ;

            if (fabs(rayObject.direction.x) < 0.0001f)
            {
                if ((rayObject.start.x > 0.5f) || (rayObject.start.x < -0.5f))
                    return;
                else
                {
                    tminX = numeric_limits<float>::lowest();
                    tmaxX = numeric_limits<float>::max();
                }
            }
            else
            {
                float t1 = (-0.5f - rayObject.start.x) / rayObject.direction.x;
                float t2 = (0.5f - rayObject.start.x) / rayObject.direction.x;
                tminX = std::min(t1, t2);
                tmaxX = std::max(t1, t2);
            }

            if (fabs(rayObject.direction.y) < 0.0001f)
            {
                if ((rayObject.start.y > 0.5f) || (rayObject.start.y < -0.5f))
                {
                    return;
                }
                else
                {
                    tminY = numeric_limits<float>::lowest();
                    tmaxY = numeric_limits<float>::max();
                }
            }
            else
            {
                float t1 = (-0.5f - rayObject.start.y) / rayObject.direction.y;
                float t2 = (0.5f - rayObject.start.y) / rayObject.direction.y;
                tminY = std::min(t1, t2);
                tmaxY = std::max(t1, t2);
            }

            if (fabs(rayObject.direction.z) < 0.0001f)
            {
                if ((rayObject.start.z > 0.5f) || (rayObject.start.z < -0.5f))
                {
                    return;
                }
                else
                {
                    tminZ = numeric_limits<float>::lowest();
                    tmaxZ = numeric_limits<float>::max();
                }
            }
            else
            {
                float t1 = (-0.5f - rayObject.start.z) / rayObject.direction.z;
                float t2 = (0.5f - rayObject.start.z) / rayObject.direction.z;
                tminZ = std::min(t1, t2);
                tmaxZ = std::max(t1, t2);
            }

            float tmin, tmax;

            tmin = std::max<float>(tminX, std::max<float>(tminY, tminZ));
            tmax = std::min<float>(tmaxX, std::min<float>(tmaxY, tmaxZ));

            if ((tmin < tmax) && (tmax > 0))
            {
                float t;
                if (tmin > 0)
                    t = tmin;
                else
                    t = tmax;

                if (t < hitRecord.time)
                {
                    hitRecord.time = t;

                    hitRecord.point = rayView.start + rayView.direction * t;

                    glm::vec4 pointInLeaf =
                        rayObject.start + rayObject.direction * t;

                    if (fabs(pointInLeaf.x - 0.5f) < 0.001)
                    {
                        hitRecord.normal.x = 1;
                        hitRecord.texcoord = windowTransform(
                            glm::vec2(pointInLeaf.z, pointInLeaf.y),
                            glm::vec2(-0.5f, -0.5f), glm::vec2(0.5f, 0.5f),
                            glm::vec2(0.5f, 0.25f), glm::vec2(0.75f, 0.5f));
                    }
                    else if (fabs(pointInLeaf.x + 0.5f) < 0.001)
                    {
                        hitRecord.normal.x = -1;
                        hitRecord.texcoord = windowTransform(
                            glm::vec2(pointInLeaf.z, pointInLeaf.y),
                            glm::vec2(-0.5f, -0.5f), glm::vec2(0.5f, 0.5f),
                            glm::vec2(0.25f, 0.25f), glm::vec2(0, 0.5f));
                    }
                    else
                        hitRecord.normal.x = 0;

                    if (fabs(pointInLeaf.y - 0.5f) < 0.001)
                    {
                        hitRecord.normal.y = 1;
                        hitRecord.texcoord = windowTransform(
                            glm::vec2(pointInLeaf.x, pointInLeaf.z),
                            glm::vec2(-0.5f, -0.5f), glm::vec2(0.5f, 0.5f),
                            glm::vec2(0.25f, 0.5f), glm::vec2(0.5f, 0.75f));
                    }
                    else if (fabs(pointInLeaf.y + 0.5f) < 0.001)
                    {
                        hitRecord.normal.y = -1;
                        hitRecord.texcoord = windowTransform(
                            glm::vec2(pointInLeaf.z, pointInLeaf.z),
                            glm::vec2(-0.5f, -0.5f), glm::vec2(0.5f, 0.5f),
                            glm::vec2(0.25f, 0.25f), glm::vec2(0.5f, 0.0f));
                    }
                    else
                        hitRecord.normal.y = 0;

                    if (fabs(pointInLeaf.z - 0.5f) < 0.001)
                    {
                        hitRecord.normal.z = 1;
                        hitRecord.texcoord = windowTransform(
                            glm::vec2(pointInLeaf.x, pointInLeaf.y),
                            glm::vec2(-0.5f, -0.5f), glm::vec2(0.5f, 0.5f),
                            glm::vec2(1.0f, 0.25f), glm::vec2(0.75f, 0.5f));
                    }
                    else if (fabs(pointInLeaf.z + 0.5f) < 0.001)
                    {
                        hitRecord.normal.z = -1;
                        hitRecord.texcoord = windowTransform(
                            glm::vec2(pointInLeaf.x, pointInLeaf.y),
                            glm::vec2(-0.5f, -0.5f), glm::vec2(0.5f, 0.5f),
                            glm::vec2(0.25f, 0.25f), glm::vec2(0.5f, 0.5f));
                    }
                    else
                        hitRecord.normal.z = 0;

                    hitRecord.normal.w = 0;

                    hitRecord.normal =
                        glm::vec4(glm::normalize(glm::vec3(hitRecord.normal.x,
                                                           hitRecord.normal.y,
                                                           hitRecord.normal.z)),
                                  0);

                    hitRecord.normal =
                        glm::transpose(viewToLeaf) * hitRecord.normal;
                    hitRecord.normal =
                        glm::vec4(glm::normalize(glm::vec3(hitRecord.normal.x,
                                                           hitRecord.normal.y,
                                                           hitRecord.normal.z)),
                                  0);

                    hitRecord.material = material;
                    hitRecord.textureName = this->textureName;
                }
            }
        }
    }

   private:
    glm::vec2 windowTransform(glm::vec2 wcoords, glm::vec2 minw1,
                              glm::vec2 maxw1, glm::vec2 minw2, glm::vec2 maxw2)
    {
        glm::vec2 v;

        v.x =
            (wcoords.x - minw1.x) * (maxw2.x - minw2.x) / (maxw1.x - minw1.x) +
            minw2.x;
        v.y =
            (wcoords.y - minw1.y) * (maxw2.y - minw2.y) / (maxw1.y - minw1.y) +
            minw2.y;
        return v;
    }
};
}
#endif
