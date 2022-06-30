// code by jph
package ch.alpine.ascona.util.api;

import ch.alpine.sophus.crv.d2.PolygonArea;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;

public enum Box2D {
  ;
  public static final Tensor SQUARE = polygon(xy(Clips.unit())).unmodifiable();
  public static final Tensor CORNERS = polygon(xy(Clips.absoluteOne())).unmodifiable();

  /** @param clip
   * @return */
  public static CoordinateBoundingBox xy(Clip clip) {
    return CoordinateBoundingBox.of(clip, clip);
  }

  /** @param coordinateBoundingBox
   * @return polygon defined by the corners of the first two dimensions of given
   * coordinateBoundingBox starting with the min,min corner and visiting ccw
   * so that {@link PolygonArea} gives a non-negative number */
  public static Tensor polygon(CoordinateBoundingBox coordinateBoundingBox) {
    Clip clipX = coordinateBoundingBox.getClip(0);
    Clip clipY = coordinateBoundingBox.getClip(1);
    Tensor c00 = Tensors.of(clipX.min(), clipY.min());
    Tensor c10 = Tensors.of(clipX.max(), clipY.min());
    Tensor c11 = Tensors.of(clipX.max(), clipY.max());
    Tensor c01 = Tensors.of(clipX.min(), clipY.max());
    return Unprotect.byRef(c00, c10, c11, c01);
  }
}
