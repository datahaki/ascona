// code by jph
package ch.alpine.ascona.gbc.d1;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.ascona.util.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.dv.Biinvariants;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.sophus.math.var.InversePowerVariogram;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Transpose;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;

/* package */ abstract class A1BarycentricCoordinateDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, ManifoldDisplays.R2_ONLY);
    }

    public LogWeightings logWeightings = LogWeightings.LAGRAINATE;
    public Biinvariants biinvariants = Biinvariants.METRIC;
    @FieldSelectionArray({ "30", "40", "50", "75", "100", "150", "200", "250" })
    public Integer resolution = 50;
    public ColorDataGradients cdg = ColorDataGradients.PARULA;
  }

  private final Param param;

  protected A1BarycentricCoordinateDemo() {
    this(new Param());
  }

  protected A1BarycentricCoordinateDemo(Param param) {
    super(param);
    this.param = param;
    controlPointsRender.setMidpointIndicated(false);
    // ---
    setControlPointsSe2(Tensors.fromString("{{0, 0, 0}, {1, 1, 0}, {2, 2, 0}}"));
  }

  @Override // from RenderInterface
  public final void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor control = getGeodesicControlPoints();
    RenderQuality.setQuality(graphics);
    if (1 < control.length()) {
      Tensor support = control.get(Tensor.ALL, 0);
      Tensor funceva = control.get(Tensor.ALL, 1);
      // ---
      Tensor domain = domain(support);
      // ---
      Tensor sequence = support.map(this::lift);
      Manifold manifold = (Manifold) param.manifoldDisplays.manifoldDisplay().geodesicSpace();
      Sedarim sedarim = param.logWeightings.sedarim(param.biinvariants.ofSafe(manifold), InversePowerVariogram.of(2), sequence);
      ScalarTensorFunction scalarTensorFunction = //
          point -> sedarim.sunder(lift(point));
      Tensor basis = domain.map(scalarTensorFunction);
      {
        Tensor curve = Transpose.of(Tensors.of(domain, basis.dot(funceva)));
        new PathRender(Color.BLUE, 1.25f).setCurve(curve, false).render(geometricLayer, graphics);
      }
      ColorDataIndexed colorDataIndexed = ColorDataLists._097.cyclic();
      for (int index = 0; index < funceva.length(); ++index) {
        Color color = colorDataIndexed.getColor(index);
        Tensor curve = Transpose.of(Tensors.of(domain, basis.get(Tensor.ALL, index)));
        new PathRender(color, 1f).setCurve(curve, false).render(geometricLayer, graphics);
      }
      {
        LeversRender leversRender = LeversRender.of(manifoldDisplay(), control, null, geometricLayer, graphics);
        // leversRender.renderSequence();
        leversRender.renderIndexP();
      }
    }
  }

  abstract Tensor domain(Tensor support);

  abstract Tensor lift(Scalar x);
}
