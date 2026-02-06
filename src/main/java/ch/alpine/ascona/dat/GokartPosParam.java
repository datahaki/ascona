// code by jph
package ch.alpine.ascona.dat;

import java.util.List;

import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.SpaceParam;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSelectionCallback;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.Tensor;

@ReflectionMarker
public class GokartPosParam extends SpaceParam {
  private final GokartPos gokartPos;

  public GokartPosParam( //
      GokartPos gokartPos, //
      List<ManifoldDisplays> list) {
    super(list);
    this.gokartPos = gokartPos;
    manifoldDisplays = ManifoldDisplays.Se2;
    string = gokartPos.list().getFirst();
  }

  public GokartPos gpd() {
    return gokartPos;
  }

  @FieldSelectionCallback("gokartPoseData")
  public String string;
  @FieldSelectionArray({ "100", "250", "500", "1000", "2000", "5000" })
  public Integer limit = 1000;

  public final List<String> gokartPoseData() {
    return gokartPos.list();
  }

  public final Tensor getPoses() {
    return Tensor.of(gokartPos.getData(string).stream().limit(limit));
  }
}
