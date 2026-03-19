// code by jph
package ch.alpine.ascona.geo;

import java.awt.Color;
import java.awt.Graphics;

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
}
