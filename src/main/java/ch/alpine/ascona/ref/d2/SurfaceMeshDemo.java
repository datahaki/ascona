// code by jph
package ch.alpine.ascona.ref.d2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.HashSet;
import java.util.Set;

import ch.alpine.ascona.util.api.ControlPointsDemo;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.AxesRender;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.ascona.util.win.LookAndFeels;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.FieldPreferredWidth;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.crv.d2.PolygonArea;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.hs.r2.Extract2D;
import ch.alpine.sophus.ref.d2.SurfaceMeshRefinement;
import ch.alpine.sophus.srf.SurfaceMesh;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Sort;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.sca.Chop;
import ch.alpine.tensor.sca.Sign;

public class SurfaceMeshDemo extends ControlPointsDemo {
  private static final ColorDataIndexed COLOR_DATA_INDEXED_DRAW = ColorDataLists._097.cyclic().deriveWithAlpha(192);
  private static final ColorDataIndexed COLOR_DATA_INDEXED_FILL = ColorDataLists._097.cyclic().deriveWithAlpha(192);

  // ---
  @ReflectionMarker
  public static class Param {
    public Boolean axes = true;
    public Boolean ctrl = true;
    public SurfaceMeshRefinements ref = SurfaceMeshRefinements.LINEAR;
    @FieldSlider
    @FieldPreferredWidth(100)
    @FieldInteger
    @FieldClip(min = "0", max = "4")
    public Scalar refine = RealScalar.of(0);
  }

  private final Param param = new Param();
  private final SurfaceMesh surfaceMesh = SurfaceMeshExamples.mixed11();

  // TODO ASCONA DEMO needs BM
  public SurfaceMeshDemo() {
    super(false, ManifoldDisplays.SE2C_R2);
    ToolbarFieldsEditor.add(param, timerFrame.jToolBar);
    // ---
    setControlPointsSe2(surfaceMesh.vrt);
    // ---
    timerFrame.geometricComponent.setOffset(100, 600);
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    if (param.axes)
      AxesRender.INSTANCE.render(geometricLayer, graphics);
    surfaceMesh.vrt = getControlPointsSe2();
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
    SurfaceMeshRefinement surfaceMeshRefinement = param.ref.operator(homogeneousSpace.biinvariantMean(Chop._08));
    SurfaceMesh refine = surfaceMesh;
    for (int count = 0; count < param.refine.number().intValue(); ++count)
      refine = surfaceMeshRefinement.refine(refine);
    if (false) {
      for (int index = 0; index < refine.faces().size(); ++index) {
        Tensor polygon_face = Tensor.of(refine.polygon_face(refine.face(index)).stream().map(Extract2D.FUNCTION));
        Scalar area = PolygonArea.of(polygon_face);
        if (Sign.isNegativeOrZero(area))
          System.err.println("neg");
      }
    }
    for (Tensor polygon : refine.polygons()) {
      Path2D path2d = geometricLayer.toPath2D(polygon);
      path2d.closePath();
      graphics.setColor(COLOR_DATA_INDEXED_DRAW.getColor(0));
      graphics.draw(path2d);
      graphics.setColor(COLOR_DATA_INDEXED_FILL.getColor(0));
      graphics.fill(path2d);
    }
    {
      // TODO levers render
      graphics.setColor(new Color(192, 192, 192, 192));
      Tensor shape = manifoldDisplay.shape().multiply(RealScalar.of(0.5));
      for (Tensor mean : refine.vrt) {
        geometricLayer.pushMatrix(manifoldDisplay.matrixLift(mean));
        graphics.fill(geometricLayer.toPath2D(shape));
        geometricLayer.popMatrix();
      }
    }
    if (param.ctrl) {
      GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
      Tensor domain = Subdivide.of(0.0, 1.0, 10);
      Set<Tensor> set = new HashSet<>();
      for (int[] array : surfaceMesh.faces()) {
        for (int index = 0; index < array.length; ++index) {
          int beg = array[index];
          int end = array[(index + 1) % array.length];
          if (set.add(Sort.of(Tensors.vector(beg, end)))) {
            ScalarTensorFunction scalarTensorFunction = //
                geodesicSpace.curve(surfaceMesh.vrt.get(beg), surfaceMesh.vrt.get(end));
            Tensor points = domain.map(scalarTensorFunction);
            new PathRender(new Color(0, 0, 255, 128), 1.5f).setCurve(points, false).render(geometricLayer, graphics);
          }
        }
      }
      {
        LeversRender leversRender = LeversRender.of(manifoldDisplay, surfaceMesh.vrt, null, geometricLayer, graphics);
        leversRender.renderSequence();
      }
    }
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.updateUI();
    new SurfaceMeshDemo().setVisible(1200, 800);
  }
}
