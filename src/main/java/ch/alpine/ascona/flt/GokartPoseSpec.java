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

  public Boolean diff = false;
  public Boolean spec = false;
  public Boolean data = false;
  public Boolean conv = false;
  public Boolean symi = false;
  public WindowFunctions spinnerKernel = WindowFunctions.GAUSSIAN;
}
