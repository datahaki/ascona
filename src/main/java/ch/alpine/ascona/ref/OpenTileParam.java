// code by jph
package ch.alpine.ascona.ref;

import java.awt.image.BufferedImage;
import java.util.List;

import ch.alpine.ascona.dat.OpenTile;
import ch.alpine.bridge.ref.ann.FieldSelectionCallback;
import ch.alpine.bridge.ref.ann.ReflectionMarker;

@ReflectionMarker
public final class OpenTileParam {
  @ReflectionMarker
  public static List<String> keys() {
    return OpenTile.INSTANCE.keys();
  }

  @FieldSelectionCallback("keys")
  public String string;

  public OpenTileParam() {
    String name = "opentopo/13_4017_3003.png";
    string = keys().contains(name) ? name : keys().getFirst();
  }

  public BufferedImage getImage() {
    return OpenTile.INSTANCE.getImage(string);
  }
}
