// code by jph
package ch.alpine.ascona.crv;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.JFreeChart;

import ch.alpine.ascona.util.api.RnLineTrim;
import ch.alpine.ascona.util.ren.AxesRender;
import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.bridge.fig.ListPlot;
import ch.alpine.bridge.fig.VisualSet;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.lie.r2.CirclePoints;
import ch.alpine.tensor.sca.Ceiling;
import ch.alpine.tensor.sca.Clips;
import ch.alpine.tensor.sca.pow.Sqrt;

@ReflectionMarker
public class CirclesDemo extends AbstractDemo {
  @FieldSlider
  @FieldClip(min = "1", max = "20")
  public Scalar quality = RealScalar.of(10);
  public Boolean plot = true;

  public CirclesDemo() {
    ToolbarFieldsEditor.add(this, timerFrame.jToolBar);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    AxesRender.INSTANCE.render(geometricLayer, graphics);
    VisualSet visualSet = new VisualSet();
    for (Tensor _x : Subdivide.of(0.1, 2, 20)) {
      Scalar radius = (Scalar) _x;
      int n = Math.max(2, Ceiling.intValueExact(Sqrt.FUNCTION.apply(radius).multiply(quality)));
      Tensor curve = CirclePoints.of(n).multiply(radius);
      graphics.draw(geometricLayer.toPath2D(curve, true));
      if (plot)
        visualSet.add(Subdivide.increasing(Clips.unit(), curve.length() - 1), //
            RnLineTrim.TRIPLE_REDUCE_EXTRAPOLATION.apply( //
                curve));
    }
    if (plot) {
      JFreeChart jFreeChart = ListPlot.of(visualSet, true);
      jFreeChart.draw(graphics, new Rectangle2D.Double(0, 0, 400, 300));
    }
  }

  public static void main(String[] args) {
    new CirclesDemo().setVisible(1000, 800);
  }
}
