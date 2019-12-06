package sgraph;

import org.joml.Vector2f;
import org.joml.Vector4f;

/**
 * Created by ashesh on 4/12/2016.
 */
public class HitRecord {
    public float time;
    public Vector4f point,normal;
    public util.Material material;
    public String textureName;
    public Vector2f texcoord;

    public HitRecord() {
        time = Float.POSITIVE_INFINITY;
        point = new Vector4f(0,0,0,1);
        normal = new Vector4f(0,0,1,0);
        material = new util.Material();
        textureName = "white";
        texcoord = new Vector2f(0,0);
    }

    public boolean intersected()
    {
        return time < Float.POSITIVE_INFINITY;
    }
}
