// code by jph
package ch.alpine.ascona.util.api;

import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.ref.d1h.HermiteHiParam;
import ch.alpine.sophus.ref.d1h.HermiteLoParam;

@ReflectionMarker
public class HermiteSubdivisionParam {
  public static final HermiteSubdivisionParam GLOBAL = new HermiteSubdivisionParam();
  // ---
  public final HermiteLoParam hermiteLoParam = HermiteLoParam.standard();
  public final HermiteHiParam hermiteHiParam = HermiteHiParam.standard();
}
