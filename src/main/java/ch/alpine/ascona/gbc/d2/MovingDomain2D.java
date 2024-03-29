// code by jph
package ch.alpine.ascona.gbc.d2;

import java.util.Objects;

import ch.alpine.ascona.util.api.ImageTiling;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.Unprotect;

/** Reference:
 * "Weighted Averages on Surfaces"
 * by Daniele Panozzo, Ilya Baran, Olga Diamanti, Olga Sorkine-Hornung */
/* package */ abstract class MovingDomain2D {
  private final Tensor origin;
  final Tensor domain;
  final Tensor[][] weights;
  /* for visualization only */
  private Tensor _wgs = null;

  /** @param origin reference control points that will be associated to given targets
   * @param sedarim
   * @param domain */
  protected MovingDomain2D(Tensor origin, Sedarim sedarim, Tensor domain) {
    this.origin = origin;
    this.domain = domain;
    int rows = domain.length();
    int cols = Unprotect.dimension1(domain);
    weights = new Tensor[rows][cols];
    for (int cx = 0; cx < rows; ++cx)
      for (int cy = 0; cy < cols; ++cy) {
        Tensor point = domain.get(cx, cy);
        weights[cx][cy] = sedarim.sunder(point);
      }
  }

  public final Tensor origin() {
    return origin;
  }

  public abstract Tensor[][] forward(Tensor target, BiinvariantMean biinvariantMean);

  /** @return array of weights for visualization */
  public final Tensor arrayReshape_weights() {
    if (Objects.isNull(_wgs)) {
      int rows = weights.length;
      int cols = weights[0].length;
      Tensor wgs = Tensors.matrix((i, j) -> weights[i][j], rows, cols);
      _wgs = ImageTiling.of(wgs);
    }
    return _wgs;
  }
}
