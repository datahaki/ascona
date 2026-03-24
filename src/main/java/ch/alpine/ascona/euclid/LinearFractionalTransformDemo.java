// code by jph
package ch.alpine.ascona.euclid;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.ren.ColorStroke;
import ch.alpine.ascony.ren.GridRender;
import ch.alpine.ascony.ren.ImageRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.EuclideanPlaneDemo;
import ch.alpine.bridge.fig.Meshgrid;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.plt.ImagePlot;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.api.Genesis;
import ch.alpine.sophis.gbc.d2.ThreePointCoordinate;
import ch.alpine.sophis.gbc.d2.ThreePointScalings;
import ch.alpine.sophus.lie.se2.Se2Matrix;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.ext.ResourceData;
import ch.alpine.tensor.img.ImageResize;
import ch.alpine.tensor.io.ImageFormat;
import ch.alpine.tensor.itp.Interpolation;
import ch.alpine.tensor.itp.LinearInterpolation;
import ch.alpine.tensor.jet.LinearFractionalTransform;
import ch.alpine.tensor.mat.DiagonalMatrix;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Clips;

class LinearFractionalTransformDemo extends EuclideanPlaneDemo {
  final BufferedImage bufferedImage = ResourceData.bufferedImage("ch/alpine/ascona/image/album_it.jpg");

  @ReflectionMarker
  static class Param {
    public ThreePointScalings tps = ThreePointScalings.MEAN_VALUE;
    public ImageResize imgRes = ImageResize.DEGREE_0;
    public Integer resw = 40;
    public Integer resh = 30;
    @FieldFuse
    public Boolean fuse = false;
  }

  private final Param param;
  private final Tensor src;
  private final Interpolation interpolation;

  public LinearFractionalTransformDemo() {
    super(param = new Param());
    setTitle("" + bufferedImage.getWidth() + " " + bufferedImage.getHeight());
    src = ImageFormat.from(bufferedImage);
    interpolation = LinearInterpolation.of(src);
    // setGeodesicControlPoints(Tensors.fromString("{{46.25, 28.0}, {132.0, 1.0}, {132.0, 99.0}, {46.75, 51.0}}"));
    setGeodesicControlPoints(Tensors.fromString("{{1.25, 1.5}, {132.0, 1.0}, {132.0, 99.0}, {1.0, 99.0}}"));
    geometricComponent().setModel2Pixel(Se2Matrix.flipY(500).dot(DiagonalMatrix.of(4, 4, 1)));
    geometricComponent().addRenderInterfaceBackground(new GridRender(geometricComponent()::getSize));
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.HEAD_TAIL;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    if (param.fuse) {
      param.fuse = false;
      IO.println(getGeodesicControlPoints());
    }
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    CoordinateBoundingBox cbb = CoordinateBoundingBox.of( //
        Clips.positive(bufferedImage.getWidth()), //
        Clips.positive(bufferedImage.getHeight()));
    ImageRender imageRender = new ImageRender(bufferedImage, cbb);
    imageRender.render(geometricLayer, graphics);
    Tensor sequence = getGeodesicControlPoints();
    {
      LeversRender leversRender = //
          LeversRender.of(manifoldDisplay, sequence, null, geometricLayer, graphics);
      new PathRender(ColorStroke.CONVEX_HULL, sequence, true).render(geometricLayer, graphics);
      leversRender.renderIndexP();
      int h = bufferedImage.getHeight();
      final int resw = bufferedImage.getWidth();
      final int resh = bufferedImage.getHeight();
      Tensor points = Tensor.of(sequence.stream().map(p -> Tensors.of( //
          RealScalar.of(h).subtract(p.Get(1)), p.Get(0))));
      LinearFractionalTransform lft = lft(resw, resh, points);
      leversRender.renderMatrix2(Tensors.vector(0, 0, 0), lft.matrix());
      Dimension dimension = geometricComponent().getSize();
      dimension.width /= 2;
      dimension.height /= 2;
      {
        Show show = new Show();
        show.setShowLabel("Mean Value");
        show.add(ImagePlot.of(ImageFormat.of(rectify1(src, points, resw, resh)), param.imgRes));
        Rectangle rectangle = new Rectangle(dimension.width, dimension.height, dimension.width, dimension.height);
        show.render_autoIndent(graphics, rectangle);
        graphics.draw(rectangle);
      }
      {
        Show show = new Show();
        show.setShowLabel("Linear Fractional Transform");
        TensorUnaryOperator lft2 = lft(resw, resh, points).andThen(interpolation::get);
        Meshgrid meshgrid = new Meshgrid( //
            CoordinateBoundingBox.of( //
                Clips.positive(bufferedImage.getWidth()), //
                Clips.positive(bufferedImage.getHeight())), //
            param.resw, param.resh);
        show.add(ImagePlot.of(ImageFormat.of(rectify2(lft2, meshgrid)), param.imgRes));
        show.render_autoIndent(graphics, new Rectangle(dimension.width, 0, dimension.width, dimension.height));
      }
    }
  }

  private static Tensor reference(int width, int height) {
    return Tensors.matrixInt( //
        new int[][] { { 0, 0 }, { width, 0 }, { width, height }, { 0, height } }) //
        .maps(RealScalar.of(-0.5)::add);
  }

  /** @param width
   * @param height
   * @param points in order: SW, SE, NE, NW
   * @return LTF that takes input points in [0,...,height-1] x [0,...,width-1]
   * and maps the input somewhere in the quad spanned by points */
  public static LinearFractionalTransform lft(int width, int height, Tensor points) {
    return LinearFractionalTransform.fit(reference(width, height), points);
  }

  private Tensor rectify1(Tensor src, Tensor points, int width, int height) {
    Tensor reference = reference(width, height);
    Genesis genesis = ThreePointCoordinate.of(param.tps);
    Interpolation interpolation = LinearInterpolation.of(src);
    return Tensors.matrix((i, j) -> {
      Tensor p = Tensors.vectorDouble(-i, -j);
      Tensor ref = Tensor.of(reference.stream().map(p::add));
      try {
        return interpolation.get(genesis.origin(ref).dot(points));
      } catch (Exception e) {
        return Array.zeros(4);
      }
    }, height, width);
  }

  /** @param src for instance image
   * @param points
   * @param width
   * @param height
   * @return */
  private static Tensor rectify2(TensorUnaryOperator tuo, Meshgrid meshgrid) {
    TensorUnaryOperator ttuo = x -> {
      try {
        return tuo.apply(x);
      } catch (Exception e) {
        return Array.zeros(4);
      }
    };
    return meshgrid.image(ttuo);
  }

  static void main() {
    new LinearFractionalTransformDemo().runStandalone();
  }
}
