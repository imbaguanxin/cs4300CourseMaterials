import com.jogamp.graph.curve.Region;
import com.jogamp.graph.curve.opengl.RegionRenderer;
import com.jogamp.graph.curve.opengl.RenderState;
import com.jogamp.graph.curve.opengl.TextRegionUtil;
import com.jogamp.graph.font.FontFactory;
import com.jogamp.graph.geom.SVertex;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.AnimatorBase;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.PMVMatrix;
import com.jogamp.opengl.util.awt.TextRenderer;

import javax.swing.*;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.nio.IntBuffer;

/**
 * Created by ashesh on 9/18/2015.
 */
public class JOGLFrame extends JFrame {
  private View view;
  private MyTextRenderer textRenderer;
  private GLCanvas canvas;

  public JOGLFrame(String title) {
    //routine JFrame setting stuff
    super(title);
    setSize(600, 600); //this opens a 600x600 window
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //when X is pressed, close program

    //Our View class is the actual driver of the OpenGL stuff
    view = new View();

    GLProfile glp = GLProfile.getGL2GL3();
    GLCapabilities caps = new GLCapabilities(glp);
    canvas = new GLCanvas(caps);

    add(canvas);


    canvas.addGLEventListener(new GLEventListener() {
      @Override
      public void init(GLAutoDrawable glAutoDrawable) { //called the first time this canvas is created. Do your initialization here
        try {
          view.init(glAutoDrawable);
          textRenderer = new MyTextRenderer(glAutoDrawable);
          glAutoDrawable.getGL().setSwapInterval(0);
        } catch (Exception e) {
          JOptionPane.showMessageDialog(JOGLFrame.this, e.getMessage(), "Error while loading", JOptionPane.ERROR_MESSAGE);
        }
      }

      @Override
      public void dispose(GLAutoDrawable glAutoDrawable) { //called when the canvas is destroyed.
        view.dispose(glAutoDrawable);
        textRenderer.dispose(glAutoDrawable);
      }

      @Override
      public void display(GLAutoDrawable glAutoDrawable) { //called every time this window must be redrawn

        view.draw(glAutoDrawable);
        String text = "Frame rate: " + canvas.getAnimator().getLastFPS();
        textRenderer.drawText(glAutoDrawable,text, 10, canvas
                .getHeight() - 50,1,0,0,20.0f);
      }

      @Override
      public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) { //called every time this canvas is resized
        view.reshape(glAutoDrawable, x, y, width, height);
        textRenderer.reshape(glAutoDrawable,canvas.getSurfaceWidth(),canvas.getSurfaceHeight());
        repaint(); //refresh window
      }
    });

    //Add an animator to the canvas
    AnimatorBase animator = new FPSAnimator(canvas, 300);
    animator.setUpdateFPSFrames(100, null);
    animator.start();
  }

  private class MyTextRenderer {
    private TextRegionUtil textRegionUtil;
    private RenderState renderState;
    private RegionRenderer regionRenderer;
    private com.jogamp.graph.font.Font font;
    private int fontSet = FontFactory.JAVA;
    /* 2nd pass texture size for antialiasing. Samplecount = 4 is usuallly enough */
    private final int[] sampleCount = {4};
    //vao for the curve text renderer. This is because of a bug in the JOGL curve text rendering
    private IntBuffer textVAO; //text renderer VAO

    public MyTextRenderer(GLAutoDrawable glAutoDrawable) throws IOException {
      GL3 gl = glAutoDrawable.getGL().getGL3();

      //set up the text rendering
      textVAO = IntBuffer.allocate(1);
      gl.glGenVertexArrays(1,textVAO);
      gl.glBindVertexArray(textVAO.get(0));
      /**
       *  JogAmp FontFactory will load a true type font
       *
       *  fontSet = 0 loads
       *  jogamp.graph.font.fonts.ubunto found inside jogl-fonts-p0.jar
       *  http://jogamp.org/deployment/jogamp-current/atomic/jogl-fonts-p0.jar
       *
       *  fontSet = 1 loads LucidaBrightRegular from the JRE
       */
      font = FontFactory.get(fontSet).getDefault();

      //initialize OpenGL specific classes that know how to render the graph API shapes
      renderState = RenderState.createRenderState(SVertex.factory());
      //define a RED color to render our shape with
      renderState.setColorStatic(1.0f, 0.0f, 0.0f, 1.0f);
      renderState.setHintMask(RenderState.BITHINT_GLOBAL_DEPTH_TEST_ENABLED);
      regionRenderer = RegionRenderer.create(renderState, RegionRenderer.defaultBlendEnable, RegionRenderer.defaultBlendDisable);
      regionRenderer.init(gl, Region.MSAA_RENDERING_BIT);
      textRegionUtil = new TextRegionUtil(Region.MSAA_RENDERING_BIT);


    }

    public void drawText(GLAutoDrawable glAutoDrawable,String text,int x, int y,float r,float g,float b,float fontSize) {
      GL3 gl = glAutoDrawable.getGL().getGL3();

      gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
      gl.glEnable(gl.GL_DEPTH_TEST);
      //draw the shape using RegionRenderer and TextREgionUtil
      //The RegionRenderer PMVMatrix helps us to place and size the text
      if (!regionRenderer.isInitialized()) {
        regionRenderer.init(gl, Region.VBAA_RENDERING_BIT);

      }
      final PMVMatrix pmv = regionRenderer.getMatrix();
      pmv.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
      pmv.glLoadIdentity();
      pmv.glTranslatef(x,y,-999.0f);

      regionRenderer.enable(gl,true);
      gl.glBindVertexArray(textVAO.get(0));
      renderState.setColorStatic(r,g,b,1.0f);
      textRegionUtil.drawString3D(gl,regionRenderer,font,fontSize,text,null,sampleCount);
      gl.glBindVertexArray(0);
      regionRenderer.enable(gl,false);
    }

    public void dispose(GLAutoDrawable glAutoDrawable) {
      GL3 gl = glAutoDrawable.getGL().getGL3();
      gl.glDeleteVertexArrays(1,textVAO);

    }

    public void reshape(GLAutoDrawable glAutoDrawable,int width,int height) {
      GL3 gl = glAutoDrawable.getGL().getGL3();
      regionRenderer.enable(gl,true);
      regionRenderer.reshapeOrtho(width,height,0.1f,1000.0f);
      regionRenderer.enable(gl,false);
    }


  }


}
