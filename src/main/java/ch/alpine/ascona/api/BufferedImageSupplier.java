// code by jph
package ch.alpine.ascona.api;

import java.awt.image.BufferedImage;

@FunctionalInterface
public interface BufferedImageSupplier {
  /** @return bufferedImage */
  BufferedImage bufferedImage();
}
