// code by jph
package ch.alpine.ascona.gbc.d2;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.List;
import java.util.Objects;

import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.ascona.util.arp.ArrayPlotImage;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.ren.MeshRender;
import ch.alpine.ascona.util.ren.PointsRender;
import ch.alpine.ascona.util.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.dv.Biinvariants;
import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.sophus.math.var.InversePowerVariogram;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Rescale;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.sca.N;

// TODO ASCONA maps to target every frame right now
/* package */ abstract class AbstractDeformationDemo extends ControlPointsDemo {
  private static final PointsRender POINTS_RENDER_POINTS = //
      new PointsRender(new Color(64, 128, 64, 64), new Color(64, 128, 64, 255));
  private static final Stroke STROKE = //
      new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0);
  /** for parameterization of geodesic */
  private static final Tensor DOMAIN = Subdivide.of(0.0, 1.0, 10);

  // ---
  @ReflectionMarker
  public static class Param0 extends AsconaParam {
    public Param0(List<ManifoldDisplays> list) {
      super(false, list);
    }

    public LogWeightings logWeightings = LogWeightings.COORDINATE;
    public Biinvariants biinvariants = Biinvariants.METRIC;
    public ColorDataGradients cdg = ColorDataGradients.RAINBOW;
    public Scalar refine = RealScalar.of(20);
    public Boolean target = true;
    @FieldFuse
    public transient Boolean snap = true; // true intentional
  }

  @ReflectionMarker
  public static class Param1 {
    @FieldClip(min = "3", max = "12")
    public Integer length = 6;
  }

  private final Param0 param0;
  private final Param1 param1;
  // ---
  /** in coordinate specific to geodesic display */
  private Tensor movingOrigin;
  private MovingDomain2D movingDomain2D;

  protected AbstractDeformationDemo(List<ManifoldDisplays> list, Object object) {
    this(new Param0(list), new Param1(), object);
  }

  protected AbstractDeformationDemo(Param0 param0, Param1 param1, Object object) {
    super(param0, param1, object);
    this.param0 = param0;
    this.param1 = param1;
    fieldsEditor(0).addUniversalListener(this::recompute);
    fieldsEditor(1).addUniversalListener(this::shuffleSnap);
    fieldsEditor(2).addUniversalListener(this::recompute);
    // ---
    setControlPointsSe2(shufflePointsSe2(param1.length));
  }

  protected final void shuffleSnap() {
    setControlPointsSe2(shufflePointsSe2(param1.length));
    param0.snap = true;
    recompute();
  }

  protected final void recompute() {
    if (param0.snap) {
      param0.snap = false;
      ManifoldDisplay manifoldDisplay = manifoldDisplay();
      movingOrigin = Tensor.of(getControlPointsSe2().map(N.DOUBLE).stream().map(manifoldDisplay::xya2point));
    }
    System.out.println("recomp");
    movingDomain2D = updateMovingDomain2D(movingOrigin, param0.refine.number().intValue());
  }

  protected final Sedarim operator(Tensor sequence) {
    Manifold manifold = (Manifold) param0.manifoldDisplays.geodesicSpace();
    return param0.logWeightings.sedarim(param0.biinvariants.ofSafe(manifold), InversePowerVariogram.of(2), sequence);
  }

  @Override // from RenderInterface
  public final synchronized void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    if (Objects.isNull(movingDomain2D))
      recompute();
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor origin = movingDomain2D.origin();
    Tensor target = getGeodesicControlPoints();
    // ---
    {
      ColorDataGradient colorDataGradient = param0.cdg.deriveWithOpacity(RealScalar.of(0.5));
      new MeshRender(movingDomain2D.forward(target, biinvariantMean()), colorDataGradient) //
          .render(geometricLayer, graphics);
    }
    if (param0.target) { // connect origin and target pairs with lines/geodesics
      GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
      graphics.setColor(new Color(128, 128, 128, 255));
      graphics.setStroke(STROKE);
      for (int index = 0; index < origin.length(); ++index) {
        ScalarTensorFunction scalarTensorFunction = //
            geodesicSpace.curve(origin.get(index), target.get(index));
        Tensor points = Tensor.of(DOMAIN.map(scalarTensorFunction).stream() //
            .map(manifoldDisplay::point2xy));
        graphics.draw(geometricLayer.toPath2D(points));
      }
      graphics.setStroke(new BasicStroke(1));
    }
    POINTS_RENDER_POINTS //
        .show(manifoldDisplay::matrixLift, shapeOrigin(), origin) //
        .render(geometricLayer, graphics);
    LeversRender leversRender = LeversRender.of(manifoldDisplay, param0.target //
        ? getGeodesicControlPoints()
        : origin, null, geometricLayer, graphics);
    leversRender.renderIndexP(param0.target ? "q" : "p");
    {
      Rescale rescale = new Rescale(movingDomain2D.arrayReshape_weights());
      ArrayPlotImage.of(rescale.result(), rescale.scalarSummaryStatistics().getClip(), param0.cdg).draw(graphics);
    }
  }

  protected abstract Tensor shufflePointsSe2(int n);

  /** @return method to compute mean (for instance approximation instead of exact mean) */
  protected abstract BiinvariantMean biinvariantMean();

  protected abstract MovingDomain2D updateMovingDomain2D(Tensor movingOrigin, int res);

  protected abstract Tensor shapeOrigin();
}
