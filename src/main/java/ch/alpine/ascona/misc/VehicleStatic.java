// code by jph
package ch.alpine.ascona.misc;

import java.awt.image.BufferedImage;

import ch.alpine.tensor.ext.ResourceData;

public enum VehicleStatic {
  INSTANCE;

  private final BufferedImage bufferedImage_c = ResourceData.bufferedImage("/ch/alpine/ascona/image/vehicle_c.png");
  private final BufferedImage bufferedImage_o = ResourceData.bufferedImage("/ch/alpine/ascona/image/vehicle_o.png");
  private final BufferedImage bufferedImage_g = ResourceData.bufferedImage("/ch/alpine/ascona/image/vehicle_g.png");
  private final BufferedImage bufferedImage_r = ResourceData.bufferedImage("/ch/alpine/ascona/image/vehicle_r.png");

  public BufferedImage bufferedImage_c() {
    return bufferedImage_c;
  }

  public BufferedImage bufferedImage_o() {
    return bufferedImage_o;
  }

  public BufferedImage bufferedImage_g() {
    return bufferedImage_g;
  }

  public BufferedImage bufferedImage_r() {
    return bufferedImage_r;
  }
}
