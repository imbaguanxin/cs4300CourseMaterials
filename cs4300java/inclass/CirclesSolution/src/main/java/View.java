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


/**
 * Created by ashesh on 9/18/2015.
 *
 * The View class is the "controller" of all our OpenGL stuff. It cleanly
 * encapsulates all our OpenGL functionality from the rest of Java GUI, managed
 * by the JOGLFrame class.
 */
public class View {
  private int WINDOW_WIDTH, WINDOW_HEIGHT;
  private Matrix4f modelview,proj;
  //the position of the center of the circle
  private Vector4f center;
  //the motion vector. The length of this vector is its speed
  private Vector4f motion;
  private ObjectInstance obj;
  private ShaderLocationsVault shaderLocations;


  private FloatBuffer color;

  ShaderProgram program;


  public View() {
    proj = new Matrix4f();
    proj.identity();

    modelview = new Matrix4f();
    modelview.identity();

    center = new Vector4f(100,100,0,1);
    motion = new Vector4f(1,1,0,0);

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



        /*
          Now we create a triangle mesh from these
          vertices.

          The mesh has vertex positions and indices for now.

         */

        /*
        Now we create the vertices of the circle. Drawing a circle can be
        thought of as assembling one from pizza slices. The thinner the
        slices, the smoother the outline will be.

        For simplicity, we will create a circle with center at the origin
        and radius 1. We can modify both of them using transformations later
         */

    List<Vector4f> positions = new ArrayList<Vector4f>();

    //create vertices here
    int SLICES = 50;

    //push the center of the circle as the first vertex
    positions.add(new Vector4f(0,0,0,1));
    for (int i=0;i<SLICES;i++) {
      float theta = (float)(i*2*Math.PI/SLICES);
      positions.add(new Vector4f(
              (float)Math.cos(theta),
              (float)Math.sin(theta),
              0,
              1));
    }

    //we add the last vertex to make the circle watertight
    positions.add(new Vector4f(1,0,0,1));


    //set up vertex attributes (in this case we have only position)
    List<IVertexData> vertexData = new ArrayList<IVertexData>();
    VertexAttribProducer producer = new VertexAttribProducer();
    for (Vector4f pos : positions) {
      IVertexData v = producer.produce();
      v.setData("position", new float[]{pos.x,
              pos.y,
              pos.z,
              pos.w});
      vertexData.add(v);
    }



        /*
        Now we create the indices that will form the pizza slices of the
        circle. Think about what mode you will use, and accordingly push the
        indices
         */
    List<Integer> indices = new ArrayList<Integer>();

    /* we will use a GL_TRIANGLE_FAN, because this seems tailormade for
    what we want to do here. This mode will use the indices in order
    (0,1,2), (0,2,3), (0,3,4), ..., (0,n-1,n)
     */
    for (int i=0;i<positions.size();i++) {
      indices.add(i);
    }



    //now we create a polygon mesh object
    PolygonMesh<IVertexData> mesh;

    mesh = new PolygonMesh<IVertexData>();


    mesh.setVertexData(vertexData);
    mesh.setPrimitives(indices);



    mesh.setPrimitiveType(GL.GL_TRIANGLE_FAN);
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
    obj = new ObjectInstance(gl, program, shaderLocations, shaderToVertexAttribute, mesh, "triangles");

    //we will draw it using red color
    color = FloatBuffer.wrap(new float[]{1, 0, 0, 0});

  }


  public void draw(GLAutoDrawable gla) {
    float radius = 50;
    //move the circle
    center = center.add(motion);
    //if the center is out of bounds in x, we reverse the motion direction in x
    if ((center.x<radius) || (center.x>(WINDOW_WIDTH-radius))) {
      motion.x = -motion.x;
    }

    if ((center.y<radius) || (center.y>(WINDOW_HEIGHT-radius))) {
      motion.y = -motion.y;
    }

    GL3 gl = gla.getGL().getGL3();
    FloatBuffer fb16 = Buffers.newDirectFloatBuffer(16);

    modelview = new Matrix4f().translate(center.x,center.y,center.z)
                              .scale(radius,radius,radius);



    //set the background color to be white
    gl.glClearColor(1,1,1,0);
    //clear the background
    gl.glClear(gl.GL_COLOR_BUFFER_BIT);
    //enable the shader program
    program.enable(gl);

    //pass the projection matrix to the shader
    gl.glUniformMatrix4fv(
            shaderLocations.getLocation("projection"),
            1, false, proj.get(fb16));

    //pass the modelview matrix to the shader
    gl.glUniformMatrix4fv(
            shaderLocations.getLocation("modelview"),
            1, false, modelview.get(fb16));

    //send the color of the triangle
    gl.glUniform4fv(
            shaderLocations.getLocation("vColor")
            , 1, color);


   // gl.glPolygonMode(GL.GL_FRONT_AND_BACK,GL3.GL_LINE);
    //draw the object
    obj.draw(gla);

    gl.glFlush();
    //disable the program
    program.disable(gl);
  }

  //this method is called from the JOGLFrame class, everytime the window resizes
  public void reshape(GLAutoDrawable gla, int x, int y, int width, int height) {
    GL gl = gla.getGL();
    WINDOW_WIDTH = width;
    WINDOW_HEIGHT = height;
    gl.glViewport(0, 0, width, height);

    proj = new Matrix4f().ortho2D(0,width,0,height);

  }

  public void dispose(GLAutoDrawable gla) {
    obj.cleanup(gla);
  }
}
