// code by ob, jph
package ch.alpine.ascona.flt;

import ch.alpine.ascona.util.dat.GokartPoseData;
import ch.alpine.ascona.util.dat.GokartPoseParam;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.sca.win.WindowFunctions;

@ReflectionMarker
public abstract class GokartPoseSpec extends GokartPoseParam {
  public GokartPoseSpec(GokartPoseData gokartPoseData) {
    super(gokartPoseData);
  }

  public Boolean diff = true;
  public Boolean spec = true;
  public Boolean data = true;
  public Boolean conv = true;
  public Boolean symi = false;
  public WindowFunctions kernel = WindowFunctions.GAUSSIAN;
}
