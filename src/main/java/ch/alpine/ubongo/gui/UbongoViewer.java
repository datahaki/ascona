// code by jph
package ch.alpine.ubongo.gui;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import ch.alpine.ascona.usr.DuneNoiseDemo;
import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.bridge.fig.DensityPlot;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.ubongo.UbongoPublish;

public class UbongoViewer extends AbstractDemo {
  private static final int SCALE = 46;

  @ReflectionMarker
  public static class Param {
    public UbongoPublish ubongoPublish = UbongoPublish.LETTERH1;
  }

  private final Param param;
  private final BufferedImage bufferedImage;

  public UbongoViewer() {
    this(new Param());
  }

  public UbongoViewer(Param param) {
    super(param);
    this.param = param;
    DensityPlot densityPlot = DuneNoiseDemo.densityPlot();
    densityPlot.setPlotPoints(300);
    bufferedImage = densityPlot.getImage();
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    graphics.drawImage(bufferedImage, 0, 0, 800, 800, null);
    StaticHelper.draw(graphics, param.ubongoPublish, SCALE);
  }

  public static void main(String[] args) {
    launch();
  }
}
