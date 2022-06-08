// code by jph
package ch.alpine.ascona.util.arp;

import java.util.function.Function;

import ch.alpine.ascona.util.api.Box2D;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.nrm.Vector2NormSquared;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Clips;
import ch.alpine.tensor.sca.Sign;
import ch.alpine.tensor.sca.pow.Sqrt;

public enum S2ArrayPlot implements HsArrayPlot {
  INSTANCE;

  public static final CoordinateBoundingBox COORDINATE_BOUNDING_BOX = Box2D.xy(Clips.absolute(1.0));

  @Override // from GeodesicArrayPlot
  public Tensor raster(int resolution, Function<Tensor, ? extends Tensor> function, Tensor fallback) {
    Tensor dx = Subdivide.increasing(COORDINATE_BOUNDING_BOX.getClip(0), resolution - 1);
    Tensor dy = Subdivide.decreasing(COORDINATE_BOUNDING_BOX.getClip(1), resolution - 1);
    return Tensor.of(dy.stream().parallel() //
        .map(py -> Tensor.of(dx.stream() //
            .map(px -> Tensors.of(px, py)) // in R2
            .map(point -> {
              Scalar z2 = RealScalar.ONE.subtract(Vector2NormSquared.of(point));
              return Sign.isPositive(z2) ? function.apply(point.append(Sqrt.FUNCTION.apply(z2))) : fallback;
            }))));
  }
}
