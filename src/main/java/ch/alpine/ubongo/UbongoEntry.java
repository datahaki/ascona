// code by jph
package ch.alpine.ubongo;

import java.io.Serializable;

import ch.alpine.tensor.Tensor;

public record UbongoEntry(int i, int j, UbongoPiece ubongoPiece, Tensor stamp) implements Serializable {
}
