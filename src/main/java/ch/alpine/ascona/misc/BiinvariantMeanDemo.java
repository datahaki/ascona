// code by jph
package ch.alpine.ascona.misc;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.ImageRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.fig.ListLinePlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.Biinvariant;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophis.fit.HsWeiszfeldMethod;
import ch.alpine.sophis.fit.SpatialMedian;
import ch.alpine.sophus.bm.MeanDefect;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.hs.s.SnPhongMean;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.ConstantArray;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.mat.Tolerance;
import ch.alpine.tensor.nrm.FrobeniusNorm;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.pdf.RandomSample;
import ch.alpine.tensor.pdf.RandomSampleInterface;
import ch.alpine.tensor.sca.Chop;
import ch.alpine.tensor.sca.Clips;
import ch.alpine.tensor.sca.exp.Log10;
import ch.alpine.tensor.sca.var.InversePowerVariogram;

public class BiinvariantMeanDemo extends ControlPointsDemo {
  private static final ColorDataIndexed COLOR_DATA_INDEXED_DRAW = ColorDataLists._097.cyclic().deriveWithAlpha(192);
  private static final ColorDataIndexed COLOR_DATA_INDEXED_FILL = ColorDataLists._097.cyclic().deriveWithAlpha(182);
  private static final Stroke STROKE = //
      new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0);
  private static final CoordinateBoundingBox BOX = CoordinateBoundingBox.of( //
      Clips.interval(-0.22, 0.53), //
      Clips.interval(-0.22, 0.22));

  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, ManifoldDisplays.manifolds());
    }

    public Biinvariants biinvariants = Biinvariants.LEVERAGES;
    public Boolean median = false;
    @FieldFuse
    public transient Boolean shuffle = false;
    public Boolean vehicle = false;
  }

  private final Param param;

  public BiinvariantMeanDemo() {
    this(new Param());
  }

  public BiinvariantMeanDemo(Param param) {
    super(param);
    this.param = param;
    controlPointsRender.setMidpointIndicated(false);
    // ---
    fieldsEditor(0).addUniversalListener(() -> {
      if (param.shuffle) {
        param.shuffle = false;
        shuffle();
      }
    });
    shuffle();
  }

  public void shuffle() {
    RandomSampleInterface randomSampleInterface = manifoldDisplay().randomSampleInterface();
    Tensor tensor = RandomSample.of(randomSampleInterface, 6);
    tensor.set(Scalar::zero, Tensor.ALL, 2);
    setControlPointsSe2(tensor);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor sequence = getGeodesicControlPoints();
    int length = sequence.length();
    if (0 == length)
      return;
    Tensor weights = ConstantArray.of(RationalScalar.of(1, length), length);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
    Tensor mean = null;
    try {
      mean = homogeneousSpace.biinvariantMean().mean(sequence, weights);
    } catch (Exception e) {
      graphics.setColor(Color.RED);
      graphics.drawString("mean does not exist", 0, 30);
    }
    try {
      Tensor shifted = sequence.get(0);
      if (homogeneousSpace.toString().equals("S"))
        shifted = SnPhongMean.INSTANCE.mean(sequence, weights);
      Tensor points = Tensors.empty();
      for (int iteration = 0; iteration < 100; ++iteration) {
        MeanDefect meanDefect = MeanDefect.of(sequence, weights, homogeneousSpace.exponential(shifted));
        shifted = meanDefect.shifted();
        Scalar err = FrobeniusNorm.of(meanDefect.tangent());
        if (Tolerance.CHOP.isZero(err))
          break;
        points.append(Tensors.of(RealScalar.of(iteration), Log10.FUNCTION.apply(err)));
      }
      Show show = new Show();
      show.add(ListLinePlot.of(points));
      Dimension dimension = timerFrame.geometricComponent.jComponent.getSize();
      show.render_autoIndent(graphics, new Rectangle(dimension.width - 400, 0, 400, 400));
    } catch (Exception e) {
      System.err.println("mean iteration failed");
    }
    graphics.setColor(Color.LIGHT_GRAY);
    graphics.setStroke(STROKE);
    RenderQuality.setQuality(graphics);
    if (Objects.nonNull(mean))
      for (Tensor point : sequence) {
        Tensor curve = Subdivide.of(0, 1, 20).maps(homogeneousSpace.curve(point, mean));
        graphics.draw(geometricLayer.toPath2D(Tensor.of(curve.stream().map(manifoldDisplay::point2xy))));
      }
    graphics.setStroke(new BasicStroke(1));
    if (param.median) {
      Map<Biinvariants, Biinvariant> map = Biinvariants.all(homogeneousSpace);
      Biinvariant biinvariant = map.getOrDefault(param.biinvariants, Biinvariants.LEVERAGES.ofSafe(homogeneousSpace));
      Sedarim sedarim = biinvariant.weighting(InversePowerVariogram.of(1), sequence);
      SpatialMedian spatialMedian = new HsWeiszfeldMethod(homogeneousSpace.biinvariantMean(), sedarim, Chop._05);
      Optional<Tensor> optional = spatialMedian.uniform(sequence);
      if (optional.isPresent()) {
        Tensor median = optional.orElseThrow();
        geometricLayer.pushMatrix(manifoldDisplay.matrixLift(median));
        Path2D path2d = geometricLayer.toPath2D(manifoldDisplay.shape().multiply(RealScalar.of(0.7)), true);
        graphics.setColor(COLOR_DATA_INDEXED_FILL.getColor(1));
        graphics.fill(path2d);
        graphics.setColor(COLOR_DATA_INDEXED_DRAW.getColor(1));
        graphics.draw(path2d);
        geometricLayer.popMatrix();
      }
    }
    if (param.vehicle) {
      for (Tensor point : sequence) {
        geometricLayer.pushMatrix(manifoldDisplay.matrixLift(point));
        new ImageRender(VehicleStatic.INSTANCE.bufferedImage_o(), BOX).render(geometricLayer, graphics);
        geometricLayer.popMatrix();
      }
      if (Objects.nonNull(mean)) {
        geometricLayer.pushMatrix(manifoldDisplay.matrixLift(mean));
        new ImageRender(VehicleStatic.INSTANCE.bufferedImage_g(), BOX).render(geometricLayer, graphics);
        geometricLayer.popMatrix();
      }
    } else {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, sequence, mean, geometricLayer, graphics);
      leversRender.renderOrigin();
      leversRender.renderIndexP();
      leversRender.renderIndexX();
    }
  }

  static void main() {
    launch();
  }
}
