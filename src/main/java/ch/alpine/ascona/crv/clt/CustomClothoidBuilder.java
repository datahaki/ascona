// code by jph
package ch.alpine.ascona.crv.clt;

import ch.alpine.sophus.clt.ClothoidBuilder;
import ch.alpine.sophus.clt.ClothoidBuilderImpl;
import ch.alpine.sophus.clt.ClothoidIntegrations;
import ch.alpine.tensor.Scalar;

public enum CustomClothoidBuilder {
  ;
  public static ClothoidBuilder of(Scalar lambda) {
    return ClothoidBuilderImpl.custom(lambda, ClothoidIntegrations.ANALYTIC);
  }
}
