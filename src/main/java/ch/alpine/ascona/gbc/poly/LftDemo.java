// code by jph
package ch.alpine.ascona.gbc.poly;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.ImageRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.gbc.d2.ThreePointCoordinate;
import ch.alpine.sophis.gbc.d2.ThreePointScalings;
import ch.alpine.sophus.lie.se2.Se2Matrix;
import ch.alpine.sophus.math.Genesis;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.io.ImageFormat;
import ch.alpine.tensor.itp.Interpolation;
import ch.alpine.tensor.itp.LinearInterpolation;
import ch.alpine.tensor.jet.LinearFractionalTransform;
import ch.alpine.tensor.mat.DiagonalMatrix;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Clips;

public class LftDemo extends ControlPointsDemo {
  private Tensor REF;

  @ReflectionMarker
  public static class Param0 extends AsconaParam {
    public Param0() {
      super(false, ManifoldDisplays.R2_ONLY);
    }
  }

  BufferedImage bi = null;

  public LftDemo() {
    super(new Param0());
    try {
      // FIXME ASCONA this does not work
      bi = ImageIO.read(HomeDirectory.file("public_html/other/front", "album_it.jpg"));
      int w = bi.getWidth() - 1;
      int h = bi.getHeight() - 1;
      REF = Tensors.fromString("{{1,1,0}, {" + w + ",1,0}, {" + w + "," + h + ",0}, {1," + h + ",0}}");
    } catch (IOException e) {
      e.printStackTrace();
    }
    setControlPointsSe2(REF);
    timerFrame.geometricComponent.setModel2Pixel(Se2Matrix.flipY(400).dot(DiagonalMatrix.of(2, 2, 1)));
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    CoordinateBoundingBox cbb = CoordinateBoundingBox.of( //
        Clips.positive(bi.getWidth()), //
        Clips.positive(bi.getHeight()));
    ImageRender imageRender = new ImageRender(bi, cbb);
    imageRender.render(geometricLayer, graphics);
    RenderQuality.setQuality(graphics);
    {
      LeversRender leversRender = LeversRender.of( //
          manifoldDisplay, REF, REF.get(0), geometricLayer, graphics);
      leversRender.renderSequence();
    }
    Tensor sequence = getGeodesicControlPoints();
    {
      LeversRender leversRender = //
          LeversRender.of(manifoldDisplay, sequence, sequence.get(0), geometricLayer, graphics);
      leversRender.renderSequence();
      PathRender pathRender = new PathRender(Color.BLUE);
      pathRender.setCurve(sequence, true);
      pathRender.render(geometricLayer, graphics);
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
      {
        BufferedImage res1 = ImageFormat.of(rectify1(src, points, resw, resh));
        int x = res1.getWidth() * f;
        graphics.drawImage(res1, x + 2, 0, x, res1.getHeight() * f, null);
      }
      {
        Tensor res = rectify2(src, points, resw, resh);
        // res = res.map(Clips.positive(255));
        BufferedImage res2 = ImageFormat.of(res);
        graphics.drawImage(res2, 0, 0, res2.getWidth() * f, res2.getHeight() * f, null);
      }
    }
  }

  public static LinearFractionalTransform lft(Tensor points, int width, int height) {
    Tensor reference = Tensors.matrixInt( //
        new int[][] { { height, 0 }, { height, width }, { 0, width }, { 0, 0 } });
    reference = reference.map(RealScalar.of(-0.5)::add);
    return LinearFractionalTransform.fit(reference, points);
  }

  private static Tensor rectify1(Tensor src, Tensor points, int width, int height) {
    Tensor reference = Tensors.matrixInt( //
        new int[][] { { height, 0 }, { height, width }, { 0, width }, { 0, 0 } });
    Tensor ref2 = reference.map(RealScalar.of(-0.5)::add);
    Genesis genesis = ThreePointCoordinate.of(ThreePointScalings.MEAN_VALUE);
    Interpolation interpolation = LinearInterpolation.of(src);
    try {
      return Tensors.matrix((i, j) -> {
        Tensor p = Tensors.vectorDouble(-i, -j);
        Tensor ref = Tensor.of(ref2.stream().map(p::add));
        return interpolation.get(genesis.origin(ref).dot(points));
      }, height, width);
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
    return Array.zeros(1, 1, 4);
  }

  private static Tensor rectify2(Tensor src, Tensor points, int width, int height) {
    LinearFractionalTransform lft = lft(points, width, height);
    Interpolation interpolation = LinearInterpolation.of(src);
    try {
      return Tensors.matrix((i, j) -> interpolation.get(lft.apply(Tensors.vectorDouble(i, j))), height, width);
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
    return Array.zeros(1, 1, 4);
  }

  static void main() {
    launch();
  }
}
