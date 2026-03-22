// code by jph
package ch.alpine.ascona.geo;

import java.awt.Color;
import java.awt.Graphics;

import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Partition;
import ch.alpine.tensor.io.Import;
import ch.alpine.tensor.qty.Quantity;

enum StaticHelper {
  ;
  public static void draw(Graphics graphics, String string, int x, int y) {
    graphics.setColor(Color.BLACK);
    graphics.drawString(string, x - 1, y - 1);
    graphics.drawString(string, x - 1, y - 0);
    graphics.drawString(string, x - 1, y + 1);
    graphics.drawString(string, x + 1, y + 1);
    graphics.drawString(string, x + 1, y + 0);
    graphics.drawString(string, x + 1, y - 1);
    graphics.setColor(Color.WHITE);
    graphics.drawString(string, x, y);
  }

  public static Tensor segments() {
    Tensor tensor = Import.of("ch/alpine/ascona/geo/2024_routes.csv");
    return Tensor.of(tensor.stream() //
        .filter(r -> r.length() == 8) //
        .map(r -> Partition.of(r.extract(4, 8).maps(s -> Quantity.of(s, "deg")), 2)));
  }
}
