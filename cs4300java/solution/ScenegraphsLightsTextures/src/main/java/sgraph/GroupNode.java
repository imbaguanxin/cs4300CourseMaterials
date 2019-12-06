package sgraph;

import com.jogamp.opengl.GLAutoDrawable;

import org.joml.Matrix4f;

import util.Light;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * This class represents a group node in the scenegraph. A group node is simply
 * a logical grouping of other nodes. It can have an arbitrary number of
 * children. Its children can be nodes of any type
 *
 * @author Amit Shesh
 */
public class GroupNode extends AbstractNode {
  /**
   * A list of its children
   */
  protected List<INode> children;

  public GroupNode(IScenegraph graph, String name) {
    super(graph, name);
    children = new ArrayList<INode>();
  }

  /**
   * Searches recursively into its subtree to look for node with specified
   * name.
   *
   * @param name name of node to be searched
   * @return the node whose name this is if it exists within this subtree, null
   * otherwise
   */
  @Override
  public INode getNode(String name) {
    INode n = super.getNode(name);
    if (n != null) {
      return n;
    }

    int i = 0;
    INode answer = null;

    while ((i < children.size()) && (answer == null)) {
      answer = children.get(i).getNode(name);
      i++;
    }
    return answer;
  }

  /**
   * Sets the reference to the scene graph object for this node, and then
   * recurses down to children for the same
   *
   * @param graph a reference to the scenegraph object of which this tree is a
   *              part
   */
  @Override
  public void setScenegraph(IScenegraph graph) {
    super.setScenegraph(graph);
    for (int i = 0; i < children.size(); i++) {
      children.get(i).setScenegraph(graph);
    }
  }

  /**
   * To draw this node, it simply delegates to all its children
   *
   * @param context   the generic renderer context {@link sgraph.IScenegraphRenderer}
   * @param modelView the stack of modelview matrices
   */
  @Override
  public void draw(IScenegraphRenderer context, Stack<Matrix4f> modelView) {
    for (int i = 0; i < children.size(); i++) {
      children.get(i).draw(context, modelView);
    }
  }

  /**
   * Makes a deep copy of the subtree rooted at this node
   *
   * @return a deep copy of the subtree rooted at this node
   */
  @Override
  public INode clone() {
    ArrayList<INode> newc = new ArrayList<INode>();

    for (int i = 0; i < children.size(); i++) {
      newc.add(children.get(i).clone());
    }

    GroupNode newgroup = new GroupNode(scenegraph, name);

    for (int i = 0; i < children.size(); i++) {
      try {
        newgroup.addChild(newc.get(i));
      } catch (IllegalArgumentException e) {

      }
    }
    return newgroup;
  }

  /**
   * Since a group node is capable of having children, this method overrides the
   * default one in {@link sgraph.AbstractNode} and adds a child to this node
   *
   * @throws IllegalArgumentException this class does not throw this exception
   */
  @Override
  public void addChild(INode child) throws IllegalArgumentException {
    children.add(child);
    child.setParent(this);
  }

  /**
   * Get a list of all its children, for convenience purposes
   *
   * @return a list of all its children
   */

  public List<INode> getChildren() {
    return children;
  }

  /**
   * Overridden version from @link{AbstractNode}. This version first collects
   * all the lights from its children, and then appends to them the lights from
   * this node as well.
   *
   * It uses the original version for getting the lights in this node.
   */
  @Override
  public List<Light> getLightsInView(Stack<Matrix4f> modelview) {
    List<Light> lights = new ArrayList<Light>();
    for (int i = 0; i < children.size(); i++) {
      lights.addAll(children.get(i).getLightsInView(modelview));
    }

    //now get the lights from this node's lights
    lights.addAll(super.getLightsInView(modelview));
    return lights;
  }
}
