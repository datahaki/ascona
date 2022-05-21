// code by jph
package ch.alpine.ascona.util.dis;

import java.util.Random;

import ch.alpine.ascona.util.api.Box2D;
import ch.alpine.ascona.util.arp.HsArrayPlot;
import ch.alpine.ascona.util.arp.R2ArrayPlot;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.VectorQ;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.pdf.Distribution;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.UniformDistribution;
import ch.alpine.tensor.sca.Clips;

public class R2Display extends RnDisplay {
  private static final Scalar RADIUS = RealScalar.of(1.0);
  // ---
  public static final ManifoldDisplay INSTANCE = new R2Display();

  private R2Display() {
    super(2);
  }

  @Override // from ManifoldDisplay
  public Tensor toPoint(Tensor p) {
    return VectorQ.requireLength(p, 2);
  }

  @Override // from ManifoldDisplay
  public Tensor matrixLift(Tensor p) {
    return GfxMatrix.translation(p);
  }

  @Override
  public CoordinateBoundingBox coordinateBoundingBox() {
    return Box2D.xy(Clips.absolute(RADIUS));
  }

  @Override // from ManifoldDisplay
  public HsArrayPlot arrayPlot() {
    return new R2ArrayPlot(coordinateBoundingBox());
  }

  @Override // from ManifoldDisplay
  public RandomSampleInterface randomSampleInterface() {
    Distribution distribution = UniformDistribution.of(RADIUS.negate(), RADIUS);
    return new RandomSampleInterface() {
      @Override
      public Tensor randomSample(Random random) {
        return RandomVariate.of(distribution, random, 2).append(RealScalar.ZERO);
      }
    };
  }
}
