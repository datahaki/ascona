// code by jph
package ch.alpine.ascona.flt;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import ch.alpine.ascony.api.BufferedImageSupplier;
import ch.alpine.ascony.sym.SymGeodesic;
import ch.alpine.ascony.sym.SymLinkImage;
import ch.alpine.ascony.sym.SymLinkImages;
import ch.alpine.ascony.sym.SymSequence;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.flt.ga.Regularization2Step;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.sca.N;

@ReflectionMarker
public class Regularization2StepDemo extends AbstractSpectrogramDemo implements BufferedImageSupplier {
  @ReflectionMarker
  public static class Ratio {
    /** regularization parameter in the interval [0, 1] */
    @FieldSlider
    @FieldClip(min = "0.0", max = "1.0")
    public Scalar ratio = RealScalar.of(0.6);
  }

  private final Ratio ratio;

  public Regularization2StepDemo() {
    this(new Ratio());
  }

  public Regularization2StepDemo(Ratio ratio) {
    super(ratio);
    this.ratio = ratio;
    // ---
    updateState();
  }

  @Override // from AbstractDatasetFilterDemo
  public Tensor protected_render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Regularization2Step regularization2Step = new Regularization2Step(gokartPoseSpec.manifoldDisplays.manifoldDisplay().geodesicSpace(), //
        N.DOUBLE.apply(ratio.ratio));
    return regularization2Step.string(control());
  }

  @Override // from UniformDatasetFilterDemo
  protected String plotLabel() {
    return "Regularization2Step " + ratio.ratio;
  }

  @Override // from BufferedImageSupplier
  public BufferedImage bufferedImage() {
    Scalar factor = ratio.ratio;
    TensorUnaryOperator tensorUnaryOperator = new Regularization2Step(SymGeodesic.INSTANCE, factor)::string;
    Tensor vector = SymSequence.of(3);
    Tensor tensor = tensorUnaryOperator.apply(vector);
    SymLinkImage symLinkImage = new SymLinkImage(tensor.get(1), SymLinkImages.FONT_SMALL);
    symLinkImage.title("Regularization2Step [" + factor + "]");
    return symLinkImage.bufferedImage();
  }

  static void main() {
    new Regularization2StepDemo().runStandalone();
  }
}
