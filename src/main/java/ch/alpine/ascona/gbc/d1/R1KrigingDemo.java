// code by jph
package ch.alpine.ascona.gbc.d1;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import ch.alpine.ascona.util.dis.R2Display;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.ascona.util.win.LookAndFeels;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.sophus.itp.Kriging;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Join;
import ch.alpine.tensor.alg.Reverse;
import ch.alpine.tensor.alg.Sort;
import ch.alpine.tensor.alg.Transpose;
import ch.alpine.tensor.mat.DiagonalMatrix;
import ch.alpine.tensor.sca.Abs;

// TODO ASCONA DEMO behaves counter intuitively!?
public class R1KrigingDemo extends A1AveragingDemo {
  public R1KrigingDemo() {
    super(R2Display.INSTANCE);
    setControlPointsSe2(Tensors.fromString("{{0, 0, 0}, {1, 1, 1}, {2, 2, 0}}"));
  }

  @Override
  public void protected_render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor control = Sort.of(getControlPointsSe2());
    if (1 < control.length()) {
      Tensor support = control.get(Tensor.ALL, 0);
      Tensor funceva = control.get(Tensor.ALL, 1);
      Tensor cvarian = control.get(Tensor.ALL, 2).multiply(RationalScalar.HALF).map(Abs.FUNCTION);
      // ---
      graphics.setColor(new Color(0, 128, 128));
      Scalar IND = RealScalar.of(0.1);
      for (int index = 0; index < support.length(); ++index) {
        geometricLayer.pushMatrix(GfxMatrix.translation(control.get(index)));
        Scalar v = cvarian.Get(index);
        graphics.draw(geometricLayer.toLine2D(Tensors.of(v.zero(), v), Tensors.of(v.zero(), v.negate())));
        graphics.draw(geometricLayer.toLine2D(Tensors.of(IND, v), Tensors.of(IND.negate(), v)));
        graphics.draw(geometricLayer.toLine2D(Tensors.of(IND, v.negate()), Tensors.of(IND.negate(), v.negate())));
        geometricLayer.popMatrix();
      }
      // ---
      Tensor sequence = support.map(Tensors::of);
      Tensor covariance = DiagonalMatrix.with(cvarian);
      Sedarim weightingInterface = operator(sequence);
      try {
        Kriging kriging = Kriging.regression(weightingInterface, sequence, funceva, covariance);
        // ---
        Tensor domain = domain();
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

  public static void main(String[] args) {
    LookAndFeels.LIGHT.tryUpdateUI();
    new R1KrigingDemo().setVisible(1000, 800);
  }
}
