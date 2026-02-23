// code by jph
package ch.alpine.ascona.usr;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.Random;
import java.util.random.RandomGenerator;

import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.fig.ImagePlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.ShowGridComponent;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.pro.ManipulateProvider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.fit.WeiszfeldMethod;
import ch.alpine.sophus.lie.se2.Se2Matrix;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Outer;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.img.Raster;
import ch.alpine.tensor.io.ImageFormat;
import ch.alpine.tensor.lie.rot.CirclePoints;
import ch.alpine.tensor.mat.re.Inverse;
import ch.alpine.tensor.nrm.Vector2Norm;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.UniformDistribution;
import ch.alpine.tensor.sca.Chop;

@ReflectionMarker
/* package */ enum SpatialMedianShow implements ManipulateProvider {
  INSTANE;

  static final Tensor SE2 = Tensors.fromString("{{180, 0, 6}, {0, -180, 186}, {0, 0, 1}}").unmodifiable();
  static final Tensor POINT = CirclePoints.of(10).multiply(RealScalar.of(0.015)).unmodifiable();
  static final Tensor SE2_2 = Tensors.fromString("{{180*2, 0, 6*2}, {0, -180*2, 186*2}, {0, 0, 1}}").unmodifiable();
  // ---
  public Integer seed = 30;

  /** @param points --- */
  private record Pixel2Coord(Tensor points) {
    private static final Tensor INVERSE = Inverse.of(SE2);

    Scalar dist(Scalar y, Scalar x) {
      Tensor p = INVERSE.dot(Tensors.of(x, y, RealScalar.ONE)).extract(0, 2);
      return points.stream().map(r -> Vector2Norm.between(r, p)).reduce(Scalar::add).get();
    }
  }

  private BufferedImage bufferedImage() {
    RandomGenerator randomGenerator = new Random(seed);
    Tensor points = RandomVariate.of(UniformDistribution.unit(), randomGenerator, 15, 2);
    Optional<Tensor> optional = new WeiszfeldMethod(Chop._10).uniform(points);
    GeometricLayer geometricLayer = new GeometricLayer(SE2);
    BufferedImage bufferedImage = createWhite();
    if (optional.isPresent()) {
      Tensor solution = optional.get();
      Tensor px = Range.of(0, 192);
      Tensor py = Range.of(0, 192);
      Pixel2Coord some = new Pixel2Coord(points);
      Tensor image = Outer.of(some::dist, px, py);
      BufferedImage background = ImageFormat.of(Raster.of(image, ColorDataGradients.DENSITY));
      Graphics2D graphics = bufferedImage.createGraphics();
      graphics.drawImage(background, 0, 0, null);
      RenderQuality.setQuality(graphics);
      {
        graphics.setColor(new Color(128, 128, 255));
        for (Tensor point : points) {
          Path2D path2d = geometricLayer.toPath2D(Tensors.of(solution, point));
          graphics.draw(path2d);
        }
      }
      {
        graphics.setColor(Color.GREEN);
        geometricLayer.pushMatrix(Se2Matrix.translation(solution));
        Path2D path2d = geometricLayer.toPath2D(POINT);
        path2d.closePath();
        graphics.fill(path2d);
        geometricLayer.popMatrix();
      }
      graphics.setColor(Color.RED);
      for (Tensor point : points) {
        geometricLayer.pushMatrix(Se2Matrix.translation(point));
        Path2D path2d = geometricLayer.toPath2D(POINT);
        graphics.fill(path2d);
        geometricLayer.popMatrix();
      }
      graphics.dispose();
    }
    return bufferedImage;
  }

  static BufferedImage createWhite(int size) {
    BufferedImage bufferedImage = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);
    Graphics2D graphics = bufferedImage.createGraphics();
    graphics.setColor(Color.WHITE);
    graphics.fillRect(0, 0, size, size);
    graphics.dispose();
    return bufferedImage;
  }

  static BufferedImage createWhite() {
    return createWhite(192);
  }

  @Override
  public Container getContainer() {
    Show show = new Show();
    show.add(ImagePlot.of(bufferedImage()));
    return ShowGridComponent.of(show);
  }

  static void main() {
    INSTANE.runStandalone();
  }
}
