// code by jph
package ch.alpine.ascona.flt;

import java.util.List;

import ch.alpine.ascona.util.dat.GokartPoseDataV2;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.bridge.ref.ann.ReflectionMarker;

@ReflectionMarker
public class GokartPoseSpecV2 extends GokartPoseSpec {
  public static final GokartPoseSpec INSTANCE = new GokartPoseSpecV2();

  private GokartPoseSpecV2() {
    super(GokartPoseDataV2.INSTANCE);
  }

  @Override
  public List<ManifoldDisplays> manifoldDisplays() {
    return ManifoldDisplays.SE2_R2;
  }
}
