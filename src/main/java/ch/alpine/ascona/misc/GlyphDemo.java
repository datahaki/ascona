// code by jph
package ch.alpine.ascona.misc;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ch.alpine.ascony.ren.BezierGlyphRender;
import ch.alpine.ascony.ren.ClothoidGlyphRender;
import ch.alpine.ascony.ren.GridRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.EuclideanPlaneDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.PvmBuilder;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.lie.se2.Se2Matrix;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.io.TableBuilder;

class GlyphDemo extends EuclideanPlaneDemo {
  @ReflectionMarker
  static class Param {
    @FieldSelectionArray({ "0", "8", "16", "24", "32", "40", "48", "56", "64", "72", "80", "88", "96", "104", "112", "120", "128", "136" })
    public Integer ofs = 0;
    @FieldSlider
    @FieldClip(min = "1", max = "20")
    public Integer res = 20;
    public Font font = new Font(Font.DIALOG, Font.PLAIN, 12);
  }

  private final Param param;
  Font font;

  public GlyphDemo() {
    super(param = new Param());
    Path path = HomeDirectory.Ephemeral.resolve("font", "SourceSerifPro-Regular.otf");
    path = HomeDirectory.Ephemeral.resolve("font", "ArtisticCalligraphy.otf");
    font = new Font(Font.DIALOG, Font.PLAIN, 12);
    try {
      font = Font.createFont(Font.TRUETYPE_FONT, path.toFile()).deriveFont(12f);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    Tensor pvm = PvmBuilder.rhs().setOffset(100, 400).setPerPixel(50).digest();
    geometricComponent().setModel2Pixel(pvm);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    new GridRender(this::getSize).render(geometricLayer, graphics);
    Tensor domain = Subdivide.of(0.0, 1.0, param.res);
    TableBuilder tableBuilder = new TableBuilder();
    String collect = IntStream.range(0, 8).map(i -> i + param.ofs) //
        .mapToObj(i -> ' ' + i).map(Character::toString).collect(Collectors.joining());
    // collect = "$&%";
    FontRenderContext frc = new FontRenderContext(null, true, true);
    GlyphVector gv = font.createGlyphVector(frc, collect); // or any string
    for (int index = 0; index < gv.getNumGlyphs(); ++index) {
      Shape shape = gv.getGlyphOutline(index); // first character
      graphics.setColor(Color.BLACK);
      graphics.setStroke(new BasicStroke(2));
      new BezierGlyphRender(shape, domain).render(geometricLayer, graphics);
      geometricLayer.pushMatrix(Se2Matrix.translation(Tensors.vector(0, -10)));
      graphics.setColor(Color.RED);
      graphics.setStroke(new BasicStroke(1));
      new BezierGlyphRender(shape, domain).render(geometricLayer, graphics);
      graphics.setColor(Color.BLACK);
      graphics.setStroke(new BasicStroke(2));
      new ClothoidGlyphRender(shape, domain).render(geometricLayer, graphics);
      geometricLayer.popMatrix();
    }
    setGeodesicControlPoints(tableBuilder.getTable());
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.DELEGATED;
  }

  static void main() {
    new GlyphDemo().runStandalone();
  }
}
