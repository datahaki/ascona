// code by jph
package ch.alpine.ascona.util.api;

import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.ref.d1h.HermiteHiConfig;
import ch.alpine.sophus.ref.d1h.HermiteLoConfig;
import ch.alpine.tensor.Scalar;

@ReflectionMarker
public class HermiteSubdivisionParam {
  @ReflectionMarker
  public static class HermiteLoParam {
    public Scalar lambda = HermiteLoConfig.STANDARD.lambda();
    public Scalar mu = HermiteLoConfig.STANDARD.mu();

    public HermiteLoConfig config() {
      return new HermiteLoConfig(lambda, mu);
    }
  }

  @ReflectionMarker
  public static class HermiteHiParam {
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
