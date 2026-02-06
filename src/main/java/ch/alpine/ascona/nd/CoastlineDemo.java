// code by jph
package ch.alpine.ascona.nd;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.io.IOException;

import ch.alpine.ascony.ren.AxesRender;
import ch.alpine.ascony.win.AbstractDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.img.ColorDataLists;

public class CoastlineDemo extends AbstractDemo {
  @ReflectionMarker
  public static class Param {
    @FieldSelectionArray({ "100", "200", "500", "1000" })
    public Integer count = 200;
    @FieldClip(min = "1", max = "10")
    public Integer minPts = 5;
    public CenterNorms centerNorms = CenterNorms._2;
    @FieldSlider
    @FieldClip(min = "0", max = "1")
    public Scalar radius = RealScalar.of(0.3);
    @FieldFuse
    public transient Boolean shuffle = false;
    public ColorDataLists cdl = ColorDataLists._097;
  }

  private final Param param;
  private Tensor points;

  public CoastlineDemo() {
    this(new Param());
    // TODO CSV file
    points = Unprotect.Import(HomeDirectory.file("iberia.csv"));
  }

  public CoastlineDemo(Param param) {
    super(param);
    this.param = param;
    fieldsEditor(0).addUniversalListener(() -> {
      if (param.shuffle) {
        param.shuffle = false;
      }
    });
    timerFrame.geometricComponent.setOffset(100, 600);
    // System.out.println(pointsAll.length());
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    AxesRender.INSTANCE.render(geometricLayer, graphics);
    graphics.setColor(Color.GRAY);
    for (Tensor point : points) {
      Point2D point2d = geometricLayer.toPoint2D(point);
      graphics.fillRect((int) point2d.getX(), (int) point2d.getY(), 1, 1);
    }
  }

  static void main() throws IOException {
    launch();
  }
}
