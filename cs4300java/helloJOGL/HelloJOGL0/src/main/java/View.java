import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.GLBuffers;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import util.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
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
  protected IntBuffer vbo;//all our Vertex Buffer Object IDs
  private ShaderLocationsVault shaderLocations;


  private Vector4f color;

  ShaderProgram program;


  public View() {
    proj = new Matrix4f();
    proj.identity();

    shaderLocations = null;
    WINDOW_WIDTH = WINDOW_HEIGHT = 0;
  }

  public void init(GLAutoDrawable gla) throws Exception {
    GL3 gl = (GL3) gla.getGL().getGL3();


    //compile and make our shader program. Look at the ShaderProgram class for details on how this is done
    program = new ShaderProgram();
    program.createProgram(gl, "shaders/default.vert", "shaders/default.frag");

    shaderLocations = program.getAllShaderVariables(gl);


    //BEGIN: uses vertices directly and glDrawArrays to draw

    float[] vertexDataAsFloats = {-100,-100,100,-100,100,100,-100,-100,100,
            100,-100,100};


    FloatBuffer vertexDataAsBuffer = FloatBuffer.wrap(vertexDataAsFloats);


    vbo = IntBuffer.allocate(1);

    program.enable(gl);
    gl.glGenBuffers(1, vbo);
    gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vbo.get(0));
    gl.glBufferData(GL3.GL_ARRAY_BUFFER, vertexDataAsBuffer.capacity() * GLBuffers.SIZEOF_FLOAT, vertexDataAsBuffer, GL3.GL_STATIC_DRAW);

    gl.glVertexAttribPointer(shaderLocations.getLocation("vPosition")
            ,2
            , GL3.GL_FLOAT
            , false
            , 0
            , 0);
    //enable this attribute so that when rendered, this is sent to the vertex shader
    gl.glEnableVertexAttribArray(shaderLocations.getLocation("vPosition"));


    //END: uses vertices directly and glDrawArrays to draw

    /*
 //BEGIN: using vertices and indices, uses glDrawElements to draw

    float[] vertexDataAsFloats = {-100,-100,100,-100,100,100,-100,100};
    int []indices = {0,1,2,0,2,3};

    FloatBuffer vertexDataAsBuffer = FloatBuffer.wrap(vertexDataAsFloats);
    IntBuffer indexDataAsBuffer = IntBuffer.wrap(indices);

    vbo = IntBuffer.allocate(2);
    program.enable(gl);
    gl.glGenBuffers(2,vbo);
    gl.glBindBuffer(GL3.GL_ARRAY_BUFFER,vbo.get(0));
    gl.glBufferData(GL3.GL_ARRAY_BUFFER,
            vertexDataAsBuffer.capacity()*GLBuffers.SIZEOF_FLOAT,
            vertexDataAsBuffer,GL3.GL_STATIC_DRAW);

    gl.glVertexAttribPointer(shaderLocations.getLocation("vPosition")
            ,2
            , GL3.GL_FLOAT
            , false
            , 0
            , 0);
    //enable this attribute so that when rendered, this is sent to the vertex shader
    gl.glEnableVertexAttribArray(shaderLocations.getLocation("vPosition"));

    gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER,vbo.get(1));
    gl.glBufferData(GL3.GL_ELEMENT_ARRAY_BUFFER,
            indexDataAsBuffer.capacity()*GLBuffers.SIZEOF_INT,
            indexDataAsBuffer,GL3.GL_STATIC_DRAW);

    //END: using vertices and indices, uses glDrawElements to draw
*/
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

    //use this if using only vertices
    //total 6 vertices that form 2 triangles
    gl.glDrawArrays(GL.GL_TRIANGLES,0,6);

    //use this if using vertices and indices
    //total 6 indices that form 2 triangles (GL_TRIANGLES = take 3 indices at
    // a time
    //gl.glDrawElements(GL.GL_TRIANGLES, 6,GL.GL_UNSIGNED_INT, 0);

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
  }
}
