// code by jph
package ch.alpine.ascona.ext;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import ch.alpine.ascony.win.AbstractDemo;
import ch.alpine.bridge.fig.ListLinePlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.hs.Exponential;
import ch.alpine.sophus.hs.st.StiefelManifold;
import ch.alpine.sophus.hs.st.TStMemberQ;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.lie.rot.CirclePoints;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.pdf.RandomSample;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.NormalDistribution;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;

public class StExponentialDemo extends AbstractDemo {
  @ReflectionMarker
  public static class Param {
    @FieldSlider
    @FieldClip(min = "-3", max = "30")
    public Scalar scalar = RealScalar.of(0);
  }

  private final Param param;
  private final Tensor circle = CirclePoints.of(50);
  private final int n = 5;
  private final int k = 2;
  private final StiefelManifold stiefelManifold = new StiefelManifold(n, k);
  private Tensor p;
  private Tensor v;

  public StExponentialDemo() {
    this(new Param());
  }

  public StExponentialDemo(Param param) {
    super(param);
    this.param = param;
    p = RandomSample.of(stiefelManifold);
    v = new TStMemberQ(p).projection(RandomVariate.of(NormalDistribution.of(0, 0.4), k, n));
    circle.append(circle.get(0));
  }

  @Override // from RenderInterface
  public synchronized void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Exponential exponential = stiefelManifold.exponential(p);
    ScalarTensorFunction stf = s -> exponential.exp(v.multiply(s));
    Clip clip = Clips.translation(param.scalar).apply(Clips.absolute(2));
    Tensor res = Subdivide.increasing(clip, 20).maps(stf);
    Show show = new Show();
    show.add(ListLinePlot.of(circle));
    for (int i = 0; i < n; ++i)
      show.add(ListLinePlot.of(res.get(Tensor.ALL, Tensor.ALL, i)));
    show.setCbb(CoordinateBoundingBox.of(Clips.absoluteOne(), Clips.absoluteOne()));
    Dimension dimension = timerFrame.geometricComponent.jComponent.getSize();
    show.setAspectRatioOne();
    show.render(graphics, new Rectangle(20, 20, dimension.width - 50, dimension.height - 50));
  }

  static void main() {
    launch();
  }
}
