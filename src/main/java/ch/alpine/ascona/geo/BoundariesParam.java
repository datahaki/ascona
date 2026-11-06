// code by jph
package ch.alpine.ascona.geo;

import java.util.List;

import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSelectionCallback;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.sca.exp.Exp;

@ReflectionMarker
public class BoundariesParam {
  @FieldSelectionCallback("countries")
  public String country = "Spain";
  @FieldSlider
  @FieldClip(min = "-20", max = "-2")
  public Integer minizoom = -4;

  public List<String> countries() {
    return GeoBoundaries.INSTANCE.countries();
  }

  public Scalar epsilon() {
    if (minizoom == -20)
      return RealScalar.ZERO;
    return Exp.FUNCTION.apply(RealScalar.of(minizoom));
  }
}
