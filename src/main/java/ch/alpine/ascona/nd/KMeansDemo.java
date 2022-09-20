// code by jph
package ch.alpine.ascona.nd;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.dv.Biinvariant;
import ch.alpine.sophus.dv.Biinvariants;
import ch.alpine.sophus.fit.KMeans;
import ch.alpine.sophus.hs.r2.ConvexHull2D;
import ch.alpine.sophus.lie.rn.RnGroup;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.pdf.Distribution;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.NormalDistribution;
import ch.alpine.tensor.pdf.c.UniformDistribution;
import ch.alpine.tensor.sca.Chop;

public class KMeansDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, ManifoldDisplays.R2_ONLY);
    }

    @FieldSelectionArray({ "100", "200", "500", "1000" })
    public Integer count = 200;
    @FieldFuse
    public transient Boolean shuffle = false;
  }

  private final Param param;
  private Tensor pointsAll;

  public KMeansDemo() {
    this(new Param());
  }

  public KMeansDemo(Param param) {
    super(param);
    this.param = param;
    fieldsEditor(0).addUniversalListener(() -> {
      if (param.shuffle) {
        param.shuffle = false;
        pointsAll = recomp();
      }
    });
    controlPointsRender.setMidpointIndicated(false);
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
    Tensor sequence = Tensor.of(pointsAll.stream().limit(param.count));
    Biinvariant biinvariant = Biinvariants.METRIC.ofSafe(RnGroup.INSTANCE);
    BiinvariantMean biinvariantMean = RnGroup.INSTANCE.biinvariantMean(Chop._08);
    Tensor seeds = getGeodesicControlPoints();
    if (0 < seeds.length()) {
      KMeans kMeans = new KMeans(biinvariant.distances(sequence), biinvariantMean, sequence);
      kMeans.setSeeds(seeds);
      for (int i = 0; i < 10; ++i)
        kMeans.iterate();
      Tensor partition = kMeans.partition();
      ColorDataIndexed colorDataIndexed = ColorDataLists._097.strict();
      ColorDataIndexed colorFillIndexed = ColorDataLists._097.strict().deriveWithAlpha(128);
      int index = 0;
      for (Tensor subset : partition) {
        graphics.setColor(colorDataIndexed.getColor(index));
        {
          Tensor tensor = ConvexHull2D.of(subset);
          graphics.setColor(colorFillIndexed.getColor(index));
          graphics.fill(geometricLayer.toPath2D(tensor, true));
        }
        for (Tensor point : subset) {
          Point2D point2d = geometricLayer.toPoint2D(point);
          graphics.fillRect((int) point2d.getX() - 2, (int) point2d.getY() - 2, 5, 5);
        }
        ++index;
      }
    } else {
      graphics.setColor(Color.BLACK);
      for (Tensor point : sequence) {
        Point2D point2d = geometricLayer.toPoint2D(point);
        graphics.fillRect((int) point2d.getX() - 2, (int) point2d.getY() - 2, 5, 5);
      }
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
