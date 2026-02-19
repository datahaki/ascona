// code by jph
package ch.alpine.ascona.crv.dub;

import java.util.LinkedList;
import java.util.List;

import ch.alpine.bridge.fig.DensityPlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.pro.ShowWindow;
import ch.alpine.sophis.crv.dub.DubinsPath;
import ch.alpine.sophis.crv.dub.DubinsPathComparators;
import ch.alpine.sophis.crv.dub.FixedRadiusDubins;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.api.ScalarBinaryOperator;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Clips;

/* package */ enum DubinsPathImages implements ScalarBinaryOperator {
  TYPE {
    @Override
    public Scalar apply(Scalar x, Scalar y) {
      Tensor xya = Tensors.of(x, y, ALPHA);
      DubinsPath dubinsPath = FixedRadiusDubins.of(xya, RADIUS).stream().min(DubinsPathComparators.LENGTH).orElseThrow();
      int ordinal = dubinsPath.dubinsType().ordinal();
      return RealScalar.of(ordinal);
    }
  },
  CURVATURE {
    @Override
    public Scalar apply(Scalar x, Scalar y) {
      Tensor xya = Tensors.of(x, y, ALPHA);
      DubinsPath dubinsPath = FixedRadiusDubins.of(xya, RADIUS).stream().min(DubinsPathComparators.LENGTH).orElseThrow();
      int ordinal = dubinsPath.dubinsType().ordinal();
      return dubinsPath.totalCurvature().add(RealScalar.of(ordinal));
    }
  };

  private static final Scalar ALPHA = RealScalar.of(-2.0);
  private static final Scalar RADIUS = RealScalar.of(0.5);

  static void main() {
    List<Show> list = new LinkedList<>();
    CoordinateBoundingBox cbb = CoordinateBoundingBox.of(Clips.absolute(2), Clips.absolute(2));
    for (DubinsPathImages d : DubinsPathImages.values()) {
      Show show = new Show();
      show.add(DensityPlot.of(d, cbb, ColorDataGradients.CLASSIC));
      list.add(show);
    }
    ShowWindow.asDialog(list);
  }
}
