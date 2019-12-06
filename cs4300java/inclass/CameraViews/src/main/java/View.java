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
import java.util.Stack;


/**
 * Created by ashesh on 9/18/2015.
 *
 * The View class is the "controller" of all our OpenGL stuff. It cleanly
 * encapsulates all our OpenGL functionality from the rest of Java GUI, managed
 * by the JOGLFrame class.
 */
public class View {
  private enum TypeOfCamera {GLOBAL,FPS};
  private enum ObjectName {JACK_BOX_FACE,JACK_BOX_CAP,FLOOR,WALL_BACK,WALL_FRONT,WALL_LEFT,WALL_RIGHT,RED_ORB,YELLOW_CUBE,PURPLE_CUBE,NEPTUNE,AEROPLANE}
  private int WINDOW_WIDTH,WINDOW_HEIGHT;
  private Matrix4f proj;
  private Stack<Matrix4f> modelView;
  private Map<ObjectName,ObjectInstance> meshObjects;
  private Map<ObjectName,Matrix4f> meshTransforms,animationTransforms;
  private Map<ObjectName,util.Material> meshMaterials;

  private util.ShaderProgram program;
  private util.ShaderLocationsVault shaderLocations;
  private float time;
  TypeOfCamera cameraMode;




  public View() {
    proj = new Matrix4f();
    proj.identity();

    modelView = new Stack<Matrix4f>();

    meshObjects = new HashMap<ObjectName,ObjectInstance>();
    meshTransforms = new HashMap<ObjectName,Matrix4f>();
    animationTransforms = new HashMap<ObjectName,Matrix4f>();
    meshMaterials = new HashMap<ObjectName,util.Material>();

    time = 0;
    cameraMode = TypeOfCamera.GLOBAL;
  }

