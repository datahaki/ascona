// code by jph
package ch.alpine.ascona.util.sym;

import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Array;

/* package */ class SymWeights {
  public static Tensor of(SymScalar root) {
    return new SymWeights(root).vector();
  }

  // ---
  private final Tensor sum = Array.zeros(0);
  private int max = 0;

  private SymWeights(SymScalar root) {
    visit(RealScalar.ONE, root);
  }

  private void visit(Scalar weight, SymScalar root) {
    if (root instanceof SymScalarPart symScalarPart) {
      visit(weight.multiply(RealScalar.ONE.subtract(symScalarPart.ratio())), symScalarPart.getP());
      visit(weight.multiply(symScalarPart.ratio()), symScalarPart.getQ());
    } else {
      Scalar scalar = root.evaluate();
      int index = scalar.number().intValue();
      max = Math.max(max, index);
      while (sum.length() <= index)
        sum.append(RealScalar.ZERO);
      sum.set(value -> value.add(weight), index);
    }
  }

  Tensor vector() {
    return sum.extract(0, max + 1);
  }
}
