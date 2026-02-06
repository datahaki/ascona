// code by jph
package ch.alpine.ascona.crv.clt;

import ch.alpine.sophis.crv.clt.ClothoidBuilder;
import ch.alpine.sophis.crv.clt.ClothoidBuilderImpl;
import ch.alpine.sophis.crv.clt.ClothoidIntegrations;
import ch.alpine.tensor.Scalar;

public enum CustomClothoidBuilder {
  ;
  public static ClothoidBuilder of(Scalar lambda) {
    return new ClothoidBuilderImpl(CustomClothoidQuadratic.of(lambda), ClothoidIntegrations.ANALYTIC);
  }
}
