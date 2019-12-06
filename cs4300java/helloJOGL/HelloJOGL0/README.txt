This basic program shows how to draw 2 triangles that share vertices on screen using JOGL using one color. It does it from first principles.

The first way is to specify all vertices directly in an array and use glDrawArrays to draw. The second way is to specify vertices in an array, and use another array that stores indices into the first array. The second way uses glDrawElements to draw.

Look for sections of code in the View's init function to comment and uncomment. Then comment and uncomment the drawing commands in View's draw.