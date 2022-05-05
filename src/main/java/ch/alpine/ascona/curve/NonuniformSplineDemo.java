// code by jph
package ch.alpine.ascona.curve;

import java.awt.Graphics2D;
import java.util.Arrays;

import ch.alpine.ascona.util.api.ControlPointsDemo;
import ch.alpine.ascona.util.api.Curvature2DRender;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.bridge.win.AxesRender;
import ch.alpine.sophus.crv.spline.GeodesicBSplineFunction;
import ch.alpine.sophus.lie.rn.RnGeodesic;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Last;
import ch.alpine.tensor.alg.Ordering;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.alg.Transpose;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;

@ReflectionMarker
public class NonuniformSplineDemo extends ControlPointsDemo {
  @FieldInteger
  @FieldSelectionArray(value = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" })
  public Scalar degree = RealScalar.of(1);
  @FieldInteger
  @FieldSelectionArray(value = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" })
  public Scalar refine = RealScalar.of(4);

  public NonuniformSplineDemo() {
    super(true, ManifoldDisplays.R2_ONLY);
    // ---
    ToolbarFieldsEditor.add(this, timerFrame.jToolBar);
    // ---
    setControlPointsSe2(Tensors.fromString("{{0, 0, 0}, {1, 0, 0}}"));
    // ---
    timerFrame.geometricComponent.addRenderInterfaceBackground(AxesRender.INSTANCE);
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    int _degree = degree.number().intValue();
    int _levels = refine.number().intValue();
    Tensor control = getGeodesicControlPoints();
    // ---
    Tensor _effective = control;
    // ---
    int[] array = Ordering.INCREASING.of(_effective.get(Tensor.ALL, 0));
    Tensor x = Tensor.of(Arrays.stream(array).mapToObj(i -> _effective.get(i, 0)));
    Tensor y = Tensor.of(Arrays.stream(array).mapToObj(i -> _effective.get(i, 1)));
    ScalarTensorFunction scalarTensorFunction = //
        GeodesicBSplineFunction.of(RnGeodesic.INSTANCE, _degree, x, y);
    Clip clip = Clips.interval(x.Get(0), Last.of(x));
    Tensor domain = Subdivide.increasing(clip, 4 << _levels);
    Tensor values = domain.map(scalarTensorFunction);
    renderControlPoints(geometricLayer, graphics);
    Curvature2DRender.of(Transpose.of(Tensors.of(domain, values)), false, geometricLayer, graphics);
  }

  public static void main(String[] args) {
    new NonuniformSplineDemo().setVisible(1000, 800);
  }
}
