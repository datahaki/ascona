// code by jph
package ch.alpine.ascona.crv.clt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.JFreeChart;

import ch.alpine.ascona.lev.LeversRender;
import ch.alpine.ascona.util.api.ControlPointsDemo;
import ch.alpine.ascona.util.api.RnLineTrim;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.dis.Se2Display;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.fig.ListPlot;
import ch.alpine.bridge.fig.VisualSet;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.crv.clt.Clothoid;
import ch.alpine.sophus.crv.clt.ClothoidBuilder;
import ch.alpine.sophus.crv.clt.ClothoidSampler;
import ch.alpine.sophus.hs.r2.Extract2D;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Join;
import ch.alpine.tensor.alg.Reverse;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.lie.r2.AngleVector;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.UniformDistribution;

@ReflectionMarker
public class ClothoidTransitionDemo extends ControlPointsDemo {
  public Boolean ctrl = true;
  @FieldSlider
  @FieldClip(min = "0.01", max = "1")
  public Scalar beta = RealScalar.of(0.1);
  public Boolean smpl = false;
  public Boolean plot = false;
  public Boolean shade = true;
  @FieldSlider
  @FieldClip(min = "0", max = "1.5708")
  public Scalar angle = RealScalar.of(0.8);
  @FieldSlider
  @FieldClip(min = "0", max = "0.7")
  public Scalar width = RealScalar.of(0.3);

  public ClothoidTransitionDemo() {
    super(true, ManifoldDisplays.CL_ONLY);
    ToolbarFieldsEditor.add(this, timerFrame.jToolBar);
    // ---
    setControlPointsSe2(RandomVariate.of(UniformDistribution.of(0, 8), 1 * 2, 3));
    timerFrame.geometricComponent.setOffset(100, 700);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor sequence = getGeodesicControlPoints();
    GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
    ClothoidBuilder clothoidBuilder = (ClothoidBuilder) geodesicSpace;
    VisualSet visualSet = new VisualSet();
    for (int index = 0; index < sequence.length() - 1; index += 2) {
      Tensor cr = sequence.get(index + 0);
      Tensor l1 = sequence.get(index + 1);
      Clothoid clothoid = clothoidBuilder.curve(cr, l1);
      // ClothoidTransition clothoidTransition = ClothoidTransition.of(cr, l1, clothoid);
      Tensor samples = ClothoidSampler.samples(clothoid, beta);
      Tensor linearized = samples.map(clothoid);
      graphics.setColor(ColorDataLists._097.strict().getColor(index / 2));
      graphics.setStroke(new BasicStroke(2));
      graphics.draw(geometricLayer.toPath2D(linearized));
      if (smpl)
        POINTS_RENDER_1.show( //
            Se2Display.INSTANCE::matrixLift, //
            Se2Display.INSTANCE.shape(), //
            linearized).render(geometricLayer, graphics);
      if (plot)
        visualSet.add(samples, RnLineTrim.TRIPLE_REDUCE_EXTRAPOLATION.apply( //
            Tensor.of(linearized.stream().map(Extract2D.FUNCTION))));
      if (shade) {
        Tensor ofs = AngleVector.of(angle).multiply(width);
        Tensor center = Tensor.of(linearized.stream().map(Extract2D.FUNCTION));
        Tensor hi = Tensor.of(center.stream().map(ofs::add));
        Tensor lo = Tensor.of(center.stream().map(ofs.negate()::add));
        graphics.setColor(new Color(128, 128, 128, 64));
        Path2D path2d = geometricLayer.toPath2D(Join.of(hi, Reverse.of(lo)), true);
        graphics.setStroke(new BasicStroke(1));
        graphics.fill(path2d);
      }
    }
    if (plot) {
      JFreeChart jFreeChart = ListPlot.of(visualSet, true);
      jFreeChart.draw(graphics, new Rectangle2D.Double(0, 0, 400, 300));
    }
    if (ctrl) {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, sequence, null, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexP();
    }
  }

  public static void main(String[] args) {
    new ClothoidTransitionDemo().setVisible(1000, 800);
  }
}