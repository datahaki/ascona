// code by jph
package ch.alpine.ascona;

import java.util.Objects;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.sophis.prc.CurveRandomProcess;
import ch.alpine.sophus.api.Manifold;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.pdf.RandomSample;
import ch.alpine.tensor.pdf.RandomSampleInterface;

public enum RandomPoints {
  ;
  public static Tensor scattered(ManifoldDisplay manifoldDisplay, int n) {
    return RandomSample.of(manifoldDisplay.randomSampleInterface(), n);
  }

  public static Tensor on_line(ManifoldDisplay manifoldDisplay, int n) {
    Manifold manifold = manifoldDisplay.manifold();
    if (Objects.isNull(manifold))
      return scattered(manifoldDisplay, n);
    RandomSampleInterface rsi = manifoldDisplay.randomSampleInterface();
    Tensor p = RandomSample.of(rsi);
    return Tensor.of(CurveRandomProcess.stream(manifold, RealScalar.of(0.2), p).limit(n));
  }
}
