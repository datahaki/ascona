// code by jph
package ch.alpine.ascona.misc;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.nio.file.Path;
import java.util.stream.Collectors;

import ch.alpine.ascony.ren.GridRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.EuclideanPlaneDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.PvmBuilder;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.BezierCurve;
import ch.alpine.sophis.crv.clt.Clothoid;
import ch.alpine.sophis.crv.clt.ClothoidBuilders;
import ch.alpine.sophus.bm.LinearBiinvariantMean;
import ch.alpine.sophus.lie.so2.ArcTan2D;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Append;
import ch.alpine.tensor.alg.Flatten;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.io.TableBuilder;
import ch.alpine.tensor.pdf.Distribution;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.d.DiscreteUniformDistribution;

class GlyphDemo extends EuclideanPlaneDemo {
  public static Clothoid clothoid(Tensor p0, Tensor p1, Tensor p2, Tensor p3) {
    return ClothoidBuilders.SE2_ANALYTIC.clothoidBuilder().curve( //
        Append.of(p0, ArcTan2D.of(p1.subtract(p0))), //
        Append.of(p3, ArcTan2D.of(p3.subtract(p2))));
  }

  @ReflectionMarker
  static class Param {
    public Boolean bezier = true;
  }

  private final Param param;
  GlyphVector gv;

  public GlyphDemo() {
    super(param = new Param());
    FontRenderContext frc = new FontRenderContext(null, true, true);
    Distribution distribution = DiscreteUniformDistribution.of(0, 128);
    Tensor tensor = RandomVariate.of(distribution, 50);
    String collect = Flatten.scalars(tensor).map(s -> s.number().intValue()).map(i -> ' ' + i).map(Character::toString).collect(Collectors.joining());
    // collect = "$&%";
    Path path = HomeDirectory.Ephemeral.resolve("font", "SourceSerifPro-Regular.otf");
    Font font = new Font(Font.DIALOG, Font.PLAIN, 12);
    try {
      font = Font.createFont(Font.TRUETYPE_FONT, path.toFile()).deriveFont(12f);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    gv = font.createGlyphVector(frc, collect); // or any string
    Tensor pvm = PvmBuilder.rhs().setOffset(100, 400).setPerPixel(50).digest();
    geometricComponent().setModel2Pixel(pvm);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    new GridRender(this::getSize).render(geometricLayer, graphics);
    Tensor domain = Subdivide.of(0.0, 1.0, 10);
    TableBuilder tableBuilder = new TableBuilder();
    for (int index = 0; index < gv.getNumGlyphs(); ++index) {
      Shape glyphShape = gv.getGlyphOutline(index); // first character
      PathIterator pathIterator = glyphShape.getPathIterator(new AffineTransform(1, 0, 0, -1, 0, 0));
      float[] coords = new float[6];
      Tensor prev = null;
      Tensor next = null;
      Tensor stol = null;
      Tensor last = null;
      graphics.setColor(Color.BLACK);
      graphics.setStroke(new BasicStroke(2));
      while (!pathIterator.isDone()) {
        int type = pathIterator.currentSegment(coords);
        switch (type) {
        case PathIterator.SEG_MOVETO:
          prev = Tensors.vector(coords[0], coords[1]);
          tableBuilder.appendRow(prev);
          break;
        case PathIterator.SEG_LINETO: {
          next = Tensors.vector(coords[0], coords[1]);
          Line2D line2d = geometricLayer.toLine2D(prev, next);
          graphics.draw(line2d);
          prev = next;
          tableBuilder.appendRow(next);
          break;
        }
        case PathIterator.SEG_QUADTO: {
          next = Tensors.vector(coords[0], coords[1]);
          last = Tensors.vector(coords[2], coords[3]);
          Tensor sequence = Tensors.of(prev, next, last);
          ScalarTensorFunction stf = BezierCurve.of(LinearBiinvariantMean.INSTANCE, sequence);
          Path2D path2d = geometricLayer.toPath2D(domain.maps(stf));
          graphics.draw(path2d);
          prev = last;
          tableBuilder.appendRow(next);
          tableBuilder.appendRow(last);
          break;
        }
        case PathIterator.SEG_CUBICTO: {
          next = Tensors.vector(coords[0], coords[1]);
          stol = Tensors.vector(coords[2], coords[3]);
          last = Tensors.vector(coords[4], coords[5]);
          ScalarTensorFunction clo = clothoid(prev, next, stol, last);
          ScalarTensorFunction bez = BezierCurve.of(LinearBiinvariantMean.INSTANCE, Tensors.of(prev, next, stol, last));
          if (param.bezier) {
            graphics.draw(geometricLayer.toPath2D(domain.maps(bez)));
          } else {
            graphics.draw(geometricLayer.toPath2D(domain.maps(clo)));
          }
          prev = last;
          tableBuilder.appendRow(next);
          tableBuilder.appendRow(stol);
          tableBuilder.appendRow(last);
          break;
        }
        case PathIterator.SEG_CLOSE:
          break;
        }
        pathIterator.next();
      }
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
