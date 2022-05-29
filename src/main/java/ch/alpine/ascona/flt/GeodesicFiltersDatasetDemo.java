// code by jph, ob
package ch.alpine.ascona.flt;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import ch.alpine.ascona.util.api.BufferedImageSupplier;
import ch.alpine.ascona.util.api.GeodesicFilters;
import ch.alpine.ascona.util.dat.GokartPoseDataV1;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.sym.SymLinkImages;
import ch.alpine.ascona.util.win.LookAndFeels;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.swing.SpinnerLabel;
import ch.alpine.sophus.flt.CenterFilter;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.red.Nest;

public class GeodesicFiltersDatasetDemo extends AbstractDatasetKernelDemo implements BufferedImageSupplier {
  private final SpinnerLabel<GeodesicFilters> spinnerFilters = SpinnerLabel.of(GeodesicFilters.class);
  private final SpinnerLabel<Integer> spinnerConvolution = SpinnerLabel.of(1, 2, 3, 4, 5, 6, 7, 8);

  public GeodesicFiltersDatasetDemo() {
    super(ManifoldDisplays.SE2_R2, GokartPoseDataV1.INSTANCE);
    {
      spinnerFilters.addToComponentReduced(timerFrame.jToolBar, new Dimension(170, 28), "filter type");
      spinnerFilters.addSpinnerListener(type -> updateState());
    }
    {
      spinnerConvolution.addToComponentReduced(timerFrame.jToolBar, new Dimension(50, 28), "convolution");
      spinnerConvolution.addSpinnerListener(type -> updateState());
    }
    spinnerLabelString.setIndex(15);
    // ---
    updateState();
  }

  @Override // from UniformDatasetFilterDemo
  protected void updateState() {
    super.updateState();
  }

  @Override // from RenderInterface
  protected Tensor protected_render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    ScalarUnaryOperator smoothingKernel = spinnerKernel.getValue().get();
    GeodesicFilters geodesicFilters = spinnerFilters.getValue();
    TensorUnaryOperator tensorUnaryOperator = geodesicFilters.from(manifoldDisplay, smoothingKernel);
    return Nest.of( //
        new CenterFilter(tensorUnaryOperator, spinnerRadius.getValue()), //
        control(), spinnerConvolution.getValue());
  }

  @Override // from BufferedImageSupplier
  public BufferedImage bufferedImage() {
    GeodesicFilters geodesicFilters = spinnerFilters.getValue();
    switch (geodesicFilters) {
    case GEODESIC:
      return SymLinkImages.ofGC(spinnerKernel.getValue().get(), spinnerRadius.getValue()).bufferedImage();
    default:
      return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    }
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.updateUI();
    new GeodesicFiltersDatasetDemo().setVisible(1000, 800);
  }
}
