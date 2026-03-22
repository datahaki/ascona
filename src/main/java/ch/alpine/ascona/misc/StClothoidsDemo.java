// code by jph
package ch.alpine.ascona.misc;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.util.Random;
import java.util.random.RandomGenerator;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.bridge.gfx.GeometricComponent;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.PvmBuilder;
import ch.alpine.bridge.gfx.RenderInterface;
import ch.alpine.bridge.pro.ManipulateProvider;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.clt.Clothoid;
import ch.alpine.sophis.crv.clt.ClothoidBuilder;
import ch.alpine.sophis.crv.clt.ClothoidComparators;
import ch.alpine.sophis.crv.clt.PriorityClothoid;
import ch.alpine.sophis.ts.ClothoidTransition;
import ch.alpine.sophus.api.TangentSpace;
import ch.alpine.sophus.hs.st.StiefelManifold;
import ch.alpine.sophus.hs.st.TStMemberQ;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Dimensions;
import ch.alpine.tensor.alg.Transpose;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.pdf.RandomSample;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.NormalDistribution;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;

@ReflectionMarker
class StClothoidsDemo implements ManipulateProvider, RenderInterface {
  @FieldSlider
  @FieldClip(min = "4", max = "20")
  public Integer n = 10;
  public ClothoidComparators cc = ClothoidComparators.LENGTH;
  @FieldSlider
  @FieldClip(min = "-10", max = "10")
  public Scalar split = RealScalar.of(0.2);
  public Clip clip = Clips.absolute(15.0);
  // ---
  private final GeometricComponent geometricComponent = new GeometricComponent();

  public StClothoidsDemo() {
    geometricComponent.addRenderInterface(this);
    Tensor digest = PvmBuilder.rhs().setOffset(600, 300).setPerPixel(100).digest();
    geometricComponent.setModel2Pixel(digest);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    StiefelManifold stiefelManifold = new StiefelManifold(n, 3);
    RandomGenerator randomGenerator = new Random(3);
    Tensor p = RandomSample.of(stiefelManifold.randomSampleInterface());
    Tensor v = new TStMemberQ(p).projection( //
        RandomVariate.of(NormalDistribution.of(0, 0.4), randomGenerator, Dimensions.of(p)));
    TangentSpace exponential = stiefelManifold.tangentSpace(p);
    ScalarTensorFunction stf = s -> exponential.exp(v.multiply(s));
    Tensor beg = Tensors.vector(-3, 0, 0);
    Tensor sequence = Transpose.of(stf.apply(split)).multiply(RealScalar.of(3));
    // IO.println(Pretty.of(sequence.maps(Round._3)));
    graphics.setColor(Color.BLUE);
    graphics.setStroke(new BasicStroke(1.5f));
    ClothoidBuilder clothoidBuilder = new PriorityClothoid(cc, clip);
    for (int index = 0; index < sequence.length(); ++index) {
      Tensor end = sequence.get(index);
      Clothoid clothoid = clothoidBuilder.curve(beg, end);
      ClothoidTransition clothoidTransition = ClothoidTransition.of(beg, end, clothoid);
      graphics.draw(geometricLayer.toPath2D(clothoidTransition.linearized(RealScalar.of(0.05))));
    }
    {
      ManifoldDisplay manifoldDisplay = ManifoldDisplays.Se2.manifoldDisplay();
      LeversRender leversRender = LeversRender.of(manifoldDisplay, sequence, null, geometricLayer, graphics);
      leversRender.renderSequence();
    }
  }

  @Override
  public Container getContainer() {
    return geometricComponent;
  }

  static void main() {
    new StClothoidsDemo().runStandalone();
  }
}
