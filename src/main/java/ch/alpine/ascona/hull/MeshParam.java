// code by jph
package ch.alpine.ascona.hull;

import ch.alpine.ascona.ref.ShuffleFuse;
import ch.alpine.bridge.ref.Cacheable;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.srf.SurfaceMesh;

@ReflectionMarker
class MeshParam extends Cacheable {
  public RandomMethod method = RandomMethod.SPHERE;
  @FieldSelectionArray({ "50", "100", "200", "400" })
  public Integer count = 200;
  public final ShuffleFuse shuffleFuse = new ShuffleFuse();

  public SurfaceMesh mesh() {
    return method.mesh(count);
  }
}
