// code by jph
package ch.alpine.ascona.nd;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import ch.alpine.ascony.arp.CenterNorms;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.dis.S2Display;
import ch.alpine.ascony.win.ManifoldDisplayDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.lie.se2.Se2Matrix;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.mat.DiagonalMatrix;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.opt.nd.CoordinateBounds;
import ch.alpine.tensor.opt.nd.NdCenterInterface;
import ch.alpine.tensor.opt.nd.NdMap;
import ch.alpine.tensor.opt.nd.NdMatch;
import ch.alpine.tensor.opt.nd.NdTreeMap;
import ch.alpine.tensor.pdf.RandomSample;
import ch.alpine.tensor.qty.Timing;
import ch.alpine.tensor.red.Max;
import ch.alpine.tensor.sca.Abs;
import ch.alpine.tensor.sca.Round;

class NdTreeMapDemo extends ManifoldDisplayDemo {
  @ReflectionMarker
  static class Param0 {
    @FieldSelectionArray({ "100", "200", "500", "1000", "2000", "5000", "10000" })
    public Integer count = 1000;
    @FieldClip(min = "1", max = "5")
    public Integer leafSizeMax = 5;
  }

  @ReflectionMarker
  static class Param1 {
    @FieldClip(min = "1", max = "10")
    public Integer limit = 4;
    public Boolean nearest = false;
    public CenterNorms centerNorms = CenterNorms._2;
    @FieldSlider
    @FieldClip(min = "0", max = "1")
    public Scalar radius = RealScalar.of(0.3);
  }

  private final Param0 param0;
  private final Param1 param1;

  record Triple(Tensor points, CoordinateBoundingBox cbb, NdMap<Void> ndMap) {
  }

  private Triple triple;

  protected NdTreeMapDemo() {
    super(param0 = new Param0(), param1 = new Param1());
    fieldsEditor(0).addUniversalListener(this::shuffle);
    geometricComponent().setModel2Pixel(DiagonalMatrix.of(200, -200, 1));
    // geometricComponent().setOffset(300, 300);
    addChangeListener(this::shuffle);
    setManifoldDisplay(ManifoldDisplays.S2);
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.R2_S2;
  }

  void shuffle() {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor points = RandomSample.of(manifoldDisplay.randomSampleInterface(), param0.count);
    CoordinateBoundingBox cbb = CoordinateBounds.of(points);
    NdMap<Void> ndMap = NdTreeMap.of(cbb, param0.leafSizeMax);
    for (Tensor point : points)
      ndMap.insert(point, null);
    triple = new Triple(points, cbb, ndMap);
  }

  /** @param xya
   * @return mouse position in manifold space */
  private Tensor center(Tensor xya) {
    ManifoldDisplays manifoldDisplays = getSelectedMD();
    if (manifoldDisplays.equals(ManifoldDisplays.S2)) {
      Optional<Tensor> optionalZ = S2Display.optionalZ(xya);
      Tensor xyz = optionalZ.orElse(xya.extract(0, 2).append(RealScalar.ZERO));
      xyz.set(Abs.FUNCTION, 2);
      return xyz;
    }
    return xya.extract(0, 2);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor mouse = geometricComponent().getMouseSe2CState();
    Tensor xyz = center(mouse);
    // normal rendering quality
    graphics.setColor(Color.GRAY);
    Tensor points = triple.points;
    for (Tensor point : points) {
      Point2D point2d = geometricLayer.toPoint2D(point);
      graphics.fillRect((int) point2d.getX(), (int) point2d.getY(), 2, 2);
    }
    Scalar radius = param1.radius;
    Timing timing = Timing.started();
    CenterNorms centerNorms = param1.centerNorms;
    NdCenterInterface ndCenterInterface = centerNorms.ndCenterInterface(xyz);
    int limit = param1.limit;
    final Collection<NdMatch<Void>> collection;
    if (param1.nearest) {
      GraphicNearest<Void> graphicNearest = //
          new GraphicNearest<>(ndCenterInterface, limit, geometricLayer, graphics);
      triple.ndMap().visit(graphicNearest);
      collection = graphicNearest.queue();
    } else {
      GraphicRadius<Void> graphicSpherical = //
          new GraphicRadius<>(ndCenterInterface, radius, geometricLayer, graphics);
      triple.ndMap().visit(graphicSpherical);
      collection = graphicSpherical.list();
    }
    Scalar seconds = timing.seconds();
    graphics.setColor(Color.GRAY);
    graphics.drawString(String.format("%d %d %s", triple.ndMap().size(), collection.size(), seconds.maps(Round._3)), 0, 40);
    graphics.setColor(new Color(255, 0, 0, 128));
    if (param1.nearest) {
      Optional<Scalar> optional = collection.stream() //
          .map(NdMatch::distance) //
          .reduce(Max::of);
      if (optional.isPresent())
        radius = optional.orElseThrow();
    }
    {
      graphics.setColor(Color.BLUE);
      // if (centerNorms.equals(CenterNorms._2)) {
      // HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
      // // manifoldDisplay.
      // // createTangent
      // // Exponential exponential = homogeneousSpace.exponential(p);
      // // exponential.exp(p)
      // } else {
      geometricLayer.pushMatrix(Se2Matrix.translation(xyz));
      graphics.draw(geometricLayer.toPath2D(centerNorms.shape().multiply(radius), true));
      geometricLayer.popMatrix();
      // }
    }
    graphics.setColor(new Color(0, 128, 0, 255));
    for (NdMatch<Void> ndMatch : collection) {
      Tensor point = ndMatch.location();
      Point2D point2d = geometricLayer.toPoint2D(point);
      graphics.fillRect((int) point2d.getX() - 1, (int) point2d.getY() - 1, 4, 4);
    }
    {
      Tensor mxy = xyz;
      Tensor spc = triple.cbb().mapInside(mxy);
      graphics.setColor(new Color(0, 128, 255, 255));
      graphics.draw(geometricLayer.toLine2D(mxy, spc));
    }
  }

  static void main() {
    new NdTreeMapDemo().runStandalone();
  }
}
