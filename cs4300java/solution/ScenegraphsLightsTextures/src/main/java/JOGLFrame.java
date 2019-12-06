import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.AnimatorBase;
import com.jogamp.opengl.util.FPSAnimator;
import javax.swing.*;
import java.awt.event.*;
import java.io.InputStream;
import util.JOGLGraphTextRenderer;


/**
 * Created by ashesh on 9/18/2015.
 */
public class JOGLFrame extends JFrame {
  private View view;
  private JOGLGraphTextRenderer textRenderer;
  private GLCanvas canvas;

  public JOGLFrame(String title) {
    //routine JFrame setting stuff
    super(title);
    setSize(500, 500); //this opens a 400x400 window
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //when X is pressed, close program

    //Our View class is the actual driver of the OpenGL stuff
    view = new View();

    GLProfile glp = GLProfile.getGL2GL3();
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
          view.init(canvas);
          InputStream in = getClass().getClassLoader()
                  .getResourceAsStream
                          ("scenegraphmodels/face-hierarchy" +
                                  ".xml");
          view.initScenegraph(canvas, in);
          textRenderer = new JOGLGraphTextRenderer(glAutoDrawable);
          glAutoDrawable.getGL().setSwapInterval(1);
        } catch (Exception e) {
          JOptionPane.showMessageDialog(JOGLFrame.this, e.getMessage(), "Error while loading", JOptionPane.ERROR_MESSAGE);
          System.exit(1);
        }
      }

      @Override
      public void dispose(GLAutoDrawable glAutoDrawable) { //called when the canvas is destroyed.
        view.dispose(glAutoDrawable);
        textRenderer.dispose(glAutoDrawable);
      }

      @Override
      public void display(GLAutoDrawable glAutoDrawable) { //called every time this window must be redrawn
        view.draw(canvas);
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


}
