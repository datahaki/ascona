// code by jph, ob
package ch.alpine.ascona.flt;

import java.awt.image.BufferedImage;

import ch.alpine.ascony.api.GeodesicFilters;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.sym.SymLinkImages;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.flt.CenterFilter;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.red.Nest;

class GeodesicFiltersDatasetDemo extends AbstractSpectrogramDemo {
  @ReflectionMarker
  static class Param {
    public GeodesicFilters gf = GeodesicFilters.GEODESIC;
    @FieldSelectionArray({ "1", "2", "3", "4", "5" })
    public Integer radius = 3;
  }

  private final Param param;

  public GeodesicFiltersDatasetDemo() {
    super(param = new Param());
  }

  @Override // from RenderInterface
  protected Tensor process(Tensor control) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    ScalarUnaryOperator smoothingKernel = specParam.kernel.get();
    GeodesicFilters geodesicFilters = param.gf;
    TensorUnaryOperator tensorUnaryOperator = geodesicFilters.supply(manifoldDisplay.geodesicSpace(), smoothingKernel);
    return Nest.of( //
        new CenterFilter(tensorUnaryOperator, specParam.radius), //
        control, param.radius);
  }

  @Override // from BufferedImageSupplier
  protected BufferedImage bufferedImage() {
    GeodesicFilters geodesicFilters = param.gf;
    return switch (geodesicFilters) {
    case GEODESIC -> SymLinkImages.ofGC(specParam.kernel.get(), specParam.radius).bufferedImage();
    default -> null;
    };
  }

  static void main() {
    new GeodesicFiltersDatasetDemo().runStandalone();
  }
}
