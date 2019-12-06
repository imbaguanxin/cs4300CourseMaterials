package sgraph;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.texture.Texture;

import org.joml.Matrix4f;

import util.IVertexData;
import util.Light;
import util.TextureImage;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.*;

/**
 * This is a scene graph renderer implementation that works specifically with
 * the JOGL library It mandates OpenGL 3 and above.
 *
 * @author Amit Shesh
 */
public class GL3ScenegraphRenderer implements IScenegraphRenderer {
  /**
   * The JOGL specific rendering context
   */
  protected GLAutoDrawable glContext;
  /**
   * A table of shader locations and variable names
   */
  protected util.ShaderLocationsVault shaderLocations;
  /**
   * A table of shader variables -> vertex attribute names in each mesh
   */
  protected Map<String, String> shaderVarsToVertexAttribs;

  /**
   * A map to store all the textures
   */
  protected Map<String, TextureImage> textures;
  /**
   * A table of renderers for individual meshes
   */
  protected Map<String, util.ObjectInstance> meshRenderers;

  /**
   * A variable tracking whether shader locations have been set. This must be
   * done before drawing!
   */
  private boolean shaderLocationsSet;

  public GL3ScenegraphRenderer() {
    meshRenderers = new HashMap<String, util.ObjectInstance>();
    shaderLocations = new util.ShaderLocationsVault();
    shaderLocationsSet = false;
    textures = new HashMap<String,TextureImage>();
  }

  /**
   * Specifically checks if the passed rendering context is the correct
   * JOGL-specific rendering context {@link com.jogamp.opengl.GLAutoDrawable}
   *
   * @param obj the rendering context (should be {@link com.jogamp.opengl.GLAutoDrawable})
   * @throws IllegalArgumentException if given rendering context is not {@link
   *                                  com.jogamp.opengl.GLAutoDrawable}
   */
  @Override
  public void setContext(Object obj) throws IllegalArgumentException {
    if (obj instanceof GLAutoDrawable) {
      glContext = (GLAutoDrawable) obj;
      GL3 gl = glContext.getGL().getGL3();
      for (TextureImage texi : textures.values()) {
        Texture tex = texi.getTexture();
        tex.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
        tex.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
        tex.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        tex.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
      }
    } else
      throw new IllegalArgumentException("Context not of type GLAutoDrawable");
  }

  /**
   * Add a mesh to be drawn later. The rendering context should be set before
   * calling this function, as this function needs it This function creates a
   * new {@link util.ObjectInstance} object for this mesh
   *
   * @param name the name by which this mesh is referred to by the scene graph
   * @param mesh the {@link util.PolygonMesh} object that represents this mesh
   */
  @Override
  public <K extends IVertexData> void addMesh(String name, util.PolygonMesh<K> mesh) throws Exception {
    if (!shaderLocationsSet)
      throw new Exception("Attempting to add mesh before setting shader variables. Call initShaderProgram first");
    if (glContext == null)
      throw new Exception("Attempting to add mesh before setting GL context. Call setContext and pass it a GLAutoDrawable first.");

    if (meshRenderers.containsKey(name))
      return;

    //verify that the mesh has all the vertex attributes as specified in the map
    if (mesh.getVertexCount() <= 0)
      return;
    K vertexData = mesh.getVertexAttributes().get(0);
    GL3 gl = glContext.getGL().getGL3();

    for (Map.Entry<String, String> e : shaderVarsToVertexAttribs.entrySet()) {
      if (!vertexData.hasData(e.getValue()))
        throw new IllegalArgumentException("Mesh does not have vertex attribute " + e.getValue());
    }
    util.ObjectInstance obj = new util.ObjectInstance(gl,
            shaderLocations, shaderVarsToVertexAttribs, mesh, name);

    meshRenderers.put(name, obj);
  }

  @Override
  public void addTexture(String name, String path) {
    if (textures.containsKey(name))
      return;

    TextureImage image = null;
    String imageFormat = path.substring(path.indexOf('.') + 1);
    try {
      image = new TextureImage(path, imageFormat, name);
    } catch (IOException e) {
      throw new IllegalArgumentException("Texture " + path + " cannot be read!");
    }
    if (glContext!=null) {
      Texture tex = image.getTexture();
      GL3 gl = glContext.getGL().getGL3();
      tex.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
      tex.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
      tex.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
      tex.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
    }
    textures.put(name, image);
  }

  /**
   * Begin rendering of the scene graph from the root
   */
  @Override
  public void draw(INode root, Stack<Matrix4f> modelView) {
    GL3 gl = glContext.getGL().getGL3();

    List<Light> listOfLights = root.getLightsInView(modelView);
    initLightsInShader(listOfLights);

    gl.glEnable(GL.GL_TEXTURE_2D);
    gl.glActiveTexture(GL.GL_TEXTURE0);
    int loc = -1;
    loc = shaderLocations.getLocation("image");
    if (loc >= 0) {
      gl.glUniform1i(loc, 0);
    }
    root.draw(this, modelView);
  }

  @Override
  public void dispose() {
    for (util.ObjectInstance s : meshRenderers.values())
      s.cleanup(glContext);
  }

