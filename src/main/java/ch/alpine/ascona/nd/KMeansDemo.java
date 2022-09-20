// code by jph
package ch.alpine.ascona.nd;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Random;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
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
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.hs.MetricManifold;
import ch.alpine.sophus.hs.r2.ConvexHull2D;
import ch.alpine.sophus.hs.r2.Extract2D;
import ch.alpine.sophus.math.noise.SimplexContinuousNoise;
import ch.alpine.sophus.math.sample.RandomSample;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.mat.Tolerance;
import ch.alpine.tensor.sca.Chop;

public class KMeansDemo extends ControlPointsDemo {
  private static final Random RANDOM = new Random();

  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, ManifoldDisplays.R2_S2_SE2C);
    }

    @FieldSelectionArray({ "100", "200", "500", "1000" })
    public Integer count = 200;
    @FieldSelectionArray({ "3", "5", "10", "20" })
    public Integer depth = 3;
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
      // if (param.shuffle) {
      // param.shuffle = false;
      // pointsAll = recomp();
      // }
      pointsAll = recomp();
    });
    controlPointsRender.setMidpointIndicated(false);
    pointsAll = recomp();
  }

  private Tensor recomp() {
    Tensor points = Tensors.empty();
    RandomSampleInterface randomSampleInterface = manifoldDisplay().randomSampleInterface();
    for (int index = 0; index < 1000; ++index) {
      Tensor point = RandomSample.of(randomSampleInterface);
      Scalar scalar = SimplexContinuousNoise.FUNCTION.apply(point);
      Scalar p = RealScalar.of(RANDOM.nextDouble() * 2 - 1);
      if (Scalars.lessThan(p, scalar)) {
        points.append(point);
      }
      if (points.length() == param.count)
        return points;
    }
    return points;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    graphics.setColor(Color.GRAY);
    Tensor sequence = Tensor.of(pointsAll.stream().limit(param.count));
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
    Biinvariant biinvariant = Biinvariants.METRIC.ofSafe(homogeneousSpace);
    BiinvariantMean biinvariantMean = homogeneousSpace.biinvariantMean(Chop._08);
    Tensor seeds = getGeodesicControlPoints();
    if (0 < seeds.length()) {
      KMeans kMeans = new KMeans(biinvariant.distances(sequence), biinvariantMean, sequence);
      kMeans.setSeeds(seeds);
      for (int i = 0; i < param.depth; ++i)
        kMeans.iterate();
      Tensor partition = kMeans.partition();
      ColorDataIndexed colorSeedIndexed = ColorDataLists._097.strict();
      ColorDataIndexed colorDataIndexed = ColorDataLists._097.strict().deriveWithAlpha(128);
      ColorDataIndexed colorFillIndexed = ColorDataLists._097.strict().deriveWithAlpha(64);
      int index = 0;
      for (Tensor subset : partition) {
        if (homogeneousSpace instanceof MetricManifold) {
          Tensor tensor = ConvexHull2D.of(subset.stream().map(Extract2D.FUNCTION), Tolerance.CHOP);
          graphics.setColor(colorFillIndexed.getColor(index));
          graphics.fill(geometricLayer.toPath2D(tensor, true));
        }
        graphics.setColor(colorDataIndexed.getColor(index));
        for (Tensor point : subset) {
          Point2D point2d = geometricLayer.toPoint2D(point);
          graphics.fillRect((int) point2d.getX() - 2, (int) point2d.getY() - 2, 5, 5);
        }
        ++index;
      }
      index = 0;
      for (Tensor point : kMeans.seeds()) {
        graphics.setColor(colorSeedIndexed.getColor(index));
        Point2D point2d = geometricLayer.toPoint2D(point);
        graphics.fillRect((int) point2d.getX() - 4, (int) point2d.getY() - 4, 9, 9);
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
