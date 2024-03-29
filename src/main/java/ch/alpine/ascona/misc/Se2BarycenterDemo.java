// code by jph
package ch.alpine.ascona.misc;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.crv.d2.Arrowhead;
import ch.alpine.sophus.crv.dub.DubinsGenerator;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.RotateRight;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.red.Total;
import ch.alpine.tensor.sca.Chop;

public class Se2BarycenterDemo extends ControlPointsDemo {
  private static final ColorDataIndexed COLOR_DATA_INDEXED_DRAW = ColorDataLists._097.cyclic().deriveWithAlpha(192);
  private static final ColorDataIndexed COLOR_DATA_INDEXED_FILL = ColorDataLists._097.cyclic().deriveWithAlpha(182);

  public Se2BarycenterDemo() {
    super(new AsconaParam(false, ManifoldDisplays.SE2C_SE2));
    Tensor tensor = DubinsGenerator.of(Tensors.vector(0, 0, 0), Tensors.fromString("{{5, 0, -1}}")) //
        .append(Tensors.vector(0, -1, 0)) //
        .append(Tensors.vector(0, 0, Math.PI / 7));
    setControlPointsSe2(tensor);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    // if (axes.isSelected())
    // AxesRender.INSTANCE.render(geometricLayer, graphics);
    Tensor sequence = getControlPointsSe2();
    if (sequence.length() == 4)
      try {
        HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
        final ScalarTensorFunction curve = homogeneousSpace.curve(sequence.get(0), sequence.get(1));
        {
          Tensor tensor = Subdivide.of(-0.5, 1.5, 55).map(curve);
          Path2D path2d = geometricLayer.toPath2D(Tensor.of(tensor.stream().map(manifoldDisplay::point2xy)));
          graphics.setColor(Color.BLUE);
          graphics.draw(path2d);
        }
        // ---
        BiinvariantMean biinvariantMean = homogeneousSpace.biinvariantMean(Chop._08);
        Tensor tX = Subdivide.of(-1, 1, 20);
        Tensor tY = Subdivide.of(-1, 1, 8);
        int n = tY.length();
        Tensor[][] array = new Tensor[tX.length()][tY.length()];
        {
          int c0 = 0;
          for (Tensor x : tX) {
            int c1 = 0;
            for (Tensor y : tY) {
              Scalar w = RationalScalar.HALF;
              Tensor weights = Tensors.of(w, x, y);
              weights.append(RealScalar.ONE.subtract(Total.ofVector(weights)));
              weights = RotateRight.of(weights, 1);
              Tensor mean = biinvariantMean.mean(sequence, weights);
              array[c0][c1] = mean;
              ++c1;
            }
            ++c0;
          }
        }
        // ---
        graphics.setColor(Color.LIGHT_GRAY);
        for (Tensor[] value : array)
          for (int c1 = 1; c1 < n; ++c1)
            graphics.draw(geometricLayer.toPath2D(Tensors.of(value[c1 - 1], value[c1])));
        for (int c0 = 1; c0 < array.length; ++c0)
          for (int c1 = 0; c1 < n; ++c1)
            graphics.draw(geometricLayer.toPath2D(Tensors.of(array[c0 - 1][c1], array[c0][c1])));
        // ---
        for (Tensor[] tensors : array)
          for (Tensor mean : tensors) {
            geometricLayer.pushMatrix(manifoldDisplay.matrixLift(mean));
            Path2D path2d = geometricLayer.toPath2D(Arrowhead.of(0.1));
            path2d.closePath();
            graphics.fill(path2d);
            geometricLayer.popMatrix();
          }
        // ---
        {
          geometricLayer.pushMatrix(manifoldDisplay.matrixLift(curve.apply(RationalScalar.HALF)));
          Path2D path2d = geometricLayer.toPath2D(Arrowhead.of(0.5));
          path2d.closePath();
          graphics.setColor(COLOR_DATA_INDEXED_FILL.getColor(0));
          graphics.fill(path2d);
          graphics.setColor(COLOR_DATA_INDEXED_DRAW.getColor(0));
          graphics.draw(path2d);
          geometricLayer.popMatrix();
        }
      } catch (Exception exception) {
        exception.printStackTrace();
      }
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, sequence, null, geometricLayer, graphics);
      // leversRender.renderSequence();
      leversRender.renderIndexP();
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
