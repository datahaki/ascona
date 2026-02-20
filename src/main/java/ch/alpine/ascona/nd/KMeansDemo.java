// code by jph
package ch.alpine.ascona.nd;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Objects;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.PointsRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.d2.Extract2D;
import ch.alpine.sophis.crv.d2.alg.ConvexHull2D;
import ch.alpine.sophis.dv.Biinvariant;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.fit.KMeans;
import ch.alpine.sophus.bm.CenterMean;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.math.api.MetricManifold;
import ch.alpine.sophus.math.noise.SimplexContinuousNoise;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.mat.Tolerance;
import ch.alpine.tensor.pdf.Distribution;
import ch.alpine.tensor.pdf.RandomSample;
import ch.alpine.tensor.pdf.RandomSampleInterface;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.UniformDistribution;
import ch.alpine.tensor.qty.Timing;
import ch.alpine.tensor.sca.Clips;

public class KMeansDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param1 extends AsconaParam {
    public Param1() {
      super(true, ManifoldDisplays.R2_H2_S2_SE2C);
    }

    @FieldSelectionArray({ "100", "200", "500", "1000" })
    public Integer count = 200;
    @FieldFuse
    public transient Boolean shuffle = false;
  }

  @ReflectionMarker
  public static class Param2 {
    @FieldSelectionArray({ "1", "2", "3", "4", "5", "10", "20", "30" })
    public Integer depth = 5;
    @FieldFuse
    public transient Boolean recomp = false;
  }

  private final Param1 param1;
  private final Param2 param2;
  private Tensor pointsAll;
  private KMeans kMeans;

  public KMeansDemo() {
    this(new Param1(), new Param2());
  }

  public KMeansDemo(Param1 param1, Param2 param2) {
    super(param1, param2);
    this.param1 = param1;
    this.param2 = param2;
    fieldsEditor(0).addUniversalListener(() -> {
      pointsAll = shuffle();
      recomp(pointsAll);
    });
    fieldsEditor(1).addUniversalListener(() -> {
      recomp(pointsAll);
    });
    controlPointsRender.setMidpointIndicated(false);
    pointsAll = shuffle();
  }

  private Tensor shuffle() {
    Tensor points = Tensors.empty();
    RandomSampleInterface randomSampleInterface = manifoldDisplay().randomSampleInterface();
    Distribution distribution = UniformDistribution.of(Clips.interval(-0.5, 1));
    for (int index = 0; index < 10000; ++index) {
      Tensor point = RandomSample.of(randomSampleInterface);
      Scalar scalar = SimplexContinuousNoise.FUNCTION.apply(point);
      Scalar p = RandomVariate.of(distribution);
      if (Scalars.lessThan(p, scalar)) {
        points.append(point);
      }
      if (points.length() == param1.count)
        return points;
    }
    return points;
  }

  private void recomp(Tensor sequence) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
    Biinvariant biinvariant = Biinvariants.METRIC.ofSafe(homogeneousSpace);
    // BiinvariantMean biinvariantMean = homogeneousSpace.biinvariantMean(Chop._08);
    Tensor seeds = getGeodesicControlPoints();
    if (0 < seeds.length()) {
      kMeans = new KMeans(biinvariant.relative_distances(sequence), new CenterMean(homogeneousSpace.biinvariantMean()), sequence);
      kMeans.setSeeds(seeds);
      Timing timing = Timing.started();
      int iterations = kMeans.complete();
      IO.println(iterations + " steps in " + timing.seconds());
    } else
      kMeans = null;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    graphics.setColor(Color.GRAY);
    Tensor sequence = Tensor.of(pointsAll.stream().limit(param1.count));
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
    if (Objects.nonNull(kMeans)) {
      Tensor partition = kMeans.partition();
      ColorDataIndexed colorDataIndexed = ColorDataLists._097.strict().deriveWithAlpha(128);
      ColorDataIndexed colorFillIndexed = ColorDataLists._097.strict().deriveWithAlpha(64);
      int index = 0;
      Tensor seeds2 = kMeans.seeds();
      for (Tensor subset : partition) {
        if (homogeneousSpace instanceof MetricManifold) {
          Tensor tensor = ConvexHull2D.of(subset.stream().map(Extract2D.FUNCTION), Tolerance.CHOP);
          graphics.setColor(colorFillIndexed.getColor(index));
          graphics.fill(geometricLayer.toPath2D(tensor, true));
        }
        PointsRender pointsRender = new PointsRender( //
            colorFillIndexed.getColor(index), //
            colorDataIndexed.getColor(index));
        pointsRender.show(manifoldDisplay::matrixLift, manifoldDisplay.shape().multiply(RealScalar.of(0.2)), subset) //
            .render(geometricLayer, graphics);
        // ---
        if (seeds2.length() == partition.length()) {
          pointsRender.show(manifoldDisplay::matrixLift, manifoldDisplay.shape().multiply(RealScalar.of(0.5)), Tensors.of(seeds2.get(index))) //
              .render(geometricLayer, graphics);
        }
        ++index;
      }
    } else {
      PointsRender pointsRender = new PointsRender( //
          Color.GRAY, //
          Color.BLACK);
      pointsRender.show(manifoldDisplay::matrixLift, manifoldDisplay.shape().multiply(RealScalar.of(0.2)), sequence) //
          .render(geometricLayer, graphics);
    }
  }

  static void main() {
    new KMeansDemo().runStandalone();
  }
}
