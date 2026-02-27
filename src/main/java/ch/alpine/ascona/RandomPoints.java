package ch.alpine.ascona;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.sophus.api.Manifold;
import ch.alpine.sophus.api.TangentSpace;
import ch.alpine.sophus.rsm.LocalRandomSample;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.pdf.RandomSample;
import ch.alpine.tensor.pdf.RandomSampleInterface;

public enum RandomPoints {
  ;
  public static Tensor scattered(ManifoldDisplay manifoldDisplay, int n) {
    RandomSampleInterface randomSampleInterface = manifoldDisplay.randomSampleInterface();
    return RandomSample.of(randomSampleInterface, n);
  }

  public static Tensor on_line(ManifoldDisplay manifoldDisplay, int n) {
    RandomSampleInterface rsi = manifoldDisplay.randomSampleInterface();
    Manifold manifold = manifoldDisplay.manifold();
    Tensor p = RandomSample.of(rsi);
    TangentSpace tangentSpace = manifold.exponential(p);
    RandomSampleInterface lrs = LocalRandomSample.of(tangentSpace, 0.2);
    Tensor q = RandomSample.of(lrs);
    return null;
  }
}
