// code by jph
package ch.alpine.ascona.decim;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophis.decim.CurveDecimation;
import ch.alpine.sophis.decim.LineDistances;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.lie.se2.Se2CoveringGroup;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.pdf.Distribution;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.NormalDistribution;
import ch.alpine.tensor.pdf.c.UniformDistribution;

// TODO ASCONA DEMO init properly, draw better, offer configuration of parameters
public class BulkDecimationDemo extends ControlPointsDemo {
  private static final ColorDataIndexed COLOR_DATA_INDEXED_DRAW = ColorDataLists._097.cyclic().deriveWithAlpha(192);

  public BulkDecimationDemo() {
    super(new AsconaParam(true, ManifoldDisplays.SE2_R2));
    Distribution dX = UniformDistribution.of(-3, 3);
    Distribution dY = NormalDistribution.of(0, .3);
    Distribution dA = NormalDistribution.of(1, .5);
    Tensor tensor = Tensor.of(Array.of(_ -> Tensors.of( //
        RandomVariate.of(dX), RandomVariate.of(dY), RandomVariate.of(dA)), 4).stream() //
        .map(Se2CoveringGroup.INSTANCE.exponential0()::exp));
    setControlPointsSe2(tensor);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor sequence = getGeodesicControlPoints();
    int length = sequence.length();
    if (0 == length)
      return;
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    HomogeneousSpace geodesicSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
    graphics.setColor(Color.LIGHT_GRAY);
    // graphics.setStroke(STROKE);
    RenderQuality.setQuality(graphics);
    // graphics.setStroke(new BasicStroke(1));
    Tensor domain = Subdivide.of(0, 1, 10);
    {
      PathRender pathRender = new PathRender(COLOR_DATA_INDEXED_DRAW.getColor(0));
      for (int index = 1; index < sequence.length(); ++index) {
        Tensor tensor = domain.maps(geodesicSpace.curve(sequence.get(index - 1), sequence.get(index)));
        pathRender.setCurve(tensor, false);
        pathRender.render(geometricLayer, graphics);
      }
    }
    CurveDecimation curveDecimation = CurveDecimation.of( //
        LineDistances.STANDARD.supply(geodesicSpace), //
        RealScalar.ONE);
    Tensor decimate = curveDecimation.apply(sequence);
    {
      PathRender pathRender = new PathRender(COLOR_DATA_INDEXED_DRAW.getColor(1));
      for (int index = 1; index < decimate.length(); ++index) {
        Tensor tensor = domain.maps(geodesicSpace.curve(decimate.get(index - 1), decimate.get(index)));
        pathRender.setCurve(tensor, false);
        pathRender.render(geometricLayer, graphics);
      }
    }
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, sequence, null, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexP();
    }
  }

  static void main() {
    new BulkDecimationDemo().runStandalone();
  }
}
