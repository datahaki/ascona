// code by jph
package ch.alpine.ascona.gbc.d2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.MeshRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophis.gbc.d2.InsidePolygonCoordinate;
import ch.alpine.sophis.gbc.d2.SPatch;
import ch.alpine.sophis.gbc.d2.ThreePointCoordinate;
import ch.alpine.sophis.gbc.d2.ThreePointScalings;
import ch.alpine.sophis.math.api.Genesis;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.Rational;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Outer;
import ch.alpine.tensor.alg.PadRight;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.sca.N;

public class SPatchDemo extends ControlPointsDemo {
  private final SPatch sPatch;
  private final MovingDomain2D movingDomain2D;

  public SPatchDemo() {
    super(new AsconaParam(false, ManifoldDisplays.SE2C_R2));
    // ---
    Genesis genesis = new InsidePolygonCoordinate(ThreePointCoordinate.of(ThreePointScalings.MEAN_VALUE));
    sPatch = SPatch.of(5, genesis, 2);
    Tensor embed = sPatch.getEmbed();
    setControlPointsSe2(Tensor.of(embed.stream() //
        .map(xy -> xy.multiply(RealScalar.of(3))).map(PadRight.zeros(3))));
    int res = 35;
    Tensor dx = Subdivide.of(-1, 1, res - 1).maps(N.DOUBLE);
    Tensor dy = Subdivide.of(-1, 1, res - 1).maps(N.DOUBLE);
    Tensor domain = Outer.of(Tensors::of, dx, dy);
    movingDomain2D = AveragedMovingDomain2D.of(embed, sPatch, domain);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor sequence = getGeodesicControlPoints();
    HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
    {
      Tensor[][] forward = movingDomain2D.forward(sequence, homogeneousSpace.biinvariantMean());
      new MeshRender(forward, ColorDataGradients.CLASSIC.deriveWithOpacity(Rational.HALF)) //
          .render(geometricLayer, graphics);
      Tensor shape = manifoldDisplay.shape().multiply(RealScalar.of(0.5));
      int x = 0;
      for (Tensor[] row : forward) {
        if (x % 2 == 0) {
          int y = 0;
          for (Tensor xya : row) {
            if (y % 2 == 0) {
              geometricLayer.pushMatrix(manifoldDisplay.matrixLift(xya));
              Path2D path2d = geometricLayer.toPath2D(shape);
              graphics.setColor(new Color(128, 128, 128, 64));
              graphics.fill(path2d);
              geometricLayer.popMatrix();
            }
            ++y;
          }
        }
        ++x;
      }
    }
    {
      int n = 5;
      Tensor domain = Subdivide.of(0.0, 1.0, 15);
      graphics.setColor(new Color(0, 0, 0, 128));
      for (int i = 0; i < n; ++i)
        for (int j = 0; j < n; ++j) {
          Tensor r0 = Tensors.vector(i, j);
          Tensor r1 = Tensors.vector(i, (j + 1) % n);
          int i0 = sPatch.basis(r0);
          int i1 = sPatch.basis(r1);
          ScalarTensorFunction stf = homogeneousSpace.curve(sequence.get(i0), sequence.get(i1));
          Tensor points = Tensor.of(domain.maps(stf).stream().map(manifoldDisplay::point2xy));
          Path2D path2d = geometricLayer.toPath2D(points);
          graphics.draw(path2d);
        }
    }
    LeversRender leversRender = LeversRender.of(manifoldDisplay, sequence, null, geometricLayer, graphics);
    leversRender.renderSequence();
  }

  static void main() {
    new SPatchDemo().runStandalone();
  }
}
