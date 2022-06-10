// code by jph
package ch.alpine.ascona.flt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import ch.alpine.ascona.misc.VehicleStatic;
import ch.alpine.ascona.util.api.ControlPointsDemo;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.dis.Se2Display;
import ch.alpine.ascona.util.ren.ImageRender;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.win.LookAndFeels;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.FieldsEditor;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.fit.HsWeiszfeldMethod;
import ch.alpine.sophus.fit.SpatialMedian;
import ch.alpine.sophus.hs.Biinvariant;
import ch.alpine.sophus.hs.Biinvariants;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.math.sample.RandomSample;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.sophus.math.var.InversePowerVariogram;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.ConstantArray;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Chop;
import ch.alpine.tensor.sca.Clips;

public class BiinvariantMeanDemo extends ControlPointsDemo {
  private static final ColorDataIndexed COLOR_DATA_INDEXED_DRAW = ColorDataLists._097.cyclic().deriveWithAlpha(192);
  private static final ColorDataIndexed COLOR_DATA_INDEXED_FILL = ColorDataLists._097.cyclic().deriveWithAlpha(182);
  private static final Stroke STROKE = //
      new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0);
  private static final CoordinateBoundingBox BOX = CoordinateBoundingBox.of(Stream.of( //
      Clips.interval(-0.22, 0.53), //
      Clips.interval(-0.22, 0.22)));

  @ReflectionMarker
  public static class Param {
    public Biinvariants biinvariants = Biinvariants.LEVERAGES;
    public Boolean median = false;
    @FieldFuse("shuffle")
    public transient Boolean shuffle = false;
    public Boolean vehicle = false;
  }

  private final Param param = new Param();

  public BiinvariantMeanDemo() {
    super(true, ManifoldDisplays.MANIFOLDS);
    setMidpointIndicated(false);
    // ---
    setManifoldDisplay(Se2Display.INSTANCE);
    // ---
    FieldsEditor fieldsEditor = ToolbarFieldsEditor.add(param, timerFrame.jToolBar);
    fieldsEditor.addUniversalListener(() -> {
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
    BiinvariantMean biinvariantMean = homogeneousSpace.biinvariantMean(Chop._03);
    Tensor mean = null;
    try {
      mean = biinvariantMean.mean(sequence, weights);
    } catch (Exception e) {
      System.err.println("mean does not exist");
    }
    graphics.setColor(Color.LIGHT_GRAY);
    graphics.setStroke(STROKE);
    RenderQuality.setQuality(graphics);
    if (Objects.nonNull(mean))
      for (Tensor point : sequence) {
        Tensor curve = Subdivide.of(0, 1, 20).map(homogeneousSpace.curve(point, mean));
        Path2D path2d = geometricLayer.toPath2D(Tensor.of(curve.stream().map(manifoldDisplay::toPoint)));
        graphics.draw(path2d);
      }
    graphics.setStroke(new BasicStroke(1));
    if (param.median) {
      Biinvariant biinvariant = param.biinvariants;
      TensorUnaryOperator weightingInterface = //
          biinvariant.weighting(homogeneousSpace, InversePowerVariogram.of(1), sequence);
      SpatialMedian spatialMedian = new HsWeiszfeldMethod(biinvariantMean, weightingInterface, Chop._05);
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
      leversRender.renderSequence();
      leversRender.renderOrigin();
      leversRender.renderIndexP();
      leversRender.renderIndexX();
    }
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.updateUI();
    new BiinvariantMeanDemo().setVisible(1200, 600);
  }
}
