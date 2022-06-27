// code by jph
package ch.alpine.ascona.util.dis;

import ch.alpine.ascona.util.win.RenderInterface;

public class H1Display extends HnDisplay {
  public static final ManifoldDisplay INSTANCE = new H1Display();

  // ---
  private H1Display() {
    super(1);
  }

  @Override
  public RenderInterface background() {
    return H1Background.INSTANCE;
  }
}
