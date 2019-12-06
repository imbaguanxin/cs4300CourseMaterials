import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;

import util.*;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
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
  private int WINDOW_WIDTH,WINDOW_HEIGHT;
  private Matrix4f proj,modelView;
  private util.ObjectInstance meshObject;
  private util.Material material;


  private util.ShaderProgram program;
  int angleOfRotation;
  private ShaderLocationsVault shaderLocations;




  public View() {
    proj = new Matrix4f();
    proj.identity();

    modelView = new Matrix4f();
    modelView.identity();

    angleOfRotation = 0;
  }

  private void initObjects(GL3 gl) throws FileNotFoundException
  {
    util.PolygonMesh tmesh;

    InputStream in;

    in = new FileInputStream("models/thomas-lyons-object.obj");

    tmesh = util.ObjImporter.importFile(new VertexAttribProducer(),in,true);

    Map<String, String> shaderToVertexAttribute = new HashMap<String, String>();

    //currently there is only one per-vertex attribute: position
    shaderToVertexAttribute.put("vPosition", "position");


    meshObject = new util.ObjectInstance(gl,
            program,
            shaderLocations,
            shaderToVertexAttribute,
            tmesh,new
            String(""));

    Vector4f min = tmesh.getMinimumBounds();
    Vector4f max = tmesh.getMaximumBounds();


    util.Material mat =  new util.Material();

    mat.setAmbient(1,1,1);
    mat.setDiffuse(1,1,1);
    mat.setSpecular(1,1,1);

    material = mat;




  }

  public void init(GLAutoDrawable gla) throws Exception {
    GL3 gl = (GL3) gla.getGL().getGL3();


    //compile and make our shader program. Look at the ShaderProgram class for details on how this is done
    program = new ShaderProgram();
    program.createProgram(gl, "shaders/default.vert", "shaders/default.frag");

    shaderLocations = program.getAllShaderVariables(gl);

    initObjects(gl);


  }


  public void draw(GLAutoDrawable gla) {
    GL3 gl = gla.getGL().getGL3();
    FloatBuffer fb16 = Buffers.newDirectFloatBuffer(16);
    FloatBuffer fb4 = Buffers.newDirectFloatBuffer(4);

    angleOfRotation = (angleOfRotation+1)%360;

    //set the background color to be black
    gl.glClearColor(0, 0, 0, 1);
    //clear the background
    gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);
    gl.glEnable(GL.GL_DEPTH_TEST);
    //enable the shader program
    program.enable(gl);

    modelView = new Matrix4f().lookAt(new Vector3f(0,200,200),new Vector3f(0,
            0,0),new Vector3f(0,1,0));
    modelView = modelView.mul(new Matrix4f().scale(200,200,200))
            .mul(new Matrix4f().rotate((float)Math.toRadians(angleOfRotation),0,1,0));

    //pass the projection matrix to the shader
    gl.glUniformMatrix4fv(
            shaderLocations.getLocation("projection"),
            1, false, proj.get(fb16));

    //pass the modelview matrix to the shader
    gl.glUniformMatrix4fv(
            shaderLocations.getLocation("modelview"),
            1, false, modelView.get(fb16));

    //send the color of the triangle
    gl.glUniform4fv(
            shaderLocations.getLocation("vColor")
            , 1, material.getAmbient().get(fb4));

    gl.glPolygonMode(GL.GL_FRONT_AND_BACK,GL3.GL_LINE); //OUTLINES

    //draw the object
    meshObject.draw(gla);

    gl.glFlush();
    //disable the program
    program.disable(gl);
    gl.glPolygonMode(GL.GL_FRONT_AND_BACK,GL3.GL_FILL); //BACK TO FILL
  }

  //this method is called from the JOGLFrame class, everytime the window resizes
  public void reshape(GLAutoDrawable gla, int x, int y, int width, int height) {
    GL gl = gla.getGL();
    WINDOW_WIDTH = width;
    WINDOW_HEIGHT = height;
    gl.glViewport(0, 0, width, height);

    proj = new Matrix4f().perspective((float)Math.toRadians(60.0f),
            (float) width/height,
            0.1f,
            10000.0f);

   proj = new Matrix4f().ortho(-400,400,-400,400,0.1f,10000.0f);

  }

  public void dispose(GLAutoDrawable gla) {
    meshObject.cleanup(gla);
  }
}
