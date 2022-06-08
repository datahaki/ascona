// code by jph
package ch.alpine.ascona.util.api;

import java.awt.image.BufferedImage;

@FunctionalInterface
public interface BufferedImageSupplier {
  /** @return bufferedImage */
  BufferedImage bufferedImage();
}
