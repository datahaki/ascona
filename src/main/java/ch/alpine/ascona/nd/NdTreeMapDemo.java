// code by jph
package ch.alpine.ascona.nd;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Optional;
import java.util.Random;

import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.bridge.win.AbstractDemo;
import ch.alpine.bridge.win.LookAndFeels;
import ch.alpine.sophus.math.sample.BoxRandomSample;
import ch.alpine.sophus.math.sample.RandomSample;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.ext.Timing;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.opt.nd.CoordinateBounds;
import ch.alpine.tensor.opt.nd.NdCenterInterface;
import ch.alpine.tensor.opt.nd.NdMap;
import ch.alpine.tensor.opt.nd.NdMatch;
import ch.alpine.tensor.opt.nd.NdTreeMap;
import ch.alpine.tensor.red.Max;
import ch.alpine.tensor.sca.Abs;

public class NdTreeMapDemo extends AbstractDemo {
  @ReflectionMarker
  public static class Param {
    // @FieldPreferredWidth(40)
    @FieldInteger
    public Scalar leafSizeMax = RealScalar.of(5);
    // @FieldPreferredWidth(100)
    @FieldInteger
    @FieldClip(min = "1", max = "10000")
    public Scalar count = RealScalar.of(1000);
    // @FieldPreferredWidth(40)
    @FieldInteger
    @FieldClip(min = "1", max = "20")
    public Scalar multi = RealScalar.of(10);
    // @FieldPreferredWidth(40)
    @FieldInteger
    public Scalar pCount = RealScalar.of(4);
    public Boolean nearest = false;
    public CenterNorms centerNorms = CenterNorms._2;
  }

  private final Param param = new Param();
  private final CoordinateBoundingBox box = CoordinateBounds.of(Tensors.vector(0, 0), Tensors.vector(10, 8));
  private final Tensor pointsAll = RandomSample.of(BoxRandomSample.of(box), 5000);

  public NdTreeMapDemo() {
    ToolbarFieldsEditor.add(param, timerFrame.jToolBar);
    timerFrame.geometricComponent.setOffset(100, 600);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    // normal rendering quality
    graphics.setColor(Color.GRAY);
    Tensor points = Tensor.of(pointsAll.stream().limit(param.count.number().intValue()));
    for (Tensor point : points) {
      Point2D point2d = geometricLayer.toPoint2D(point);
      graphics.fillRect((int) point2d.getX(), (int) point2d.getY(), 2, 2);
    }
    Tensor xya = timerFrame.geometricComponent.getMouseSe2CState();
    Scalar radius = Abs.FUNCTION.apply(xya.Get(2).multiply(RealScalar.of(0.3)));
    CoordinateBoundingBox actual = CoordinateBounds.of(points);
    NdMap<Void> ndMap = NdTreeMap.of(actual, param.leafSizeMax.number().intValue());
    Random random = new Random(1);
    int multi = param.multi.number().intValue();
    for (Tensor point : points) {
      int count = 1 + random.nextInt(multi);
      for (int index = 0; index < count; ++index)
        ndMap.insert(point, null);
    }
    Timing timing = Timing.started();
    CenterNorms centerNorms = param.centerNorms;
    NdCenterInterface ndCenterInterface = centerNorms.ndCenterInterface(xya.extract(0, 2));
    int limit = param.pCount.number().intValue();
    final Collection<NdMatch<Void>> collection;
    if (param.nearest) {
      GraphicNearest<Void> graphicNearest = //
          new GraphicNearest<>(ndCenterInterface, limit, geometricLayer, graphics);
      ndMap.visit(graphicNearest);
      collection = graphicNearest.queue();
    } else {
      GraphicSpherical<Void> graphicSpherical = //
          new GraphicSpherical<>(ndCenterInterface, radius, geometricLayer, graphics);
      ndMap.visit(graphicSpherical);
      collection = graphicSpherical.list();
    }
    double seconds = timing.seconds();
    graphics.drawString(String.format("%d %d %6.4f", ndMap.size(), collection.size(), seconds), 0, 40);
    graphics.setColor(new Color(255, 0, 0, 128));
    if (param.nearest) {
      Optional<Scalar> optional = collection.stream() //
          .map(NdMatch::distance) //
          .reduce(Max::of);
      if (optional.isPresent())
        radius = optional.orElseThrow();
    }
    {
      graphics.setColor(Color.BLUE);
      geometricLayer.pushMatrix(GfxMatrix.translation(xya.extract(0, 2)));
      graphics.draw(geometricLayer.toPath2D(centerNorms.shape().multiply(radius), true));
      geometricLayer.popMatrix();
    }
    graphics.setColor(new Color(0, 128, 0, 255));
    for (NdMatch<Void> ndMatch : collection) {
      Tensor point = ndMatch.location();
      Point2D point2d = geometricLayer.toPoint2D(point);
      graphics.fillRect((int) point2d.getX() - 1, (int) point2d.getY() - 1, 4, 4);
    }
    {
      Tensor mxy = xya.extract(0, 2);
      Tensor spc = actual.mapInside(mxy);
      graphics.setColor(new Color(0, 128, 255, 255));
      graphics.draw(geometricLayer.toLine2D(mxy, spc));
    }
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.updateUI();
    new NdTreeMapDemo().setVisible(1000, 800);
  }
}
