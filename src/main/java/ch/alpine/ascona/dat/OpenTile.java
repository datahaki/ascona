// code by jph
package ch.alpine.ascona.dat;

import java.awt.image.BufferedImage;
import java.util.List;

import ch.alpine.ascony.res.ResourceMapper;

public enum OpenTile {
  INSTANCE;

  private final ResourceMapper resourceMapper = //
      ResourceMapper.of("ch/alpine/ascona/tile/resource_index.vector");

  public List<String> keys() {
    return resourceMapper.list();
  }

  public BufferedImage getImage(String key) {
    return resourceMapper.importImage(key);
  }
}
