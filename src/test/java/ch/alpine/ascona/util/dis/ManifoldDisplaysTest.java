// code by jph
package ch.alpine.ascona.util.dis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.hs.MetricManifold;
import ch.alpine.sophus.math.sample.BoxRandomSample;
import ch.alpine.sophus.math.sample.RandomSample;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.alg.Dimensions;
import ch.alpine.tensor.alg.VectorQ;
import ch.alpine.tensor.ext.Serialization;
import ch.alpine.tensor.mat.MatrixQ;
import ch.alpine.tensor.mat.Tolerance;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Chop;
import ch.alpine.tensor.sca.Clips;

class ManifoldDisplaysTest {
  @Test
  void testSimple() {
    assertTrue(12 <= ManifoldDisplays.values().length);
  }

  @ParameterizedTest
  @EnumSource(ManifoldDisplays.class)
  void testSerializable(ManifoldDisplays manifoldDisplays) throws ClassNotFoundException, IOException {
    Serialization.copy(manifoldDisplays);
  }

  @ParameterizedTest
  @EnumSource(ManifoldDisplays.class)
  void testDimensions(ManifoldDisplays manifoldDisplays) {
    assertTrue(0 < manifoldDisplays.manifoldDisplay().dimensions());
  }

  @ParameterizedTest
  @EnumSource(ManifoldDisplays.class)
  void testShape(ManifoldDisplays manifoldDisplays) {
    Tensor shape = manifoldDisplays.manifoldDisplay().shape();
    List<Integer> list = Dimensions.of(shape);
    assertEquals(list.get(1), 2);
    MatrixQ.require(shape);
  }

  @ParameterizedTest
  @EnumSource(ManifoldDisplays.class)
  void testProject(ManifoldDisplays manifoldDisplays) {
    ManifoldDisplay manifoldDisplay = manifoldDisplays.manifoldDisplay();
    Tensor tensor = manifoldDisplay.xya2point(Array.zeros(3));
    assertNotNull(tensor);
    manifoldDisplay.matrixLift(tensor);
    assertThrows(Exception.class, () -> manifoldDisplay.xya2point(null));
  }

  @ParameterizedTest
  @EnumSource(ManifoldDisplays.class)
  void testToPoint(ManifoldDisplays manifoldDisplays) {
    ManifoldDisplay manifoldDisplay = manifoldDisplays.manifoldDisplay();
    Tensor xya = Tensors.vector(0.1, 0.2, 0.3);
    Tensor p = manifoldDisplay.xya2point(xya);
    VectorQ.requireLength(manifoldDisplay.point2xy(p), 2);
    Tensor matrix = manifoldDisplay.matrixLift(p);
    assertEquals(Dimensions.of(matrix), List.of(3, 3));
    assertThrows(Exception.class, () -> manifoldDisplay.point2xy(null));
  }

  @ParameterizedTest
  @EnumSource(ManifoldDisplays.class)
  void testMatrixLiftNull(ManifoldDisplays manifoldDisplays) {
    ManifoldDisplay manifoldDisplay = manifoldDisplays.manifoldDisplay();
    assertThrows(Exception.class, () -> manifoldDisplay.matrixLift(null));
  }

  @ParameterizedTest
  @EnumSource(ManifoldDisplays.class)
  void testGeodesicSpace(ManifoldDisplays manifoldDisplays) {
    ManifoldDisplay manifoldDisplay = manifoldDisplays.manifoldDisplay();
    assertNotNull(manifoldDisplay.geodesicSpace());
  }

  @ParameterizedTest
  @EnumSource(ManifoldDisplays.class)
  void testBiinvariantMean(ManifoldDisplays manifoldDisplays) {
    ManifoldDisplay manifoldDisplay = manifoldDisplays.manifoldDisplay();
    if (manifoldDisplay.geodesicSpace() instanceof HomogeneousSpace homogeneousSpace) {
      BiinvariantMean biinvariantMean = homogeneousSpace.biinvariantMean(Chop._04);
      assertNotNull(biinvariantMean);
    }
  }

