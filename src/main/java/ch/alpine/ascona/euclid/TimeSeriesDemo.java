// code by jph
package ch.alpine.ascona.euclid;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Optional;

import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.EuclideanPlaneDemo;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.plt.ListLinePlot;
import ch.alpine.bridge.fig.plt.TsPlot;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
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
class TimeSeriesDemo extends EuclideanPlaneDemo {
  @ReflectionMarker
  static class Param {
    public Boolean hide = false;
    public ResamplingMethods rm = ResamplingMethods.LINEAR_INTERPOLATION;
    public Integer refine = 5;
  }

  private final Param param;
  private final TimeSeries timeSeries;

  public TimeSeriesDemo() {
    super(param = new Param());
    // timerFrame.geometricComponent.setOffset(100, 600);
    RandomFunction randomFunction = RandomFunction.of(WienerProcess.standard());
    Distribution distribution = UniformDistribution.of(0, 10);
    RandomVariate.of(distribution, 100).stream() //
        .map(Scalar.class::cast).forEach(randomFunction::evaluate);
    randomFunction.evaluate(RealScalar.of(10));
    timeSeries = randomFunction.timeSeries();
    setControlPointsSe2(Tensors.fromString("{{2,3,0},{4,4,0},{7,2,0},{10,2,0}}"));
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.CURVYCURV;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Dimension dimension = getSize();
    TimeSeries custom = TimeSeries.empty(param.rm.get());
    for (Tensor row : getGeodesicControlPoints()) {
      Scalar key = row.Get(0);
      custom.insert(key, row.get(1));
    }
    TimeSeries product = TsEntrywise.times(timeSeries, custom);
    Show show = new Show();
    show.add(TsPlot.of(timeSeries)).setLabel("wiener");
    show.add(TsPlot.of(custom)).setLabel("custom");
    {
      TimeSeriesAggregate tsa = TimeSeriesAggregate.of(Entrywise.max(), ResamplingMethod.HOLD_VALUE_FROM_LEFT);
      TimeSeries result = tsa.of(timeSeries, RealScalar.of(0), RealScalar.ONE);
      show.add(ListLinePlot.of(result.path())).setLabel("max");
    }
    {
      TimeSeriesAggregate tsa = TimeSeriesAggregate.of(Entrywise.min(), ResamplingMethod.HOLD_VALUE_FROM_LEFT);
      TimeSeries result = tsa.of(timeSeries, RealScalar.of(0), RealScalar.ONE);
      show.add(ListLinePlot.of(result.path())).setLabel("min");
    }
    show.add(TsPlot.of(TsEntrywise.plus(timeSeries, custom))).setLabel("sum");
    show.add(TsPlot.of(product)).setLabel("times");
    show.add(TsPlot.of(TimeSeriesIntegrate.of(product))).setLabel("prd-integral");
    Optional<Rectangle> optional = Show.optionalDefaultInsets(dimension, graphics.getFont().getSize());
    if (optional.isPresent()) {
      Rectangle rectangle = optional.orElseThrow();
      CoordinateBoundingBox cbb = geometricLayer.fromRectangle(rectangle).orElseThrow();
      show.setCbb(cbb);
      if (!param.hide)
        show.render(graphics, rectangle);
    }
  }

  static void main() {
    new TimeSeriesDemo().runStandalone();
  }
}
