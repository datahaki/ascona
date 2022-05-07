// code by jph
package ch.alpine.ascona.util.sym;

import ch.alpine.tensor.Tensor;

public class SymLinkBuilder {
  /** @param control
   * @param symScalar
   * @return */
  public static SymLink of(Tensor control, SymScalar symScalar) {
    return new SymLinkBuilder(control).build(symScalar);
  }

  // ---
  private final Tensor control;

  private SymLinkBuilder(Tensor control) {
    this.control = control;
  }

  private SymLink build(SymScalar symScalar) {
    if (symScalar instanceof SymScalarPart symScalarPart) {
      return new SymLinkPart( //
          build(symScalarPart.getP()), //
          build(symScalarPart.getQ()), //
          symScalarPart.ratio());
    }
    SymLinkLeaf symNode = new SymLinkLeaf(symScalar.evaluate());
    symNode.position = control.get(symNode.getIndex());
    return symNode;
  }
}
