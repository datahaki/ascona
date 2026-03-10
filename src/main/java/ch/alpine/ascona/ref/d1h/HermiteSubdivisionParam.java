// code by jph
package ch.alpine.ascona.ref.d1h;

import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.ref.d1h.HermiteHiConfig;
import ch.alpine.sophis.ref.d1h.HermiteLoConfig;
import ch.alpine.tensor.Scalar;

@ReflectionMarker
class HermiteSubdivisionParam {
  @ReflectionMarker
  static class HermiteLoParam {
    public Scalar lambda = HermiteLoConfig.STANDARD.lambda();
    public Scalar mu = HermiteLoConfig.STANDARD.mu();

    public HermiteLoConfig config() {
      return new HermiteLoConfig(lambda, mu);
    }
  }

  @ReflectionMarker
  static class HermiteHiParam {
    public Scalar theta = HermiteHiConfig.STANDARD.theta();
    public Scalar omega = HermiteHiConfig.STANDARD.omega();

    public HermiteHiConfig config() {
      return new HermiteHiConfig(theta, omega);
    }
  }

  public static final HermiteSubdivisionParam GLOBAL = new HermiteSubdivisionParam();
  // ---
  public final HermiteLoParam hermiteLoParam = new HermiteLoParam();
  public final HermiteHiParam hermiteHiParam = new HermiteHiParam();
}
