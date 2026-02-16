// code by jph
package ch.alpine.ascona.ref.d1;

import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.Rational;
import ch.alpine.tensor.Scalar;

@ReflectionMarker
public class CurveSubdivisionParam {
  public static final CurveSubdivisionParam GLOBAL = new CurveSubdivisionParam();
  // ---
  @FieldSelectionArray({ "1/100", "1/10", "1/8", "1/6", "1/4", "1/3", "1/2", "2/3", "9/10", "99/100" })
  public Scalar magicC = Rational.of(1, 6);
  @FieldSelectionArray({ "-1/16", "-1/36", "0", "1/72", "1/42", "1/36", "1/18", "1/16" })
  public Scalar omega = Rational.of(1, 16);
}
