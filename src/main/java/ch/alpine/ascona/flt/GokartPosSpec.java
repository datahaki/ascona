// code by ob, jph
package ch.alpine.ascona.flt;

import java.util.List;

import ch.alpine.ascona.dat.gok.GokartPosParam;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.sca.win.WindowFunctions;

@ReflectionMarker
public class GokartPosSpec extends GokartPosParam {
  public GokartPosSpec(List<ManifoldDisplays> list) {
    super(list);
  }

  public Boolean diff = true;
  public Boolean spec = false;
  public Boolean data = true;
  public Boolean conv = true;
  public Boolean symi = false;
  public WindowFunctions kernel = WindowFunctions.GAUSSIAN;
}
