package ch.alpine.ascona.dat;

import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Dimensions;
import ch.alpine.tensor.io.Import;

public enum ImportDemo {
  ;
  static void main() {
    Tensor tensor = Import.of("/ch/alpine/ascona/euroc/tpq/200Hz/MH_04_difficult.csv");
    IO.println(Dimensions.of(tensor));
  }
}
