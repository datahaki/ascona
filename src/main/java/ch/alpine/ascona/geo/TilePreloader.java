// code by jph
package ch.alpine.ascona.geo;

import ch.alpine.bridge.geo.MapImagesCache;
import ch.alpine.bridge.geo.Tile;
import ch.alpine.bridge.geo.TileServers;

enum TilePreloader {
  ;
  static void main() {
    MapImagesCache mapImagesCache = TileServers.OpenStreetMap.cache();
    mapImagesCache.debug_print = true;
    final int zmax = 7;
    for (int z = 0; z <= zmax; ++z) {
      int max = Tile.maxInclusive(z);
      for (int x = 0; x <= max; ++x)
        for (int y = 0; y <= max; ++y) {
          Tile tile = new Tile(z, x, y);
          mapImagesCache.getTile(tile);
          if (1000 < mapImagesCache.getDownloadCount())
            return;
        }
    }
  }
}
