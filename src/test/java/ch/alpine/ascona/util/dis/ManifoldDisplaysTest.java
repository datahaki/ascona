// code by jph
package ch.alpine.ascona.util.dis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.alg.Dimensions;
import ch.alpine.tensor.alg.VectorQ;
import ch.alpine.tensor.ext.Serialization;
import ch.alpine.tensor.mat.MatrixQ;

class ManifoldDisplaysTest {
  private static Collection<Arguments> parameters() {
    return ManifoldDisplays.ALL.stream().map(Arguments::of).toList();
  }

  @Test
  public void testSimple() {
    assertTrue(12 <= ManifoldDisplays.ALL.size());
  }

  @ParameterizedTest
  @MethodSource("parameters")
  public void testSerializable(ManifoldDisplay manifoldDisplay) throws ClassNotFoundException, IOException {
    Serialization.copy(manifoldDisplay);
  }

  @ParameterizedTest
  @MethodSource("parameters")
  public void testDimensions(ManifoldDisplay manifoldDisplay) {
    assertTrue(0 < manifoldDisplay.dimensions());
  }

  @ParameterizedTest
  @MethodSource("parameters")
  public void testShape(ManifoldDisplay manifoldDisplay) {
    Tensor shape = manifoldDisplay.shape();
    List<Integer> list = Dimensions.of(shape);
    assertEquals(list.get(1), 2);
    MatrixQ.require(shape);
  }

  @ParameterizedTest
  @MethodSource("parameters")
  public void testProject(ManifoldDisplay manifoldDisplay) {
    Tensor tensor = manifoldDisplay.project(Array.zeros(3));
    assertNotNull(tensor);
    manifoldDisplay.matrixLift(tensor);
    assertThrows(Exception.class, () -> manifoldDisplay.project(null));
  }

  @ParameterizedTest
  @MethodSource("parameters")
  public void testToPoint(ManifoldDisplay manifoldDisplay) {
    Tensor xya = Tensors.vector(0.1, 0.2, 0.3);
    Tensor p = manifoldDisplay.project(xya);
    VectorQ.requireLength(manifoldDisplay.toPoint(p), 2);
    Tensor matrix = manifoldDisplay.matrixLift(p);
    assertEquals(Dimensions.of(matrix), Arrays.asList(3, 3));
    assertThrows(Exception.class, () -> manifoldDisplay.toPoint(null));
  }

  @ParameterizedTest
  @MethodSource("parameters")
  public void testMatrixLiftNull(ManifoldDisplay manifoldDisplay) {
    assertThrows(Exception.class, () -> manifoldDisplay.matrixLift(null));
  }

  @ParameterizedTest
  @MethodSource("parameters")
  public void testGeodesicSpace(ManifoldDisplay manifoldDisplay) {
    assertNotNull(manifoldDisplay.geodesicSpace());
  }

  @ParameterizedTest
  @MethodSource("parameters")
  public void testLieExponential(ManifoldDisplay manifoldDisplay) {
    manifoldDisplay.lieGroup();
  }

  @ParameterizedTest
  @MethodSource("parameters")
  public void testParametricDistance(ManifoldDisplay manifoldDisplay) {
    // TODO
    if (manifoldDisplay.lieGroup() == null)
      System.out.println(manifoldDisplay);
  }

  @Test
  public void testToString() {
    long count = ManifoldDisplays.ALL.stream().map(Object::toString).distinct().count();
    assertEquals(count, ManifoldDisplays.ALL.size());
  }
}
