// code by jph
package ch.alpine.ascona.util.sym;

import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.itp.BinaryAverage;

/** SymNode extends from here */
// TODO ASCONA make interface
public abstract class SymLink {
  public abstract boolean isNode();

  public abstract int depth();

  public int getIndex() {
    throw new RuntimeException();
  }

  public abstract Tensor getPosition();

  public abstract Tensor getPosition(BinaryAverage binaryAverage);
}
