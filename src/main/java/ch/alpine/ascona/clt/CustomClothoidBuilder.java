// code by jph
package ch.alpine.ascona.clt;

import ch.alpine.sophus.crv.clt.ClothoidBuilder;
import ch.alpine.sophus.crv.clt.ClothoidBuilderImpl;
import ch.alpine.sophus.crv.clt.par.ClothoidIntegrations;
import ch.alpine.tensor.Scalar;

public enum CustomClothoidBuilder {
  ;
  public static ClothoidBuilder of(Scalar lambda) {
    return new ClothoidBuilderImpl(CustomClothoidQuadratic.of(lambda), ClothoidIntegrations.ANALYTIC);
  }
}
