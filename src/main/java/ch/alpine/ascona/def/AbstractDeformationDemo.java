// code by jph
package ch.alpine.ascona.def;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.msh.MovingDomain2D;
import ch.alpine.ascony.ren.ColorPair;
import ch.alpine.ascony.ren.GridRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.MeshRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.fig.Meshgrid;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.plt.ArrayPlot;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.d2.ex.Box2D;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Append;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.jet.AppendOne;
import ch.alpine.tensor.lie.rot.CirclePoints;
import ch.alpine.tensor.nrm.Vector2Norm;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.pdf.RandomSample;
import ch.alpine.tensor.sca.Clips;

abstract class AbstractDeformationDemo extends ControlPointsDemo {
  static final Stroke STROKE = //
      new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0);
  /** for parameterization of geodesic */
  static final Tensor DOMAIN = Subdivide.of(0.0, 1.0, 10);

  @ReflectionMarker
  static class Param0 {
    @FieldClip(min = "3", max = "15")
    public Integer length = 8;
  }

  @ReflectionMarker
  static class Param2 {
    public ColorDataGradients cdg = ColorDataGradients.RAINBOW;
    public Boolean show = true;
    public Boolean target = true;
  }

  private final Param0 param0;
  private final Param2 param2;
  MovingDomain2D movingDomain2D;

  public AbstractDeformationDemo(Object obj2) {
    super(param0 = new Param0(), obj2, param2 = new Param2());
    fieldsEditor(param0).addUniversalListener(this::shuffleSnap);
    geometricComponent().addRenderInterfaceBackground(new GridRender(geometricComponent()::getSize));
  }

  @Override
  protected final ControlPointType controlPointType() {
    return ControlPointType.HEAD_TAIL;
  }

  protected final Tensor shufflePoints(int n) {
    return switch (getSelectedMD()) {
    case H2 -> CirclePoints.of(n).multiply(RealScalar.of(2));
    default -> RandomSample.of(manifoldDisplay().randomSampleInterface(), n);
    };
  }

  protected final Tensor updateWeights(Tensor movingOrigin, int res, Scalar s2z, Sedarim sedarim) {
    switch (getSelectedMD()) {
    case R2: {
      CoordinateBoundingBox cbb = manifoldDisplay().d2Raster_coordinateBoundingBox();
      return Meshgrid.of(cbb, res).image(sedarim::sunder);
    }
    case S2: {
      CoordinateBoundingBox cbb = manifoldDisplay().d2Raster_coordinateBoundingBox();
      TensorUnaryOperator tuo = xy -> Vector2Norm.NORMALIZE.apply(Append.of(xy, s2z));
      return Meshgrid.of(cbb, res).image(tuo.andThen(sedarim::sunder));
    }
    case H2:
      return Meshgrid.of(Box2D.xy(Clips.absolute(1.0)), res).image(sedarim::sunder);
    case Se2C:
    case Se2: {
      CoordinateBoundingBox cbb = Box2D.xy(Clips.absolute(2.0));
      return Meshgrid.of(cbb, res).image(AppendOne.FUNCTION.andThen(sedarim::sunder));
    }
    default:
      throw new IllegalArgumentException();
    }
  }

  protected final BiinvariantMean biinvariantMean() {
    HomogeneousSpace homogeneousSpace = manifoldDisplay().homogeneousSpace();
    return homogeneousSpace.biinvariantMean();
  }

  @Override // from RenderInterface
  public final void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor origin = movingOrigin();
    Tensor target = getGeodesicControlPoints();
    {
      ColorDataGradient colorDataGradient = param2.cdg.deriveWithOpacity(RealScalar.of(0.5));
      new MeshRender(movingDomain2D.forward(target), colorDataGradient) //
          .render(geometricLayer, graphics);
    }
    if (param2.target) { // connect origin and target pairs with lines/geodesics
      GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
      graphics.setColor(new Color(128, 128, 128, 255));
      graphics.setStroke(STROKE);
      for (int index = 0; index < origin.length(); ++index) {
        ScalarTensorFunction scalarTensorFunction = //
            geodesicSpace.curve(origin.get(index), target.get(index));
        Tensor points = Tensor.of(DOMAIN.maps(scalarTensorFunction).stream() //
            .map(manifoldDisplay::point2xy));
        graphics.draw(geometricLayer.toPath2D(points));
      }
      graphics.setStroke(new BasicStroke());
    }
    manifoldDisplay.showPoints(ColorPair.REFERENCE, RealScalar.of(0.8), origin) //
        .render(geometricLayer, graphics);
    LeversRender leversRender = LeversRender.of(manifoldDisplay, param2.target //
        ? getGeodesicControlPoints()
        : origin, null, geometricLayer, graphics);
    leversRender.renderIndexP(param2.target ? "q" : "p");
    if (param2.show) {
      Tensor weights = movingDomain2D.arrayReshape_weights();
      Show show = new Show();
      show.add(ArrayPlot.of(weights, param2.cdg));
      Dimension dimension = geometricComponent().getSize();
      show.render_autoIndent(graphics, new Rectangle(0, 0, dimension.width - 100, 300));
    }
  }

  protected abstract Tensor movingOrigin();

  protected abstract void shuffleSnap();

  protected final Param0 param0() {
    return param0;
  }
}
