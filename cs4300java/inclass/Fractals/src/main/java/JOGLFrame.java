import com.jogamp.graph.curve.Region;
import com.jogamp.graph.curve.opengl.RegionRenderer;
import com.jogamp.graph.curve.opengl.RenderState;
import com.jogamp.graph.curve.opengl.TextRegionUtil;
import com.jogamp.graph.font.Font;
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
import com.jogamp.opengl.util.PMVMatrix;
import com.jogamp.opengl.util.awt.TextRenderer;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.IntBuffer;

/**
 * Created by ashesh on 9/18/2015.
 */
public class JOGLFrame extends JFrame {
  private View view;
  private int mouseX, mouseY;
  private boolean mousePressed;
  private final GLCanvas canvas;
  private MyTextRenderer textRenderer;

  public JOGLFrame(String title) {
    //routine JFrame setting stuff
    super(title);
    setSize(600, 600); //this opens a 400x400 window
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //when X is pressed, close program

    //Our View class is the actual driver of the OpenGL stuff
    view = new View();

    GLProfile glp = GLProfile.getGL2GL3();
    GLCapabilities caps = new GLCapabilities(glp);
    canvas = new GLCanvas(caps);

    add(canvas);

    EventListener listener = new EventListener();

    canvas.addGLEventListener(listener);

    canvas.addMouseListener(listener);
    canvas.addMouseMotionListener(listener);
    canvas.addMouseWheelListener(listener);
    canvas.addKeyListener(listener);
    canvas.setFocusable(true);
    canvas.requestFocus();
  }

  class EventListener extends MouseAdapter implements GLEventListener, KeyListener {
    @Override
    public void init(GLAutoDrawable glAutoDrawable) { //called the first time this canvas is created. Do your initialization here
      try {
        view.init(glAutoDrawable);
        textRenderer = new MyTextRenderer(glAutoDrawable);
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
      textRenderer.drawText(glAutoDrawable,view.getFrameInfoString(),10,canvas
              .getSurfaceHeight()-50,1,1,0,20.0f);
      textRenderer.drawText(glAutoDrawable,view.getIterationInfoString(),10,canvas
              .getSurfaceHeight()-80,1,1,0,20.0f);

    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) { //called every time this canvas is resized
      view.reshape(glAutoDrawable, x, y, width, height);
      textRenderer.reshape(glAutoDrawable,canvas.getSurfaceWidth(),canvas.getSurfaceHeight());
      repaint(); //refresh window
    }


    @Override
    public void mousePressed(MouseEvent e) {
      mouseX = e.getX();
      mouseY = canvas.getHeight() - e.getY();

    }

    @Override
    public void mouseDragged(MouseEvent e) {
      view.translate(e.getX() - mouseX, canvas.getHeight() - e.getY() - mouseY);
      mouseX = e.getX();
      mouseY = canvas.getHeight() - e.getY();
      canvas.repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
      if (e.getWheelRotation() > 0)
        view.zoomIn();
      else
        view.zoomOut();
      canvas.repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
      switch (e.getKeyCode()) {
        case KeyEvent.VK_PLUS:
        case KeyEvent.VK_EQUALS:
          view.increaseMaxIterations();
          canvas.repaint();
          break;
        case KeyEvent.VK_MINUS:
        case KeyEvent.VK_UNDERSCORE:
          view.decreaseMaxIterations();
          canvas.repaint();
          break;
        case KeyEvent.VK_UP:
          view.zoomIn();
          canvas.repaint();
          break;
        case KeyEvent.VK_DOWN:
          view.zoomOut();
          canvas.repaint();
          break;
        case KeyEvent.VK_LEFT:
          view.translate(-5,0);
          canvas.repaint();
          break;
        case KeyEvent.VK_RIGHT:
          view.translate(5,0);
          canvas.repaint();
          break;
      }
    }

    @Override
    public void keyReleased(KeyEvent e) {

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
