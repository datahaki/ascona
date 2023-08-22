// code by jph
package ch.alpine.ubongo;

import java.util.List;

import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Dimensions;

/* package */ enum StaticHelper {
  ;
  public static final Scalar FREE = RealScalar.ONE.negate();

  public static boolean isSingleFree(Tensor board) {
    List<Integer> size = Dimensions.of(board);
    for (int bi = 0; bi < size.get(0); ++bi)
      for (int bj = 0; bj < size.get(1); ++bj)
        if (board.get(bi, bj).equals(FREE)) {
          boolean neighbor = //
              (0 < bi && board.get(bi - 1, bj).equals(FREE)) || //
                  (0 < bj && board.get(bi, bj - 1).equals(FREE)) || //
                  (bi + 1 < size.get(0) && board.get(bi + 1, bj).equals(FREE)) || //
                  (bj + 1 < size.get(1) && board.get(bi, bj + 1).equals(FREE));
          if (!neighbor)
            return false;
        }
    return true;
  }
}
