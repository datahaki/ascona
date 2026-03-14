// code by jph
package ch.alpine.ascona.euclid;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.ren.ColorPairIndexed;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.EuclideanPlaneDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophis.crv.d2.PolyclipResult;
import ch.alpine.sophis.crv.d2.PolygonCentroid;
import ch.alpine.sophis.crv.d2.alg.SutherlandHodgmanAlgorithm;
import ch.alpine.sophis.crv.d2.ex.CogPoints;
import ch.alpine.sophus.lie.se2.Se2Matrix;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.io.ScalarArray;
import ch.alpine.tensor.lie.rot.CirclePoints;
import ch.alpine.tensor.lie.rot.Cross;
import ch.alpine.tensor.red.Mean;

class PolygonClipDemo extends EuclideanPlaneDemo {
  private static final ColorDataIndexed COLOR_DATA_INDEXED = ColorDataLists._097.strict();
  private static final Tensor CIRCLE = CirclePoints.of(7).multiply(RealScalar.of(4));
  private static final SutherlandHodgmanAlgorithm POLYGON_CLIP = SutherlandHodgmanAlgorithm.of(CIRCLE);

  public PolygonClipDemo() {
    setControlPointsSe2(Tensor.of(CogPoints.of(4, RealScalar.of(5), RealScalar.of(-2)).stream().map(row -> row.append(RealScalar.ZERO))));
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.CURVYCURV;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    new PathRender(COLOR_DATA_INDEXED.getColor(3), 1.5, CIRCLE, true).render(geometricLayer, graphics);
    Tensor sequence = getGeodesicControlPoints();
    new PathRender(COLOR_DATA_INDEXED.getColor(0), 1.5, sequence, true).render(geometricLayer, graphics);
    PolyclipResult polyclipResult = POLYGON_CLIP.apply(sequence);
    graphics.setColor(new Color(128, 255, 128, 128));
    Tensor result = polyclipResult.tensor();
    graphics.fill(geometricLayer.toPath2D(result));
    new PathRender(COLOR_DATA_INDEXED.getColor(1), 3.5, result, true).render(geometricLayer, graphics);
    {
      ColorPairIndexed colorPairIndexed = new ColorPairIndexed(COLOR_DATA_INDEXED, 128, 255);
      for (int index = 0; index < result.length(); ++index) {
        int cind = polyclipResult.belong().Get(index).number().intValue();
        manifoldDisplay.showPoints(colorPairIndexed.getColorPair(cind), RealScalar.of(2), Tensors.of(result.get(index))) //
            .render(geometricLayer, graphics);
      }
    }
    Tensor nsum = Array.zeros(2);
    {
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
      graphics.draw(geometricLayer.toLine2D(nsum));
      geometricLayer.popMatrix();
    }
    LeversRender leversRender = LeversRender.of(manifoldDisplay, result, null, geometricLayer, graphics);
    leversRender.renderIndexP();
    // new PathRender(COLOR_DATA_INDEXED.getColor(1), 2.5f).setCurve(HILBERT, false).render(geometricLayer, graphics);
  }

  static void main() {
    new PolygonClipDemo().runStandalone();
  }
}
