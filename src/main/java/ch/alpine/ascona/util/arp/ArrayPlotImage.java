// code by jph
package ch.alpine.ascona.util.arp;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Set;

import ch.alpine.bridge.fig.BarLegend;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.io.ImageFormat;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Round;

public class ArrayPlotImage {
  private final BufferedImage bufferedImage;
  private final int width;
  private final int height;
  private final BarLegend legend;
  private final BufferedImage create;

  public ArrayPlotImage(Tensor matrix, Clip clip, ScalarTensorFunction colorDataGradient) {
    bufferedImage = ImageFormat.of(matrix.map(colorDataGradient));
    width = bufferedImage.getWidth();
    height = bufferedImage.getHeight();
    Set<Scalar> set = Set.of(Round._3.apply(clip.min()), Round._3.apply(clip.max()));
    legend = BarLegend.of(colorDataGradient, clip, set);
    create = legend.createImage(new Dimension(10, height));
  }

  public void draw(Graphics graphics) {
    graphics.drawImage(bufferedImage, //
        0, //
        0, //
        width, //
        height, null);
    graphics.drawImage(create, //
        width + 10, //
        0, //
        null);
  }

  public BufferedImage bufferedImage() {
    return bufferedImage;
  }

  public BufferedImage legend() {
    return create;
  }

  public int height() {
    return height;
  }

  public BufferedImage export() {
    BufferedImage bi = new BufferedImage( //
        width + 10 + create.getWidth(), // magic constant corresponds to width of legend
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
