// code by jph
package ch.alpine.ascona.crv;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import org.jfree.chart.JFreeChart;

import ch.alpine.ascona.util.api.ControlPointsDemo;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.fig.ListPlot;
import ch.alpine.bridge.fig.VisualSet;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.FieldPreferredWidth;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.bridge.swing.LookAndFeels;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.itp.BarycentricMetricInterpolation;
import ch.alpine.sophus.itp.BarycentricRationalInterpolation;
import ch.alpine.sophus.math.api.TensorMetric;
import ch.alpine.sophus.math.var.InversePowerVariogram;
import ch.alpine.sophus.math.win.KnotSpacing;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
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
  public static class Param {
    @FieldPreferredWidth(100)
    @FieldSelectionArray({ "0", "1/4", "1/2", "3/4", "1" })
    public Scalar beta = RealScalar.ZERO;
    @FieldPreferredWidth(100)
    @FieldInteger
    @FieldSelectionArray({ "0", "1", "2", "3", "4", "5", "6", "7" })
    public Scalar degree = RealScalar.ONE;
    public Boolean lagra = false;
    public Boolean basis = true;
  }

  private final Param param = new Param();

  public BarycentricRationalInterpolationDemo() {
    super(true, ManifoldDisplays.metricManifolds());
    ToolbarFieldsEditor.add(param, timerFrame.jToolBar);
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
      Tensor basis2 = domain.map(param.lagra //
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
      Tensor basis1 = domain.map(BarycentricRationalInterpolation.of(knots, param.degree.number().intValue()));
      try {
        Tensor curve = Tensor.of(basis1.stream().map(weights -> biinvariantMean.mean(control, weights)));
        new PathRender(Color.BLUE) //
            .setCurve(curve, false) //
            .render(geometricLayer, graphics);
      } catch (Exception exception) {
        System.err.println("no can do");
      }
      if (param.basis) {
        {
          VisualSet visualSet = new VisualSet();
          for (Tensor values : Transpose.of(basis2))
            visualSet.add(domain, values);
          JFreeChart jFreeChart = ListPlot.of(visualSet, true);
          Dimension dimension = timerFrame.geometricComponent.jComponent.getSize();
          jFreeChart.draw(graphics, new Rectangle(dimension.width - WIDTH, dimension.height - HEIGHT, WIDTH, HEIGHT));
        }
        {
          VisualSet visualSet = new VisualSet();
          for (Tensor values : Transpose.of(basis1))
            visualSet.add(domain, values);
          JFreeChart jFreeChart = ListPlot.of(visualSet, true);
          Dimension dimension = timerFrame.geometricComponent.jComponent.getSize();
          jFreeChart.draw(graphics, new Rectangle(dimension.width - WIDTH, 0, WIDTH, HEIGHT));
        }
      }
    }
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, control, null, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexP();
    }
  }

  public static void main(String[] args) {
    LookAndFeels.INTELLI_J.updateComponentTreeUI();
    new BarycentricRationalInterpolationDemo().setVisible(1200, 600);
  }
}
