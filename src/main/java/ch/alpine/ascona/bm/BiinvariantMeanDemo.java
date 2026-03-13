// code by jph
package ch.alpine.ascona.bm;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ch.alpine.ascona.RandomPoints;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.ColorPair;
import ch.alpine.ascony.ren.ImageRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.fig.ListLinePlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.Biinvariant;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophis.fit.HsWeiszfeldMethod;
import ch.alpine.sophis.fit.SpatialMedian;
import ch.alpine.sophis.var.InversePowerVariogram;
import ch.alpine.sophus.bm.MeanDefect;
import ch.alpine.sophus.bm.ReducingMeanEstimate;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.hs.s.SnPhongMean;
import ch.alpine.sophus.hs.s.Sphere;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.mat.Tolerance;
import ch.alpine.tensor.nrm.AveragingWeights;
import ch.alpine.tensor.nrm.FrobeniusNorm;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Chop;
import ch.alpine.tensor.sca.Clips;
import ch.alpine.tensor.sca.exp.Log10;

class BiinvariantMeanDemo extends ControlPointsDemo {
  private static final Stroke STROKE = //
      new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0);
  private static final CoordinateBoundingBox BOX = CoordinateBoundingBox.of( //
      Clips.interval(-0.22, 0.53), //
      Clips.interval(-0.22, 0.22));

  @ReflectionMarker
  static class Param0 {
    public Biinvariants biinvariants = Biinvariants.USANCE;
    public Boolean median = true;
    public Boolean vehicle = false;
  }

  private final Param0 param0;

  public BiinvariantMeanDemo() {
    super(param0 = new Param0());
    // ---
    addChangeListener(this::shuffle);
    setManifoldDisplay(ManifoldDisplays.H2);
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.manifolds();
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.SCATTERED;
  }

  private void shuffle() {
    setGeodesicControlPoints(RandomPoints.scattered(manifoldDisplay(), 6));
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor sequence = getGeodesicControlPoints();
    int length = sequence.length();
    if (0 == length)
      return;
    Tensor weights = AveragingWeights.of(length);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    HomogeneousSpace homogeneousSpace = manifoldDisplay.homogeneousSpace();
    Tensor mean_approximation = new ReducingMeanEstimate(homogeneousSpace).estimate(sequence, weights);
    Optional<Tensor> optional = homogeneousSpace.biinvariantMean().optional(sequence, weights);
    if (optional.isEmpty()) {
      graphics.setColor(Color.RED);
      graphics.drawString("mean does not exist", 0, 30);
    }
    try {
      Tensor shifted = sequence.get(0);
      if (homogeneousSpace instanceof Sphere)
        shifted = SnPhongMean.INSTANCE.estimate(sequence, weights);
      Tensor points = Tensors.empty();
      for (int iteration = 0; iteration < 100; ++iteration) {
        MeanDefect meanDefect = MeanDefect.of(sequence, weights, homogeneousSpace.tangentSpace(shifted));
        shifted = meanDefect.shifted();
        Scalar err = FrobeniusNorm.of(meanDefect.tangent());
        if (Tolerance.CHOP.isZero(err))
          break;
        points.append(Tensors.of(RealScalar.of(iteration), Log10.FUNCTION.apply(err)));
      }
      Show show = new Show();
      show.add(ListLinePlot.of(points));
      Dimension dimension = getSize();
      show.render_autoIndent(graphics, new Rectangle(dimension.width - 400, 0, 400, 400));
    } catch (Exception e) {
      System.err.println("mean iteration failed");
    }
    if (optional.isPresent()) {
      Tensor mean = optional.orElseThrow();
      graphics.setColor(Color.LIGHT_GRAY);
      graphics.setStroke(STROKE);
      for (Tensor point : sequence) {
        Tensor curve = Subdivide.of(0, 1, 20).maps(homogeneousSpace.curve(point, mean));
        graphics.draw(geometricLayer.toPath2D(manifoldDisplay.point2xy().slash(curve)));
      }
    }
    graphics.setStroke(new BasicStroke());
    if (param0.median) {
      Map<Biinvariants, Biinvariant> map = Biinvariants.all(homogeneousSpace);
      Biinvariant biinvariant = map.getOrDefault(param0.biinvariants, Biinvariants.USANCE.ofSafe(homogeneousSpace));
      Sedarim sedarim = biinvariant.weighting(InversePowerVariogram.of(1), sequence);
      SpatialMedian spatialMedian = new HsWeiszfeldMethod(homogeneousSpace.biinvariantMean(), sedarim, Chop._05);
      Optional<Tensor> optionalSM = spatialMedian.uniform(sequence);
      if (optionalSM.isPresent()) {
        Tensor median = optionalSM.orElseThrow();
        manifoldDisplay.showPoints(ColorPair.SPACIAL_MEDIAN, RealScalar.of(0.7), Tensors.of(median)) //
            .render(geometricLayer, graphics);
      } else {
        graphics.setColor(Color.RED);
        graphics.drawString("spatial mean does not exist", 0, 50);
      }
    }
    if (param0.vehicle) {
      for (Tensor point : sequence) {
        geometricLayer.pushMatrix(manifoldDisplay.matrixLift(point));
        new ImageRender(VehicleStatic.INSTANCE.bufferedImage_o(), BOX).render(geometricLayer, graphics);
        geometricLayer.popMatrix();
      }
      if (optional.isPresent()) {
        Tensor mean = optional.orElseThrow();
        geometricLayer.pushMatrix(manifoldDisplay.matrixLift(mean));
        new ImageRender(VehicleStatic.INSTANCE.bufferedImage_g(), BOX).render(geometricLayer, graphics);
        geometricLayer.popMatrix();
      }
    } else {
      {
        Tensor mean = optional.orElse(null);
        LeversRender leversRender = LeversRender.of(manifoldDisplay, sequence, mean, geometricLayer, graphics);
        leversRender.renderOrigin();
        leversRender.renderIndexP();
        leversRender.renderIndexX();
      }
      {
        Tensor origin = mean_approximation;
        Tensor shape = manifoldDisplay.shape();
        FontMetrics fontMetrics = graphics.getFontMetrics();
        int fheight = fontMetrics.getAscent();
        graphics.setColor(Color.BLACK);
        geometricLayer.pushMatrix(manifoldDisplay.matrixLift(origin));
        Rectangle rectangle = geometricLayer.toPath2D(shape, true).getBounds();
        int pix = rectangle.x;
        int piy = rectangle.y + rectangle.height + (-rectangle.height + fheight) / 2;
        {
          String string = "apx";
          pix -= fontMetrics.stringWidth(string);
          graphics.drawString(string, pix, piy - fheight / 3);
        }
        geometricLayer.popMatrix();
        manifoldDisplay.showPoints(ColorPair.APPROXIMATION, RealScalar.ONE, Tensors.of(origin)) //
            .render(geometricLayer, graphics);
      }
    }
  }

  static void main() {
    new BiinvariantMeanDemo().runStandalone();
  }
}
