// code by jph
package ch.alpine.ascona.usr;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Random;
import java.util.random.RandomGenerator;

import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.sophus.fit.WeiszfeldMethod;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Outer;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.img.Raster;
import ch.alpine.tensor.io.Export;
import ch.alpine.tensor.io.ImageFormat;
import ch.alpine.tensor.mat.re.Inverse;
import ch.alpine.tensor.nrm.Vector2Norm;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.UniformDistribution;
import ch.alpine.tensor.sca.Chop;

/* package */ enum SpatialMedianShow {
  ;
  /** @param points --- */
  private record Pixel2Coord(Tensor points) {
    private static final Tensor INVERSE = Inverse.of(StaticHelper.SE2);

    Scalar dist(Scalar y, Scalar x) {
      Tensor p = INVERSE.dot(Tensors.of(x, y, RealScalar.ONE)).extract(0, 2);
      return points.stream().map(r -> Vector2Norm.between(r, p)).reduce(Scalar::add).get();
    }
  }

  private static Tensor image(int seed) {
    RandomGenerator random = new Random(seed);
    Tensor points = RandomVariate.of(UniformDistribution.unit(), random, 15, 2);
    Optional<Tensor> optional = new WeiszfeldMethod(Chop._10).uniform(points);
    GeometricLayer geometricLayer = new GeometricLayer(StaticHelper.SE2);
    BufferedImage bufferedImage = StaticHelper.createWhite();
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
        geometricLayer.pushMatrix(GfxMatrix.translation(solution));
        Path2D path2d = geometricLayer.toPath2D(StaticHelper.POINT);
        path2d.closePath();
        graphics.fill(path2d);
        geometricLayer.popMatrix();
      }
      graphics.setColor(Color.RED);
      for (Tensor point : points) {
        geometricLayer.pushMatrix(GfxMatrix.translation(point));
        Path2D path2d = geometricLayer.toPath2D(StaticHelper.POINT);
        graphics.fill(path2d);
        geometricLayer.popMatrix();
      }
      graphics.dispose();
    }
    return ImageFormat.from(bufferedImage);
  }

  public static void main(String[] args) throws IOException {
    File folder = HomeDirectory.Pictures(SpatialMedianShow.class.getSimpleName());
    folder.mkdir();
    for (int seed = 30; seed < 40; ++seed) {
      Tensor image = image(seed);
      Export.of(new File(folder, String.format("%03d.png", seed)), image);
    }
    {
      Export.of(HomeDirectory.Pictures(SpatialMedianShow.class.getSimpleName() + ".png"), image(35));
    }
  }
}
