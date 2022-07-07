// code by jph
package ch.alpine.ascona.nd;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.IntStream;

import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.swing.LookAndFeels;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.ext.Timing;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.lie.r2.ConvexHull;
import ch.alpine.tensor.opt.nd.Dbscan;
import ch.alpine.tensor.pdf.Distribution;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.NormalDistribution;
import ch.alpine.tensor.pdf.c.UniformDistribution;

public class DbscanDemo extends AbstractDemo {
  private static final ColorDataIndexed COLOR_DATA_INDEXED = ColorDataLists._097.cyclic();
  private static final ColorDataIndexed COLOR_FILL_INDEXED = COLOR_DATA_INDEXED.deriveWithAlpha(96);

  @ReflectionMarker
  public static class Param {
    @FieldInteger
    @FieldSelectionArray({ "100", "200", "500", "1000" })
    public Scalar count = RealScalar.of(200);
    @FieldInteger
    @FieldClip(min = "1", max = "10")
    public Scalar minPts = RealScalar.of(5);
    public CenterNorms centerNorms = CenterNorms._2;
    @FieldSlider
    @FieldClip(min = "0", max = "1")
    public Scalar radius = RealScalar.of(0.3);
    @FieldFuse
    public transient Boolean shuffle = false;
  }

  private final Param param;
  private Tensor pointsAll;

  public DbscanDemo() {
    this(new Param());
  }

  public DbscanDemo(Param param) {
    super(param);
    this.param = param;
    fieldsEditor.addUniversalListener(() -> {
      if (param.shuffle) {
        param.shuffle = false;
        pointsAll = recomp();
      }
    });
    timerFrame.geometricComponent.setOffset(100, 600);
    // System.out.println(pointsAll.length());
    pointsAll = recomp();
  }

  private static Tensor recomp() {
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
    graphics.setColor(Color.GRAY);
    Tensor points = Tensor.of(pointsAll.stream().limit(param.count.number().intValue()));
    Tensor xya = timerFrame.geometricComponent.getMouseSe2CState();
    Scalar radius = param.radius;
    Timing timing = Timing.started();
    CenterNorms centerNorms = param.centerNorms;
    Integer[] labels = Dbscan.of(points, centerNorms::ndCenterInterface, radius, param.minPts.number().intValue());
    double seconds = timing.seconds();
    graphics.drawString(String.format("%6.4f", seconds), 0, 40);
    {
      Map<Integer, Tensor> map = new HashMap<>();
      IntStream.range(0, labels.length) //
          .forEach(index -> map.computeIfAbsent(labels[index], i -> Tensors.empty()).append(points.get(index)));
      for (Entry<Integer, Tensor> entry : map.entrySet())
        if (Dbscan.NOISE < entry.getKey()) {
          Tensor tensor = ConvexHull.of(entry.getValue());
          graphics.setColor(COLOR_FILL_INDEXED.getColor(entry.getKey()));
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
            : COLOR_DATA_INDEXED.getColor(label));
        graphics.fillRect((int) point2d.getX() - 2, (int) point2d.getY() - 2, 5, 5);
        ++index;
      }
    }
    {
      graphics.setColor(Color.BLUE);
      geometricLayer.pushMatrix(GfxMatrix.translation(xya.extract(0, 2)));
      graphics.draw(geometricLayer.toPath2D(centerNorms.shape().multiply(radius), true));
      geometricLayer.popMatrix();
    }
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.updateComponentTreeUI();
    new DbscanDemo().setVisible(1000, 800);
  }
}
