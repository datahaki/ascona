// code by jph
package ch.alpine.ascona.util.dis;

import ch.alpine.ascona.util.ren.EmptyRender;
import ch.alpine.ascona.util.ren.RenderInterface;
import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.sophus.hs.rs.Se2inR2S;

public class R2S1ADisplay extends R2S1AbstractDisplay {
  public static final ManifoldDisplay INSTANCE = new R2S1ADisplay();

  private R2S1ADisplay() {
  }

  @Override // from ManifoldDisplay
  public GeodesicSpace geodesicSpace() {
    return Se2inR2S.METHOD_0;
  }

  @Override // from ManifoldDisplay
  public RenderInterface background() {
    return EmptyRender.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public String toString() {
    return "R2S1 A";
  }
}
