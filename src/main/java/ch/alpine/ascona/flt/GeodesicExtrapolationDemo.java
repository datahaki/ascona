// code by ob
package ch.alpine.ascona.flt;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import ch.alpine.ascona.util.api.BufferedImageSupplier;
import ch.alpine.ascona.util.dat.GokartPoseDataV2;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.sym.SymGeodesic;
import ch.alpine.ascona.util.sym.SymLinkImage;
import ch.alpine.ascona.util.sym.SymLinkImages;
import ch.alpine.ascona.util.sym.SymSequence;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophus.flt.ga.GeodesicExtrapolation;
import ch.alpine.sophus.flt.ga.GeodesicExtrapolationFilter;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorUnaryOperator;

public class GeodesicExtrapolationDemo extends AbstractSpectrogramDemo implements BufferedImageSupplier {
  private Tensor refined = Tensors.empty();

  public GeodesicExtrapolationDemo() {
    super(ManifoldDisplays.SE2_R2, GokartPoseDataV2.INSTANCE);
    updateState();
  }

  @Override
  protected void updateState() {
    super.updateState();
    // ---
    ManifoldDisplay manifoldDisplay = gokartPoseSpec.manifoldDisplays.manifoldDisplay();
    TensorUnaryOperator tensorUnaryOperator = //
        GeodesicExtrapolation.of(manifoldDisplay.geodesicSpace(), gokartPoseSpec.kernel.get());
    refined = GeodesicExtrapolationFilter.of(tensorUnaryOperator, manifoldDisplay.geodesicSpace(), param.radius).apply(control());
  }

  @Override // from RenderInterface
  protected Tensor protected_render(GeometricLayer geometricLayer, Graphics2D graphics) {
    return refined;
  }

  @Override // from BufferedImageSupplier
  public BufferedImage bufferedImage() {
    ScalarUnaryOperator smoothingKernel = gokartPoseSpec.kernel.get();
    int radius = param.radius;
    TensorUnaryOperator tensorUnaryOperator = GeodesicExtrapolation.of(SymGeodesic.INSTANCE, smoothingKernel);
    Tensor vector = SymSequence.of(radius + 1);
    Tensor tensor = tensorUnaryOperator.apply(vector);
    SymLinkImage symLinkImage = new SymLinkImage(tensor, SymLinkImages.FONT_SMALL);
    symLinkImage.title(smoothingKernel + "[" + (radius + 1) + "]");
    return symLinkImage.bufferedImage();
  }

  public static void main(String[] args) {
    launch();
  }
}
