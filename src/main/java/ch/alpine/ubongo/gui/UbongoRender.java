// code by jph
package ch.alpine.ubongo.gui;

import java.util.Arrays;
import java.util.List;

import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.alg.Dimensions;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.CyclicColorDataIndexed;
import ch.alpine.tensor.img.ImageRotate;
import ch.alpine.tensor.img.StrictColorDataIndexed;
import ch.alpine.ubongo.UbongoEntry;
import ch.alpine.ubongo.UbongoPiece;

/* package */ enum UbongoRender {
  ;
  private static final ColorDataIndexed INSTANCE = //
      StrictColorDataIndexed.of(Tensor.of(Arrays.stream(UbongoPiece.values()).map(UbongoPiece::colorVector)));
  private static final ColorDataIndexed MONOCHROME = //
      CyclicColorDataIndexed.of(Tensors.of(Tensors.vector(160, 160, 160, 255)));

  /** @param list
   * @param solution
   * @return */
  public static Tensor matrix(List<Integer> list, List<UbongoEntry> solution) {
    Tensor image = Array.same(DoubleScalar.INDETERMINATE, list);
    for (UbongoEntry ubongoEntry : solution) {
      List<Integer> size = Dimensions.of(ubongoEntry.stamp());
      for (int si = 0; si < size.get(0); ++si)
        for (int sj = 0; sj < size.get(1); ++sj)
          if (Scalars.nonZero(ubongoEntry.stamp().Get(si, sj)))
            image.set(RealScalar.of(ubongoEntry.ubongoPiece().ordinal()), ubongoEntry.i() + si, ubongoEntry.j() + sj);
    }
    return image;
  }

  /** @param list
   * @param solution
   * @return */
  public static Tensor of(List<Integer> list, List<UbongoEntry> solution) {
    return matrix(list, solution).map(INSTANCE);
  }

  /** @param list
   * @param solution
   * @return */
  public static Tensor gray(List<Integer> list, List<UbongoEntry> solution) {
    return matrix(list, solution).map(MONOCHROME);
  }

  // ---
  public static Tensor matrix(UbongoPiece ubongoPiece) {
    Scalar ord = RealScalar.of(ubongoPiece.ordinal());
    return ImageRotate.cw(ubongoPiece.mask()) //
        .map(s -> Scalars.isZero(s) //
            ? DoubleScalar.INDETERMINATE
            : ord);
  }

  public static Tensor of(UbongoPiece ubongoPiece) {
    return matrix(ubongoPiece).map(INSTANCE);
  }
}
