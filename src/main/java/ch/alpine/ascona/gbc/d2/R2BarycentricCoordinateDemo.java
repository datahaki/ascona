// code by jph
package ch.alpine.ascona.gbc.d2;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.JToggleButton;

import ch.alpine.ascony.api.LogWeightings;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.msh.AveragedMovingDomain2D;
import ch.alpine.ascony.msh.Thinning;
import ch.alpine.ascony.ren.AxesRender;
import ch.alpine.ascony.ren.ColorPairs;
import ch.alpine.ascony.ren.ColorStroke;
import ch.alpine.ascony.ren.MeshRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.bridge.fig.Meshgrid;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.plt.ArrayPlot;
import ch.alpine.bridge.fig.plt.PolygonPlot;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.PvmBuilder;
import ch.alpine.sophis.crv.d2.alg.PolygonRegion;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophis.hull.d2.ConvexHull2D;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.Rational;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.opt.nd.CoordinateBounds;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;

final class R2BarycentricCoordinateDemo extends AbstractScatteredSetWeightingDemo {
  public static List<LogWeightings> list2() {
    List<LogWeightings> list = new ArrayList<>();
    // list.addAll(List.of(PolygonCoordinates.values()));
    Arrays.stream(LogWeightings.values()).forEach(list::add);
    // list.addAll(List.of(MixedLogWeightings.values()));
    return list;
  }

  // ---
  private final JToggleButton jToggleEntire = new JToggleButton("entire");

  public R2BarycentricCoordinateDemo() {
    super(list2());
    geometricComponent().addRenderInterfaceBackground(AxesRender.INSTANCE);
    weightingsParam.logWeightings = LogWeightings.COORDINATE;
    jToolBar().add(jToggleEntire);
    setControlPointsSe2(Tensors.fromString("{{0, -2, 0}, {3, -2, -1}, {4, 2, 1}, {-1, 3, 2}}"));
    Tensor pvm = PvmBuilder.rhs().setOffset(200, 600).setPerPixel(100).digest();
    geometricComponent().setModel2Pixel(pvm);
  }

  @Override
  protected Collection<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.R2_ONLY;
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.CURVYCURV;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ColorDataGradient colorDataGradient = scatteredSetParam.cdg;
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor controlPoints = getGeodesicControlPoints();
    HomogeneousSpace homogeneousSpace = manifoldDisplay.homogeneousSpace();
    // BiinvariantMean biinvariantMean = homogeneousSpace.biinvariantMean(Chop._08);
    if (2 < controlPoints.length()) {
      Tensor domain = Tensor.of(controlPoints.stream().map(manifoldDisplay::point2xy));
      PolygonRegion polygonRegion = new PolygonRegion(domain);
      Tensor hull = ConvexHull2D.of(domain);
      new PathRender(ColorStroke.CONVEX_HULL, hull, true).render(geometricLayer, graphics);
      Sedarim sedarim = weightingsParam.operator(manifoldDisplay.manifold(), domain);
      CoordinateBoundingBox cbb = CoordinateBounds.of(hull);
      Tensor weights = Meshgrid.of(cbb, scatteredSetParam.refine).image(sedarim::sunder);
      Tensor fallback = manifoldDisplay.indetPoint();
      AveragedMovingDomain2D averagedMovingDomain2D = new AveragedMovingDomain2D(weights, homogeneousSpace.biinvariantMean(), fallback);
      Tensor[][] array = averagedMovingDomain2D.forward(controlPoints);
      if (scatteredSetParam.show) { // render basis functions
        int n = weights.get(0, 0).length();
        Clip clip = cbb.clip(0);
        Clip clipx = Clips.interval(clip.min(), clip.min().add(clip.length().multiply(RealScalar.of(n))));
        CoordinateBoundingBox cbb_ext = CoordinateBoundingBox.of(clipx, cbb.clip(1));
        Show show = new Show();
        show.add(ArrayPlot.of(averagedMovingDomain2D.arrayReshape_weights(), cbb_ext, colorDataGradient, false));
        for (int i = 0; i < n; ++i) {
          final int fi = i; // render polygon on top of basis function
          TensorUnaryOperator tuo = p -> p.add(Tensors.of(clip.length().multiply(RealScalar.of(fi)), RealScalar.ZERO));
          show.add(PolygonPlot.of(tuo.slash(controlPoints))).setColor(Color.BLACK);
        }
        Dimension dimension = geometricComponent().getSize();
        show.render_autoIndent(graphics, new Rectangle(0, 0, dimension.width - 100, 300));
      }
      // render grid lines functions
      new MeshRender(array, colorDataGradient.deriveWithOpacity(Rational.HALF)) //
          .render(geometricLayer, graphics);
      if (scatteredSetParam.arrows) {
        Tensor points = Thinning.flatten(array, 2, 2);
        manifoldDisplay.showPoints(ColorPairs.INTERMEDIATE, RealScalar.of(0.5), points) //
            .render(geometricLayer, graphics);
      }
    }
  }

  static void main() {
    new R2BarycentricCoordinateDemo().runStandalone();
  }
}
