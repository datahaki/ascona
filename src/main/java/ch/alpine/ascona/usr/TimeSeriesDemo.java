// code by jph
package ch.alpine.ascona.usr;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.fig.ListLinePlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.TsPlot;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.pdf.Distribution;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.UniformDistribution;
import ch.alpine.tensor.prc.RandomFunction;
import ch.alpine.tensor.prc.WienerProcess;
import ch.alpine.tensor.red.Entrywise;
import ch.alpine.tensor.sca.Clips;
import ch.alpine.tensor.tmp.ResamplingMethod;
import ch.alpine.tensor.tmp.ResamplingMethods;
import ch.alpine.tensor.tmp.TimeSeries;
import ch.alpine.tensor.tmp.TimeSeriesAggregate;
import ch.alpine.tensor.tmp.TimeSeriesIntegrate;
import ch.alpine.tensor.tmp.TsEntrywise;

/** split interface and biinvariant mean based curve subdivision */
// TODO ASCONA insert graph around ctrl point area
public class TimeSeriesDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, ManifoldDisplays.R2_ONLY);
    }

    public ResamplingMethods rm = ResamplingMethods.LINEAR_INTERPOLATION;
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
    // AxesRender.INSTANCE.render(geometricLayer, graphics);
    TimeSeries custom = TimeSeries.empty(param.rm.get());
    for (Tensor row : getGeodesicControlPoints()) {
      Scalar key = row.Get(0);
      custom.insert(key, row.get(1));
    }
    pathRender.render(geometricLayer, graphics);
    controlPointsRender.render(geometricLayer, graphics);
    int row = 0;
    {
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
      // Dimension dimension = timerFrame.geometricComponent.jComponent.getSize();
      // show.render_autoIndent(graphics, new Rectangle(dimension.width - 500, row++ * 300, 500, 300));
      double amp = 6;
      Point2D lh = geometricLayer.toPoint2D(0, amp);
      Point2D rl = geometricLayer.toPoint2D(10, -amp);
      Rectangle rectangle = new Rectangle( //
          (int) lh.getX(), (int) lh.getY(), //
          (int) (rl.getX() - lh.getX()), //
          (int) (rl.getY() - lh.getY()));
      show.setCbb(CoordinateBoundingBox.of(Clips.positive(10), Clips.absolute(amp)));
      show.render(graphics, rectangle);
    }
    TimeSeries product = TsEntrywise.times(timeSeries, custom);
    {
      Show show = new Show();
      show.add(TsPlot.of(TsEntrywise.plus(timeSeries, custom))).setLabel("sum");
      show.add(TsPlot.of(product)).setLabel("times");
      Dimension dimension = timerFrame.geometricComponent.jComponent.getSize();
      show.render_autoIndent(graphics, new Rectangle(dimension.width - 500, row++ * 300, 500, 300));
    }
    {
      Show show = new Show();
      show.add(TsPlot.of(TimeSeriesIntegrate.of(product))).setLabel("prd-integral");
      Dimension dimension = timerFrame.geometricComponent.jComponent.getSize();
      show.render_autoIndent(graphics, new Rectangle(dimension.width - 500, row++ * 300, 500, 300));
    }
  }

  static void main() {
    new TimeSeriesDemo().runStandalone();
  }
}
