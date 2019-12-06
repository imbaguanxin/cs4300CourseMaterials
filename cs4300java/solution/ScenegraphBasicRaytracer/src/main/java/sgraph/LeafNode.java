package sgraph;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Stack;

import util.Material;

/**
 * This node represents the leaf of a scene graph. It is the only type of node
 * that has actual geometry to render.
 *
 * @author Amit Shesh
 */
public class LeafNode extends AbstractNode {
  /**
   * The name of the object instance that this leaf contains. All object
   * instances are stored in the scene graph itself, so that an instance
   * can be
   * reused in several leaves
   */
  protected String objInstanceName;
  /**
   * The material associated with the object instance at this leaf
   */
  protected util.Material material;

  protected String textureName;

  public LeafNode(String instanceOf, IScenegraph graph, String name) {
    super(graph, name);
    this.objInstanceName = instanceOf;
  }


  /*
   *Set the material of each vertex in this object
   */
  @Override
  public void setMaterial(util.Material mat) {
    material = new util.Material(mat);
  }

  /**
   * Set texture ID of the texture to be used for this leaf
   */
  @Override
  public void setTextureName(String name) {
    textureName = name;
  }

  /*
   * gets the material
   */
  public util.Material getMaterial() {
    return material;
  }

  @Override
  public INode clone() {
    LeafNode newclone = new LeafNode(this.objInstanceName, scenegraph, name);
    newclone.setMaterial(this.getMaterial());
    return newclone;
  }


  /**
   * Delegates to the scene graph for rendering. This has two advantages:
   * <ul>
   * <li>It keeps the leaf light.</li>
   * <li>It abstracts the actual drawing to the specific implementation of the
   * scene graph renderer</li>
   * </ul>
   *
   * @param context   the generic renderer context
   * {@link sgraph.IScenegraphRenderer}
   * @param modelView the stack of modelview matrices
   */
  @Override
  public void draw(IScenegraphRenderer context,
                   Stack<Matrix4f> modelView) throws IllegalArgumentException {
    if (objInstanceName.length() > 0) {
      context.drawMesh(objInstanceName,
              material,
              textureName,
              modelView.peek());
    }
  }

