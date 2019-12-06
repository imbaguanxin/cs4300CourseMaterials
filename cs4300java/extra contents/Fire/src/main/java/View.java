import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import util.ObjectInstance;


import java.io.*;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by ashesh on 9/18/2015.
 *
 * The View class is the "controller" of all our OpenGL stuff. It cleanly
 * encapsulates all our OpenGL functionality from the rest of Java GUI, managed
 * by the JOGLFrame class.
 */
public class View {
  private int WINDOW_WIDTH, WINDOW_HEIGHT;
  private Matrix4f proj, modelView;
  ObjectInstance quadObj;
  List<Texture> fireTextures;
  List<FireParticle> fireParticles;
  private float time, refreshRate, fireSpeed, defaultSize;
  private Vector3f fireSource;
  private final int numParticles;


  private util.ShaderProgram program;
  private util.ShaderLocationsVault shaderLocations;


  public View() {
    proj = new Matrix4f();
    proj.identity();

    modelView = new Matrix4f();
    modelView.identity();
    fireTextures = new ArrayList<Texture>();
    time = 0;
    refreshRate = 1;
    fireSpeed = 1;
    defaultSize = 5;
    fireSource = new Vector3f(100, 0, 0);
    numParticles = 50000;
  }

  private void initObjects(GL3 gl) throws FileNotFoundException {

    util.PolygonMesh<?> tmesh;

    InputStream in;

    in = getClass().getClassLoader().getResourceAsStream
            ("models/quad.obj");

    tmesh = util.ObjImporter.importFile(new VertexAttribProducer(), in, true);
    util.ObjectInstance obj;

    Map<String, String> shaderToVertexAttribute = new HashMap<String, String>();

    shaderToVertexAttribute.put("vPosition", "position");
    shaderToVertexAttribute.put("vTexCoord", "texcoord");


    quadObj = new util.ObjectInstance(
            gl,
            program,
            shaderLocations,
            shaderToVertexAttribute,
            tmesh, new String(""));
    // texture

    for (int i = 0; i < 4; i++) {
      in = getClass().getClassLoader().getResourceAsStream("textures/gfire-" + (i + 1) + ".png");

      Texture tex = null;

      try {
        tex = TextureIO.newTexture(in, false, "png");
      } catch (IOException e) {
        e.printStackTrace();
      }


      tex.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
      tex.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
      tex.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
      tex.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);

      fireTextures.add(tex);

    }

  }

  private void initFireParticles() {
    fireParticles = new ArrayList<FireParticle>();
    for (int i = 0; i < numParticles; i++) {
      fireParticles.add(getRandomFireParticle());
    }
  }

  private FireParticle getRandomFireParticle() {
    Vector4f position;
    Vector4f dir;
    float temperature;
    float startTime;
    float lifetime;
    float size;

    position = new Vector4f(-50 + (int) (100 * Math.random()), 0, 0, 1);
    dir = new Vector4f(-0.4f + 0.8f * (float) Math.random(), 0.4f + 0.6f * (float) Math.random(), 0, 0);
    dir = dir.normalize();
    dir = new Vector4f(dir.x * fireSpeed * (float) Math.random(), dir.y * fireSpeed * (float) Math.random(), dir.z * fireSpeed * (float) Math.random(), 0.0f);

    temperature = 1000;
    startTime = time + 5.0f * (float) Math.random();
    lifetime = 5 * (float) Math.random();
    size = defaultSize * (float) Math.random();

    return new FireParticle(position, dir, size, temperature, startTime, lifetime);
  }


  public void init(GLAutoDrawable gla) throws Exception {
    GL3 gl = gla.getGL().getGL3();


    //compile and make our shader program. Look at the ShaderProgram class for details on how this is done
    program = new util.ShaderProgram();
    program.createProgram(gl, "shaders/fire.vert", "shaders/fire.frag");
    shaderLocations = program.getAllShaderVariables(gl);
    initObjects(gl);

    initFireParticles();
  }

  private void animate() {
    time += (float) refreshRate / 1000;
    for (int i = 0; i < fireParticles.size(); i++) {
      fireParticles.get(i).advance(time);
      if (fireParticles.get(i).isDead()) {
        fireParticles.set(i, getRandomFireParticle());
      }
    }
  }


  public void draw(GLAutoDrawable gla) {
    GL3 gl = gla.getGL().getGL3();
    FloatBuffer fb16 = Buffers.newDirectFloatBuffer(16);
    FloatBuffer fb4 = Buffers.newDirectFloatBuffer(4);

    animate();
    program.enable(gl);
    gl.glClearColor(0, 0, 0, 0);
    gl.glClear(gl.GL_COLOR_BUFFER_BIT);

    gl.glEnable(GL.GL_BLEND);
    gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
    //gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

    gl.glEnable(GL.GL_TEXTURE_2D);
    gl.glActiveTexture(GL.GL_TEXTURE0);


    gl.glUniform1i(shaderLocations.getLocation("sprite"), 0);
        /*
         *In order to change the shape of this triangle, we can either move the vertex positions above, or "transform" them
         * We use a modelview matrix to store the transformations to be applied to our triangle.
         * Right now this matrix is identity, which means "no transformations"
         */
    modelView = new Matrix4f().lookAt(new Vector3f(0, 0, 200.0f), new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));



    /*
     *Supply the shader with all the matrices it expects.
    */
    gl.glUniformMatrix4fv(
            shaderLocations.getLocation("projection"),
            1, false,
            proj.get(fb16));
    //return;


    for (int i = 0; i < fireParticles.size(); i++) {
      FireParticle particle = fireParticles.get(i);
      if (particle.hasStarted()) {
        Matrix4f transformation = new Matrix4f(modelView)
                .translate(fireSource)
                .translate(particle.getPosition().x, particle.getPosition().y, particle.getPosition().z)
                .scale(particle.getSize(), particle.getSize(), particle.getSize());
        gl.glUniformMatrix4fv(
                shaderLocations.getLocation("modelview"),
                1, false,
                transformation.get(fb16));
        Vector4f color = getColor(particle.getTemperature());
        gl.glUniform4fv(
                shaderLocations.getLocation("vColor"), 1,
                color.get(fb4));
        fireTextures.get(2 * (i % 2) + 1).bind(gl);
        quadObj.draw(gla);
      }
    }
    gl.glFlush();
    gl.glDisable(GL.GL_BLEND);

    program.disable(gl);


  }

  private Vector4f getColor(float temperature) {
    float r, g, b;

   //  return new Vector4f(1,1,1,1);

  /*  if (temperature>980)
    {
        r = 1;
        g = 1;
        b = (temperature-980)/20;
    }
    else*/
    if (temperature > 800) {
      r = 1;
      g = 0.5f + 0.5f * (temperature - 800) / 100;
      b = 0;
    } else {
      r = temperature / 1000;
      g = 0.5f * temperature / 1000;
      b = 0;
    }
    return new Vector4f(r, g, b, 1);
  }

  public void reshape(GLAutoDrawable gla, int x, int y, int width, int height) {
    GL gl = gla.getGL();
    WINDOW_WIDTH = width;
    WINDOW_HEIGHT = height;
    gl.glViewport(0, 0, width, height);

    //proj = new Matrix4f().perspective((float) Math.toRadians(120.0f), (float) width / height, 0.1f, 10000.0f);
    proj = new Matrix4f().ortho(-0.25f * WINDOW_WIDTH, 0.25f * WINDOW_WIDTH, -0.25f * WINDOW_HEIGHT, 0.25f * WINDOW_HEIGHT, -10000.0f, 10000.0f);

  }

  public void moveFireSource(int x, int y) {
    fireSource = new Vector3f(0.5f * x - 0.25f * WINDOW_WIDTH, 0.5f * (WINDOW_HEIGHT - y) - 0.25f * WINDOW_HEIGHT, 0);
  }

  public void dispose(GLAutoDrawable gla) {
    GL3 gl = gla.getGL().getGL3();

  }


}
