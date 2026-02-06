// code by jph
package ch.alpine.ascona.flt;

import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.bridge.ref.ann.ReflectionMarker;

@ReflectionMarker
public class GokartPoseSpecV2 extends GokartPoseSpec {
  public static final GokartPoseSpec INSTANCE = new GokartPoseSpecV2();

  private GokartPoseSpecV2() {
    super(ManifoldDisplays.SE2_R2);
  }
}
