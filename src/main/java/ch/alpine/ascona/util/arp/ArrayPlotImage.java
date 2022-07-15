// code by jph
package ch.alpine.ascona.util.arp;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Rescale;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.io.ImageFormat;
import ch.alpine.tensor.red.Max;
import ch.alpine.tensor.red.Min;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;

public class ArrayPlotImage {
  /** @param matrix
   * @param colorDataGradient
   * @param magnify
   * @param coverZero
   * @return */
  // TODO ASCONA separate image and legend
  public static ArrayPlotImage rescale( //
      Tensor matrix, ScalarTensorFunction colorDataGradient, boolean coverZero) {
    Rescale rescale = new Rescale(matrix);
    Clip clip = rescale.scalarSummaryStatistics().getClip();
    return new ArrayPlotImage( //
        rescale.result(), //
        coverZero //
            ? cover(clip, clip.width().zero())
            : clip, //
        colorDataGradient);
  }

  /** @param clip
   * @param scalar
   * @return */
  /* package */ static Clip cover(Clip clip, Scalar scalar) {
    return Clips.interval( //
        Min.of(clip.min(), scalar), //
        Max.of(clip.max(), scalar));
  }

  // ---
  private final BufferedImage bufferedImage;
  private final int width;
  private final int height;
  private final BufferedImage legend;

  private ArrayPlotImage(Tensor matrix, Clip clip, ScalarTensorFunction colorDataGradient) {
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
    draw(bi.createGraphics());
    return bi;
  }

  public Dimension getDimension() {
    return new Dimension(width, height);
  }
}
