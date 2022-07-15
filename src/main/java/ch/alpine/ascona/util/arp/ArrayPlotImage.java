// code by jph
package ch.alpine.ascona.util.arp;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.io.ImageFormat;
import ch.alpine.tensor.sca.Clip;

public class ArrayPlotImage {
  private final BufferedImage bufferedImage;
  private final int width;
  private final int height;
  private final BufferedImage legend;

  public ArrayPlotImage(Tensor matrix, Clip clip, ScalarTensorFunction colorDataGradient) {
    bufferedImage = ImageFormat.of(matrix.map(colorDataGradient));
    width = bufferedImage.getWidth();
    height = bufferedImage.getHeight();
    legend = BarLegend.of(colorDataGradient, height, clip);
  }

  public void draw(Graphics graphics) {
    graphics.drawImage(bufferedImage, //
        0, //
        0, //
        width, //
        height, null);
    graphics.drawImage(legend, //
        width + 10, //
        0, //
        null);
  }

  public BufferedImage bufferedImage() {
    return bufferedImage;
  }

  public BufferedImage legend() {
    return legend;
  }

  public int height() {
    return height;
  }

  public BufferedImage export() {
    BufferedImage bi = new BufferedImage( //
        width + 10 + legend.getWidth(), // magic constant corresponds to width of legend
        height, //
        BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = bi.createGraphics();
    draw(graphics);
    graphics.dispose();
    return bi;
  }

  public Dimension getDimension() {
    return new Dimension(width, height);
  }
}