  @Test
  void testTensorMetric() {
    for (ManifoldDisplays manifoldDisplays : ManifoldDisplays.metricManifolds())
      assertTrue(manifoldDisplays.manifoldDisplay().geodesicSpace() instanceof MetricManifold);
  }

  @Test
  void testRandomSample() {
    for (ManifoldDisplays manifoldDisplays : ManifoldDisplays.manifolds()) {
      if (Objects.isNull(manifoldDisplays.manifoldDisplay().randomSampleInterface())) {
        System.err.println(manifoldDisplays);
        fail();
      }
    }
  }

  @ParameterizedTest
  @EnumSource(ManifoldDisplays.class)
  void testList(ManifoldDisplays manifoldDisplays) {
    ManifoldDisplay manifoldDisplay = manifoldDisplays.manifoldDisplay();
    RandomSampleInterface randomSampleInterface = manifoldDisplay.randomSampleInterface();
    if (Objects.nonNull(randomSampleInterface)) {
      Tensor p = RandomSample.of(randomSampleInterface);
      Tensor xya = manifoldDisplay.point2xya(p);
      Tensor q = manifoldDisplay.xya2point(xya);
      if (!manifoldDisplays.equals(ManifoldDisplays.So3))
        Tolerance.CHOP.requireClose(p, q);
    }
  }

  @ParameterizedTest
  @EnumSource(ManifoldDisplays.class)
  void testToPoint2(ManifoldDisplays manifoldDisplays) {
    ManifoldDisplay manifoldDisplay = manifoldDisplays.manifoldDisplay();
    RandomSampleInterface randomSampleInterface = manifoldDisplay.randomSampleInterface();
    if (Objects.nonNull(randomSampleInterface)) {
      Tensor p = RandomSample.of(randomSampleInterface);
      Tensor xya = manifoldDisplay.point2xya(p);
      Tensor xy_ = manifoldDisplay.point2xy(p);
      Tolerance.CHOP.requireClose(xya.extract(0, 2), xy_);
    }
  }

  @ParameterizedTest
  @EnumSource(ManifoldDisplays.class)
  void testToPoint3(ManifoldDisplays manifoldDisplays) {
    ManifoldDisplay manifoldDisplay = manifoldDisplays.manifoldDisplay();
    RandomSampleInterface randomSampleInterface = BoxRandomSample.of(CoordinateBoundingBox.of(Clips.unit(), Clips.unit(), Clips.unit()));
    if (Objects.nonNull(randomSampleInterface)) {
      Tensor rand = RandomSample.of(randomSampleInterface);
      Tensor p = manifoldDisplay.xya2point(rand);
      Tensor xya = manifoldDisplay.point2xya(p);
      Tensor xy_ = manifoldDisplay.point2xy(p);
      Tolerance.CHOP.requireClose(xya.extract(0, 2), xy_);
    }
  }

  @Test
  void testHs() {
    for (ManifoldDisplays manifoldDisplays : ManifoldDisplays.manifolds())
      assertTrue(manifoldDisplays.manifoldDisplay().geodesicSpace() instanceof HomogeneousSpace);
  }

  @Test
  void testMetricConsistency() {
    for (ManifoldDisplays manifoldDisplays : ManifoldDisplays.metricManifolds()) {
      ManifoldDisplay manifoldDisplay = manifoldDisplays.manifoldDisplay();
      RandomSampleInterface randomSampleInterface = manifoldDisplay.randomSampleInterface();
      Tensor p = RandomSample.of(randomSampleInterface);
      Tensor q = RandomSample.of(randomSampleInterface);
      HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
      MetricManifold metricManifold = (MetricManifold) manifoldDisplay.geodesicSpace();
      Scalar distance = metricManifold.distance(p, q);
      Tensor log = homogeneousSpace.exponential(p).vectorLog(q);
      Scalar norm = metricManifold.norm(log);
      if (!manifoldDisplays.equals(ManifoldDisplays.Spd2))
        Tolerance.CHOP.requireClose(distance, norm);
    }
  }

  @Test
  void testToString() {
    long count = ManifoldDisplays.ALL.stream().map(Object::toString).distinct().count();
    assertEquals(count, ManifoldDisplays.ALL.size());
  }
}
