import org.joml.Vector4f;

/**
 * This represents a particle
 */
public class Particle {
    //the position of this particle
    private final Vector4f position;
    //the color of this particle
    private final Vector4f color;
    //the velocity at which this particle moves
    private final Vector4f velocity;
    //the mass of this particle
    private final float mass;
    //the time of birth of this particle
    private final float startTime;

    public Particle(Vector4f pos,Vector4f vel,Vector4f col,float mass,float start) {
        this.position = new Vector4f(pos);
        this.color = new Vector4f(col);
        this.velocity = new Vector4f(vel);
        this.mass = mass;
        this.startTime = start;
    }

    public float getStartTime() { return startTime;}
    public float getMass() { return mass;}
    public Vector4f getPosition() { return new Vector4f(position);}
    public Vector4f getColor() { return new Vector4f(color);}
    public Vector4f getVelocity() { return new Vector4f(velocity);}
}