  public void initLightsInShader(List<Light> lights) {
    int loc = -1;

    Objects.requireNonNull(glContext);

    GL3 gl = glContext.getGL().getGL3();

    FloatBuffer fb = Buffers.newDirectFloatBuffer(4);

    loc = shaderLocations.getLocation("numLights");
    if (loc >= 0) {
      gl.glUniform1i(loc, lights.size());
    } else {
      throw new IllegalArgumentException("No shader variable for \" numLights \"");
    }

    for (int i = 0; i < lights.size(); i++) {
      String name = "light[" + i + "].";
      loc = shaderLocations.getLocation(name + "ambient");
      if (loc >= 0) {
        gl.glUniform3fv(loc, 1, lights.get(i).getAmbient().get(fb));
      } else {
        throw new IllegalArgumentException("No shader variable for \" " + name + "ambient" + " \"");
      }

      loc = shaderLocations.getLocation(name + "diffuse");
      if (loc >= 0) {
        gl.glUniform3fv(loc, 1, lights.get(i).getDiffuse().get(fb));
      } else {
        throw new IllegalArgumentException("No shader variable for \" " + name + "diffuse" + " \"");
      }

      loc = shaderLocations.getLocation(name + "specular");
      if (loc >= 0) {
        gl.glUniform3fv(loc, 1, lights.get(i).getSpecular().get(fb));
      } else {
        throw new IllegalArgumentException("No shader variable for \" " + name + "specular" + " \"");
      }

      loc = shaderLocations.getLocation(name + "position");
      if (loc >= 0) {
        gl.glUniform4fv(loc, 1, lights.get(i).getPosition().get(fb));
      } else {
        throw new IllegalArgumentException("No shader variable for \" " + name + "position" + " \"");
      }


      loc = shaderLocations.getLocation(name + "spotdirection");
      if (loc >= 0) {
        gl.glUniform4fv(loc, 1, lights.get(i).getSpotDirection().get
                (fb));
      } else {
        throw new IllegalArgumentException("No shader variable for \" " + name + "spotdirection" + " \"");
      }

      loc = shaderLocations.getLocation(name + "cosSpotCutoff");
      if (loc >= 0) {
        gl.glUniform1f(loc, (float) Math.cos(Math.toRadians(lights
                .get(i)
                .getSpotCutoff())));
      } else {
        throw new IllegalArgumentException("No shader variable for \" " + name + "cosSpotCutoff" + " \"");
      }
    }


  }

  /**
   * Draws a specific mesh. If the mesh has been added to this renderer, it
   * delegates to its correspond mesh renderer This function first passes the
   * material to the shader. Currently it uses the shader variable "vColor" and
   * passes it the ambient part of the material. When lighting is enabled, this
   * method must be overriden to set the ambient, diffuse, specular, shininess
   * etc. values to the shader
   */
  @Override
  public void drawMesh(String name, util.Material material, String textureName, final Matrix4f transformation) {
    if (meshRenderers.containsKey(name)) {
      GL3 gl = glContext.getGL().getGL3();
      //get the color

      FloatBuffer fb4 = Buffers.newDirectFloatBuffer(4);
      FloatBuffer fb16 = Buffers.newDirectFloatBuffer(16);

      int loc = -1;

      loc = shaderLocations.getLocation("material.ambient");
      if (loc >= 0) {
        gl.glUniform3fv(loc, 1, material.getAmbient().get(fb4));
      } else {
        throw new IllegalArgumentException("No shader variable for \" material.ambient \"");
      }

      loc = shaderLocations.getLocation("material.diffuse");
      if (loc >= 0) {
        gl.glUniform3fv(loc, 1, material.getDiffuse().get(fb4));
      } else {
        throw new IllegalArgumentException("No shader variable for \" material.diffuse \"");
      }

      if (loc >= 0) {
        loc = shaderLocations.getLocation("material.specular");
        gl.glUniform3fv(loc, 1, material.getSpecular().get(fb4));
      } else {
        throw new IllegalArgumentException("No shader variable for \" material.specular \"");
      }

      loc = shaderLocations.getLocation("material.shininess");
      if (loc >= 0) {
        gl.glUniform1f(loc, material.getShininess());
      } else {
        throw new IllegalArgumentException("No shader variable for \" material.shininess \"");
      }

      loc = shaderLocations.getLocation("modelview");
      if (loc >= 0) {
        gl.glUniformMatrix4fv(loc, 1, false, transformation.get(fb16));
      }
      else {
        throw new IllegalArgumentException("No shader variable for \" modelview \"");
      }

      loc = shaderLocations.getLocation("normalmatrix");
      if (loc>=0) {
        Matrix4f normalmatrix = new Matrix4f(transformation).invert().transpose();
        gl.glUniformMatrix4fv(loc,1,false,normalmatrix.get(fb16));
      }
      else {
        throw new IllegalArgumentException("No shader variable for \" normalmatrix \"");
      }



      if (textures.containsKey(textureName))
        textures.get(textureName).getTexture().bind(gl);
      else if (textures.containsKey("white"))
        textures.get("white").getTexture().bind(gl);

      meshRenderers.get(name).draw(glContext);
    }
  }

  /**
   * Queries the shader program for all variables and locations, and adds them
   * to itself
   */
  @Override
  public void initShaderProgram(util.ShaderProgram shaderProgram, Map<String, String> shaderVarsToVertexAttribs) {
    Objects.requireNonNull(glContext);
    GL3 gl = glContext.getGL().getGL3();

    shaderLocations = shaderProgram.getAllShaderVariables(gl);
    this.shaderVarsToVertexAttribs = new HashMap<String, String>(shaderVarsToVertexAttribs);
    shaderLocationsSet = true;
  }


  @Override
  public int getShaderLocation(String name) {
    return shaderLocations.getLocation(name);
  }
}