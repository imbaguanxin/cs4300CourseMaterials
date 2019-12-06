package sgraph;

import org.joml.Vector4f;

/**
 * Created by ashesh on 4/12/2016.
 */
public class Ray {
    public Vector4f start;
    public Vector4f direction;

    public Ray() {
        this.start = new Vector4f(0,0,0,1);
        this.direction = new Vector4f(0,0,1,0);
    }
}