  private void initObjects(GL3 gl) throws FileNotFoundException
  {
    util.PolygonMesh mesh;
    util.ObjectInstance o;
    Matrix4f transform;
    util.Material mat;
    VertexProducer<?> vertexProducer = new VertexAttribProducer();

    InputStream in;

    Map<String, String> shaderToVertexAttribute = new HashMap<String, String>();

    //currently there is only one per-vertex attribute: position
    shaderToVertexAttribute.put("vPosition", "position");


    mat =  new util.Material();

    //floor
    in = new FileInputStream("models/box.obj");
    mesh = util.ObjImporter.importFile(vertexProducer,in,false);
    o = new util.ObjectInstance(gl,
            program,
            shaderLocations,
            shaderToVertexAttribute,
            mesh,new String("floor"));
    mat.setAmbient(0,0,1); //only this one is used currently to determine color
    mat.setDiffuse(0,0,1);
    mat.setSpecular(0,0,1);

    meshObjects.put(ObjectName.FLOOR,o);
    meshMaterials.put(ObjectName.FLOOR,new util.Material(mat));
    transform = new Matrix4f().translate(0,-1,0)
            .scale(500,2,500);
    meshTransforms.put(ObjectName.FLOOR,transform);
    animationTransforms.put(ObjectName.FLOOR,new Matrix4f());


    //back wall
    in = new FileInputStream("models/box.obj");
    mesh = util.ObjImporter.importFile(vertexProducer,in,true);
    o = new util.ObjectInstance(gl,
            program,
            shaderLocations,
            shaderToVertexAttribute,
            mesh,new String("back wall"));
    mat.setAmbient(1,0,0); //only this one is used currently to determine color
    mat.setDiffuse(1,0,0);
    mat.setSpecular(1,0,0);
    meshMaterials.put(ObjectName.WALL_BACK,new util.Material(mat));
    meshObjects.put(ObjectName.WALL_BACK,o);
    transform = new Matrix4f().translate(0,150,-250)
            .scale(500,300,2);
    meshTransforms.put(ObjectName.WALL_BACK,transform);
    animationTransforms.put(ObjectName.WALL_BACK,new Matrix4f());

    //front wall
    in = new FileInputStream("models/box.obj");
    mesh = util.ObjImporter.importFile(vertexProducer,in,true);
    o = new util.ObjectInstance(gl,
            program,
            shaderLocations,
            shaderToVertexAttribute,
            mesh,new String("front wall"));
    mat.setAmbient(0,1,0); //only this one is used currently to determine color
    mat.setDiffuse(0,1,0);
    mat.setSpecular(0,1,0);
    meshMaterials.put(ObjectName.WALL_FRONT,new util.Material(mat));
    meshObjects.put(ObjectName.WALL_FRONT,o);
    transform = new Matrix4f().translate(0,150,250)
            .scale(500,300,2);
    meshTransforms.put(ObjectName.WALL_FRONT,transform);
    animationTransforms.put(ObjectName.WALL_FRONT,new Matrix4f());

    //left wall
    in = new FileInputStream("models/box.obj");
    mesh = util.ObjImporter.importFile(vertexProducer,in,true);
    o = new util.ObjectInstance(gl,
            program,
            shaderLocations,
            shaderToVertexAttribute,
            mesh,new String("left wall"));
    mat.setAmbient(1,0.5f,0); //only this one is used currently to determine color
    mat.setDiffuse(1,0.5f,0);
    mat.setSpecular(1,0.5f,0);
    meshMaterials.put(ObjectName.WALL_LEFT,new util.Material(mat));
    meshObjects.put(ObjectName.WALL_LEFT,o);
    transform = new Matrix4f().translate(-250,150,0)
            .scale(2,300,500);
    meshTransforms.put(ObjectName.WALL_LEFT,transform);
    animationTransforms.put(ObjectName.WALL_LEFT,new Matrix4f());

    //right wall
    in = new FileInputStream("models/box.obj");
    mesh = util.ObjImporter.importFile(vertexProducer,in,true);
    o = new util.ObjectInstance(gl,
            program,
            shaderLocations,
            shaderToVertexAttribute,
            mesh,new String("right wall"));
    mat.setAmbient(1,0,0.5f); //only this one is used currently to determine color
    mat.setDiffuse(1,0,0.5f);
    mat.setSpecular(1,0,0.5f);
    meshMaterials.put(ObjectName.WALL_RIGHT,new util.Material(mat));
    meshObjects.put(ObjectName.WALL_RIGHT,o);
    transform = new Matrix4f().translate(250,150,0)
            .scale(2,300,500);
    meshTransforms.put(ObjectName.WALL_RIGHT,transform);
    animationTransforms.put(ObjectName.WALL_RIGHT,new Matrix4f());


    //jack in the box face
    in = new FileInputStream("models/sphere.obj");
    mesh = util.ObjImporter.importFile(vertexProducer,in,false);
    o = new util.ObjectInstance(gl,
            program,
            shaderLocations,
            shaderToVertexAttribute,
            mesh,new String("sphere"));
    mat.setAmbient(1,0,0); //only this one is used currently to determine color
    mat.setDiffuse(1,0,0);
    mat.setSpecular(1,0,0);
    meshMaterials.put(ObjectName.JACK_BOX_FACE,new util.Material(mat));
    meshObjects.put(ObjectName.JACK_BOX_FACE,o);
    transform = new Matrix4f().translate(0,30,0).scale(50,30,50);
    meshTransforms.put(ObjectName.JACK_BOX_FACE,transform);
    animationTransforms.put(ObjectName.JACK_BOX_FACE,new Matrix4f());

    //jack in the box cap
    in = new FileInputStream("models/cone.obj");
    mesh = util.ObjImporter.importFile(vertexProducer,in,false);
    o = new util.ObjectInstance(gl,
            program,
            shaderLocations,
            shaderToVertexAttribute,
            mesh,new String("cone"));
    mat.setAmbient(0,1,0); //only this one is used currently to determine color
    mat.setDiffuse(0,1,0);
    mat.setSpecular(0,1,0);
    meshMaterials.put(ObjectName.JACK_BOX_CAP,new util.Material(mat));
    meshObjects.put(ObjectName.JACK_BOX_CAP,o);
    transform = new Matrix4f().translate(0,60,0)
            .scale(20,100,20);
    meshTransforms.put(ObjectName.JACK_BOX_CAP,transform);
    animationTransforms.put(ObjectName.JACK_BOX_CAP,new Matrix4f());


    //aeroplane

    in = new FileInputStream("models/aeroplane.obj");
    mesh = util.ObjImporter.importFile(vertexProducer,in,true);
    Vector4f maxB = mesh.getMaximumBounds();
    o = new util.ObjectInstance(gl,
            program,
            shaderLocations,
            shaderToVertexAttribute,
            mesh,new String("aeroplane"));
    mat.setAmbient(1,1,0); //only this one is used currently to determine color
    mat.setDiffuse(1,1,0);
    mat.setSpecular(1,1,0);
    meshMaterials.put(ObjectName.AEROPLANE,new util.Material(mat));
    meshObjects.put(ObjectName.AEROPLANE,o);
    transform = new Matrix4f()
            .rotate((float)Math.toRadians(90),1,0,0)
            .scale(100,100,100)
            .rotate((float)Math.toRadians(180),0,1,0)
            .translate(0,-maxB.y,0);
    meshTransforms.put(ObjectName.AEROPLANE,transform);
    animationTransforms.put(ObjectName.AEROPLANE,new Matrix4f());

    //yellow cube

    in = new FileInputStream("models/box.obj");
    mesh = util.ObjImporter.importFile(vertexProducer,in,false);
    o = new util.ObjectInstance(gl,
            program,
            shaderLocations,
            shaderToVertexAttribute,
            mesh,new String("yellow box"));
    mat.setAmbient(1,1,0); //only this one is used currently to determine color
    mat.setDiffuse(1,1,0);
    mat.setSpecular(1,1,0);
    meshMaterials.put(ObjectName.YELLOW_CUBE,new util.Material(mat));
    meshObjects.put(ObjectName.YELLOW_CUBE,o);
    transform = new Matrix4f().scale(20,20,20);
    meshTransforms.put(ObjectName.YELLOW_CUBE,transform);
    animationTransforms.put(ObjectName.YELLOW_CUBE,new Matrix4f());

    //purple cube
    in = new FileInputStream("models/box.obj");
    mesh = util.ObjImporter.importFile(vertexProducer,in,false);
    o = new util.ObjectInstance(gl,
            program,
            shaderLocations,
            shaderToVertexAttribute,
            mesh,new String("purple box"));
    mat.setAmbient(1,0,1); //only this one is used currently to determine color
    mat.setDiffuse(1,0,1);
    mat.setSpecular(1,0,1);
    meshMaterials.put(ObjectName.PURPLE_CUBE,new util.Material(mat));
    meshObjects.put(ObjectName.PURPLE_CUBE,o);
    transform = new Matrix4f().scale(20,20,20);
    meshTransforms.put(ObjectName.PURPLE_CUBE,transform);
    animationTransforms.put(ObjectName.PURPLE_CUBE,new Matrix4f());

    //red orb
    in = new FileInputStream("models/sphere.obj");
    mesh = util.ObjImporter.importFile(vertexProducer,in,false);
    o = new util.ObjectInstance(gl,
            program,
            shaderLocations,
            shaderToVertexAttribute,
            mesh,new String("red orb"));
    mat.setAmbient(1,0,0); //only this one is used currently to determine color
    mat.setDiffuse(1,0,0);
    mat.setSpecular(1,0,0);
    meshMaterials.put(ObjectName.RED_ORB,new util.Material(mat));
    meshObjects.put(ObjectName.RED_ORB,o);
    transform = new Matrix4f().scale(20,20,20);
    meshTransforms.put(ObjectName.RED_ORB,transform);
    animationTransforms.put(ObjectName.RED_ORB,new Matrix4f());

    //neptune
    in = new FileInputStream("models/neptune.obj");
    mesh = util.ObjImporter.importFile(vertexProducer,in,true);
    o = new util.ObjectInstance(gl,
            program,
            shaderLocations,
            shaderToVertexAttribute,
            mesh,new String("neptune"));
    mat.setAmbient(1,0,1); //only this one is used currently to determine color
    mat.setDiffuse(1,0,1);
    mat.setSpecular(1,0,1);
    meshMaterials.put(ObjectName.NEPTUNE,new util.Material(mat));
    meshObjects.put(ObjectName.NEPTUNE,o);
    //the translation correction in y is because the base of the nepture model is not exactly horizontal
    transform = new Matrix4f().scale(80,80,80).translate(0,-mesh.getMinimumBounds().y+0.1f,0);
    meshTransforms.put(ObjectName.NEPTUNE,transform);
    animationTransforms.put(ObjectName.NEPTUNE,new Matrix4f());
  }

