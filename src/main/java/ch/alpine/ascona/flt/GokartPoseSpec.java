// code by ob, jph
package ch.alpine.ascona.flt;

import java.util.List;

import ch.alpine.ascona.util.dat.GokartPoseData;
import ch.alpine.ascona.util.dat.GokartPoseParam;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.sca.win.WindowFunctions;

@ReflectionMarker
public class GokartPoseSpec extends GokartPoseParam {
  public GokartPoseSpec(GokartPoseData gokartPoseData, List<ManifoldDisplays> list) {
    super(gokartPoseData, list);
  }

  public Boolean diff = true;
  public Boolean spec = true;
  public Boolean data = true;
  public Boolean conv = true;
  public Boolean symi = false;
  public WindowFunctions kernel = WindowFunctions.GAUSSIAN;
}
