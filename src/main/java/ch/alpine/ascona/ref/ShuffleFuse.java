// code by jph
package ch.alpine.ascona.ref;

import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.ReflectionMarker;

@ReflectionMarker
public class ShuffleFuse {
  @FieldFuse
  public transient Boolean shuffle = false;
}
