// code by jph
package ch.alpine.ascona.gbc.d1;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import ch.alpine.ascony.api.LogWeightings;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.dv.Kriging;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.sophus.lie.se2.Se2Matrix;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Join;
import ch.alpine.tensor.alg.Reverse;
import ch.alpine.tensor.alg.Sort;
import ch.alpine.tensor.alg.Transpose;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.mat.DiagonalMatrix;
import ch.alpine.tensor.sca.Abs;
import ch.alpine.tensor.sca.var.InversePowerVariogram;

// TODO ASCONA DEMO behaves counter intuitively!?
public class R1KrigingDemo extends A1AveragingDemo {
  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, ManifoldDisplays.R2_ONLY);
    }

    public LogWeightings logWeightings = LogWeightings.WEIGHTING;
    public Biinvariants biinvariants = Biinvariants.METRIC;
    public Boolean type = false;
    @FieldSelectionArray({ "30", "40", "50", "75", "100", "150", "200", "250" })
    public Integer resolution = 40;
    public ColorDataGradients cdg = ColorDataGradients.PARULA;
  }

  private final Param param;

  public R1KrigingDemo() {
    this(new Param());
  }

  public R1KrigingDemo(Param param) {
    super(param);
    this.param = param;
    controlPointsRender.setMidpointIndicated(false);
    // ---
    setControlPointsSe2(Tensors.fromString("{{0, 0, 0}, {1, 1, 1}, {2, 2, 0}}"));
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor control = Sort.of(getControlPointsSe2());
    if (1 < control.length()) {
      Tensor support = control.get(Tensor.ALL, 0);
      Tensor funceva = control.get(Tensor.ALL, 1);
      Tensor cvarian = control.get(Tensor.ALL, 2).multiply(RationalScalar.HALF).maps(Abs.FUNCTION);
      // ---
      graphics.setColor(new Color(0, 128, 128));
      Scalar IND = RealScalar.of(0.1);
      for (int index = 0; index < support.length(); ++index) {
        geometricLayer.pushMatrix(Se2Matrix.translation(control.get(index)));
        Scalar v = cvarian.Get(index);
        graphics.draw(geometricLayer.toLine2D(Tensors.of(v.zero(), v), Tensors.of(v.zero(), v.negate())));
        graphics.draw(geometricLayer.toLine2D(Tensors.of(IND, v), Tensors.of(IND.negate(), v)));
        graphics.draw(geometricLayer.toLine2D(Tensors.of(IND, v.negate()), Tensors.of(IND.negate(), v.negate())));
        geometricLayer.popMatrix();
      }
      // ---
      Tensor sequence = support.maps(Tensors::of);
      Tensor covariance = DiagonalMatrix.sparse(cvarian);
      Manifold manifold = (Manifold) manifoldDisplay().geodesicSpace();
      Sedarim sedarim = param.logWeightings.sedarim(param.biinvariants.ofSafe(manifold), InversePowerVariogram.of(2), sequence);
      // Sedarim sedarim = operator(sequence);
      try {
        Kriging kriging = Kriging.regression(sedarim, sequence, funceva, covariance);
        // ---
        Tensor domain = StaticHelper.domain(getControlPointsSe2());
        Tensor result = Tensor.of(domain.stream().map(Tensors::of).map(kriging::estimate));
        Tensor errors = Tensor.of(domain.stream().map(Tensors::of).map(kriging::variance));
        // ---
        Path2D path2d = geometricLayer.toPath2D(Join.of( //
            Transpose.of(Tensors.of(domain, result.add(errors))), //
            Reverse.of(Transpose.of(Tensors.of(domain, result.subtract(errors))))));
        graphics.setColor(new Color(128, 128, 128, 32));
        graphics.fill(path2d);
        new PathRender(Color.BLUE, 1.25f) //
            .setCurve(Transpose.of(Tensors.of(domain, result)), false) //
            .render(geometricLayer, graphics);
      } catch (Exception exception) {
        // ---
      }
    }
  }

  static void main() {
    launch();
  }
}
