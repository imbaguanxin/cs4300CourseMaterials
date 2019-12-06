import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.AnimatorBase;
import com.jogamp.opengl.util.FPSAnimator;

import javax.swing.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowListener;
import java.io.IOException;

/**
 * Created by ashesh on 9/18/2015.
 */
public class JOGLFrame extends JFrame {
  private View view;
  int frameno;

  public JOGLFrame(String title) {
    //routine JFrame setting stuff
    super(title);
    setSize(500, 400); //this opens a 400x400 window
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //when X is pressed, close program

    //Our View class is the actual driver of the OpenGL stuff
    view = new View();

    GLProfile glp = GLProfile.getMaxProgrammable(true);
    GLCapabilities caps = new GLCapabilities(glp);
    GLCanvas canvas = new GLCanvas(caps);

    add(canvas);

    canvas.addGLEventListener(new GLEventListener() {
      @Override
      public void init(GLAutoDrawable glAutoDrawable) { //called the first time this canvas is created. Do your initialization here
        try {
          view.init(glAutoDrawable);
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
        view.draw(glAutoDrawable);
        if (frameno<500) {
          String filename = "output/image"
                  +String.format("%03d.png",frameno);
          try {
            view.captureFrame(filename,glAutoDrawable);
          } catch (Exception e) {
            JOptionPane.showMessageDialog(JOGLFrame.this, e.getMessage(), "Error while loading", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
          }
          frameno++;
        }

      }

      @Override
      public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) { //called every time this canvas is resized
        view.reshape(glAutoDrawable, x, y, width, height);
      //  repaint(); //refresh window
      }
    });

    //Add an animator to the canvas
    AnimatorBase animator = new FPSAnimator(canvas, 300);
    animator.setUpdateFPSFrames(100, null);
    animator.start();

    frameno = 0;
  }


}
