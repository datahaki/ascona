// code by jph
package ch.alpine.ascona.geo;

import ch.alpine.bridge.geo.TileServers;

enum GeoLoader {
  ;
  static void main() {
    for (int index = 0; index < GeoPlot.segments.length(); ++index)
      GeoPlot.load(index, TileServers.OpenTopoMap);
  }
}