  @Override
  public void intersect(Ray rayView,
                        Stack<Matrix4f> modelView,
                        HitRecord hitRecord) {
    Ray rayObject = new Ray();
    Matrix4f leafToView = new Matrix4f(modelView.peek());
    Matrix4f viewToLeaf = new Matrix4f(leafToView).invert();
    rayObject.start = new Vector4f(rayView.start);
    rayObject.direction = new Vector4f(rayView.direction);

    rayObject.start = viewToLeaf.transform(rayObject.start);
    rayObject.direction = viewToLeaf.transform(rayObject.direction);


    if (objInstanceName.equals("sphere")) {
      float a, b, c;

      a = rayObject.direction.lengthSquared();
      b = 2 * rayObject.start.dot(rayObject.direction);
      c = rayObject.start.lengthSquared() - 1 - 1;

      if ((b * b - 4 * a * c) >= 0) {
        float t1 = (-b + (float) Math.sqrt(b * b - 4 * a * c)) / (2 * a);
        float t2 = (-b - (float) Math.sqrt(b * b - 4 * a * c)) / (2 * a);

        float t;
        if (t1 >= 0) {
          if (t2 >= 0) {
            t = Math.min(t1, t2);
          } else {
            t = t1;
          }
        } else {
          if (t2 >= 0)
            t = t2;
          else
            return;
        }

        if (t<hitRecord.time)
        {
          hitRecord.time = t;
          hitRecord.point = new Vector4f(rayView.start.x+t*rayView.direction.x,
                  rayView.start.y+t*rayView.direction.y,
                  rayView.start.z+t*rayView.direction.z,
                  1);
          hitRecord.normal = new Vector4f(rayObject.start.x+t*rayObject.direction.x,
                  rayObject.start.y+t*rayObject.direction.y,
                  rayObject.start.z+t*rayObject.direction.z,
                  0);

          hitRecord.texcoord = new Vector2f((float)((Math.PI+(float)Math.atan2(-hitRecord.normal.z,hitRecord.normal.x))/(2*Math.PI)),
                  (float)((Math.PI/2+(float)Math.asin(hitRecord.normal.y))/Math.PI));
          hitRecord.texcoord.x = (hitRecord.texcoord.x+0.5f)%1;

          hitRecord.textureName = this.textureName;
          hitRecord.normal = new Matrix4f(viewToLeaf).transpose().transform(hitRecord.normal);
          hitRecord.normal = new Vector4f(new Vector3f(hitRecord
                  .normal
                  .x,hitRecord.normal.y,hitRecord.normal.z)
                  .normalize(),0.0f);
          hitRecord.material = new Material(this.material);

        }

      }
    } else if (objInstanceName.equals("box")) {
      float tmaxX, tmaxY, tmaxZ;
      float tminX, tminY, tminZ;

      if (Math.abs(rayObject.direction.x) < 0.0001f) {
        if ((rayObject.start.x > 0.5f) || (rayObject.start.x < -0.5f))
          return;
        else {
          tminX = Float.NEGATIVE_INFINITY;
          tmaxX = Float.POSITIVE_INFINITY;
        }
      } else {
        float t1 = (-0.5f - rayObject.start.x) / rayObject.direction.x;
        float t2 = (0.5f - rayObject.start.x) / rayObject.direction.x;
        tminX = Math.min(t1, t2);
        tmaxX = Math.max(t1, t2);
      }

      if (Math.abs(rayObject.direction.y) < 0.0001f) {
        if ((rayObject.start.y > 0.5f) || (rayObject.start.y < -0.5f)) {
          return;
        } else {
          tminY = Float.NEGATIVE_INFINITY;
          tmaxY = Float.POSITIVE_INFINITY;
        }
      } else {
        float t1 = (-0.5f - rayObject.start.y) / rayObject.direction.y;
        float t2 = (0.5f - rayObject.start.y) / rayObject.direction.y;
        tminY = Math.min(t1, t2);
        tmaxY = Math.max(t1, t2);
      }

      if (Math.abs(rayObject.direction.z) < 0.0001f) {
        if ((rayObject.start.z > 0.5f) || (rayObject.start.z < -0.5f)) {
          return;
        } else {
          tminZ = Float.NEGATIVE_INFINITY;
          tmaxZ = Float.POSITIVE_INFINITY;
        }
      } else {
        float t1 = (-0.5f - rayObject.start.z) / rayObject.direction.z;
        float t2 = (0.5f - rayObject.start.z) / rayObject.direction.z;
        tminZ = Math.min(t1, t2);
        tmaxZ = Math.max(t1, t2);
      }

      float tmin, tmax;

      tmin = Math.max(tminX, Math.max(tminY, tminZ));
      tmax = Math.min(tmaxX, Math.min(tmaxY, tmaxZ));

      if ((tmin < tmax) && (tmax > 0)) {
        float t;
        if (tmin > 0)
          t = tmin;
        else
          t = tmax;

        if (t < hitRecord.time) {
          hitRecord.time = t;

          hitRecord.point = new Vector4f(
                  rayView.start.x + t * rayView.direction.x,
                  rayView.start.y + t * rayView.direction.y,
                  rayView.start.z + t * rayView.direction.z,
                  1);

          Vector4f pointInLeaf = new Vector4f(
                  rayObject.start.x + t * rayObject.direction.x,
                  rayObject.start.y + t * rayObject.direction.y,
                  rayObject.start.z + t * rayObject.direction.z,
                  1);

          if (Math.abs(pointInLeaf.x - 0.5f) < 0.001) { //right
            hitRecord.normal.x = 1;
            hitRecord.texcoord = windowTransform(new Vector2f(pointInLeaf.z,
                            pointInLeaf.y),
                    new Vector2f(-0.5f, -0.5f), new Vector2f(0.5f, 0.5f),
                    new Vector2f(0.5f, 0.25f), new Vector2f(0.75f, 0.5f));

          } else if (Math.abs(pointInLeaf.x + 0.5f) < 0.001) //left
          {
            hitRecord.normal.x = -1;


            hitRecord.texcoord = windowTransform(new Vector2f(pointInLeaf.z,
                            pointInLeaf.y),
                    new Vector2f(-0.5f, -0.5f), new Vector2f(0.5f, 0.5f),
                    new Vector2f(0.25f, 0.25f), new Vector2f(0, 0.5f));

          } else
            hitRecord.normal.x = 0;

          if (Math.abs(pointInLeaf.y - 0.5f) < 0.001) //top
          {
            hitRecord.normal.y = 1;
            hitRecord.texcoord = windowTransform(new Vector2f(pointInLeaf.x,
                            pointInLeaf.z),
                    new Vector2f(-0.5f, -0.5f), new Vector2f(0.5f, 0.5f),
                    new Vector2f(0.25f, 0.5f), new Vector2f(0.5f, 0.75f));
          } else if (Math.abs(pointInLeaf.y + 0.5f) < 0.001) //bottom
          {
            hitRecord.normal.y = -1;
            hitRecord.texcoord = windowTransform(new Vector2f(pointInLeaf.z,
                            pointInLeaf.z),
                    new Vector2f(-0.5f, -0.5f), new Vector2f(0.5f, 0.5f),
                    new Vector2f(0.25f, 0.25f), new Vector2f(0.5f, 0.0f));
          } else
            hitRecord.normal.y = 0;

          if (Math.abs(pointInLeaf.z - 0.5f) < 0.001) //front
          {
            hitRecord.normal.z = 1;
            hitRecord.texcoord = windowTransform(new Vector2f(pointInLeaf.x,
                            pointInLeaf.y),
                    new Vector2f(-0.5f, -0.5f), new Vector2f(0.5f, 0.5f),
                    new Vector2f(1.0f, 0.25f), new Vector2f(0.75f, 0.5f));
          } else if (Math.abs(pointInLeaf.z + 0.5f) < 0.001) //rear
          {
            hitRecord.normal.z = -1;
            hitRecord.texcoord = windowTransform(new Vector2f(pointInLeaf.x,
                            pointInLeaf.y),
                    new Vector2f(-0.5f, -0.5f), new Vector2f(0.5f, 0.5f),
                    new Vector2f(0.25f, 0.25f), new Vector2f(0.5f, 0.5f));
          } else
            hitRecord.normal.z = 0;

          hitRecord.normal.w = 0;
          hitRecord.normal.normalize();


          hitRecord.normal = new Matrix4f(viewToLeaf).transpose()
                                                     .transform(hitRecord.normal);
          hitRecord.normal = new Vector4f(new Vector3f(hitRecord
                  .normal
                  .x, hitRecord.normal.y, hitRecord.normal.z)
                  .normalize(), 0.0f);

          hitRecord.material = new Material(this.material);
          hitRecord.textureName = this.textureName;
        }
      }

    }
  }

  private Vector2f windowTransform(Vector2f wcoords,
                                   Vector2f minw1,
                                   Vector2f maxw1,
                                   Vector2f minw2,
                                   Vector2f maxw2) {
    Vector2f v = new Vector2f();

    v.x = (wcoords.x - minw1.x) * (maxw2.x - minw2.x) / (maxw1.x - minw1.x)
          + minw2.x;
    v.y = (wcoords.y - minw1.y) * (maxw2.y - minw2.y) / (maxw1.y - minw1.y)
          + minw2.y;
    return v;
  }


}