  public void init(GLAutoDrawable gla) throws Exception {
    GL3 gl = gla.getGL().getGL3();




    //compile and make our shader program. Look at the ShaderProgram class for details on how this is done
    program = new util.ShaderProgram();
    program.createProgram(gl,"shaders/default.vert","shaders/default.frag");

    shaderLocations = program.getAllShaderVariables(gl);

    initObjects(gl);


  }


  public void draw(GLAutoDrawable gla) {
    GL3 gl = gla.getGL().getGL3();

    gl.glClearColor(0,0,0, 0);
    gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);
    gl.glEnable(GL.GL_DEPTH_TEST);

    time +=0.01;
    animate();

    program.enable(gl);

    while (!modelView.empty())
      modelView.pop();

    modelView.push(new Matrix4f());

 // System.out.println("time="+time);
  //  proj = new Matrix4f().ortho(-400+100*time,400,-400,400,0.1f,
   //         10000.0f);

    if (cameraMode == TypeOfCamera.GLOBAL)
      modelView.peek().lookAt(
              new Vector3f(0,600,600)
              ,new Vector3f(0,0,0)
              ,new Vector3f(0,1,0));
    else {
      modelView.peek().lookAt(
              new Vector3f(0,0,-50)
              ,new Vector3f(0,0,500)
              ,new Vector3f(0,1,0))
               .mul(new Matrix4f(animationTransforms.get(ObjectName.AEROPLANE)).invert());

    }


