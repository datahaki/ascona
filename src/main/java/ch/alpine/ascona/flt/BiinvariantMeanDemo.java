// code by jph
package ch.alpine.ascona.flt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.util.Optional;

import ch.alpine.ascona.util.api.ControlPointsDemo;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.dis.Se2CoveringDisplay;
import ch.alpine.ascona.util.ren.AxesRender;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.fit.HsWeiszfeldMethod;
import ch.alpine.sophus.fit.SpatialMedian;
import ch.alpine.sophus.hs.Biinvariant;
import ch.alpine.sophus.hs.Biinvariants;
import ch.alpine.sophus.lie.se2c.Se2CoveringGroup;
import ch.alpine.sophus.math.var.InversePowerVariogram;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.alg.ConstantArray;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.pdf.Distribution;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.NormalDistribution;
import ch.alpine.tensor.pdf.c.UniformDistribution;
import ch.alpine.tensor.sca.Chop;

@ReflectionMarker
public class BiinvariantMeanDemo extends ControlPointsDemo {
  private static final ColorDataIndexed COLOR_DATA_INDEXED_DRAW = ColorDataLists._097.cyclic().deriveWithAlpha(192);
  private static final ColorDataIndexed COLOR_DATA_INDEXED_FILL = ColorDataLists._097.cyclic().deriveWithAlpha(182);
  private static final Stroke STROKE = //
      new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0);
  // ---
  public Biinvariants biinvariants = Biinvariants.LEVERAGES;
  public Boolean axes = false;
  public Boolean median = true;

  public BiinvariantMeanDemo() {
    super(true, ManifoldDisplays.SE2C_SE2_S2_H2_R2);
    ToolbarFieldsEditor.add(this, timerFrame.jToolBar);
    // ---
    Distribution dX = UniformDistribution.of(-3, 3);
    Distribution dY = NormalDistribution.of(0, .3);
    Distribution dA = NormalDistribution.of(1, .5);
    Tensor tensor = Tensor.of(Array.of(l -> Tensors.of( //
        RandomVariate.of(dX), RandomVariate.of(dY), RandomVariate.of(dA)), 10).stream() //
        .map(Se2CoveringGroup.INSTANCE::exp));
    setControlPointsSe2(tensor);
    setGeodesicDisplay(Se2CoveringDisplay.INSTANCE);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    if (axes)
      AxesRender.INSTANCE.render(geometricLayer, graphics);
    Tensor sequence = getGeodesicControlPoints();
    int length = sequence.length();
    if (0 == length)
      return;
    Tensor weights = ConstantArray.of(RationalScalar.of(1, length), length);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    BiinvariantMean biinvariantMean = manifoldDisplay.biinvariantMean();
    final Tensor mean = biinvariantMean.mean(sequence, weights);
    graphics.setColor(Color.LIGHT_GRAY);
    graphics.setStroke(STROKE);
    RenderQuality.setQuality(graphics);
    GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
    for (Tensor point : sequence) {
      Tensor curve = Subdivide.of(0, 1, 20).map(geodesicSpace.curve(point, mean));
      Path2D path2d = geometricLayer.toPath2D(curve);
      graphics.draw(path2d);
    }
    graphics.setStroke(new BasicStroke(1));
    if (median) {
      Biinvariant biinvariant = biinvariants;
      TensorUnaryOperator weightingInterface = //
          biinvariant.weighting(manifoldDisplay.homogeneousSpace(), InversePowerVariogram.of(1), sequence);
      SpatialMedian spatialMedian = new HsWeiszfeldMethod(biinvariantMean, weightingInterface, Chop._05);
      Optional<Tensor> optional = spatialMedian.uniform(sequence);
      if (optional.isPresent()) {
        Tensor median = optional.orElseThrow();
        geometricLayer.pushMatrix(manifoldDisplay.matrixLift(median));
        Path2D path2d = geometricLayer.toPath2D(manifoldDisplay.shape().multiply(RealScalar.of(0.7)));
        path2d.closePath();
        graphics.setColor(COLOR_DATA_INDEXED_FILL.getColor(1));
        graphics.fill(path2d);
        graphics.setColor(COLOR_DATA_INDEXED_DRAW.getColor(1));
        graphics.draw(path2d);
        geometricLayer.popMatrix();
      }
    }
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, sequence, mean, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderOrigin();
      leversRender.renderIndexP();
      leversRender.renderIndexX();
    }
  }

  public static void main(String[] args) {
    new BiinvariantMeanDemo().setVisible(1200, 600);
  }
}
