// code by jph
package ch.alpine.ascona.crv.clt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.ren.RenderInterface;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophis.crv.clt.Clothoid;
import ch.alpine.sophis.crv.clt.ClothoidContext;
import ch.alpine.sophis.crv.clt.ClothoidEmit;
import ch.alpine.sophis.crv.clt.ClothoidSolutions;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;

record ClothoidDefectContainer(ClothoidContext clothoidContext, ClothoidSolutions clothoidSolutions) implements RenderInterface {
  private static final Scalar DENOM = RealScalar.of(5.0);

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    PathRender pathRender = new PathRender(new Color(0, 0, 0, 128));
    Tensor tensor = clothoidSolutions.defectsXY();
    pathRender.setCurve(tensor, false);
    pathRender.render(geometricLayer, graphics);
    Tensor lambdas = clothoidSolutions.lambdas();
    List<Clothoid> clothoids = ClothoidEmit.stream(clothoidContext, lambdas).toList();
    for (int index = 0; index < lambdas.length(); ++index) {
      Scalar lambda = lambdas.Get(index);
      Clothoid clothoid = clothoids.get(index);
      {
        Scalar length = clothoid.length().divide(DENOM);
        graphics.setColor(new Color(0, 128, 0));
        graphics.setStroke(new BasicStroke(2f));
        graphics.draw(geometricLayer.toLine2D(Tensors.of(lambda, length.zero()), Tensors.of(lambda, length)));
        graphics.setStroke(new BasicStroke(1f));
      }
      {
        Scalar length = clothoid.length();
        graphics.setColor(new Color(0, 128, 0));
        graphics.setStroke(new BasicStroke(2f));
        Scalar x = lambda.add(RealScalar.of(0.1));
        graphics.draw(geometricLayer.toLine2D(Tensors.of(x, length.zero()), Tensors.of(x, length)));
        graphics.setStroke(new BasicStroke(1f));
      }
    }
  }
}
