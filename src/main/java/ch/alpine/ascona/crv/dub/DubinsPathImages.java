// code by jph
package ch.alpine.ascona.crv.dub;

import java.util.LinkedList;
import java.util.List;

import ch.alpine.bridge.fig.ImagePlot;
import ch.alpine.bridge.fig.MatrixPlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.ShowWindow;
import ch.alpine.sophis.crv.dub.DubinsPath;
import ch.alpine.sophis.crv.dub.DubinsPathComparators;
import ch.alpine.sophis.crv.dub.FixedRadiusDubins;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.io.ImageFormat;

/* package */ class DubinsPathImages {
  private static final int RES = 128 + 64;
  private static final Tensor RE = Subdivide.of(-2, +2, RES - 1);
  private static final Tensor IM = Subdivide.of(-2, +2, RES - 1);
  private static final Scalar ALPHA = RealScalar.of(-2.0);
  private static final Scalar RADIUS = RealScalar.of(0.5);

  static Scalar type(int y, int x) {
    Tensor xya = Tensors.of(RE.Get(x), IM.Get(y), ALPHA);
    DubinsPath dubinsPath = FixedRadiusDubins.of(xya, RADIUS).stream().min(DubinsPathComparators.LENGTH).orElseThrow();
    int ordinal = dubinsPath.dubinsType().ordinal();
    return RealScalar.of(ordinal);
  }

  static Scalar curvature(int y, int x) {
    Tensor xya = Tensors.of(RE.Get(x), IM.Get(y), ALPHA);
    DubinsPath dubinsPath = FixedRadiusDubins.of(xya, RADIUS).stream().min(DubinsPathComparators.LENGTH).orElseThrow();
    int ordinal = dubinsPath.dubinsType().ordinal();
    return dubinsPath.totalCurvature().add(RealScalar.of(ordinal));
  }

  static void main() {
    List<Show> list = new LinkedList<>();
    {
      Show show = new Show();
      Tensor matrix = Tensors.matrix(DubinsPathImages::type, RES, RES);
      ColorDataIndexed colorDataLists = ColorDataLists._097.strict();
      Tensor image = matrix.maps(colorDataLists);
      show.add(ImagePlot.of(ImageFormat.of(image)));
      list.add(show);
    }
    {
      Show show = new Show();
      Tensor matrix = Tensors.matrix(DubinsPathImages::curvature, RES, RES);
      show.add(MatrixPlot.of(matrix, ColorDataGradients.CLASSIC));
      list.add(show);
    }
    ShowWindow.asDialog(list);
  }
}
