package ch.alpine.ubongo.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.ReflectionMarker;

public class UnicodeViewer extends AbstractDemo {
  @ReflectionMarker
  public static class Param {
    public String hex = "27";
    @FieldClip(min = "1", max = "60")
    public Integer size = 25;
  }

  private final Param param;

  public UnicodeViewer() {
    this(new Param());
  }

  public UnicodeViewer(Param param) {
    super(param);
    this.param = param;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    graphics.setColor(Color.DARK_GRAY);
    int size = param.size;
    graphics.setFont(new Font(Font.DIALOG, Font.PLAIN, size));
    int ofs = Integer.parseInt(param.hex, 16) * 256;
    for (int i = 0; i < 16; ++i) {
      for (int j = 0; j < 16; ++j) {
        int res = ofs + i * 16 + j;
        char chr = (char) res;
        graphics.drawString("" + chr, j * size, (i + 1) * size);
      }
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
