import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * This represents a particle. Fire is formed by lots of particles
 */
public class FireParticle {
    //the 3D position of this particle
    private Vector4f position;
    //its temperature, which will map to its color
    private float temperature;
    //the time it was "born"
    private float tstart;
    //the size of the particle
    private float size;
    //whether the particle has been born
    private boolean started;
    //whether the particle is dead. In this case it will be resurrected
    private boolean dead;
    //whether it has started getting smaller
    private boolean smaller;
    //how long will the particle last
    private float lifetime;
    //the direction in which the particle is headed
    private Vector4f dir;

    public FireParticle(Vector4f position, Vector4f dir, float size, float temp, float tstart, float lifetime) {
        this.position = new Vector4f(position);
        this.dir = new Vector4f(dir);
        temperature = temp;
        this.size = size;
        this.tstart = tstart;
        this.lifetime = lifetime;
        dead = false;
        started = false;
        smaller = false;
    }

    public boolean isDead() {
        return dead;
    }

    public boolean hasStarted() {
        return started;
    }

    public Vector4f getPosition() {
        return position;
    }

    public float getSize()
    {
        return size;
    }

    public float getTemperature() {
        return temperature;
    }

    /**
     * This function advances the particle in the simulation. It will assume a new position and a
     * new temperature
     *
     * @param t the absolute time
     */
    public void advance(float t) {
        Vector4f disp = new Vector4f();

        if (t < tstart) //the particle is not born yet
            return;

        if (t > (tstart + lifetime)) //the particle is already dead
        {
            dead = true;
            return;
        }

        started = true; //start the particle, if not already started

        //the displacement of the particle since it was born.
        disp.x = dir.x * (t - tstart);
        disp.z = dir.z * (t - tstart);
        //the y displacement is a quadratic function. As a result of this, the particle will accelerate upwards as time passes
        disp.y = dir.y + 0.5f * 0.25f * (t - tstart) * (t - tstart);
        disp.w = 0;

        //compute the new position
        position = position.add(disp);

        //now we vary its temperature
        //we can try various temperature functions to see which one gives us the most visually appealing solution

    /*
      Option 1: Temperature is inversely proportional to age, with linear variance
     */
        //temperature = 0.08/(t-tstart);

    /*
      Option 2: Temperature is inversely proportional to age, with square root variance. This means that the particle
      cools down slowly once it is born.
     */
        if ((t - tstart) > 0.001)
            temperature = 100 / (float) Math.pow((t - tstart + 0.005), 0.5);

        //   temperature = 1000 * pow((tstart-t+lifetime),2)/(lifetime*lifetime)-100;
        //cap the temperature
        temperature = (temperature > 1000 ? 1000 : temperature);

    /*
    Upon being born, the particle first grows bigger, before it starts diminishing. This causes the fire near the source to be more opaque
    and transparent as it moves up
     */

        if (!smaller) //20 milliseconds
        {
            size = size * 1.5f;
            if (size > 10)
                smaller = true;
        } else {
            //a nonlinear decay in size
            if (size > 1)
                size = size - 0.5f / size;
        }
    }


}
