// code by jph
package ch.alpine.ascona.misc;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.hs.s.S3Hopf;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.num.Pi;
import ch.alpine.tensor.sca.Clips;

public class S3HopfDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param0 extends AsconaParam {
    public Param0() {
      super(true, ManifoldDisplays.S2_ONLY);
    }
  }

  private final Param0 param0;

  public S3HopfDemo() {
    this(new Param0());
  }

  public S3HopfDemo(Param0 param0) {
    super(param0);
    this.param0 = param0;
    controlPointsRender.setMidpointIndicated(false);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    {
      Tensor domain = Subdivide.increasing(Clips.absolute(Pi.VALUE), 30);
      PathRender pathRender = new PathRender(Color.BLUE);
      TensorUnaryOperator tuo = t -> Tensors.of(t.Get(1), t.Get(3));
      for (Tensor xyz : getGeodesicControlPoints()) {
        S3Hopf s3Hopf = S3Hopf.northernHemisphereGauge(xyz);
        Tensor tensor = tuo.slash(domain.maps(s3Hopf::lift));
        pathRender.setCurve(tensor, true);
        pathRender.render(geometricLayer, graphics);
      }
    }
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, getGeodesicControlPoints(), null, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexP();
    }
  }

  static void main() {
    new S3HopfDemo().runStandalone();
  }
}
