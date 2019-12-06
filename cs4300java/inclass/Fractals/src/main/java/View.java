import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.GLBuffers;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;


import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.text.DecimalFormat;
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
  private Matrix4f projection, modelView;
  private Vector2f center;
  private double scalex, scaley, scale;
  private int MAX_ITERATIONS;


  util.ShaderProgram program;
  util.ObjectInstance obj;
  private int modelviewLocation, projectionLocation, vPositionLocation, dimsLocation, maxIterLocation, centerLocation, scaleLocation;
  private util.ShaderLocationsVault shaderLocations;


  //the different kinds of attributes for a vertex.


  public View() {
    projection = new Matrix4f();
    projection.identity();

    modelView = new Matrix4f();
    modelView.identity();

    obj = null;

    //for mandelbrot, try the following points below for zoom without pan:
    // (0.0f,1.0f)
    //(-2.0f,0.0f)
    //(-0.77568377f, 0.13646737f)
    //(-1.54368901f, 0.0f)
    center = new Vector2f(-1.54368901f, 0.0f);
    scale = scalex = scaley = 1.0f;
    MAX_ITERATIONS = 50;
  }

  public String getFrameInfoString() {
    String s;

    DecimalFormat df = new DecimalFormat();
    df.setMaximumFractionDigits(10);

    s = "Range of set:(" + df.format(center.x - 0.5 * scale) + "," + df.format(center.x + 0.5 * scale) + ") to " + df.format(center.x + 0.5 * scale) + "," + df.format(center.y + 0.5 * scale) + "\t";
    return s;
  }

  public String getIterationInfoString() {
    String s;
    s = "Number of iterations: " + MAX_ITERATIONS;
    return s;
  }

  public void zoomIn() {
    scalex = 4 * scalex;
    scaley = 5 * scaley;
    if ((scalex > 10000) || (scaley > 10000)) {
      scalex = scalex / 10000;
      scaley = scaley / 10000;
    }
    scale = scalex / scaley;
  }

  public void zoomOut() {
    scalex = 0.25f * scalex;
    scaley = 0.2f * scaley;
    if ((scalex < 0.0001f) || (scaley < 0.0001f)) {
      scalex = scalex * 10000;
      scaley = scaley * 10000;
    }
    scale = scalex / scaley;
  }

  public void increaseMaxIterations() {
    MAX_ITERATIONS += 5;
  }

  public void decreaseMaxIterations() {
    if (MAX_ITERATIONS > 5)
      MAX_ITERATIONS -= 5;
  }

  public void translate(int x, int y) {
    center.x = center.x - (float) (scale * x / (WINDOW_WIDTH));
    center.y = center.y - (float) (scale * y / (WINDOW_HEIGHT));
  }

  public void init(GLAutoDrawable gla) throws Exception {
    GL3 gl = gla.getGL().getGL3();

    //compile and make our shader program. Look at the ShaderProgram class for details on how this is done
    program = new util.ShaderProgram();
    program.createProgram(gl, "shaders/fractal.vert", "shaders/fractal.frag");

    shaderLocations = program.getAllShaderVariables(gl);

    program.enable(gl);

    //get input variables that need to be given to the shader program
    projectionLocation = shaderLocations.getLocation("projection");
    modelviewLocation = shaderLocations.getLocation("modelview");
    maxIterLocation = shaderLocations.getLocation("maxiter");
    dimsLocation = shaderLocations.getLocation("dims");
    centerLocation = shaderLocations.getLocation("center");
    scaleLocation = shaderLocations.getLocation("scale");

    vPositionLocation = shaderLocations.getLocation("vPosition");

    List<Vector4f> positions = new ArrayList<Vector4f>();
    positions.add(new Vector4f(0.0f, 0.0f, 0, 1.0f));
    positions.add(new Vector4f(1.0f, 0.0f, 0, 1.0f));
    positions.add(new Vector4f(1.0f, 1.0f, 0, 1.0f));
    positions.add(new Vector4f(0.0f, 1.0f, 0, 1.0f));

    //set up vertex attributes (in this case we have only position)
    List<util.IVertexData> vertexData = new ArrayList<util.IVertexData>();
    VertexAttribProducer producer = new VertexAttribProducer();
    for (Vector4f pos : positions) {
      util.IVertexData v = producer.produce();
      v.setData("position", new float[]{pos.x,
              pos.y,
              pos.z,
              pos.w});
      vertexData.add(v);
    }


    //draw a single quad
    List<Integer> indices = new ArrayList<Integer>();
    indices.add(0);
    indices.add(1);
    indices.add(2);
    indices.add(3);

    //now we create a polygon mesh object
    util.PolygonMesh<util.IVertexData> mesh;

    mesh = new util.PolygonMesh<util.IVertexData>();


    mesh.setVertexData(vertexData);
    mesh.setPrimitives(indices);

    mesh.setPrimitiveType(GL3.GL_TRIANGLE_FAN);
    mesh.setPrimitiveSize(3);

        /*
        now we create an ObjectInstance for it
        The ObjectInstance encapsulates a lot of the
         OpenGL-specific code to draw this object
         */

        /* so in the mesh, we have some attributes for each vertex. In the shader
        we have variables for each vertex attribute. We have to provide a mapping
        between attribute name in the mesh and corresponding shader variable name.
        This will allow us to use PolygonMesh with any shader program, without
        assuming that the attribute names in the mesh and the names of shader variables
        will be the same.

        We create such a shader variable -> vertex attribute mapping now
         */
    Map<String, String> shaderToVertexAttribute = new HashMap<String, String>();

    //currently there is only one per-vertex attribute: position
    shaderToVertexAttribute.put("vPosition", "position");
    obj = new util.ObjectInstance(gl, program, shaderLocations, shaderToVertexAttribute, mesh, "triangles");


  }


  public void draw(GLAutoDrawable gla) {
    GL3 gl = gla.getGL().getGL3();

    gl.glClearColor(0, 0, 0, 1);
    gl.glClear(gl.GL_COLOR_BUFFER_BIT);


    modelView = new Matrix4f();
    //scale up the quad above so that it occupies the entire screen
    modelView = modelView.scale(WINDOW_WIDTH, WINDOW_HEIGHT, 1);

    program.enable(gl);

    /*
     *Supply the shader with all the matrices it expects.
    */
    FloatBuffer fb = Buffers.newDirectFloatBuffer(16);
    gl.glUniformMatrix4fv(projectionLocation, 1, false, projection.get(fb));
    gl.glUniformMatrix4fv(modelviewLocation, 1, false, modelView.get(fb));

    gl.glUniform1i(maxIterLocation, MAX_ITERATIONS);
    gl.glUniform2f(dimsLocation, WINDOW_WIDTH, WINDOW_HEIGHT);
    gl.glUniform2f(centerLocation, center.x, center.y);
    gl.glUniform1f(scaleLocation, (float) scale);


    obj.draw(gla);
    gl.glFlush();

    program.disable(gl);


  }

  public void reshape(GLAutoDrawable gla, int x, int y, int width, int height) {
    GL gl = gla.getGL();
    WINDOW_WIDTH = width;
    WINDOW_HEIGHT = height;
    gl.glViewport(0, 0, width, height);

    projection = new Matrix4f().ortho2D(0, (float) WINDOW_WIDTH, 0, (float) WINDOW_HEIGHT);

  }

  public void dispose(GLAutoDrawable gla) {
    GL3 gl = gla.getGL().getGL3();

    obj.cleanup(gla);
    program.releaseShaders(gl);
  }
}
