// code by jph
package ch.alpine.ascona.util.api;

import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Clip;

public enum Box2D {
  ;
  public static final Tensor SQUARE = Tensors.fromString("{{0, 0}, {1, 0}, {1, 1}, {0, 1}}").unmodifiable();
  public static final Tensor CORNERS = Tensors.fromString("{{-1, -1}, {1, -1}, {1, 1}, {-1, 1}}").unmodifiable();

  /** @param coordinateBoundingBox
   * @return polygon defined by the corners of the first two dimensions of given
   * coordinateBoundingBox */
  public static Tensor polygon(CoordinateBoundingBox coordinateBoundingBox) {
    Clip clip0 = coordinateBoundingBox.getClip(0);
    Clip clip1 = coordinateBoundingBox.getClip(1);
    Tensor c00 = Tensors.of(clip0.min(), clip1.min());
    Tensor c01 = Tensors.of(clip0.min(), clip1.max());
    Tensor c11 = Tensors.of(clip0.max(), clip1.max());
    Tensor c10 = Tensors.of(clip0.max(), clip1.min());
    return Unprotect.byRef( //
        c00, //
        c01, //
        c11, //
        c10);
  }
}
