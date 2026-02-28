// code by jph
package ch.alpine.ascona.gbc.d2;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.List;
import java.util.Objects;

import ch.alpine.ascony.api.Box2D;
import ch.alpine.ascony.api.LogWeightings;
import ch.alpine.ascony.bas.AveragedMovingDomain2D;
import ch.alpine.ascony.bas.MovingDomain2D;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.MeshRender;
import ch.alpine.ascony.ren.PointsRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointTypes;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.fig.ArrayPlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.api.Manifold;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.alg.Outer;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.lie.rot.CirclePoints;
import ch.alpine.tensor.nrm.Vector2Norm;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.pdf.Distribution;
import ch.alpine.tensor.pdf.RandomSample;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.UniformDistribution;
import ch.alpine.tensor.sca.Clips;
import ch.alpine.tensor.sca.N;
import ch.alpine.tensor.sca.var.InversePowerVariogram;

// TODO ASCONA maps to target every frame right now
class DeformationDemo extends ControlPointsDemo {
  private static final PointsRender POINTS_RENDER_POINTS = //
      new PointsRender(new Color(64, 128, 64, 64), new Color(64, 128, 64, 255));
  private static final Stroke STROKE = //
      new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0);
  /** for parameterization of geodesic */
  private static final Tensor DOMAIN = Subdivide.of(0.0, 1.0, 10);

  // ---
  @ReflectionMarker
  public static class Param0 {
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
    public Integer length = 8;
  }

  private final Param0 param0;
  private final Param1 param1;
  // ---
  /** in coordinate specific to geodesic display */
  private Tensor movingOrigin;
  private MovingDomain2D movingDomain2D;

  protected DeformationDemo() {
    super(param0 = new Param0(), param1 = new Param1());
    fieldsEditor(0).addUniversalListener(this::recompute);
    fieldsEditor(1).addUniversalListener(this::shuffleSnap);
    // ---
    addChangeListener(this::shuffleSnap);
    shuffleSnap();
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.DEFORM_2D;
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointTypes.HEAD_TAIL;
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
      movingOrigin = Tensor.of(getControlPointsSe2().maps(N.DOUBLE).stream().map(manifoldDisplay::xya2point));
    }
    System.out.println("recomp");
    movingDomain2D = updateMovingDomain2D(movingOrigin, param0.refine.number().intValue());
  }

  protected final Sedarim operator(Tensor sequence) {
    Manifold manifold = manifoldDisplay().manifold();
    return param0.logWeightings.sedarim(param0.biinvariants.ofSafe(manifold), InversePowerVariogram.of(2), sequence);
  }

  @Override // from RenderInterface
  public final synchronized void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    timerFrame.geometricComponent.renderGrid(graphics);
    graphics.setClip(null);
    if (Objects.isNull(movingDomain2D))
      recompute();
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
        Tensor points = Tensor.of(DOMAIN.maps(scalarTensorFunction).stream() //
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
      Tensor weights = movingDomain2D.arrayReshape_weights();
      Show show = new Show();
      show.add(ArrayPlot.of(weights, param0.cdg));
      show.render(graphics, new Rectangle(100, 10, 100 + Unprotect.dimension1Hint(weights) * 2, 400));
    }
  }

  protected final Tensor shapeOrigin() {
    return manifoldDisplay().shape().multiply(RealScalar.of(0.8));
  }

  /** @return method to compute mean (for instance approximation instead of exact mean) */
  protected final BiinvariantMean biinvariantMean() {
    HomogeneousSpace homogeneousSpace = manifoldDisplay().homogeneousSpace();
    return homogeneousSpace.biinvariantMean();
  }

  protected final Tensor shufflePointsSe2(int n) {
    switch (getSelectedMD()) {
    case R2: {
      ManifoldDisplay manifoldDisplay = manifoldDisplay();
      Tensor tensor = Tensor.of(RandomSample.of(manifoldDisplay.randomSampleInterface(), n).stream() //
          .map(manifoldDisplay::point2xya));
      return tensor;
    }
    case S2: {
      Distribution distribution = UniformDistribution.of(-0.5, 0.5);
      return Tensor.of(RandomVariate.of(distribution, n, 2).stream() //
          .map(Tensor::copy) //
          .map(row -> row.append(RealScalar.ZERO)));
    }
    case H2: {
      return Tensor.of(CirclePoints.of(n).multiply(RealScalar.of(2)).stream().map(row -> row.append(RealScalar.ZERO)));
    }
    case Se2C:
    case Se2: {
      ManifoldDisplay manifoldDisplay = manifoldDisplay();
      Distribution distributionp = UniformDistribution.of(-1, 7);
      Distribution distributiona = UniformDistribution.of(-1, 1);
      return Tensors.vector(_ -> manifoldDisplay.xya2point( //
          RandomVariate.of(distributionp, 2).append(RandomVariate.of(distributiona))), n);
    }
    default:
      throw new IllegalArgumentException();
    }
  }

  protected final MovingDomain2D updateMovingDomain2D(Tensor movingOrigin, int res) {
    switch (getSelectedMD()) {
    case R2: {
      ManifoldDisplay manifoldDisplay = manifoldDisplay();
      CoordinateBoundingBox coordinateBoundingBox = manifoldDisplay.d2Raster_coordinateBoundingBox();
      Tensor domain = StaticHelper.of(coordinateBoundingBox, manifoldDisplay, res);
      Sedarim sedarim = operator(movingOrigin);
      return
      // param2.mls //
      // ? new RnFittedMovingDomain2D(movingOrigin, sedarim, domain)
      // :
      new AveragedMovingDomain2D(movingOrigin, sedarim, domain, //
          manifoldDisplay().indetPoint());
    }
    case S2: {
      Tensor dx = Subdivide.of(-1, 1, res - 1);
      Tensor dy = Subdivide.of(-1, 1, res - 1);
      Tensor domain = Outer.of((cx, cy) -> Vector2Norm.NORMALIZE.apply(Tensors.of(cx, cy, RealScalar.of(1.0))), dx, dy);
      Sedarim sedarim = operator(movingOrigin);
      return new AveragedMovingDomain2D(movingOrigin, sedarim, domain, //
          manifoldDisplay().indetPoint());
    }
    case H2: {
      Tensor domain = StaticHelper.of(Box2D.xy(Clips.absolute(1.0)), manifoldDisplay(), res);
      Sedarim sedarim = operator(movingOrigin);
      return new AveragedMovingDomain2D( //
          movingOrigin, sedarim, domain, //
          manifoldDisplay().indetPoint());
    }
    case Se2C:
    case Se2: {
      Tensor dx = Subdivide.of(0, 6, res - 1);
      Tensor dy = Subdivide.of(0, 6, res - 1);
      Tensor domain = Outer.of((cx, cy) -> Tensors.of(cx, cy, RealScalar.ZERO), dx, dy);
      return new AveragedMovingDomain2D(movingOrigin, operator(movingOrigin), domain, //
          manifoldDisplay().indetPoint());
    }
    default:
      throw new IllegalArgumentException();
    }
  }

  static void main() {
    new DeformationDemo().runStandalone();
  }
}
