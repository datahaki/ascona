// code by jph
package ch.alpine.ascona.hull;

import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.col.ColorDataGradients;

@ReflectionMarker
class HullParam {
  public final MeshParam meshParam = new MeshParam();
  public Boolean cuboid = false;
  public final RotParam rotParam = new RotParam();
  public ColorDataGradients cdg = ColorDataGradients.ALPINE;
  /** IMPORTANT: the value shuffle == true is used to initialized */
}
