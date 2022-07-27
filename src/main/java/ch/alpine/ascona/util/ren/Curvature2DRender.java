// code by jph
package ch.alpine.ascona.util.ren;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophus.crv.d2.CurvatureComb;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Throw;
import ch.alpine.tensor.Unprotect;

public enum Curvature2DRender {
  ;
  private static final Color COLOR_CURVATURE_COMB = new Color(0, 0, 0, 128);
  private static final Scalar COMB_SCALE = DoubleScalar.of(-1);

  /** @param curve {{x0, y0}, {x1, y1}, ...}
   * @param isCyclic */
  public static RenderInterface of(Tensor curve, boolean isCyclic) {
    return of(curve, isCyclic, true);
  }

  /** @param curve {{x0, y0}, {x1, y1}, ...}
   * @param isCyclic
   * @param comb */
  public static RenderInterface of(Tensor curve, boolean isCyclic, boolean comb) {
    return of(curve, isCyclic, comb, COMB_SCALE);
  }

  /** Hint: when control points have coordinates with unit "m",
   * scale should have unit "m^2"
   *
   * @param curve {{x0, y0}, {x1, y1}, ...}
   * @param isCyclic
   * @param scale */
  public static RenderInterface of(Tensor curve, boolean isCyclic, Scalar scale) {
    return of(curve, isCyclic, true, scale);
  }

  /** Hint: when control points have coordinates with unit "m",
   * scale should have unit "m^2"
   * 
   * @param curve {{x0, y0}, {x1, y1}, ...}
   * @param isCyclic
   * @param comb
   * @param scale */
  public static RenderInterface of(Tensor curve, boolean isCyclic, boolean comb, Scalar scale) {
    if (0 < curve.length())
      if (Unprotect.dimension1(curve) != 2)
        throw new Throw(curve);
    RenderInterface ri1 = new PathRender(Color.BLUE, 1.25f).setCurve(curve, isCyclic);
    RenderInterface ri2 = comb //
        ? new PathRender(COLOR_CURVATURE_COMB).setCurve(CurvatureComb.of(curve, scale, isCyclic), isCyclic)
        : EmptyRender.INSTANCE;
    return new RenderInterface() {
      @Override
      public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
        ri1.render(geometricLayer, graphics);
        ri2.render(geometricLayer, graphics);
      }
    };
  }
}
