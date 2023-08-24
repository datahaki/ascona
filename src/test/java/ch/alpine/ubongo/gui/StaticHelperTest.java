package ch.alpine.ubongo.gui;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import ch.alpine.ubongo.UbongoPublish;

class StaticHelperTest {
  @Test
  void test() {
    BufferedImage bufferedImage = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = bufferedImage.createGraphics();
    StaticHelper.draw(graphics, UbongoPublish.AIRPLAN1, 46);
    graphics.dispose();
  }
}
