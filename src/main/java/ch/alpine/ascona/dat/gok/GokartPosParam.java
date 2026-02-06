// code by jph
package ch.alpine.ascona.dat.gok;

import java.util.List;

import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.SpaceParam;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSelectionCallback;
import ch.alpine.bridge.ref.ann.ReflectionMarker;

@ReflectionMarker
public class GokartPosParam extends SpaceParam {
  @FieldSelectionCallback("gokartPoseData")
  public String string;
  @FieldSelectionArray({ "100", "250", "500", "1000", "2000", "5000" })
  public Integer limit = 1000;

  public GokartPosParam(List<ManifoldDisplays> list) {
    super(list);
    manifoldDisplays = ManifoldDisplays.Se2;
    string = GokartPos.list().getFirst();
  }

  public static List<String> gokartPoseData() {
    return GokartPos.list();
  }

  public final PosHz getPosHz() {
    return GokartPos.get(string, limit);
  }
}
