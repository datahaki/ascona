// code by jph
package ch.alpine.ascona.hull;

import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;

@ReflectionMarker
public class SymParam extends RotParam {
  @FieldSlider
  @FieldInteger
  @FieldClip(min = "2", max = "10")
  public Scalar layers = RealScalar.of(5);
  @FieldSlider
  @FieldInteger
  @FieldClip(min = "3", max = "10")
  public Scalar n = RealScalar.of(5);
}
