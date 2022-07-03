// code by jph
package ch.alpine.ascona.util.dat;

import java.util.List;

import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.SpaceParam;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSelectionCallback;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;

@ReflectionMarker
public class GokartPoseParam extends SpaceParam {
  private final GokartPoseData gokartPoseData;

  public GokartPoseParam( //
      GokartPoseData gokartPoseData, //
      List<ManifoldDisplays> list) {
    super(list);
    this.gokartPoseData = gokartPoseData;
    manifoldDisplays = ManifoldDisplays.Se2;
    string = gokartPoseData.list().get(0);
  }

  public GokartPoseData gpd() {
    return gokartPoseData;
  }

  @FieldSelectionCallback("gokartPoseData")
  public String string;
  @FieldInteger
  @FieldSelectionArray({ "100", "250", "500", "1000", "2000", "5000" })
  public Scalar limit = RealScalar.of(1000);

  public final List<String> gokartPoseData() {
    return gokartPoseData.list();
  }

  public final Tensor getPoses() {
    return gokartPoseData.getPose(string, limit.number().intValue());
  }
}
