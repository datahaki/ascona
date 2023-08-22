// code by jph
package ch.alpine.ubongo;

import java.io.Serializable;
import java.util.List;

public record UbongoSolution(List<UbongoEntry> list, int search) implements Serializable {
}
