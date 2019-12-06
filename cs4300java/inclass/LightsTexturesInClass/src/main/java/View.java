import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;

import org.joml.Matrix4f;
import org.joml.Vector2f;
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
  private enum LIGHT_COORDINATE{WORLD,VIEW};
  private int WINDOW_WIDTH, WINDOW_HEIGHT;
  private Matrix4f proj, modelView;
  private List<ObjectInstance> meshObjects;
  private List<util.Material> materials;
  private List<Matrix4f> transforms;
  private List<util.Light> lights;
  private Matrix4f trackballTransform;
  private float trackballRadius;
  private Vector2f mousePos;

  class LightLocation {
    int ambient, diffuse, specular, position;

    public LightLocation() {
      ambient = diffuse = specular = position = -1;
    }
  }


  util.ShaderProgram program;
  util.ShaderLocationsVault shaderLocations;
  private List<LightLocation> lightLocations;
  private int numLightsLocation;
  int angleOfRotation;


  public View() {
    proj = new Matrix4f();
    proj.identity();

    modelView = new Matrix4f();
    modelView.identity();

    meshObjects = new ArrayList<ObjectInstance>();
    transforms = new ArrayList<Matrix4f>();
    materials = new ArrayList<util.Material>();
    lights = new ArrayList<util.Light>();
    lightLocations = new ArrayList<LightLocation>();

    trackballTransform = new Matrix4f();
    angleOfRotation = 0;
    trackballRadius = 300;

  }


  private void initObjects(GL3 gl) throws FileNotFoundException, IOException {

    util.PolygonMesh<?> tmesh;

    InputStream in;

    in = getClass().getClassLoader().getResourceAsStream
            ("models/sphere.obj");

    tmesh = util.ObjImporter.importFile(new VertexAttribProducer(), in, true);
    util.ObjectInstance obj;

    Map<String, String> shaderToVertexAttribute = new HashMap<String, String>();

    shaderToVertexAttribute.put("vPosition", "position");
    shaderToVertexAttribute.put("vNormal", "normal");


    obj = new util.ObjectInstance(
            gl,
            program,
            shaderLocations,
            shaderToVertexAttribute,
            tmesh, new String(""));
    meshObjects.add(obj);
    util.Material mat;

    mat = new util.Material();

    mat.setAmbient(0.3f, 0.3f, 0.3f);
    mat.setDiffuse(0.7f, 0.7f, 0.7f);
    mat.setSpecular(0.7f, 0.7f, 0.7f);
    mat.setShininess(100);
    materials.add(mat);

    Matrix4f t;

    t = new Matrix4f().translate(0, 0, 0).scale(50, 50, 50);
    transforms.add(t);

  }

  private void initLights() {
    util.Light l = new util.Light();
    l.setAmbient(0.8f, 0.8f, 0.8f);
    l.setDiffuse(0.5f, 0.5f, 0.5f);
    l.setSpecular(0.5f, 0.5f, 0.5f);
    l.setPosition(00, 00, 100);
    lights.add(l);
      }

  private void initShaderVariables() {
    //get input variables that need to be given to the shader program
    for (int i = 0; i < lights.size(); i++) {
      LightLocation ll = new LightLocation();
      String name;

      name = "light[" + i + "]";
      ll.ambient = shaderLocations.getLocation(name + "" + ".ambient");
      ll.diffuse = shaderLocations.getLocation(name + ".diffuse");
      ll.specular = shaderLocations.getLocation(name + ".specular");
      ll.position = shaderLocations.getLocation(name + ".position");
      lightLocations.add(ll);
    }
  }


  public void init(GLAutoDrawable gla) throws Exception {
    GL3 gl = gla.getGL().getGL3();


    //compile and make our shader program. Look at the ShaderProgram class for details on how this is done
    program = new util.ShaderProgram();
    program.createProgram(gl, "shaders/gouraud-multiple.vert",
            "shaders/gouraud-multiple.frag");
    shaderLocations = program.getAllShaderVariables(gl);

    initObjects(gl);
    initLights();
    initShaderVariables();

  }




  public void draw(GLAutoDrawable gla) {
    angleOfRotation = (angleOfRotation + 1);
    GL3 gl = gla.getGL().getGL3();
    FloatBuffer fb16 = Buffers.newDirectFloatBuffer(16);
    FloatBuffer fb4 = Buffers.newDirectFloatBuffer(4);


    gl.glClearColor(0, 0, 0, 1);
    gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);
    gl.glEnable(GL.GL_DEPTH_TEST);

    program.enable(gl);

        /*
         *In order to change the shape of this triangle, we can either move the vertex positions above, or "transform" them
         * We use a modelview matrix to store the transformations to be applied to our triangle.
         * Right now this matrix is identity, which means "no transformations"
         */
    modelView = new Matrix4f().lookAt(
            new Vector3f(0.0f,
                    0.0f, 40.0f),
            new Vector3f(0, 0, 0),
            new Vector3f(0, 1, 0));

    //modelview currently represents world-to-view transformation
    //transform all lights so that they are in the view coordinate system too
    //before you send them to the shader.
    //that way everything is in one coordinate system (view) and the math will
    //be correct
    for (int i = 0; i < lights.size(); i++) {
      Vector4f pos = lights.get(i).getPosition();
      gl.glUniform4fv(lightLocations.get(i).position, 1, pos.get(fb4));
    }

    /*
     *Supply the shader with all the matrices it expects.
    */
    gl.glUniformMatrix4fv(
            shaderLocations.getLocation("projection")
            , 1, false, proj.get(fb16));
    //return;


    //all the light properties, except positions
    gl.glUniform1i(shaderLocations.getLocation("numLights"), lights.size());
    for (int i = 0; i < lights.size(); i++) {
      gl.glUniform3fv(lightLocations.get(i).ambient, 1, lights.get(i).getAmbient().get(fb4));
      gl.glUniform3fv(lightLocations.get(i).diffuse, 1, lights.get(i).getDiffuse().get(fb4));
      gl.glUniform3fv(lightLocations.get(i).specular, 1, lights.get(i).getSpecular().get(fb4));
    }

    gl.glEnable(GL.GL_TEXTURE_2D);
    gl.glActiveTexture(GL.GL_TEXTURE0);



    for (int i = 0; i < meshObjects.size(); i++) {
      Matrix4f transformation = new Matrix4f().mul(modelView).mul(trackballTransform).mul(transforms.get(i));
      Matrix4f normalmatrix = new Matrix4f(transformation);
      normalmatrix = normalmatrix.invert().transpose();
      gl.glUniformMatrix4fv(shaderLocations.getLocation("modelview"), 1, false,
              transformation.get(fb16));
      gl.glUniformMatrix4fv(shaderLocations.getLocation("normalmatrix"), 1,
              false, normalmatrix.get(fb16));

      gl.glUniform3fv(shaderLocations.getLocation("material.ambient"), 1,
              materials.get(i).getAmbient().get(fb4));
      gl.glUniform3fv(shaderLocations.getLocation("material.diffuse"), 1,
              materials.get(i).getDiffuse().get(fb4));
      gl.glUniform3fv(shaderLocations.getLocation("material.specular"), 1,
              materials.get(i).getSpecular().get(fb4));
      gl.glUniform1f(shaderLocations.getLocation("material.shininess"),
              materials.get(i).getShininess());

      meshObjects.get(i).draw(gla);
    }
    gl.glFlush();

    program.disable(gl);


  }

  public void mousePressed(int x, int y) {
    mousePos = new Vector2f(x, y);
  }

  public void mouseReleased(int x, int y) {
    System.out.println("Released");
  }

  public void mouseDragged(int x, int y) {
    Vector2f newM = new Vector2f(x, y);

    Vector2f delta = new Vector2f(newM.x - mousePos.x, newM.y - mousePos.y);
    mousePos = new Vector2f(newM);

    trackballTransform = new Matrix4f().rotate(delta.x / trackballRadius, 0, 1, 0)
            .rotate(delta.y / trackballRadius, 1, 0, 0)
            .mul(trackballTransform);
  }

  public void reshape(GLAutoDrawable gla, int x, int y, int width, int height) {
    GL gl = gla.getGL();
    WINDOW_WIDTH = width;
    WINDOW_HEIGHT = height;
    gl.glViewport(0, 0, width, height);

    //proj = new Matrix4f().perspective((float)Math.toRadians(120.0f),(float)
    //        width/height,0.1f,10000.0f);
    proj = new Matrix4f().ortho(-50, 50, -50, 50, -50.0f, 10000.0f);

  }

  public void dispose(GLAutoDrawable gla) {
    GL3 gl = gla.getGL().getGL3();

  }


}
