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

  public static CoordinateBoundingBox xy(Clip clip) {
    return CoordinateBoundingBox.of(clip, clip);
  }

  /** @param coordinateBoundingBox
   * @return polygon defined by the corners of the first two dimensions of given
   * coordinateBoundingBox */
  public static Tensor polygon(CoordinateBoundingBox coordinateBoundingBox) {
    Clip clipX = coordinateBoundingBox.getClip(0); // x
    Clip clipY = coordinateBoundingBox.getClip(1); // y
    Tensor c00 = Tensors.of(clipX.min(), clipY.min());
    Tensor c01 = Tensors.of(clipX.min(), clipY.max());
    Tensor c11 = Tensors.of(clipX.max(), clipY.max());
    Tensor c10 = Tensors.of(clipX.max(), clipY.min());
    return Unprotect.byRef( //
        c00, //
        c01, //
        c11, //
        c10);
  }
}
