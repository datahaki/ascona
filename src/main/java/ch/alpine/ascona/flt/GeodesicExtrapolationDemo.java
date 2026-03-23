// code by ob
package ch.alpine.ascona.flt;

import java.awt.image.BufferedImage;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.sym.SymGeodesic;
import ch.alpine.ascony.sym.SymLinkImage;
import ch.alpine.ascony.sym.SymLinkImages;
import ch.alpine.ascony.sym.SymSequence;
import ch.alpine.sophis.flt.ga.GeodesicExtrapolation;
import ch.alpine.sophis.flt.ga.GeodesicExtrapolationFilter;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorUnaryOperator;

class GeodesicExtrapolationDemo extends AbstractSpectrogramDemo {
  public GeodesicExtrapolationDemo() {
    super(new Object());
  }

  @Override // from RenderInterface
  protected Tensor process(Tensor control) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    TensorUnaryOperator tensorUnaryOperator = //
        GeodesicExtrapolation.of(manifoldDisplay.geodesicSpace(), specParam.kernel.get());
    return GeodesicExtrapolationFilter.of(tensorUnaryOperator, manifoldDisplay.geodesicSpace(), specParam.radius).apply(control);
  }

  @Override // from BufferedImageSupplier
  protected BufferedImage bufferedImage() {
    ScalarUnaryOperator smoothingKernel = specParam.kernel.get();
    int radius = specParam.radius;
    TensorUnaryOperator tensorUnaryOperator = GeodesicExtrapolation.of(SymGeodesic.INSTANCE, smoothingKernel);
    Tensor vector = SymSequence.of(radius + 1);
    Tensor tensor = tensorUnaryOperator.apply(vector);
    SymLinkImage symLinkImage = new SymLinkImage(tensor, SymLinkImages.FONT_SMALL);
    symLinkImage.title(smoothingKernel + "[" + (radius + 1) + "]");
    return symLinkImage.bufferedImage();
  }

  static void main() {
    new GeodesicExtrapolationDemo().runStandalone();
  }
}
