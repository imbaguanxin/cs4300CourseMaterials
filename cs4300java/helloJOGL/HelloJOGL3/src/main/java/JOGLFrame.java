
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.AnimatorBase;
import com.jogamp.opengl.util.FPSAnimator;
import javax.swing.*;
import util.JOGLGraphTextRenderer;

/**
 * Created by ashesh on 9/18/2015.
 */
public class JOGLFrame extends JFrame {
  private View view;
  private GLCanvas canvas;
  private JOGLGraphTextRenderer textRenderer;

  public JOGLFrame(String title) {
    //routine JFrame setting stuff
    super(title);
    setSize(400, 400); //this opens a 400x400 window
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //when X is pressed, close program

    //Our View class is the actual driver of the OpenGL stuff
    view = new View();

    GLProfile glp = GLProfile.getMaxProgrammable(true);
    GLCapabilities caps = new GLCapabilities(glp);

    canvas = new GLCanvas(caps);



    add(canvas);




    canvas.addGLEventListener(new GLEventListener() {
      @Override
      public void init(GLAutoDrawable glAutoDrawable) { //called the first time this canvas is created. Do your initialization here
        try {


          view.init(glAutoDrawable);
          textRenderer = new JOGLGraphTextRenderer(glAutoDrawable);


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
        GL2GL3 gl = glAutoDrawable.getGL().getGL3();

        view.draw(glAutoDrawable);

        //draw text
        String text = "Frame rate: "+canvas.getAnimator().getLastFPS();
        textRenderer.drawText(glAutoDrawable,text,10,canvas.getSurfaceHeight()-50,1,0,0,20.0f);

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


}
