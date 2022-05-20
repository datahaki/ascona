// code by jph
package ch.alpine.ascona.util.arp;

import java.awt.Dimension;
import java.util.function.Function;
import java.util.stream.Stream;

import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.nrm.Vector2NormSquared;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;
import ch.alpine.tensor.sca.Sign;
import ch.alpine.tensor.sca.pow.Sqrt;

public enum S2ArrayPlot implements HsArrayPlot {
  INSTANCE;

  private static final double RADIUS = 1;

  @Override // from GeodesicArrayPlot
  public Tensor raster(int resolution, Function<Tensor, ? extends Tensor> function, Tensor fallback) {
    Tensor dx = Subdivide.of(-RADIUS, +RADIUS, resolution - 1);
    Tensor dy = Subdivide.of(+RADIUS, -RADIUS, resolution - 1);
    return Tensor.of(dy.stream().parallel() //
        .map(py -> Tensor.of(dx.stream() //
            .map(px -> Tensors.of(px, py)) // in R2
            .map(point -> {
              Scalar z2 = RealScalar.ONE.subtract(Vector2NormSquared.of(point));
              return Sign.isPositive(z2) ? function.apply(point.append(Sqrt.FUNCTION.apply(z2))) : fallback;
            }))));
  }

  @Override // from GeodesicArrayPlot
  public Tensor pixel2model(Dimension dimension) {
    Clip clip = Clips.absolute(1.0);
    CoordinateBoundingBox coordinateBoundingBox = CoordinateBoundingBox.of(Stream.generate(() -> clip).limit(2));
    // Tensor range = Tensors.vector(RADIUS, RADIUS).multiply(RealScalar.TWO); // model
    // Tensor xy = range.multiply(RationalScalar.HALF.negate());
    return HsArrayPlot.pixel2model(coordinateBoundingBox, dimension);
  }
}
