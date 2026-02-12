// code by jph, ob
package ch.alpine.ascona.flt;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import ch.alpine.ascony.api.BufferedImageSupplier;
import ch.alpine.ascony.api.GeodesicFilters;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.sym.SymLinkImages;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.swing.SpinnerLabel;
import ch.alpine.sophis.flt.CenterFilter;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.red.Nest;

//
public class GeodesicFiltersDatasetDemo extends AbstractSpectrogramDemo implements BufferedImageSupplier {
  private final SpinnerLabel<GeodesicFilters> spinnerFilters = SpinnerLabel.of(GeodesicFilters.class);
  private final SpinnerLabel<Integer> spinnerConvolution = SpinnerLabel.of(1, 2, 3, 4, 5, 6, 7, 8);

  public GeodesicFiltersDatasetDemo() {
    super(ManifoldDisplays.SE2_R2);
    gokartPoseSpec.string = "20190215/20190215T144349_01";
    {
      spinnerFilters.setValue(GeodesicFilters.GEODESIC);
      spinnerFilters.addToComponent(timerFrame.jToolBar, "filter type");
      spinnerFilters.addSpinnerListener(_ -> updateState());
    }
    {
      spinnerConvolution.setValue(3);
      spinnerConvolution.addToComponent(timerFrame.jToolBar, "convolution");
      spinnerConvolution.addSpinnerListener(_ -> updateState());
    }
    // ---
    updateState();
  }

  @Override // from UniformDatasetFilterDemo
  protected void updateState() {
    super.updateState();
  }

  @Override // from RenderInterface
  protected Tensor protected_render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = gokartPoseSpec.manifoldDisplays.manifoldDisplay();
    ScalarUnaryOperator smoothingKernel = gokartPoseSpec.kernel.get();
    GeodesicFilters geodesicFilters = spinnerFilters.getValue();
    TensorUnaryOperator tensorUnaryOperator = geodesicFilters.supply(manifoldDisplay.geodesicSpace(), smoothingKernel);
    return Nest.of( //
        new CenterFilter(tensorUnaryOperator, param.radius), //
        control(), spinnerConvolution.getValue());
  }

  @Override // from BufferedImageSupplier
  public BufferedImage bufferedImage() {
    GeodesicFilters geodesicFilters = spinnerFilters.getValue();
    return switch (geodesicFilters) {
    case GEODESIC -> SymLinkImages.ofGC(gokartPoseSpec.kernel.get(), param.radius).bufferedImage();
    default -> null;
    };
  }

  static void main() {
    launch();
  }
}
