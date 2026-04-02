// code by jph
package ch.alpine.ascona.euclid;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.ren.ColorPairIndexed;
import ch.alpine.ascony.ren.ColorStrokeIndexed;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.EuclideanPlaneDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.d2.PolyclipResult;
import ch.alpine.sophis.crv.d2.PolygonCentroid;
import ch.alpine.sophis.crv.d2.alg.SutherlandHodgmanAlgorithm;
import ch.alpine.sophus.lie.se2.Se2ForwardAction;
import ch.alpine.sophus.lie.se2.Se2Matrix;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.col.ColorDataIndexed;
import ch.alpine.tensor.col.ColorDataLists;
import ch.alpine.tensor.io.ScalarArray;
import ch.alpine.tensor.lie.rot.CirclePoints;
import ch.alpine.tensor.lie.rot.Cross;
import ch.alpine.tensor.red.Mean;
import ch.alpine.tensor.red.Times;

class SutherlandHodgmanAlgorithmDemo extends EuclideanPlaneDemo {
  private static final ColorDataIndexed COLOR_DATA_INDEXED = ColorDataLists._097.strict();

  @ReflectionMarker
  static class Param {
    public Boolean move = false;
    @FieldClip(min = "1", max = "10")
    public Integer n = 4;
  }

  private final Param param;

  public SutherlandHodgmanAlgorithmDemo() {
    super(param = new Param());
    setControlPointsSe2(Tensor.of(CirclePoints.of(4).stream().map(row -> row.append(RealScalar.ZERO))));
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.CURVYCURV;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    boolean isMoving = param.move;
    setPositioningEnabled(!isMoving);
    if (isMoving) {
      Tensor mouse = geometricComponent().getMouseSe2CState();
      TensorUnaryOperator se2Bijection = new Se2ForwardAction(Times.of(mouse, Tensors.vector(1, 1, 0.3)));
      Tensor sequence = Tensor.of(getGeodesicControlPoints().stream().map(se2Bijection));
      // ---
      Tensor CIRCLE = CirclePoints.of(param.n).multiply(RealScalar.of(2));
      SutherlandHodgmanAlgorithm POLYGON_CLIP = SutherlandHodgmanAlgorithm.of(CIRCLE);
      // ---
      new PathRender(ColorStrokeIndexed._097.getColorStroke(0), sequence, true).render(geometricLayer, graphics);
      new PathRender(ColorStrokeIndexed._097.getColorStroke(3), CIRCLE, true).render(geometricLayer, graphics);
      PolyclipResult polyclipResult = POLYGON_CLIP.apply(sequence);
      graphics.setColor(new Color(128, 255, 128, 128));
      Tensor result = polyclipResult.tensor();
      graphics.fill(geometricLayer.toPath2D(result));
      new PathRender(ColorStrokeIndexed._097.getColorStroke(1), result, true).render(geometricLayer, graphics);
      ColorPairIndexed colorPairIndexed = new ColorPairIndexed(COLOR_DATA_INDEXED, 128, 255);
      for (int index = 0; index < result.length(); ++index) {
        int cind = polyclipResult.belong().Get(index).number().intValue();
        manifoldDisplay.showPoints(colorPairIndexed.getColorPair(cind), RealScalar.of(2), Tensors.of(result.get(index))) //
            .render(geometricLayer, graphics);
      }
      Tensor nsum = Array.zeros(2);
      {
        graphics.setColor(Color.DARK_GRAY);
        Scalar[] prop = ScalarArray.ofVector(polyclipResult.belong());
        Tensor[] array = result.stream().toArray(Tensor[]::new);
        for (int index = 0; index < result.length(); ++index) {
          int iprev = Math.floorMod(index - 1, result.length());
          Tensor a = array[iprev];
          Tensor b = array[index];
          int ap = prop[iprev].number().intValue();
          int bp = prop[index].number().intValue();
          Tensor point = Mean.of(Tensors.of(a, b));
          Tensor norma = Cross.of(b.subtract(a)).multiply(RealScalar.of(0.3));
          if (ap == 1 && bp == 1)
            norma = norma.negate();
          nsum = nsum.add(norma);
          geometricLayer.pushMatrix(Se2Matrix.translation(point));
          graphics.draw(geometricLayer.toLine2D(norma));
          geometricLayer.popMatrix();
        }
      }
      if (0 < result.length()) {
        Tensor centroid = PolygonCentroid.of(result);
        geometricLayer.pushMatrix(Se2Matrix.translation(centroid));
        graphics.setColor(Color.BLACK);
        graphics.setStroke(new BasicStroke(2f));
        graphics.draw(geometricLayer.toLine2D(nsum));
        geometricLayer.popMatrix();
      }
      LeversRender leversRender = LeversRender.of(manifoldDisplay, result, null, geometricLayer, graphics);
      leversRender.renderIndexP();
    } else {
      Tensor sequence = getGeodesicControlPoints();
      new PathRender(ColorStrokeIndexed._097.getColorStroke(0), sequence, true) //
          .render(geometricLayer, graphics);
      LeversRender leversRender = LeversRender.of(manifoldDisplay, sequence, null, geometricLayer, graphics);
      leversRender.renderSurfaceP();
      leversRender.renderIndexP();
    }
    // RenderQuality.setDefault(graphics);
    // new PathRender(COLOR_DATA_INDEXED.getColor(1), 2.5f).setCurve(HILBERT, false).render(geometricLayer, graphics);
  }

  static void main() {
    new SutherlandHodgmanAlgorithmDemo().runStandalone();
  }
}
