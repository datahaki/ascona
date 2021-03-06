// code by jph
package ch.alpine.ascona.avg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Path2D;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.ren.AreaRender;
import ch.alpine.ascona.util.ren.RenderInterface;
import ch.alpine.ascona.util.sym.SymLink;
import ch.alpine.ascona.util.sym.SymLinkPart;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophus.crv.Transition;
import ch.alpine.sophus.crv.TransitionSpace;
import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.sca.Clips;

/** visualization of the geometric geodesic average */
/* package */ class GeometricSymLinkRender {
  private static final Stroke STROKE = //
      new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0);
  // ---
  private final ManifoldDisplay manifoldDisplay;
  public int steps = 9;

  public GeometricSymLinkRender(ManifoldDisplay manifoldDisplay) {
    this.manifoldDisplay = manifoldDisplay;
  }

  public class Link implements RenderInterface {
    private final SymLink symLink;

    public Link(SymLink symLink) {
      this.symLink = symLink;
    }

    @Override
    public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
      GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
      TransitionSpace transitionSpace = manifoldDisplay.transitionSpace();
      if (symLink instanceof SymLinkPart symLinkPart) {
        new Link(symLinkPart.lP).render(geometricLayer, graphics);
        new Link(symLinkPart.lQ).render(geometricLayer, graphics);
        {
          Tensor posP = symLinkPart.lP.getPosition(geodesicSpace);
          Tensor posQ = symLinkPart.lQ.getPosition(geodesicSpace);
          ScalarTensorFunction scalarTensorFunction = geodesicSpace.curve(posP, posQ);
          Tensor posM = scalarTensorFunction.apply(symLinkPart.lambda);
          graphics.setColor(new Color(0, 128 + 64, 0, 255));
          Scalar resolution = RealScalar.of(geometricLayer.pixel2modelWidth(4));
          {
            Transition transition = transitionSpace.connect(posP, posM);
            Tensor tensor = transition.linearized(resolution);
            Tensor points = Tensor.of(tensor.stream().map(manifoldDisplay::point2xy));
            Path2D path2d = geometricLayer.toPath2D(points);
            graphics.setStroke(new BasicStroke(1.5f));
            graphics.draw(path2d);
          }
          {
            Transition transition = transitionSpace.connect(posM, posQ);
            Tensor tensor = transition.linearized(resolution);
            Tensor points = Tensor.of(tensor.stream().map(manifoldDisplay::point2xy));
            Path2D path2d = geometricLayer.toPath2D(points);
            graphics.setStroke(STROKE);
            graphics.draw(path2d);
            graphics.setStroke(new BasicStroke(1f));
          }
          {
            Tensor tensor = Subdivide.increasing(Clips.unit(), steps).extract(1, steps) //
                .map(scalarTensorFunction);
            new AreaRender( //
                new Color(64, 128 + 64, 64, 128), //
                manifoldDisplay::matrixLift, manifoldDisplay.shape().multiply(RealScalar.of(0.5)), tensor) //
                    .render(geometricLayer, graphics);
          }
        }
        // ---
        Tensor p = symLink.getPosition(geodesicSpace);
        graphics.setColor(new Color(0, 0, 255, 192));
        geometricLayer.pushMatrix(manifoldDisplay.matrixLift(p));
        Path2D path2d = geometricLayer.toPath2D(manifoldDisplay.shape().multiply(RealScalar.of(0.7)));
        path2d.closePath();
        graphics.fill(path2d);
        geometricLayer.popMatrix();
      }
    }
  }
}
