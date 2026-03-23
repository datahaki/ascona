// code by jph
package ch.alpine.ascona.misc;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;

import ch.alpine.ascony.dat.GlyphMesh;
import ch.alpine.ascony.ren.BezierGlyphRender;
import ch.alpine.ascony.ren.ClothoidGlyphRender;
import ch.alpine.ascony.ren.ColorPair;
import ch.alpine.ascony.ren.GridRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.EuclideanPlaneDemo;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.plt.ListPlot;
import ch.alpine.bridge.fig.plt.MatrixPlot;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.PvmBuilder;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.math.DistanceMatrix;
import ch.alpine.sophis.srf.SurfaceMesh;
import ch.alpine.sophus.lie.se2.Se2Matrix;
import ch.alpine.sophus.math.UpperVectorize;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Flatten;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.io.TableBuilder;
import ch.alpine.tensor.nrm.Vector2Norm;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;

class GlyphDemo extends EuclideanPlaneDemo {
  @ReflectionMarker
  static class Param {
    @FieldClip(min = "0", max = "65500")
    public Integer charIndex = 32;
    @FieldSlider
    @FieldClip(min = "0", max = "20")
    public Integer res = 20;
    public Font font = new Font(Font.DIALOG, Font.PLAIN, 12);
    public ColorDataGradients cdg = ColorDataGradients.ALPINE;
  }

  private final Param param;

  public GlyphDemo() {
    super(param = new Param());
    geometricComponent().addRenderInterfaceBackground(new GridRender(this::getSize));
    Tensor pvm = PvmBuilder.rhs().setOffset(100, 600).setPerPixel(50).digest();
    geometricComponent().setModel2Pixel(pvm);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor domain = Subdivide.intermediate_increasing(Clips.unit(), 1 + param.res);
    TableBuilder tableBuilder = new TableBuilder();
    String collect = new String(Character.toChars(param.charIndex));
    // collect = "$&%";
    FontRenderContext frc = new FontRenderContext(null, true, true);
    GlyphVector gv = param.font.createGlyphVector(frc, collect); // or any string
    for (int index = 0; index < gv.getNumGlyphs(); ++index) {
      Shape shape = gv.getGlyphOutline(index); // first character
      graphics.setColor(Color.BLACK);
      graphics.setStroke(new BasicStroke(2));
      new BezierGlyphRender(shape, domain).render(geometricLayer, graphics);
      geometricLayer.pushMatrix(Se2Matrix.translation(Tensors.vector(10, 0)));
      graphics.setColor(Color.RED);
      graphics.setStroke(new BasicStroke(1));
      new BezierGlyphRender(shape, domain).render(geometricLayer, graphics);
      graphics.setColor(Color.BLACK);
      graphics.setStroke(new BasicStroke(2));
      new ClothoidGlyphRender(shape, domain).render(geometricLayer, graphics);
      geometricLayer.popMatrix();
      SurfaceMesh surfaceMesh = GlyphMesh.of(shape);
      Clip clip = Clips.positive(0.05);
      Tensor matrix = DistanceMatrix.of(surfaceMesh.vrt, Vector2Norm::between).maps(clip);
      manifoldDisplay().showPoints(ColorPair.CONTROL_POINTS, RealScalar.ONE, surfaceMesh.vrt) //
          .render(geometricLayer, graphics);
      Dimension dimension = getSize();
      {
        Show show = new Show();
        show.add(MatrixPlot.of(matrix, param.cdg));
        show.setShowLabel("Distance matrix");
        show.render_autoIndent(graphics, new Rectangle(dimension.width - 400, 0, 400, 300));
      }
      if (Tensors.nonEmpty(matrix)) {
        Tensor vec = Tensor.of(Flatten.scalars(UpperVectorize.of(matrix, 1)) //
            .filter(Scalars.lessThan(RealScalar.of(0.04))).sorted());
        Show show = new Show();
        show.add(ListPlot.of(Range.of(0, vec.length()), vec));
        show.render_autoIndent(graphics, new Rectangle(dimension.width - 400, 300, 400, 300));
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
