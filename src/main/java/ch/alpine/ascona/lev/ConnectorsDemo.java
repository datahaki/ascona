// code by jph
package ch.alpine.ascona.lev;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.alg.UnitVector;
import ch.alpine.tensor.itp.Interpolation;
import ch.alpine.tensor.itp.LinearInterpolation;
import ch.alpine.tensor.sca.Chop;

// TODO ASCONA cannot always compute the biinvariant mean for S2/SE(2) ...
/* package */ class ConnectorsDemo extends AbstractHoverDemo {
  public ConnectorsDemo() {
    super(10);
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics, LeversRender leversRender) {
    Tensor weights = operator(leversRender.getSequence()).sunder(leversRender.getOrigin());
    leversRender.renderLevers(weights);
    // ---
    leversRender.renderWeights(weights);
    leversRender.renderSequence();
    leversRender.renderOrigin();
    // ---
    Tensor controlPoints = leversRender.getSequence();
    int length = controlPoints.length();
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
    BiinvariantMean biinvariantMean = homogeneousSpace.biinvariantMean(Chop._08);
    graphics.setColor(Color.RED);
    Tensor domain = Subdivide.of(0.0, 1.0, 20);
    for (int index = 0; index < length; ++index) {
      Tensor blend = UnitVector.of(length, index);
      Interpolation interpolation = LinearInterpolation.of(Tensors.of(weights, blend));
      try {
        Tensor map = Tensor.of(domain.stream() //
            .map(Scalar.class::cast) //
            .map(interpolation::at) //
            .map(w -> biinvariantMean.mean(controlPoints, w)) //
            .map(manifoldDisplay::point2xy));
        Path2D path2d = geometricLayer.toPath2D(map);
        graphics.draw(path2d);
      } catch (Exception e) {
        System.err.println("no can do: " + manifoldDisplay);
      }
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
