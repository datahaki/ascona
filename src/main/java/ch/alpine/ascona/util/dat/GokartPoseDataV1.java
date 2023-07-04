// code by jph
package ch.alpine.ascona.util.dat;

import java.util.Collections;
import java.util.List;

import ch.alpine.bridge.lang.SI;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.ext.ResourceData;
import ch.alpine.tensor.io.Import;

public enum GokartPoseDataV1 implements GokartPoseData {
  INSTANCE;

  private final List<String> list = ResourceData.lines("/dubilab/app/pose/index.vector");

  @Override // from GokartPoseData
  public List<String> list() {
    return Collections.unmodifiableList(list);
  }

  @Override // from GokartPoseData
  public Tensor getPose(String name, int limit) {
    return Tensor.of(Import.of("/dubilab/app/pose/" + name + ".csv").stream() //
        .limit(limit) //
        .map(row -> row.extract(1, 4)));
  }

  @Override // from GokartPoseData
  public Scalar getSampleRate() {
    return SI.PER_SECOND.quantity(20);
  }
}
