// code by jph
package ch.alpine.ascona.euclid;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.IntStream;

import ch.alpine.ascona.ref.ShuffleFuse;
import ch.alpine.ascony.api.CenterNorms;
import ch.alpine.ascony.ren.GridRender;
import ch.alpine.bridge.gfx.GeometricComponent;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.PvmBuilder;
import ch.alpine.bridge.gfx.RenderInterface;
import ch.alpine.bridge.pro.ManipulateProvider;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.hull.d2.ConvexHull2D;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.opt.nd.Dbscan;
import ch.alpine.tensor.pdf.Distribution;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.NormalDistribution;
import ch.alpine.tensor.pdf.c.UniformDistribution;
import ch.alpine.tensor.qty.Timing;
import ch.alpine.tensor.sca.Round;

@ReflectionMarker
class DbscanDemo implements ManipulateProvider, RenderInterface {
  @FieldSelectionArray({ "10", "50", "100", "200", "500", "1000" })
  public Integer count = 200;
  @FieldClip(min = "1", max = "10")
  public Integer minPts = 5;
  public CenterNorms centerNorms = CenterNorms._2;
  @FieldSlider
  @FieldClip(min = "0", max = "1")
  public Scalar radius = RealScalar.of(0.3);
  public final ShuffleFuse shuffleFuse = new ShuffleFuse();
  public ColorDataLists cdl = ColorDataLists._097;
  GeometricComponent geometricComponent = new GeometricComponent();
  Tensor pointsAll;

  public DbscanDemo() {
    geometricComponent.addRenderInterfaceBackground(new GridRender(geometricComponent::getSize));
    geometricComponent.addRenderInterface(this);
    Tensor pvm = PvmBuilder.rhs().setOffset(100, 600).setPerPixel(50).digest();
    geometricComponent.setModel2Pixel(pvm);
    pointsAll = shuffle();
  }

  private static Tensor shuffle() {
    Distribution dist_b = UniformDistribution.of(0, 10);
    Distribution dist_r = NormalDistribution.of(0, 1);
    Tensor points = Tensors.empty();
    Tensor base = RandomVariate.of(dist_b, 5, 2);
    for (int index = 0; index < 20; ++index)
      for (Tensor r : base)
        for (Tensor p : RandomVariate.of(dist_r, 10, 2))
          points.append(r.add(p));
    return points;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Timing timing = Timing.started();
    Tensor points = Tensor.of(pointsAll.stream().limit(count));
    Integer[] labels = Dbscan.of(points, centerNorms::ndCenterInterface, radius, minPts);
    Scalar seconds = timing.seconds();
    ColorDataIndexed colorDataIndexed = cdl.cyclic();
    ColorDataIndexed colorFillIndexed = colorDataIndexed.deriveWithAlpha(96);
    {
      Map<Integer, Tensor> map = new HashMap<>();
      IntStream.range(0, labels.length) //
          .forEach(index -> map.computeIfAbsent(labels[index], _ -> Tensors.empty()).append(points.get(index)));
      for (Entry<Integer, Tensor> entry : map.entrySet())
        if (Dbscan.NOISE < entry.getKey()) {
          Tensor tensor = ConvexHull2D.of(entry.getValue());
          graphics.setColor(colorFillIndexed.getColor(entry.getKey()));
          graphics.fill(geometricLayer.toPath2D(tensor, true));
        }
    }
    {
      int index = 0;
      for (Tensor point : points) {
        Point2D point2d = geometricLayer.toPoint2D(point);
        Integer label = labels[index];
        graphics.setColor(label < 0 //
            ? Color.BLACK
            : colorDataIndexed.getColor(label));
        graphics.fillRect((int) point2d.getX() - 2, (int) point2d.getY() - 2, 5, 5);
        ++index;
      }
    }
    {
      graphics.setColor(Color.BLUE);
      graphics.draw(geometricLayer.toPath2D(centerNorms.shape().multiply(radius), true));
    }
    {
      graphics.setColor(Color.GRAY);
      graphics.drawString(seconds.maps(Round._4).toString(), 0, 40);
    }
  }

  @Override
  public Container getContainer() {
    if (shuffleFuse.shuffle) {
      shuffleFuse.shuffle = false;
      pointsAll = shuffle();
    }
    return geometricComponent;
  }

  static void main() {
    new DbscanDemo().runStandalone();
  }
}
