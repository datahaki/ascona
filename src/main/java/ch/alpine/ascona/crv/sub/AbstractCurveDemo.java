// code by jph
package ch.alpine.ascona.crv.sub;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import ch.alpine.ascona.crv.CurvatureParam;
import ch.alpine.ascony.ren.ColorStroke;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldPreferredWidth;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.Rational;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;

abstract class AbstractCurveDemo extends PointSequenceDemo {
  @ReflectionMarker
  static class AbstractCurveParam {
    public Boolean symb = true;
    public final CurvatureParam cp = new CurvatureParam();
    @FieldSelectionArray({ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" })
    public Integer degree = 3;
    @FieldSelectionArray({ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" })
    public Integer refine = 4;
    @FieldSlider
    @FieldPreferredWidth(300)
    @FieldClip(min = "0", max = "1")
    public Scalar ratio = Rational.HALF;
  }

  protected final AbstractCurveParam abstractCurveParam;

  protected AbstractCurveDemo() {
    this(new Object());
  }

  protected AbstractCurveDemo(Object object) {
    super(new SaveParam(), abstractCurveParam = new AbstractCurveParam(), object);
  }

  @Override
  public final void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor control = getGeodesicControlPoints();
    if (!Tensors.isEmpty(control)) {
      if (abstractCurveParam.symb)
        graphics.drawImage(createImage(), 0, 0, null);
      Tensor refined = protected_render(geometricLayer, graphics, control);
      Tensor euclidXY = manifoldDisplay().point2xy().slash(refined);
      new PathRender(ColorStroke.CURVE, euclidXY, false).render(geometricLayer, graphics);
      Dimension dimension = geometricComponent().getSize();
      abstractCurveParam.cp.spawnXY(manifoldDisplay(), euclidXY, new Rectangle(dimension.width - 400, 0, 400, 300)) //
          .render(geometricLayer, graphics);
    }
  }

  protected abstract BufferedImage createImage();

  protected abstract Tensor protected_render( //
      GeometricLayer geometricLayer, Graphics2D graphics, Tensor control);
}
