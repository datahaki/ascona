// code by jph
package ch.alpine.ascona;

import java.util.Objects;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.sophis.prc.CurveRandomProcess;
import ch.alpine.sophus.api.Manifold;
import ch.alpine.sophus.api.TangentSpace;
import ch.alpine.sophus.rsm.LocalRandomSample;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Join;
import ch.alpine.tensor.pdf.RandomSample;
import ch.alpine.tensor.pdf.RandomSampleInterface;

public enum RandomPoints {
  ;
  public static Tensor scattered(ManifoldDisplay manifoldDisplay, int n) {
    return RandomSample.of(manifoldDisplay.randomSampleInterface(), n);
  }

  public static Tensor aroundOne(ManifoldDisplay manifoldDisplay, int n) {
    Scalar SIGMA = RealScalar.of(0.3);
    Manifold manifold = manifoldDisplay.manifold();
    if (Objects.nonNull(manifold)) {
      Tensor p = RandomSample.of(manifoldDisplay.randomSampleInterface());
      TangentSpace tangentSpace = manifold.tangentSpace(p);
      return Join.of( //
          Tensors.of(p), //
          RandomSample.of(LocalRandomSample.of(tangentSpace, SIGMA), n - 1));
    }
    return scattered(manifoldDisplay, n);
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
