// code by jph
package ch.alpine.ascona.misc;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.Optional;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophis.crv.d2.alg.ConvexHull2D;
import ch.alpine.sophis.crv.d2.ex.StarPoints;
import ch.alpine.sophis.crv.dub.DubinsGenerator;
import ch.alpine.sophis.dv.Biinvariant;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophis.fit.HsWeiszfeldMethod;
import ch.alpine.sophis.fit.SpatialMedian;
import ch.alpine.sophis.fit.SphereFit;
import ch.alpine.sophis.fit.WeiszfeldMethod;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.lie.se2.Se2Matrix;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Outer;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.lie.rot.CirclePoints;
import ch.alpine.tensor.nrm.Vector2Norm;
import ch.alpine.tensor.opt.hun.BipartiteMatching;
import ch.alpine.tensor.red.Times;
import ch.alpine.tensor.sca.Chop;
import ch.alpine.tensor.sca.var.InversePowerVariogram;

// TODO ASCONA REV name of demo !?
// ... toggle convex hull, matching, median ...
public class SphereFitDemo extends ControlPointsDemo {
  private static final ColorDataIndexed COLOR_DATA_INDEXED = ColorDataLists._097.cyclic();
  private static final Tensor CIRCLE = CirclePoints.of(10).multiply(RealScalar.of(3));
  // ---
  private final PathRender pathRenderBall = new PathRender(COLOR_DATA_INDEXED.getColor(0), 1.5f);
  private final PathRender pathRenderHull = new PathRender(COLOR_DATA_INDEXED.getColor(1), 1.5f);

  public SphereFitDemo() {
    super(new AsconaParam(true, ManifoldDisplays.R2_ONLY));
    controlPointsRender.setMidpointIndicated(false);
    // ---
    timerFrame.geometricComponent.addRenderInterface(pathRenderHull);
    // ---
    Tensor blub = Tensors.fromString(
        "{{1, 0, 0}, {1, 0, 0}, {2, 0, 2.5708}, {1, 0, 2.1}, {1.5, 0, 0}, {2.3, 0, -1.2}, {1.5, 0, 0}, {4, 0, 3.14159}, {2, 0, 3.14159}, {2, 0, 0}}");
    setControlPointsSe2(DubinsGenerator.of(Tensors.vector(0, 0, 2.1), //
        Tensor.of(blub.stream().map(Times.operator(Tensors.vector(2, 1, 1))))));
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor control = getGeodesicControlPoints();
    if (!Tensors.isEmpty(control)) {
      Optional<SphereFit> optional = SphereFit.of(control);
      if (optional.isPresent()) {
        Tensor center = optional.get().center();
        Scalar radius = optional.get().radius();
        geometricLayer.pushMatrix(Se2Matrix.translation(center));
        pathRenderBall.setCurve(CirclePoints.of(40).multiply(radius), true);
        pathRenderBall.render(geometricLayer, graphics);
        geometricLayer.popMatrix();
      }
    }
    pathRenderHull.setCurve(ConvexHull2D.of(control), true);
    new PathRender(Color.GRAY).setCurve(CIRCLE, true).render(geometricLayer, graphics);
    if (!Tensors.isEmpty(control)) {
      Tensor matrix = Outer.of(Vector2Norm::between, control, CIRCLE);
      BipartiteMatching bipartiteMatching = BipartiteMatching.of(matrix);
      int[] matching = bipartiteMatching.matching();
      graphics.setColor(Color.RED);
      for (int index = 0; index < matching.length; ++index)
        if (matching[index] != BipartiteMatching.UNASSIGNED) {
          Path2D path2d = geometricLayer.toPath2D(Tensors.of(control.get(index), CIRCLE.get(matching[index])));
          graphics.draw(path2d);
        }
    }
    if (!Tensors.isEmpty(control)) {
      Tensor weiszfeld = new WeiszfeldMethod(Chop._04).uniform(control).get();
      geometricLayer.pushMatrix(Se2Matrix.translation(weiszfeld));
      Path2D path2d = geometricLayer.toPath2D(manifoldDisplay.shape());
      path2d.closePath();
      graphics.setColor(new Color(128, 128, 255, 64));
      graphics.fill(path2d);
      graphics.setColor(new Color(128, 128, 255, 255));
      graphics.draw(path2d);
      geometricLayer.popMatrix();
    }
    if (!Tensors.isEmpty(control)) {
      HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
      Biinvariant biinvariant = Biinvariants.METRIC.ofSafe(homogeneousSpace);
      Sedarim sedarim = biinvariant.weighting(InversePowerVariogram.of(1), control);
      SpatialMedian spatialMedian = new HsWeiszfeldMethod(homogeneousSpace.biinvariantMean(), sedarim, Chop._06);
      Optional<Tensor> optional = spatialMedian.uniform(control);
      if (optional.isPresent()) {
        Tensor weiszfeld = optional.get();
        geometricLayer.pushMatrix(Se2Matrix.translation(weiszfeld));
        Path2D path2d = geometricLayer.toPath2D(StarPoints.of(5, 0.2, 0.05));
        path2d.closePath();
        graphics.setColor(new Color(128, 128, 255, 64));
        graphics.fill(path2d);
        graphics.setColor(new Color(128, 128, 255, 255));
        graphics.draw(path2d);
        geometricLayer.popMatrix();
      }
    }
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, control, null, geometricLayer, graphics);
      // leversRender.renderSequence();
      leversRender.renderIndexP();
    }
  }

  static void main() {
    new SphereFitDemo().runStandalone();
  }
}
