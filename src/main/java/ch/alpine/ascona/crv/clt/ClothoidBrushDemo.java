// code by jph
package ch.alpine.ascona.crv.clt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.Objects;

import ch.alpine.ascony.ren.GridRender;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldPreferredWidth;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.clt.ClothoidBuilder;
import ch.alpine.sophis.crv.clt.ClothoidBuilders;
import ch.alpine.sophis.crv.clt.ClothoidSampler;
import ch.alpine.sophus.lie.se2.Se2CoveringGroup;
import ch.alpine.sophus.lie.se2.Se2Matrix;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Join;
import ch.alpine.tensor.alg.Reverse;
import ch.alpine.tensor.ext.Cache;

/** allows the user to contour letters of a font using clothoids */
class ClothoidBrushDemo extends ClothoidSequenceDemo {
  public static final Scalar BETA = RealScalar.of(0.05);

  @ReflectionMarker
  static class Param {
    @FieldPreferredWidth(200)
    public Tensor shiftL = Tensors.vector(-1.3, -1.3, 0);
    @FieldPreferredWidth(200)
    public Tensor shiftR = Tensors.vector(0, 0, 0);
    @FieldSlider
    @FieldClip(min = "0.00", max = "1")
    public Scalar round = RealScalar.of(0.1);
    public Boolean shade = true;
    @FieldSlider
    @FieldClip(min = "0", max = "1.5708")
    public Scalar angle = RealScalar.of(0.8);
    @FieldSlider
    @FieldClip(min = "0", max = "0.7")
    public Scalar width = RealScalar.of(0.3);
    public Font font = new Font(Font.DIALOG, Font.BOLD, 500);
  }

  public final Cache<Tensor, Tensor> cache = Cache.of(ClothoidBrushDemo::sample, 100);
  private final Param param;

  public ClothoidBrushDemo() {
    this(new Param());
  }

  public ClothoidBrushDemo(Param param) {
    super(param);
    this.param = param;
    try {
      // Font.TYPE1_FONT
      // Font[] fonts = Font.createFonts(new File("/usr/share/fonts/urw-base35/Z003-MediumItalic.t1"));
      // Font[] fonts = Font.createFonts(new File("/home/datahaki/.local/share/fonts/DS Elzevier Initialen.ttf"));
      // IO.println("fonts.length=" + fonts.length);
      // if (0 < fonts.length)
      // param.font = fonts[0].deriveFont(500f);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    // ---
    // Tensor image = ResourceData.of("/letter/cal2/hi/a.png");
    // if (Objects.nonNull(image)) {
    // image = image.map(s -> Scalars.isZero(s) ? RealScalar.of(192) : s);
    // BufferedImage bufferedImage = ImageFormat.of(image);
    // ImageRender imageRender = ImageRender.range(bufferedImage, Tensors.vector(10, 10));
    // // timerFrame.geometricComponent.addRenderInterfaceBackground(imageRender);
    // }
    geometricComponent().addRenderInterfaceBackground(new GridRender(this::getSize));
    // geometricComponent().setOffset(100, 700);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    if (Objects.nonNull(param.font)) {
      graphics.setColor(new Color(164, 164, 64));
      graphics.setFont(param.font);
      graphics.drawString("ABCDEF", 0, 500);
    }
    Tensor sequence = getGeodesicControlPoints();
    for (int index = 1; index < sequence.length(); ++index) {
      Tensor beg0 = sequence.get(index - 1);
      Tensor end0 = sequence.get(index + 0);
      // Se2CoveringGroupElement shL = Se2CoveringGroup.INSTANCE.element();
      Tensor beg1 = Se2CoveringGroup.INSTANCE.combine(Se2CoveringGroup.INSTANCE.combine(param.shiftL, beg0), param.shiftR);
      Tensor end1 = Se2CoveringGroup.INSTANCE.combine(Se2CoveringGroup.INSTANCE.combine(param.shiftL, end0), param.shiftR);
      Tensor crv0 = cache.apply(Tensors.of(beg0, end0));
      Tensor crv1 = cache.apply(Tensors.of(beg1, end1));
      {
        graphics.setColor(new Color(0, 0, 0, 128));
        Scalar model2pixelWidth = geometricLayer.model2pixelFactor(param.round);
        graphics.setStroke(new BasicStroke(model2pixelWidth.number().floatValue(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Tensor polygon = Join.of(crv0, Reverse.of(crv1));
        {
          Path2D path2d = geometricLayer.toPath2D(polygon, true);
          graphics.draw(path2d);
          graphics.fill(path2d);
        }
        geometricLayer.pushMatrix(Se2Matrix.translation(Tensors.vector(11, 0)));
        graphics.setColor(new Color(64, 64, 64));
        {
          Path2D path2d = geometricLayer.toPath2D(polygon, true);
          graphics.draw(path2d);
          graphics.fill(path2d);
        }
        geometricLayer.popMatrix();
        graphics.setStroke(new BasicStroke());
      }
    }
  }

  private static Tensor sample(Tensor be) {
    Tensor beg0 = be.get(0);
    Tensor end0 = be.get(1);
    ClothoidBuilder clothoidBuilder = ClothoidBuilders.SE2_COVERING.clothoidBuilder();
    return ClothoidSampler.of(clothoidBuilder.curve(beg0, end0), BETA);
  }

  static void main() {
    new ClothoidBrushDemo().runStandalone();
  }
}
