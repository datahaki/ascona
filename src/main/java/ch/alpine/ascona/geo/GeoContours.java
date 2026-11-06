package ch.alpine.ascona.geo;

import ch.alpine.tensor.Scalar;

public class GeoContours {
  private record Boundary(String name, Scalar epsilon) {
  }
}
