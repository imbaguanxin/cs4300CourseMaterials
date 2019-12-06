import com.jogamp.graph.curve.Region;
import com.jogamp.graph.curve.opengl.RegionRenderer;
import com.jogamp.graph.curve.opengl.RenderState;
import com.jogamp.graph.curve.opengl.TextRegionUtil;
import com.jogamp.graph.font.Font;
import com.jogamp.graph.font.FontFactory;
import com.jogamp.graph.geom.SVertex;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.util.AnimatorBase;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.PMVMatrix;

import javax.swing.*;

import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
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
    setSize(500, 500); //this opens a 400x400 window
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //when X is pressed, close program

    //Our View class is the actual driver of the OpenGL stuff
    view = new View();

    GLProfile glp = GLProfile.getMaxProgrammable(true);
    GLCapabilities caps = new GLCapabilities(glp);
    caps.setDepthBits(24);
    canvas = new GLCanvas(caps);

    add(canvas);


    //capture mouse events
    MyMouseAdapter mouseAdapter = new MyMouseAdapter();

    canvas.addMouseListener(mouseAdapter);
    canvas.addMouseMotionListener(mouseAdapter);
    canvas.addKeyListener(new KeyboardListener());

    canvas.addGLEventListener(new GLEventListener() {
      @Override
      public void init(GLAutoDrawable glAutoDrawable) { //called the first time this canvas is created. Do your initialization here
        try {
          view.init(glAutoDrawable);
          textRenderer = new MyTextRenderer(glAutoDrawable);
          InputStream in = getClass().getClassLoader()
                  .getResourceAsStream
                          ("scenegraphmodels/face-hierarchy.xml");
          view.initScenegraph(glAutoDrawable, in);

          glAutoDrawable.getGL().setSwapInterval(0);

        } catch (Exception e) {
          JOptionPane.showMessageDialog(JOGLFrame.this, e.getMessage(), "Error while loading", JOptionPane.ERROR_MESSAGE);
          System.exit(1);
        }
      }

      @Override
      public void dispose(GLAutoDrawable glAutoDrawable) { //called when the canvas is destroyed.
        view.dispose(glAutoDrawable);
      }

      @Override
      public void display(GLAutoDrawable glAutoDrawable) { //called every time this window must be redrawn
        view.draw(canvas);
        //draw text
        String text = "Frame rate: " + canvas.getAnimator().getLastFPS();
        textRenderer.drawText(glAutoDrawable, text, 10, canvas.getSurfaceHeight() - 50, 1, 0, 0, 20.0f);
      }

      @Override
      public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) { //called every time this canvas is resized
        view.reshape(glAutoDrawable, x, y, width, height);
        textRenderer.reshape(glAutoDrawable, canvas.getSurfaceWidth(), canvas.getSurfaceHeight());
        repaint(); //refresh window
      }
    });

    //Add an animator to the canvas
    AnimatorBase animator = new FPSAnimator(canvas, 60);
    animator.setUpdateFPSFrames(50, null);
    animator.start();
  }

  private class KeyboardListener implements KeyListener {

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
  }

  private class MyMouseAdapter extends MouseAdapter {
    @Override
    public void mousePressed(MouseEvent e) {
      if (e.getButton() == MouseEvent.BUTTON1)
        JOGLFrame.this.view.mousePressed(e.getX(), e.getY());

    }

    @Override
    public void mouseReleased(MouseEvent e) {
      if (e.getButton() == MouseEvent.BUTTON1)
        JOGLFrame.this.view.mouseReleased(e.getX(), e.getY());
    }

    @Override
    public void mouseDragged(MouseEvent e) {
      JOGLFrame.this.view.mouseDragged(e.getX(), e.getY());
      JOGLFrame.this.canvas.repaint();
    }
  }

  private class MyTextRenderer {
    private TextRegionUtil textRegionUtil;
    private RenderState renderState;
    private RegionRenderer regionRenderer;
    private Font font;
    private int fontSet = FontFactory.JAVA;
    /* 2nd pass texture size for antialiasing. Samplecount = 4 is usuallly enough */
    private final int[] sampleCount = {4};
    //vao for the curve text renderer. This is because of a bug in the JOGL curve text rendering
    private IntBuffer textVAO; //text renderer VAO

    public MyTextRenderer(GLAutoDrawable glAutoDrawable) throws IOException {
      GL3 gl = glAutoDrawable.getGL().getGL3();

      //set up the text rendering
      textVAO = IntBuffer.allocate(1);
      gl.glGenVertexArrays(1, textVAO);
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

    public void drawText(GLAutoDrawable glAutoDrawable, String text, int x, int y, float r, float g, float b, float fontSize) {
      GL3 gl = glAutoDrawable.getGL().getGL3();

      gl.glClear(gl.GL_DEPTH_BUFFER_BIT);
      gl.glEnable(gl.GL_DEPTH_TEST);
      //draw the shape using RegionRenderer and TextREgionUtil
      //The RegionRenderer PMVMatrix helps us to place and size the text
      if (!regionRenderer.isInitialized()) {
        regionRenderer.init(gl, Region.VBAA_RENDERING_BIT);

      }
      final PMVMatrix pmv = regionRenderer.getMatrix();
      pmv.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
      pmv.glLoadIdentity();
      pmv.glTranslatef(x, y, -999.0f);

      regionRenderer.enable(gl, true);
      gl.glBindVertexArray(textVAO.get(0));
      renderState.setColorStatic(r, g, b, 1.0f);
      textRegionUtil.drawString3D(gl, regionRenderer, font, fontSize, text, null, sampleCount);
      gl.glBindVertexArray(0);
      regionRenderer.enable(gl, false);
    }

    public void dispose(GLAutoDrawable glAutoDrawable) {
      GL3 gl = glAutoDrawable.getGL().getGL3();
      gl.glDeleteVertexArrays(1, textVAO);

    }

    public void reshape(GLAutoDrawable glAutoDrawable, int width, int height) {
      GL3 gl = glAutoDrawable.getGL().getGL3();
      regionRenderer.enable(gl, true);
      regionRenderer.reshapeOrtho(width, height, 0.1f, 1000.0f);
      regionRenderer.enable(gl, false);
    }


  }


}
