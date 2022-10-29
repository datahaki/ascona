// code by jph
package ch.alpine.ascona.crv;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.ascona.util.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.fig.ListLinePlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.itp.BarycentricMetricInterpolation;
import ch.alpine.sophus.itp.BarycentricRationalInterpolation;
import ch.alpine.sophus.math.api.TensorMetric;
import ch.alpine.sophus.math.var.InversePowerVariogram;
import ch.alpine.sophus.math.win.KnotSpacing;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Last;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.alg.Transpose;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.sca.Chop;

public class BarycentricRationalInterpolationDemo extends ControlPointsDemo {
  private static final int WIDTH = 400;
  private static final int HEIGHT = 300;

  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, ManifoldDisplays.metricManifolds());
      manifoldDisplays = ManifoldDisplays.R2;
    }

    @FieldClip(min = "0", max = "1")
    public Integer beta = 0;
    @FieldClip(min = "0", max = "7")
    public Integer degree = 1;
    public Boolean lagrange = false;
    public Boolean basis = true;
  }

  private final Param param;

  public BarycentricRationalInterpolationDemo() {
    this(new Param());
  }

  public BarycentricRationalInterpolationDemo(Param param) {
    super(param);
    this.param = param;
    // ---
    setControlPointsSe2(Tensors.fromString("{{0, 0, 0}, {2, 0, 0}, {4, 3, 1}, {5, -1, -2}}"));
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    Tensor control = getGeodesicControlPoints();
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
    TensorMetric tensorMetric = (TensorMetric) manifoldDisplay.geodesicSpace();
    TensorUnaryOperator tensorUnaryOperator = //
        KnotSpacing.centripetal(tensorMetric, param.beta);
    Tensor knots = tensorUnaryOperator.apply(control);
    if (1 < control.length()) {
      Tensor domain = Subdivide.of(knots.get(0), Last.of(knots), 25 * control.length());
      BiinvariantMean biinvariantMean = homogeneousSpace.biinvariantMean(Chop._03);
      Tensor basis2 = domain.map(param.lagrange //
          ? BarycentricMetricInterpolation.la(knots, InversePowerVariogram.of(2))
          : BarycentricMetricInterpolation.of(knots, InversePowerVariogram.of(2)));
      try {
        Tensor curve = Tensor.of(basis2.stream().map(weights -> biinvariantMean.mean(control, weights)));
        new PathRender(Color.RED) //
            .setCurve(curve, false) //
            .render(geometricLayer, graphics);
      } catch (Exception exception) {
        System.err.println("no can do");
      }
      Tensor basis1 = domain.map(BarycentricRationalInterpolation.of(knots, param.degree));
      try {
        Tensor curve = Tensor.of(basis1.stream().map(weights -> biinvariantMean.mean(control, weights)));
        new PathRender(Color.BLUE) //
            .setCurve(curve, false) //
            .render(geometricLayer, graphics);
      } catch (Exception exception) {
        System.err.println("no can do");
      }
      if (param.basis) {
        Dimension dimension = timerFrame.geometricComponent.jComponent.getSize();
        {
          Show show = new Show();
          show.setPlotLabel("Basis 1");
          for (Tensor values : Transpose.of(basis1))
            show.add(ListLinePlot.of(domain, values));
          Rectangle rectangle = Show.defaultInsets(new Dimension(WIDTH, HEIGHT), graphics.getFont().getSize());
          rectangle.x += dimension.width - WIDTH;
          show.render(graphics, rectangle);
        }
        {
          Show show = new Show();
          show.setPlotLabel("Basis 2");
          for (Tensor values : Transpose.of(basis2))
            show.add(ListLinePlot.of(domain, values));
          Rectangle rectangle = Show.defaultInsets(new Dimension(WIDTH, HEIGHT), graphics.getFont().getSize());
          rectangle.x += dimension.width - WIDTH;
          rectangle.y += HEIGHT;
          show.render(graphics, rectangle);
        }
      }
    }
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, control, null, geometricLayer, graphics);
      leversRender.renderIndexP();
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
