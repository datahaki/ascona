// code by jph
package ch.alpine.ascona.util.dis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import ch.alpine.sophus.api.TensorMetric;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.hs.Biinvariant;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.math.sample.RandomSample;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.alg.Dimensions;
import ch.alpine.tensor.alg.VectorQ;
import ch.alpine.tensor.ext.Serialization;
import ch.alpine.tensor.mat.MatrixQ;
import ch.alpine.tensor.mat.Tolerance;
import ch.alpine.tensor.sca.Chop;

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
    Tensor tensor = manifoldDisplay.project(Array.zeros(3));
    assertNotNull(tensor);
    manifoldDisplay.matrixLift(tensor);
    assertThrows(Exception.class, () -> manifoldDisplay.project(null));
  }

  @ParameterizedTest
  @EnumSource(ManifoldDisplays.class)
  void testToPoint(ManifoldDisplays manifoldDisplays) {
    ManifoldDisplay manifoldDisplay = manifoldDisplays.manifoldDisplay();
    Tensor xya = Tensors.vector(0.1, 0.2, 0.3);
    Tensor p = manifoldDisplay.project(xya);
    VectorQ.requireLength(manifoldDisplay.toPoint(p), 2);
    Tensor matrix = manifoldDisplay.matrixLift(p);
    assertEquals(Dimensions.of(matrix), List.of(3, 3));
    assertThrows(Exception.class, () -> manifoldDisplay.toPoint(null));
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

  @ParameterizedTest
  @EnumSource(ManifoldDisplays.class)
  void testPointDistance(ManifoldDisplays manifoldDisplays) {
    ManifoldDisplay manifoldDisplay = manifoldDisplays.manifoldDisplay();
    TensorMetric tensorMetric = manifoldDisplay.biinvariantMetric();
    Biinvariant biinvariant = manifoldDisplay.biinvariant();
    if (tensorMetric == null)
      assertNull(biinvariant);
    else
      assertNotNull(biinvariant);
  }

  @Test
  void testRandomSample() {
    for (ManifoldDisplay manifoldDisplay : ManifoldDisplays.MANIFOLDS) {
      if (Objects.isNull(manifoldDisplay.randomSampleInterface())) {
        System.err.println(manifoldDisplay);
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
      Tensor xya = manifoldDisplay.unproject(p);
      Tensor q = manifoldDisplay.project(xya);
      if (!manifoldDisplays.equals(ManifoldDisplays.So3))
        Tolerance.CHOP.requireClose(p, q);
    }
  }

  @Test
  void testHs() {
    for (ManifoldDisplay manifoldDisplay : ManifoldDisplays.MANIFOLDS)
      assertTrue(manifoldDisplay.geodesicSpace() instanceof HomogeneousSpace);
  }

  @Test
  void testToString() {
    long count = ManifoldDisplays.ALL.stream().map(Object::toString).distinct().count();
    assertEquals(count, ManifoldDisplays.ALL.size());
  }
}
