import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;

import util.*;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;


/**
 * Created by ashesh on 9/18/2015.
 *
 * The View class is the "controller" of all our OpenGL stuff. It cleanly
 * encapsulates all our OpenGL functionality from the rest of Java GUI, managed
 * by the JOGLFrame class.
 */
public class View {
  private int WINDOW_WIDTH, WINDOW_HEIGHT;
  private Matrix4f proj;
  Stack<Matrix4f> modelview;
  private ObjectInstance obj;
  private ShaderLocationsVault shaderLocations;
  private int timeclock;

  ShaderProgram program;


  public View() {
    proj = new Matrix4f();
    modelview = new Stack<Matrix4f>();
    proj.identity();
    timeclock = 0;

    obj = null;
    shaderLocations = null;
    WINDOW_WIDTH = WINDOW_HEIGHT = 0;
  }

  public void init(GLAutoDrawable gla) throws Exception {
    GL3 gl = (GL3) gla.getGL().getGL3();


    //compile and make our shader program. Look at the ShaderProgram class for details on how this is done
    program = new ShaderProgram();
    program.createProgram(gl, "shaders/default.vert", "shaders/default.frag");

    shaderLocations = program.getAllShaderVariables(gl);


    List<Vector4f> positions = new ArrayList<Vector4f>();
    positions.add(new Vector4f(0.0f, 1.0f, 0, 1.0f));
    positions.add(new Vector4f(-0.5f, 0.0f, 0, 1.0f));
    positions.add(new Vector4f(0.5f, 0.0f, 0, 1.0f));

    //we add a second attribute to each vertex: color
    //note that the shader variable has been changed to "in" as compared
    // to HellJOGL because color is now a per-vertex attribute

    List<Vector4f> colors = new ArrayList<Vector4f>();
    colors.add(new Vector4f(1, 0, 0, 1)); //red
    colors.add(new Vector4f(0, 1, 0, 1)); //green
    colors.add(new Vector4f(0, 0, 1, 1)); //blue


    //set up vertex attributes (in this case we have only position and color)
    List<IVertexData> vertexData = new ArrayList<IVertexData>();
    VertexAttribWithColorProducer producer = new VertexAttribWithColorProducer();
    for (int i = 0; i < positions.size(); i++) {
      IVertexData v = producer.produce();
      v.setData("position", new float[]{positions.get(i).x,
              positions.get(i).y,
              positions.get(i).z,
              positions.get(i).w});
      v.setData("color", new float[]{colors.get(i).x,
              colors.get(i).y,
              colors.get(i).z,
              colors.get(i).w});
      vertexData.add(v);
    }

    List<Integer> indices = new ArrayList<Integer>();
    indices.add(0);
    indices.add(1);
    indices.add(2);

    //now we create a polygon mesh object
    PolygonMesh mesh;

    mesh = new PolygonMesh();


    mesh.setVertexData(vertexData);
    mesh.setPrimitives(indices);

    mesh.setPrimitiveType(GL.GL_TRIANGLES);
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

    //currently there are two per-vertex attributes: position and color
    shaderToVertexAttribute.put("vPosition", "position");
    shaderToVertexAttribute.put("vColor", "color");
    obj = new ObjectInstance(gl, program, shaderLocations, shaderToVertexAttribute, mesh, "triangles");


  }


  public void draw(GLAutoDrawable gla) {


    GL3 gl = gla.getGL().getGL3();
    FloatBuffer fb16 = Buffers.newDirectFloatBuffer(16);
    FloatBuffer fb4 = Buffers.newDirectFloatBuffer(4);
    Vector4f color;
    int NUM_BLADES = 4;
    int ORBIT_RADIUS = 50;
    int NUM_FANS = 8;
    int PENDULUM_RADIUS = 150;
    int PENDULUM_ANGLE = 45;

    //increment the time, looping back to avoid overflow
    timeclock = (timeclock + 1) % (Integer.MAX_VALUE - 1);

    //set the background color to be white
    gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    //clear the background
    gl.glClear(GL.GL_COLOR_BUFFER_BIT);

    //enable the shader program
    program.enable(gl);

    modelview.push(new Matrix4f());

    //pass the projection matrix to the shader
    gl.glUniformMatrix4fv(shaderLocations.getLocation("projection"),
            1, false, proj.get(fb16));

    //move oscillating orbiting spinning fans
    modelview.push(new Matrix4f(modelview.peek()));
    modelview.peek()
            .translate(300, 300, 0);

    //pendulum motion, applied to orbiting spinning fans
    modelview.push(new Matrix4f(modelview.peek()));
    modelview.peek()
            .rotate((float) Math.toRadians(PENDULUM_ANGLE * (float) Math.sin
                    (0.1f * timeclock)), 0, 0, 1)
            .translate(0, -PENDULUM_RADIUS, 0);

    //the above transformation is applied to everything drawn in the loop below

    for (int j = 0; j < NUM_FANS; j++) {
      //put into orbit
      modelview.push(new Matrix4f(modelview.peek()));
      modelview.peek()
              .rotate((float) Math.toRadians(timeclock), 0, 0, 1)
              .rotate((float) Math.toRadians(j * 360.0f / NUM_FANS), 0, 0, 1)
              .translate(ORBIT_RADIUS, 0, 0);

      //spinning motion
      modelview.push(new Matrix4f(modelview.peek()));
      modelview.peek()
              .rotate((float) Math.toRadians(10 * timeclock), 0, 0, 1)
              .scale(0.125f, 0.125f, 0.125f);

      for (int i = 0; i < NUM_BLADES; i++) {
        //push a copy of modelview onto stack
        modelview.push(new Matrix4f(modelview.peek()));
        modelview.peek()
                .rotate((float) Math.toRadians(i * 360.0f / NUM_BLADES), 0, 0, 1)
                .scale(50, 100, 1.0f)
                .translate(0.0f, -1.0f, 0.0f);

        //pass the modelview matrix to the shader
        gl.glUniformMatrix4fv(shaderLocations.getLocation("modelview"),
                1, false, modelview.peek().get(fb16));

        //draw the object
        obj.draw(gla);
        //revert to the original modelview
        modelview.pop();
      }
      //undo spinning motion
      modelview.pop();

      //undo orbit
      modelview.pop();
    }

    //undo pendulum motion
    modelview.pop();
    //opengl is a pipeline-based framework. Things are not drawn as soon as
    //they are supplied. glFlush flushes the pipeline and draws everything
    gl.glFlush();
    //disable the program
    program.disable(gl);

    modelview.pop();


  }

  public void reshape(GLAutoDrawable gla, int x, int y, int width, int height) {
    GL gl = gla.getGL();
    WINDOW_WIDTH = width;
    WINDOW_HEIGHT = height;
    gl.glViewport(0, 0, width, height);

    proj = new Matrix4f().ortho2D(0, 500, 0, 500);

  }

  public void dispose(GLAutoDrawable gla) {
    obj.cleanup(gla);
  }
}
