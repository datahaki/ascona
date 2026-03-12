// code by jph
package ch.alpine.ascona.crv.sub;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.Curvature2DRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.api.TensorMetric;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.pdf.RandomSample;
import ch.alpine.tensor.sca.Round;

class GeodesicDemo extends ControlPointsDemo {
  private static final Color COLOR = new Color(128, 128, 128, 128);
  // ---
  private final PathRender pathRender = new PathRender(new Color(128, 128, 255), //
      new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0));

  @ReflectionMarker
  static class Param {
    @FieldSlider
    @FieldClip(min = "5", max = "20")
    public Integer splits = 10;
    public Boolean comb = false;
    public Boolean extrapolation = false;
  }

  private final Param param;

  public GeodesicDemo() {
    super(param = new Param());
    addChangeListener(this::shuffle);
    setManifoldDisplay(ManifoldDisplays.S2);
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.ALL;
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.HEAD_TAIL;
  }

  void shuffle() {
    setGeodesicControlPoints(RandomSample.of(manifoldDisplay().randomSampleInterface(), 2));
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
    Tensor points = getGeodesicControlPoints();
    Tensor p = points.get(0);
    Tensor q = points.get(1);
    ScalarTensorFunction scalarTensorFunction = geodesicSpace.curve(p, q);
    {
      Tensor domain = Subdivide.of(0, 1, 30);
      Tensor points2 = domain.maps(scalarTensorFunction);
      Tensor xys = manifoldDisplay.point2xy().slash(points2);
      graphics.setColor(new Color(128, 255, 0));
      graphics.setStroke(new BasicStroke(1.5f));
      graphics.draw(geometricLayer.toPath2D(xys));
    }
    if (geodesicSpace instanceof TensorMetric tensorMetric) {
      Scalar pseudoDistance = tensorMetric.distance(p, q);
      graphics.setColor(Color.DARK_GRAY);
      graphics.drawString("" + pseudoDistance.maps(Round._4), 10, 20);
    }
    manifoldDisplay.showPoints(COLOR, COLOR, RealScalar.ONE, Subdivide.of(0, 1, param.splits).maps(scalarTensorFunction)) //
        .render(geometricLayer, graphics);
    {
      Tensor sequence = Subdivide.of(0, 1, 1).maps(scalarTensorFunction);
      LeversRender leversRender = LeversRender.of(manifoldDisplay, sequence, null, geometricLayer, graphics);
      leversRender.renderIndexP();
    }
    if (param.comb) {
      Tensor refined = Subdivide.of(0, 1, param.splits * 6).maps(scalarTensorFunction);
      Tensor render = manifoldDisplay.point2xy().slash(refined);
      Curvature2DRender.of(render, false).render(geometricLayer, graphics);
    }
    if (param.extrapolation) {
      {
        Tensor refined = Subdivide.of(1, 1.5, param.splits * 3).maps(scalarTensorFunction);
        Tensor render = manifoldDisplay.point2xy().slash(refined);
        // CurveCurvatureRender.of(render, false, geometricLayer, graphics);
        pathRender.setCurve(render, false);
        pathRender.render(geometricLayer, graphics);
      }
      manifoldDisplay.showPoints( //
          new Color(255, 128, 128), COLOR, RealScalar.of(0.3), //
          Subdivide.of(1, 1.5, param.splits).maps(scalarTensorFunction)) //
          .render(geometricLayer, graphics);
    }
  }

  static void main() {
    new GeodesicDemo().runStandalone();
  }
}
