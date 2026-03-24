// code by jph
package ch.alpine.ascona.euclid;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.ren.ColorPair;
import ch.alpine.ascony.ren.ColorStroke;
import ch.alpine.ascony.ren.ImageRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.EuclideanPlaneDemo;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.plt.ImagePlot;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.api.Genesis;
import ch.alpine.sophis.gbc.d2.ThreePointCoordinate;
import ch.alpine.sophis.gbc.d2.ThreePointScalings;
import ch.alpine.sophus.lie.se2.Se2Matrix;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.ext.ResourceData;
import ch.alpine.tensor.io.ImageFormat;
import ch.alpine.tensor.itp.Interpolation;
import ch.alpine.tensor.itp.LinearInterpolation;
import ch.alpine.tensor.jet.LinearFractionalTransform;
import ch.alpine.tensor.mat.DiagonalMatrix;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Clips;

class LinearFractionalTransformDemo extends EuclideanPlaneDemo {
  private Tensor REF;
  final BufferedImage bi = ResourceData.bufferedImage("ch/alpine/ascona/image/album_it.jpg");

  @ReflectionMarker
  static class Param {
    public ThreePointScalings tps = ThreePointScalings.MEAN_VALUE;
  }

  private final Param param;

  public LinearFractionalTransformDemo() {
    super(param = new Param());
    int w = bi.getWidth() - 1;
    int h = bi.getHeight() - 1;
    REF = Tensors.fromString("{{1,1,0}, {" + w + ",1,0}, {" + w + "," + h + ",0}, {1," + h + ",0}}");
    setControlPointsSe2(REF);
    geometricComponent().setModel2Pixel(Se2Matrix.flipY(500).dot(DiagonalMatrix.of(4, 4, 1)));
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.HEAD_TAIL;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    CoordinateBoundingBox cbb = CoordinateBoundingBox.of( //
        Clips.positive(bi.getWidth()), //
        Clips.positive(bi.getHeight()));
    ImageRender imageRender = new ImageRender(bi, cbb);
    imageRender.render(geometricLayer, graphics);
    {
      LeversRender leversRender = LeversRender.of( //
          manifoldDisplay, REF, REF.get(0), geometricLayer, graphics);
      leversRender.renderSequence(ColorPair.REFERENCE);
    }
    Tensor sequence = getGeodesicControlPoints();
    {
      LeversRender leversRender = //
          LeversRender.of(manifoldDisplay, sequence, null, geometricLayer, graphics);
      new PathRender(ColorStroke.CONVEX_HULL, sequence, true).render(geometricLayer, graphics);
      leversRender.renderIndexP();
      Tensor src = ImageFormat.from(bi);
      int h = bi.getHeight();
      int f = 3;
      final int resw = bi.getWidth() / f;
      final int resh = bi.getHeight() / f;
      Tensor points = Tensor.of(sequence.stream().map(p -> Tensors.of( //
          RealScalar.of(h).subtract(p.Get(1)), p.Get(0))));
      LinearFractionalTransform lft = lft(points, resw, resh);
      leversRender.renderMatrix2(Tensors.vector(0, 0, 0), lft.matrix());
      Dimension dimension = geometricComponent().getSize();
      dimension.width /= 2;
      dimension.height /= 2;
      dimension.width -= 10;
      dimension.height -= 10;
      {
        Show show = new Show();
        show.setShowLabel("Mean Value");
        show.add(ImagePlot.of(ImageFormat.of(rectify1(src, points, resw, resh))));
        Rectangle rectangle = new Rectangle(dimension.width, dimension.height, dimension.width, dimension.height);
        show.render_autoIndent(graphics, rectangle);
        graphics.draw(rectangle);
      }
      {
        Show show = new Show();
        show.setShowLabel("Linear Fractional Transform");
        show.add(ImagePlot.of(ImageFormat.of(rectify2(src, points, resw, resh))));
        show.render_autoIndent(graphics, new Rectangle(dimension.width, 0, dimension.width, dimension.height));
      }
    }
  }

  public static LinearFractionalTransform lft(Tensor points, int width, int height) {
    Tensor reference = Tensors.matrixInt( //
        new int[][] { { height, 0 }, { height, width }, { 0, width }, { 0, 0 } });
    reference = reference.maps(RealScalar.of(-0.5)::add);
    return LinearFractionalTransform.fit(reference, points);
  }

  private Tensor rectify1(Tensor src, Tensor points, int width, int height) {
    Tensor reference = Tensors.matrixInt( //
        new int[][] { { height, 0 }, { height, width }, { 0, width }, { 0, 0 } });
    Tensor ref2 = reference.maps(RealScalar.of(-0.5)::add);
    Genesis genesis = ThreePointCoordinate.of(param.tps);
    Interpolation interpolation = LinearInterpolation.of(src);
    return Tensors.matrix((i, j) -> {
      Tensor p = Tensors.vectorDouble(-i, -j);
      Tensor ref = Tensor.of(ref2.stream().map(p::add));
      try {
        return interpolation.get(genesis.origin(ref).dot(points));
      } catch (Exception e) {
        return Array.zeros(4);
      }
    }, height, width);
  }

  private static Tensor rectify2(Tensor src, Tensor points, int width, int height) {
    LinearFractionalTransform lft = lft(points, width, height);
    Interpolation interpolation = LinearInterpolation.of(src);
    return Tensors.matrix((i, j) -> {
      try {
        return interpolation.get(lft.apply(Tensors.vectorDouble(i, j)));
      } catch (Exception e) {
        return Array.zeros(4);
      }
    }, height, width);
  }

  static void main() {
    new LinearFractionalTransformDemo().runStandalone();
  }
}
