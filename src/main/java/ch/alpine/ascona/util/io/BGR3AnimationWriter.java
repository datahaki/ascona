// code by jph
package ch.alpine.ascona.util.io;

import java.awt.image.BufferedImage;
import java.util.Objects;

import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.io.AnimationWriter;

/** for use MP4
 * internally converts images to 3-byte BGR since this is accepted by
 * {@link Mp4AnimationWriter} */
public class BGR3AnimationWriter implements AnimationWriter {
  private final AnimationWriter animationWriter;

  public BGR3AnimationWriter(AnimationWriter animationWriter) {
    this.animationWriter = Objects.requireNonNull(animationWriter);
  }

  @Override // from AnimationWriter
  public void write(BufferedImage bufferedImage) throws Exception {
    BufferedImage frame = new BufferedImage( //
        bufferedImage.getWidth(), //
        bufferedImage.getHeight(), //
        BufferedImage.TYPE_3BYTE_BGR);
    frame.createGraphics().drawImage(bufferedImage, 0, 0, null);
    animationWriter.write(frame);
  }

  @Override // from AnimationWriter
  public void write(Tensor tensor) throws Exception {
    animationWriter.write(tensor);
  }

  @Override // from AnimationWriter
  public void close() throws Exception {
    animationWriter.close();
  }
}
