// code by jph
package ch.alpine.ubongo.gui;

import java.awt.Graphics2D;

import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.swing.LookAndFeels;
import ch.alpine.ubongo.UbongoPublish;

public class UbongoViewer extends AbstractDemo {
  private static final int SCALE = 46;

  @ReflectionMarker
  public static class Param {
    public UbongoPublish ubongoPublish = UbongoPublish.LETTERH1;
  }

  private final Param param;

  public UbongoViewer() {
    this(new Param());
  }

  public UbongoViewer(Param param) {
    super(param);
    this.param = param;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    StaticHelper.draw(graphics, param.ubongoPublish, SCALE);
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.updateComponentTreeUI();
    launch();
  }
}
