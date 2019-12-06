import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import util.*;

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
  private Matrix4f proj;
  private ObjectInstance obj;
  private ShaderLocationsVault shaderLocations;


  private Vector4f color;

  ShaderProgram program;


  public View() {
    proj = new Matrix4f();
    proj.identity();

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
        Create the vertices of the two triangles to be
        drawn. Since we are drawing in 2D, z-coordinate
        of all points will be 0. The fourth number
        for each vertex is 1. This is the
        homogeneous coordinate, and "1" means this
        is a location and not a direction
         */

    List<Vector4f> positions = new ArrayList<Vector4f>();
    positions.add(new Vector4f(-100.0f, -100.0f, 0, 1.0f));
    positions.add(new Vector4f(100.0f, -100.0f, 0, 1.0f));
    positions.add(new Vector4f(100.0f, 100.0f, 0, 1.0f));
    positions.add(new Vector4f(-100.0f, 100.0f, 0, 1.0f));

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
        We now generate a series of indices.
        These indices will be for the above list
        of vertices. For example we want to use
        the above list to draw triangles.
        The first triangle will be created from
        vertex numbers 0, 1 and 2 in the list above
        (indices begin at 0 like arrays). The second
        triangle will be created from vertex numbers
        0, 2 and 3. Therefore we will create a list
        of indices {0,1,2,0,2,3}.

        What is the advantage of having a second
        list? Vertices are often shared between
        triangles, and having a separate list of
        indices avoids duplication of vertex data
         */
    List<Integer> indices = new ArrayList<Integer>();
    indices.add(0);
    indices.add(1);
    indices.add(2);

    indices.add(0);
    indices.add(2);
    indices.add(3);

    //now we create a polygon mesh object
    PolygonMesh<IVertexData> mesh;

    mesh = new PolygonMesh<IVertexData>();


    mesh.setVertexData(vertexData);
    mesh.setPrimitives(indices);

        /*
        It turns out, there are several ways of
        reading the list of indices and interpreting
        them as triangles.

        The first, simplest (and the one we have
        assumed above) is to just read the list of
        indices 3 at a time, and use them as triangles.
        In OpenGL, this is the GL_TRIANGLES mode.

        If we wanted to draw lines by reading the indices
        two at a time, we would specify GL_LINES (try this).

        In any case, this "mode" and the actual list of
        indices are related. That is, decide which mode
        you want to use, and accordingly build the list
        of indices.
         */

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

    //currently there is only one per-vertex attribute: position
    shaderToVertexAttribute.put("vPosition", "position");
    obj = new ObjectInstance(gl, program, shaderLocations, shaderToVertexAttribute, mesh, "triangles");

  }


  public void draw(GLAutoDrawable gla) {
    GL3 gl = gla.getGL().getGL3();
    FloatBuffer fb16 = Buffers.newDirectFloatBuffer(16);
    FloatBuffer fb4 = Buffers.newDirectFloatBuffer(4);

    color = new Vector4f(1,0,0,1);

    //set the background color to be white
 /*   gl.glClearColor((float)Math.random(),
            (float)Math.random(),
            (float)Math.random(), 1);*/
    gl.glClearColor(1,1,1,1);
    //clear the background
    gl.glClear(gl.GL_COLOR_BUFFER_BIT);
    //enable the shader program
    program.enable(gl);

    //pass the projection matrix to the shader
    gl.glUniformMatrix4fv(
            shaderLocations.getLocation("projection"),
            1, false, proj.get(fb16));

    //send the color of the triangle
    gl.glUniform4fv(
            shaderLocations.getLocation("vColor")
            , 1, color.get(fb4));

  //  gl.glPolygonMode(GL.GL_FRONT_AND_BACK,GL3.GL_LINE);
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

    proj = new Matrix4f().ortho2D(-150, 150, -150*(float)height/width,
            150*(float)height/width);

  }

  public void dispose(GLAutoDrawable gla) {
    obj.cleanup(gla);
  }
}
