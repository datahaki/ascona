// code by jph
package ch.alpine.ascona.crv;

import java.awt.Graphics2D;
import java.util.List;

import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldPreferredWidth;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;

public abstract class AbstractCurveDemo extends AbstractCurvatureDemo {
  @ReflectionMarker
  public static class AbstractCurveParam extends AbstractCurvatureParam {
    public AbstractCurveParam(List<ManifoldDisplays> list) {
      super(list);
    }

    @FieldSelectionArray({ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" })
    public Integer degree = 3;
    @FieldSelectionArray({ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" })
    public Integer refine = 4;
    @FieldSlider
    @FieldPreferredWidth(300)
    @FieldClip(min = "0", max = "1")
    public Scalar ratio = RationalScalar.HALF;
  }

  protected final AbstractCurveParam abstractCurveParam;

  public AbstractCurveDemo(AbstractCurveParam abstractCurveParam) {
    super(abstractCurveParam);
    this.abstractCurveParam = abstractCurveParam;
  }

  @Override
  protected final Tensor protected_render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor control = getGeodesicControlPoints();
    if (Tensors.isEmpty(control))
      return Tensors.empty();
    return protected_render(geometricLayer, graphics, abstractCurveParam.degree, abstractCurveParam.refine, control);
  }

  protected abstract Tensor protected_render( //
      GeometricLayer geometricLayer, Graphics2D graphics, int degree, int levels, Tensor control);
}
