// code by jph
package ch.alpine.ascona.misc;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.nio.file.Path;

import ch.alpine.tensor.ext.HomeDirectory;

class OtfFont {
  static void main() throws FontFormatException, IOException {
    Path path = HomeDirectory.Ephemeral.resolve("font", "SourceSerifPro-Regular.otf");
    Font font = Font.createFont(Font.TRUETYPE_FONT, path.toFile());
  }
}
