// code by jph
package ch.alpine.ascona.ext;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import ch.alpine.ascona.util.api.RnLineTrim;
import ch.alpine.ascona.util.ren.AxesRender;
import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.bridge.fig.ListLinePlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.lie.r2.CirclePoints;
import ch.alpine.tensor.sca.Ceiling;
import ch.alpine.tensor.sca.Clips;
import ch.alpine.tensor.sca.pow.Sqrt;

public class CircleQualityDemo extends AbstractDemo {
  @ReflectionMarker
  public static class Param {
    @FieldSlider
    @FieldClip(min = "1", max = "20")
    public Scalar quality = RealScalar.of(10);
    public Boolean plot = true;
  }

  private final Param param;

  public CircleQualityDemo() {
    this(new Param());
  }

  public CircleQualityDemo(Param param) {
    super(param);
    this.param = param;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    AxesRender.INSTANCE.render(geometricLayer, graphics);
    Show show = new Show();
    for (Tensor _x : Subdivide.of(0.1, 2, 20)) {
      Scalar radius = (Scalar) _x;
      int n = Math.max(2, Ceiling.intValueExact(Sqrt.FUNCTION.apply(radius).multiply(param.quality)));
      Tensor curve = CirclePoints.of(n).multiply(radius);
      graphics.draw(geometricLayer.toPath2D(curve, true));
      if (param.plot)
        show.add(ListLinePlot.of(Subdivide.increasing(Clips.unit(), curve.length() - 1), //
            RnLineTrim.TRIPLE_REDUCE_EXTRAPOLATION.apply( //
                curve)));
    }
    if (param.plot) {
      show.render_autoIndent(graphics, new Rectangle(0, 0, 400, 300));
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
