import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import util.ObjectInstance;


import java.io.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by ashesh on 9/18/2015.
 *
 * The View class is the "controller" of all our OpenGL stuff. It cleanly encapsulates all our
 * OpenGL functionality from the rest of Java GUI, managed by the JOGLFrame class.
 */
public class View {
    private int WINDOW_WIDTH, WINDOW_HEIGHT;
    private Matrix4f proj, modelView;
    ObjectInstance pointObj;
    List<Particle> particles;
    private final int numParticles;
    private float time;
    private Vector4f force;
    private Vector4f startPosition;
    private Vector4f backgroundColor;
    private Texture pointTexture;

    private util.ShaderProgram program;
    private util.ShaderLocationsVault shaderLocations;



    public View() {
        proj = new Matrix4f();
        proj.identity();

        modelView = new Matrix4f();
        modelView.identity();
        time = 0;
        force = new Vector4f(0,0,0,0);
        backgroundColor = new Vector4f(0,0,0,0);
        numParticles = 50000;

        startPosition = new Vector4f(0,0,0,1);
    }

    private void initObjects(GL3 gl) throws FileNotFoundException {

        util.PolygonMesh tmesh;
/*
        List<Vector4f> positions = new ArrayList<Vector4f>();
        positions.add(new Vector4f(0,0,0,1));
        List<Integer> primitives = new ArrayList<Integer>();
        primitives.add(0);
        tmesh = new util.PolygonMesh();
        tmesh.setVertexPositions(positions);
        tmesh.setPrimitives(primitives);
        tmesh.setPrimitiveType(GL.GL_POINTS);
        tmesh.setPrimitiveSize(1);
        pointObj = new util.ObjectInstance(gl, program, tmesh, new String(""));
*/



        InputStream in;

        in = getClass().getClassLoader().getResourceAsStream("models/quad.obj");

        tmesh = util.ObjImporter.importFile(new VertexAttribProducer(), in, true);
        util.ObjectInstance obj;

        Map<String, String> shaderToVertexAttribute = new HashMap<String, String>();

        shaderToVertexAttribute.put("vPosition", "position");
        shaderToVertexAttribute.put("vTexCoord", "texcoord");


        pointObj = new util.ObjectInstance(
                gl,
                program,
                shaderLocations,
                shaderToVertexAttribute,
                tmesh, new String(""));

        in = getClass().getClassLoader().getResourceAsStream("textures/circle"
                + ".png");
        try {
            pointTexture = TextureIO.newTexture(in, false, "png");
        } catch (IOException e) {
            e.printStackTrace();
        }


        pointTexture.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
        pointTexture.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
        pointTexture.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        pointTexture.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);


    }

    private void initParticles() {
        particles = new ArrayList<Particle>();
        for (int i = 0; i < numParticles; i++) {
            particles.add(getRandomParticle());
        }
    }

    private Particle getRandomParticle() {
        Vector4f position;
        Vector4f color;
        Vector4f velocity;
        float startTime,mass;

        position = new Vector4f(0, 0, 0, 1);
        color = new Vector4f(0.5f+0.5f*(float)Math.random(),0.5f+0.5f*(float)Math.random(),0.5f+0.5f*(float)Math.random(),1);
        velocity = new Vector4f(20*(float)Math.random()-10,40*(float)Math.random()+10,20*(float)Math.random()-10,0.0f);

        startTime = 20*(float)Math.random()+1;
        mass = 5 * (float) Math.random()+1;


        return new Particle(position,velocity,color,mass,startTime);
    }






    public void init(GLAutoDrawable gla) throws Exception {
        GL3 gl = gla.getGL().getGL3();


        //compile and make our shader program. Look at the ShaderProgram class for details on how this is done
        program = new util.ShaderProgram();
        program.createProgram(gl, "shaders/particle.vert", "shaders/particle.frag");
        shaderLocations = program.getAllShaderVariables(gl);
        initObjects(gl);

        initParticles();
    }

    private void animate() {
        time += 0.1f;
    }


    public void draw(GLAutoDrawable gla) {
        GL3 gl = gla.getGL().getGL3();
        FloatBuffer fb16 = Buffers.newDirectFloatBuffer(16);
        FloatBuffer fb4 = Buffers.newDirectFloatBuffer(4);

        animate();
        program.enable(gl);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA,GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glPointSize(2);
        gl.glClearColor(backgroundColor.x,backgroundColor.y,backgroundColor
                .z,backgroundColor.w);
        gl.glClear(gl.GL_COLOR_BUFFER_BIT);


        /*
         *In order to change the shape of this triangle, we can either move the vertex positions above, or "transform" them
         * We use a modelview matrix to store the transformations to be applied to our triangle.
         * Right now this matrix is identity, which means "no transformations"
         */
        modelView = new Matrix4f().lookAt(new Vector3f(0, 00, 70.0f),
                new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));



    /*
     *Supply the shader with all the matrices it expects.
    */
        gl.glUniformMatrix4fv(shaderLocations.getLocation("projection"), 1, false, proj
                .get(fb16));
        gl.glUniformMatrix4fv(shaderLocations.getLocation("modelview"), 1, false, modelView.get(fb16));
        //return;
        gl.glUniform3f(shaderLocations.getLocation("force"),force.x,force.y,force.z);
        gl.glUniform1f(shaderLocations.getLocation("time"),time);

        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glUniform1i(shaderLocations.getLocation("sprite"), 0);
      pointTexture.bind(gl);

        for (int i = 0; i < particles.size(); i++) {
            Particle particle = particles.get(i);

            if (time>particle.getStartTime())
            {
              gl.glUniform4fv(shaderLocations.getLocation
                      ("initialPosition"), 1,particle.getPosition().add
                      (startPosition).get(fb4));
                gl.glUniform4fv(shaderLocations.getLocation("vColor"),
                        1, particle.getColor().get(fb4));
                gl.glUniform3fv(shaderLocations.getLocation("velocity"),
                        1, particle.getVelocity().get(fb4));
                gl.glUniform1f(shaderLocations.getLocation("startTime"), particle.getStartTime());
                gl.glUniform1f(shaderLocations.getLocation("mass"), particle.getMass());

                pointObj.draw(gla);
            }

        }

        gl.glFlush();
        program.disable(gl);


    }

    public void forceRight()
    {
        force.x+=1;
    }

    public void forceLeft()
    {
        force.x-=1;
    }

    public void forceUp()
    {
        force.y+=1;
    }

    public void forceDown()
    {
        force.y-=1;
    }


    public void reshape(GLAutoDrawable gla, int x, int y, int width, int height) {
        GL gl = gla.getGL();
        WINDOW_WIDTH = width;
        WINDOW_HEIGHT = height;
        gl.glViewport(0, 0, width, height);

        proj = new Matrix4f().perspective((float) Math.toRadians(120.0f), (float) width / height, 0.1f, 10000.0f);
        //proj = new Matrix4f().ortho(-50, 50, -50, 50, 0.1f, 10000.0f);

    }

    public void setStartPosition(int x,int y) {
      float actualY = WINDOW_HEIGHT - y - 0.5f * WINDOW_HEIGHT;
      float actualX = x - 0.5f * WINDOW_WIDTH;
      actualY =
              (float)(70.0f*actualY/(0.5*WINDOW_HEIGHT/Math.tan(Math.toRadians(120.0f)/2)));
      actualX =
              (float)(70.0f*actualX/(0.5*WINDOW_HEIGHT/Math.tan(Math.toRadians(120.0f)/2)));

      startPosition = new Vector4f(actualX,
              actualY, 0, 1);
    }



    public void dispose(GLAutoDrawable gla) {
        GL3 gl = gla.getGL().getGL3();

    }


}
