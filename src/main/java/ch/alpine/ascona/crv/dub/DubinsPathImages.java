// code by jph
package ch.alpine.ascona.crv.dub;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import ch.alpine.sophis.crv.dub.DubinsPath;
import ch.alpine.sophis.crv.dub.DubinsPathComparators;
import ch.alpine.sophis.crv.dub.FixedRadiusDubins;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.io.Export;

/* package */ enum DubinsPathImages {
  ;
  private static final int RES = 128 + 64;
  private static final Tensor RE = Subdivide.of(-2, +2, RES - 1);
  private static final Tensor IM = Subdivide.of(-2, +2, RES - 1);
  private static final Scalar ALPHA = RealScalar.of(-2.0);
  private static final Scalar RADIUS = RealScalar.of(0.5);

  static Scalar type(int y, int x) {
    Tensor xya = Tensors.of(RE.Get(x), IM.Get(y), ALPHA);
    DubinsPath dubinsPath = FixedRadiusDubins.of(xya, RADIUS).stream().min(DubinsPathComparators.LENGTH).orElseThrow();
    int ordinal = dubinsPath.type().ordinal();
    return RealScalar.of(ordinal);
  }

  static Scalar curvature(int y, int x) {
    Tensor xya = Tensors.of(RE.Get(x), IM.Get(y), ALPHA);
    DubinsPath dubinsPath = FixedRadiusDubins.of(xya, RADIUS).stream().min(DubinsPathComparators.LENGTH).orElseThrow();
    int ordinal = dubinsPath.type().ordinal();
    return dubinsPath.totalCurvature().add(RealScalar.of(ordinal));
  }

  static void main() throws IOException {
    Tensor matrix = Tensors.matrix(DubinsPathImages::type, RES, RES);
    Path directory = HomeDirectory.Pictures.resolve(DubinsPathImages.class.getSimpleName());
    Files.createDirectories(directory);
    // FIXME ASCONA this does not make sense
    for (ColorDataLists colorDataLists : ColorDataLists.values()) {
      Tensor image = matrix.map(colorDataLists.strict());
      Export.of(directory.resolve(colorDataLists.name() + ".png"), image);
    }
  }
}
