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
import ch.alpine.tensor.alg.Rescale;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.io.ImageFormat;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Round;

public class ArrayPlotImage {
  private static final int COLOR_WIDTH = 10;
  private static final int IMAGE_LEGEND_SPACE = 10;

  public static ArrayPlotImage of(Tensor matrix, Clip clip, ScalarTensorFunction colorDataGradient) {
    Set<Scalar> set = Set.of(Round._3.apply(clip.min()), Round._3.apply(clip.max()));
    return new ArrayPlotImage(matrix, clip, colorDataGradient, set);
  }

  private final BufferedImage bufferedImage;
  private final BarLegend barLegend;

  /** @param matrix with entries in the interval [0, 1]
   * @param clip of data range before {@link Rescale}
   * @param colorDataGradient */
  public ArrayPlotImage(Tensor matrix, Clip clip, ScalarTensorFunction colorDataGradient, Set<Scalar> set) {
    bufferedImage = ImageFormat.of(matrix.map(colorDataGradient));
    barLegend = BarLegend.of(colorDataGradient, clip, set);
  }

  /** @param graphics */
  public void draw(Graphics graphics) {
    draw(graphics, getDimension());
  }

  public void draw(Graphics graphics, Dimension dimension) {
    BufferedImage legendImage = barLegend.createImage(new Dimension(COLOR_WIDTH, dimension.height));
    draw(graphics, dimension, legendImage);
  }

  private void draw(Graphics graphics, Dimension dimension, BufferedImage legendImage) {
    graphics.drawImage(bufferedImage, //
        0, //
        0, //
        dimension.width, //
        dimension.height, null);
    graphics.drawImage(legendImage, //
        dimension.width + IMAGE_LEGEND_SPACE, //
        0, //
        legendImage.getWidth(), //
        legendImage.getHeight(), //
        null);
  }

  public BufferedImage bufferedImage() {
    return bufferedImage;
  }

  public BarLegend legend() {
    return barLegend;
  }

  public BufferedImage export() {
    return export(getDimension());
  }

  public BufferedImage export(Dimension dimension) {
    BufferedImage legendImage = barLegend.createImage(new Dimension(COLOR_WIDTH, dimension.height));
    BufferedImage bufferedImage = new BufferedImage( //
        dimension.width + IMAGE_LEGEND_SPACE + legendImage.getWidth(), // magic constant corresponds to width of legend
        dimension.height, //
        BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = bufferedImage.createGraphics();
    draw(graphics, dimension, legendImage);
    graphics.dispose();
    return bufferedImage;
  }

  public Dimension getDimension() {
    return new Dimension( //
        bufferedImage.getWidth(), //
        bufferedImage.getHeight());
  }

  public int height() {
    return bufferedImage.getHeight();
  }
}
