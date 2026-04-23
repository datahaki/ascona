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
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import ch.alpine.ascony.ren.BezierGlyphRender;
import ch.alpine.ascony.ren.ClothoidGlyphRender;
import ch.alpine.ascony.ren.ColorPairs;
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
import ch.alpine.sophis.srf.GlyphMesh;
import ch.alpine.sophis.srf.ReduceMesh;
import ch.alpine.sophis.srf.SurfaceMesh;
import ch.alpine.sophus.lie.se2.Se2Matrix;
import ch.alpine.sophus.math.UpperVectorize;
import ch.alpine.tensor.Rational;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Flatten;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.col.ColorDataGradients;
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
    @FieldClip(min = "0", max = "10")
    public Integer res = 5;
    public Font font = new Font(Font.DIALOG, Font.PLAIN, 12);
    public ColorDataGradients cdg = ColorDataGradients.ALPINE;
    public Boolean hide = true;
    public Boolean redux = true;
    @FieldSlider(showValue = true)
    @FieldClip(min = "0.0", max = "0.2")
    public Scalar dist = RealScalar.of(1e-2);
  }

  private final Param param;

  public GlyphDemo() {
    super(param = new Param());
    geometricComponent().addRenderInterfaceBackground(new GridRender(geometricComponent()::getSize));
    Tensor pvm = PvmBuilder.rhs().setOffset(100, 600).setPerPixel(50).digest();
    geometricComponent().setModel2Pixel(pvm);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor domain = Subdivide.intermediate_increasing(Clips.unit(), 1 + param.res);
    domain = Subdivide.increasing(Clips.unit(), 1 + param.res);
    TableBuilder tableBuilder = new TableBuilder();
    String collect = new String(Character.toChars(param.charIndex));
    // collect = "$&%";
    // AffineTransform affineTransform = null; // new AffineTransform(1, 0, 0, -1, 0, 0);
    FontRenderContext frc = new FontRenderContext(null, true, true);
    GlyphVector gv = param.font.createGlyphVector(frc, collect); // or any string
    for (int index = 0; index < gv.getNumGlyphs(); ++index) {
      Shape shape = gv.getGlyphOutline(index); // first character
      {
        graphics.setColor(Color.BLUE);
        graphics.fill(shape);
        Point2D point2d = geometricLayer.toPoint2D(Tensors.vector(0, 0));
        graphics.drawGlyphVector(gv, (int) point2d.getX(), (int) point2d.getY());
        // IO.println("draw glyph vec");
      }
      AffineTransform affineTransform = new AffineTransform(1, 0, 0, -1, 0, 0);
      SurfaceMesh surfaceMesh = GlyphMesh.of(shape.getPathIterator(affineTransform));
      if (param.redux) //
        surfaceMesh = new ReduceMesh(param.dist).of(surfaceMesh);
      graphics.setColor(Color.BLACK);
      graphics.setStroke(new BasicStroke(2));
      Rectangle rectangle = shape.getBounds();
      if (param.hide)
        new BezierGlyphRender(surfaceMesh, domain).render(geometricLayer, graphics);
      geometricLayer.pushMatrix(Se2Matrix.translation(Tensors.vector(rectangle.width, 0)));
      graphics.setColor(Color.RED);
      graphics.setStroke(new BasicStroke(1));
      new BezierGlyphRender(surfaceMesh, domain).render(geometricLayer, graphics);
      graphics.setColor(Color.BLACK);
      graphics.setStroke(new BasicStroke(2));
      new ClothoidGlyphRender(surfaceMesh, domain).render(geometricLayer, graphics);
      geometricLayer.popMatrix();
      Clip clip = Clips.positive(0.2);
      Tensor matrix = DistanceMatrix.of(surfaceMesh.vrt, Vector2Norm::between).maps(clip);
      if (param.hide)
        manifoldDisplay().showPoints(ColorPairs.CONTROL_POINTS, RealScalar.ONE, surfaceMesh.vrt) //
            .render(geometricLayer, graphics);
      Dimension dimension = geometricComponent().getSize();
      dimension.height /= 2;
      {
        Show show = new Show();
        show.add(MatrixPlot.of(matrix, param.cdg.deriveWithOpacity(Rational.of(3, 4))));
        show.setShowLabel("Distance matrix");
        show.render_autoIndent(graphics, new Rectangle(dimension.width - 400, 0, 400, dimension.height));
      }
      if (Tensors.nonEmpty(matrix)) {
        Tensor vec = Tensor.of(Flatten.scalars(UpperVectorize.of(matrix, 1)) //
            .filter(Scalars.lessThan(param.dist)) //
            .sorted());
        Show show = new Show();
        show.add(ListPlot.of(Range.of(0, vec.length()), vec));
        show.setShowLabel("lowest dist.");
        show.render_autoIndent(graphics, new Rectangle(dimension.width - 400, dimension.height, 400, dimension.height));
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
