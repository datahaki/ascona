// code by jph
package ch.alpine.ascona.misc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.JFreeChart;

import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.AxesRender;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.ascona.util.win.ControlPointsDemo;
import ch.alpine.bridge.fig.ListPlot;
import ch.alpine.bridge.fig.VisualSet;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.pdf.Distribution;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.UniformDistribution;
import ch.alpine.tensor.prc.RandomFunction;
import ch.alpine.tensor.prc.WienerProcess;
import ch.alpine.tensor.red.Entrywise;
import ch.alpine.tensor.tmp.ResamplingMethod;
import ch.alpine.tensor.tmp.ResamplingMethods;
import ch.alpine.tensor.tmp.TimeSeries;
import ch.alpine.tensor.tmp.TimeSeriesAggregate;
import ch.alpine.tensor.tmp.TimeSeriesIntegrate;
import ch.alpine.tensor.tmp.TsEntrywise;

/** split interface and biinvariant mean based curve subdivision */
public class TimeSeriesDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, ManifoldDisplays.R2_ONLY);
    }

    public ResamplingMethod rm = ResamplingMethods.LINEAR_INTERPOLATION;
    public Integer refine = 5;
  }

  private final Param param;
  private final TimeSeries timeSeries;
  private final PathRender pathRender = new PathRender(new Color(0, 255, 0, 128));

  public TimeSeriesDemo() {
    this(new Param());
  }

  public TimeSeriesDemo(Param param) {
    super(param);
    this.param = param;
    controlPointsRender.setMidpointIndicated(true);
    setManifoldDisplay(ManifoldDisplays.R2);
    timerFrame.geometricComponent.setOffset(100, 600);
    RandomFunction randomFunction = RandomFunction.of(WienerProcess.standard());
    Distribution distribution = UniformDistribution.of(0, 10);
    RandomVariate.of(distribution, 100).stream() //
        .map(Scalar.class::cast).forEach(randomFunction::evaluate);
    randomFunction.evaluate(RealScalar.of(10));
    timeSeries = randomFunction.timeSeries();
    pathRender.setCurve(timeSeries.path(), false);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    AxesRender.INSTANCE.render(geometricLayer, graphics);
    TimeSeries custom = TimeSeries.empty(param.rm);
    for (Tensor row : getGeodesicControlPoints()) {
      Scalar key = row.Get(0);
      custom.insert(key, row.get(1));
    }
    pathRender.render(geometricLayer, graphics);
    controlPointsRender.render(geometricLayer, graphics);
    VisualSet visualSet = new VisualSet();
    visualSet.add(timeSeries).setLabel("wiener");
    visualSet.add(custom).setLabel("custom");
    visualSet.add(TsEntrywise.plus(timeSeries, custom)).setLabel("sum");
    TimeSeries product = TsEntrywise.times(timeSeries, custom);
    visualSet.add(product).setLabel("times");
    visualSet.add(TimeSeriesIntegrate.of(product)).setLabel("prd-integral");
    {
      TimeSeriesAggregate tsa = TimeSeriesAggregate.of(Entrywise.max(), ResamplingMethods.HOLD_VALUE_FROM_LEFT);
      TimeSeries result = tsa.of(timeSeries, RealScalar.of(0), RealScalar.ONE);
      visualSet.add(result.path()).setLabel("max");
    }
    {
      TimeSeriesAggregate tsa = TimeSeriesAggregate.of(Entrywise.min(), ResamplingMethods.HOLD_VALUE_FROM_LEFT);
      TimeSeries result = tsa.of(timeSeries, RealScalar.of(0), RealScalar.ONE);
      visualSet.add(result.path()).setLabel("min");
    }
    JFreeChart jFreeChart = ListPlot.of(visualSet, true);
    Dimension dimension = timerFrame.geometricComponent.jComponent.getSize();
    jFreeChart.draw(graphics, new Rectangle2D.Double(dimension.width - 500, 0, 500, 300));
  }

  public static void main(String[] args) {
    launch();
  }
}