    FloatBuffer fb = Buffers.newDirectFloatBuffer(16);
    FloatBuffer mfb = Buffers.newDirectFloatBuffer(4);

    gl.glUniformMatrix4fv(shaderLocations.getLocation("projection"),
            1,false,
            proj.get(fb));
    //return;


    gl.glPolygonMode(GL.GL_FRONT_AND_BACK,GL3.GL_LINE); //OUTLINES



    for (ObjectName i:ObjectName.values())
    {
      modelView.push(new Matrix4f(modelView.peek())); //save the current modelview
      modelView.peek().mul(animationTransforms.get(i)).mul(meshTransforms.get(i));

      //The total transformation is whatever was passed to it, with its own transformation
      gl.glUniformMatrix4fv(shaderLocations.getLocation("modelview"),
              1,false,
              modelView.peek().get(fb));
      //set the color for all vertices to be drawn for this object
      gl.glUniform4fv(shaderLocations.getLocation("vColor"),
              1,meshMaterials.get(i).getAmbient().get(fb));
      meshObjects.get(i).draw(gla);
      modelView.pop();
    }
    modelView.pop();

    /*
     *OpenGL batch-processes all its OpenGL commands.
          *  *The next command asks OpenGL to "empty" its batch of issued commands, i.e. draw
     *
     *This a non-blocking function. That is, it will signal OpenGL to draw, but won't wait for it to
     *finish drawing.
     *
     *If you would like OpenGL to start drawing and wait until it is done, call glFinish() instead.
     */
    gl.glFlush();

    program.disable(gl);



  }

  private void animate()
  {
    //the air plane: index 3
    animationTransforms.put(ObjectName.AEROPLANE
            ,new Matrix4f()
                    .rotate((float)Math.toRadians(-50.0f*time),0,1,0)
                    .translate(250,50,0)
                    .rotate((float)Math.toRadians(550.0f*time),0,0,1));


    //the cube: index 4
    animationTransforms.put(ObjectName.YELLOW_CUBE,new Matrix4f().translate(-200.0f,50.0f,-200.0f).rotate((float)Math.toRadians((float)-time*50),0.0f,1.0f,0.0f));

    //the cube: index 5
    animationTransforms.put(ObjectName.PURPLE_CUBE,new Matrix4f().translate(200,50.0f,200.0f).rotate((float)Math.toRadians((float)time*75),1.0f,1.0f,0.0f));

    //the sphere: index 6
    animationTransforms.put(ObjectName.RED_ORB,new Matrix4f().translate(100.0f,80.0f,(float)(100*Math.sin(time))));


    //the neptune model: index 7
    animationTransforms.put(ObjectName.NEPTUNE,new Matrix4f().translate(-200,0.0f,200.0f).rotate((float)Math.toRadians((float)time*50),0.0f,1.0f,0.0f));
  }

  public void setFPS()
  {
    cameraMode = TypeOfCamera.FPS;
  }

  public void setGlobal()
  {
    cameraMode = TypeOfCamera.GLOBAL;
  }

  public void reshape(GLAutoDrawable gla,int x,int y,int width,int height) {
    GL gl = gla.getGL();
    WINDOW_WIDTH = width;
    WINDOW_HEIGHT = height;
    gl.glViewport(0, 0, width, height);

    proj = new Matrix4f().perspective((float) Math.toRadians(60.0f), (float)
            width / height, 0.1f, 10000.0f);
    // proj = new Matrix4f().ortho(-400,400,-400,400,0.1f,10000.0f);
  }

  public void dispose(GLAutoDrawable gla) {
    for (Map.Entry<ObjectName,ObjectInstance> e:meshObjects.entrySet()) {
      e.getValue().cleanup(gla);
    }
  }
}
