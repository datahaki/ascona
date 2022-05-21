// code by jph
package ch.alpine.ascona.dv;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import ch.alpine.ascona.util.arp.HsArrayPlots;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.ren.BarLegend;
import ch.alpine.ascona.util.ren.ImageRender;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Ordering;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.img.ColorFormat;
import ch.alpine.tensor.num.Pi;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Clips;

/* package */ enum OrderingHelper {
  ;
  static final Scalar FACTOR = RealScalar.of(0.3);

  public static void of( //
      ManifoldDisplay manifoldDisplay, //
      Tensor origin, Tensor sequence, Tensor weights, //
      ColorDataGradient cdg, //
      GeometricLayer geometricLayer, Graphics2D graphics) {
    int[] integers = Ordering.INCREASING.of(weights);
    ColorDataGradient colorDataGradientF = cdg.deriveWithOpacity(RationalScalar.HALF);
    ColorDataGradient colorDataGradientD = cdg;
    Tensor shape = manifoldDisplay.shape();
    for (int index = 0; index < sequence.length(); ++index) {
      Tensor point = sequence.get(integers[index]);
      geometricLayer.pushMatrix(manifoldDisplay.matrixLift(point));
      Path2D path2d = geometricLayer.toPath2D(shape, true);
      Scalar ratio = RationalScalar.of(index, integers.length);
      graphics.setColor(ColorFormat.toColor(colorDataGradientF.apply(ratio)));
      graphics.fill(path2d);
      graphics.setColor(ColorFormat.toColor(colorDataGradientD.apply(ratio)));
      graphics.draw(path2d);
      geometricLayer.popMatrix();
    }
    {
      // 100
      BufferedImage bufferedImage = BarLegend.of(colorDataGradientD, 130, "far", "near");
      Scalar dy = Pi.VALUE;
      dy = FACTOR.multiply(Pi.VALUE);
      Scalar px = Pi.VALUE.add(RealScalar.of(0.4));
      Scalar py = dy.negate();
      CoordinateBoundingBox coordinateBoundingBox = CoordinateBoundingBox
          .of(Stream.of(Clips.interval(px, px.add(dy.add(dy))), Clips.interval(py, py.add(dy.add(dy)))));
      Tensor pixel2model = HsArrayPlots.pixel2model( //
          coordinateBoundingBox, //
          new Dimension(bufferedImage.getHeight(), bufferedImage.getHeight()));
      // HsArrayPlot.pixel2model( //
      // coordinateBoundingBox, //
      // );
      ImageRender.of(bufferedImage, pixel2model).render(geometricLayer, graphics);
    }
    {
      geometricLayer.pushMatrix(manifoldDisplay.matrixLift(origin));
      Path2D path2d = geometricLayer.toPath2D(shape, true);
      graphics.setColor(Color.DARK_GRAY);
      graphics.fill(path2d);
      graphics.setColor(Color.BLACK);
      graphics.draw(path2d);
      geometricLayer.popMatrix();
    }
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, //
          Tensor.of(IntStream.range(0, 8).limit(integers.length) //
              .map(index -> integers[index]) //
              .mapToObj(sequence::get)), //
          origin, geometricLayer, graphics);
      leversRender.renderLevers();
      leversRender.renderIndexX();
    }
  }
}
