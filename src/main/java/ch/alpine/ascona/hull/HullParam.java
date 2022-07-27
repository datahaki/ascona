package ch.alpine.ascona.hull;

import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.lie.so3.Rodrigues;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;

@ReflectionMarker
public class HullParam {
  @FieldSelectionArray({ "50", "100", "200", "400" })
  public Scalar count = RealScalar.of(200);
  public Boolean quality = false;
  public Boolean ccw = false;
  @FieldSlider
  @FieldClip(min = "-2", max = "+2")
  public Scalar t0 = RealScalar.of(0);
  @FieldSlider
  @FieldClip(min = "-2", max = "+2")
  public Scalar t1 = RealScalar.of(0);
  @FieldSlider
  @FieldClip(min = "-2", max = "+2")
  public Scalar t2 = RealScalar.of(0);
  @FieldFuse
  public transient Boolean shuffle = true;

  public Tensor rotation() {
    return Rodrigues.vectorExp(Tensors.of(t0, t1, t2));
  }
}
