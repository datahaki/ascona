// code by jph
package ch.alpine.ascona.util.dis;

import ch.alpine.ascona.util.win.RenderInterface;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;

public class H1Display extends HnDisplay {
  public static final ManifoldDisplay INSTANCE = new H1Display();

  // ---
  private H1Display() {
    super(1);
  }

  @Override // from ManifoldDisplay
  public Tensor toPoint(Tensor p) {
    return p.extract(0, 1).append(RealScalar.ZERO);
  }

  @Override
  public RenderInterface background() {
    return H1Background.INSTANCE;
  }
}
