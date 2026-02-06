// code by ob, jph
package ch.alpine.ascona.flt;

import java.util.List;

import ch.alpine.ascona.dat.GokartPos;
import ch.alpine.ascona.dat.GokartPosParam;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.sca.win.WindowFunctions;

@ReflectionMarker
public class GokartPoseSpec extends GokartPosParam {
  public GokartPoseSpec(GokartPos gokartPoseData, List<ManifoldDisplays> list) {
    super(gokartPoseData, list);
  }

  public Boolean diff = true;
  public Boolean spec = false;
  public Boolean data = true;
  public Boolean conv = true;
  public Boolean symi = false;
  public WindowFunctions kernel = WindowFunctions.GAUSSIAN;
}
