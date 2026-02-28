// code by jph
package ch.alpine.ascona.misc;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointTypes;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophus.hs.s.S3Hopf;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.num.Pi;
import ch.alpine.tensor.pdf.RandomSample;
import ch.alpine.tensor.sca.Clips;

class S3HopfDemo extends ControlPointsDemo {
  public S3HopfDemo() {
    setGeodesicControlPoints(RandomSample.of(manifoldDisplay().randomSampleInterface(), 3));
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.S2_ONLY;
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointTypes.SCATTERED;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
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
