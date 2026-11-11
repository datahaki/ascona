// code by jph
package ch.alpine.ascona.geo;

import java.awt.Rectangle;

import ch.alpine.bridge.fig.ShowableConfig;
import ch.alpine.bridge.geo.Tile;
import ch.alpine.bridge.geo.TilePixel;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;

enum GeoFit {
  ;
  static void main() {
    Rectangle rectangle = new Rectangle(0, 0, 400, 300);
    TilePixel tilePixel = new TilePixel(new Tile(8, 100, 100), 256, 0, 0);
    ShowableConfig sc0 = ShowableConfig.geo(rectangle, tilePixel);
    CoordinateBoundingBox cbb = sc0.cbb();
    IO.println(cbb);
    TilePixel tilePixel2 = TilePixel.geo(rectangle, cbb);
    IO.println(tilePixel2);
  }
}
