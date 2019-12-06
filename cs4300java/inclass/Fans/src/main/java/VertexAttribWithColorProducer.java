/**
 * Created by ashesh on 9/3/2016.
 */
public class VertexAttribWithColorProducer implements util.VertexProducer<VertexAttribWithColor> {
  @Override
  public VertexAttribWithColor produce() {
    return new VertexAttribWithColor();
  }
}
