// code by jph
package ch.alpine.ascona.lev;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.stream.IntStream;

import ch.alpine.ascona.arp.HsArrayPlot;
import ch.alpine.ascona.dis.ManifoldDisplay;
import ch.alpine.ascona.util.ren.BarLegend;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.win.ImageRender;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Ordering;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.img.ColorFormat;
import ch.alpine.tensor.num.Pi;

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
      Tensor pixel2model = HsArrayPlot.pixel2model( //
          Tensors.of(Pi.VALUE.add(RealScalar.of(0.4)), dy.negate()), //
          Tensors.of(dy.add(dy), dy.add(dy)), //
          new Dimension(bufferedImage.getHeight(), bufferedImage.getHeight()));
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
