// code by jph
package ch.alpine.ascona.misc;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Objects;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.d2.alg.ConvexHull2D;
import ch.alpine.sophis.dv.Biinvariant;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.fit.KMeans;
import ch.alpine.sophis.noise.SimplexContinuousNoise;
import ch.alpine.sophus.api.MetricManifold;
import ch.alpine.sophus.bm.CenterMean;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Flatten;
import ch.alpine.tensor.alg.PadRight;
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
import ch.alpine.tensor.sca.Round;

/** the control points are the seeds of the K-Means iteration */
class KMeansDemo extends ControlPointsDemo {
  @ReflectionMarker
  static class Param0 {
    @FieldSelectionArray({ "100", "200", "500", "1000" })
    public Integer count = 200;
    @FieldSelectionArray({ "2", "3", "4", "5" })
    public Integer initK = 5;
    @FieldFuse
    public transient Boolean shuffle = false;
  }

  @ReflectionMarker
  static class Param1 {
    @FieldSelectionArray({ "METRIC", "USANCE", "GARDEN" })
    public Biinvariants biinvariants = Biinvariants.METRIC;
    @FieldFuse
    public transient Boolean recomp = false;
  }

  @ReflectionMarker
  static class Param2 {
    public ColorDataLists cdl = ColorDataLists._097;
    public Boolean dataOnly = false;
  }

  private final Param0 param0;
  private final Param1 param1;
  private final Param2 param2;
  private Tensor pointSet;
  private Timing timing = Timing.stopped();
  private KMeans kMeans;

  public KMeansDemo() {
    super(param0 = new Param0(), param1 = new Param1(), param2 = new Param2());
    fieldsEditor(0).addUniversalListener(this::shuffle);
    fieldsEditor(1).addUniversalListener(this::recomp);
    addChangeListener(this::shuffle);
    shuffle();
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.manifolds();
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.SCATTERED;
  }

  private void shuffle() {
    RandomSampleInterface randomSampleInterface = manifoldDisplay().randomSampleInterface();
    {
      Tensor points = Tensors.empty();
      Distribution distribution = UniformDistribution.of(Clips.interval(-0.5, 1));
      while (points.length() < param0.count) {
        Tensor point = RandomSample.of(randomSampleInterface);
        Tensor probe = PadRight.zeros(4).apply(Flatten.of(point));
        Scalar scalar = SimplexContinuousNoise.FUNCTION.apply(probe);
        Scalar p = RandomVariate.of(distribution);
        if (Scalars.lessThan(p, scalar))
          points.append(point);
      }
      pointSet = points;
    }
    {
      setGeodesicControlPoints(RandomSample.of(randomSampleInterface, param0.initK));
    }
    recomp();
  }

  private void recomp() {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    HomogeneousSpace homogeneousSpace = manifoldDisplay.homogeneousSpace();
    Biinvariant biinvariant = param1.biinvariants.ofSafe(homogeneousSpace);
    Tensor seeds = getGeodesicControlPoints();
    if (0 < seeds.length()) {
      timing = Timing.started();
      kMeans = new KMeans(biinvariant.relative_distances(pointSet), new CenterMean(homogeneousSpace.biinvariantMean()), pointSet);
      kMeans.setSeeds(seeds);
      timing.stop();
      kMeans.complete();
    } else
      kMeans = null;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    graphics.setColor(Color.GRAY);
    Tensor sequence = pointSet;
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    HomogeneousSpace homogeneousSpace = manifoldDisplay.homogeneousSpace();
    if (Objects.nonNull(kMeans) && !param2.dataOnly) {
      graphics.drawString("" + timing.seconds().maps(Round._6), 0, 20);
      Tensor partition = kMeans.partition();
      ColorDataIndexed cdi = param2.cdl.cyclic();
      ColorDataIndexed colorDataIndexed = cdi.deriveWithAlpha(128);
      ColorDataIndexed colorFillIndexed = cdi.deriveWithAlpha(64);
      int index = 0;
      Tensor seeds2 = kMeans.seeds();
      for (Tensor subset : partition) {
        if (homogeneousSpace instanceof MetricManifold && 1 < manifoldDisplay.dimensions()) {
          Tensor projected = manifoldDisplay.point2xy().slash(subset);
          Tensor tensor = ConvexHull2D.of(projected, Tolerance.CHOP);
          graphics.setColor(colorFillIndexed.getColor(index));
          graphics.fill(geometricLayer.toPath2D(tensor, true));
        }
        manifoldDisplay.showPoints(colorFillIndexed.getColor(index), colorDataIndexed.getColor(index), RealScalar.of(0.2), subset) //
            .render(geometricLayer, graphics);
        // ---
        if (seeds2.length() == partition.length()) {
          manifoldDisplay.showPoints(colorFillIndexed.getColor(index), colorDataIndexed.getColor(index), RealScalar.of(0.5), Tensors.of(seeds2.get(index))) //
              .render(geometricLayer, graphics);
        }
        ++index;
      }
    } else {
      manifoldDisplay.showPoints(Color.GRAY, Color.DARK_GRAY, RealScalar.of(0.2), sequence) //
          .render(geometricLayer, graphics);
    }
  }

  static void main() {
    new KMeansDemo().runStandalone();
  }
}
