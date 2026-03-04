// code by jph
package ch.alpine.ascona.euclid.hil;

import java.awt.Container;

import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.ShowGridComponent;
import ch.alpine.bridge.pro.ManipulateProvider;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.d2.ex.HilbertPolygon;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.sca.pow.Power;

/** References:
 * "Iterative coordinates"
 * by Chongyang Deng, Qingjun Chang, Kai Hormann, 2020 */
@ReflectionMarker
class HilbertBenchmarkDemo implements ManipulateProvider {
  @FieldClip(min = "1", max = "4")
  public Integer levels = 2;
  @FieldClip(min = "20", max = "100")
  @FieldSelectionArray({ "20", "30", "50" })
  public Integer resolution = 20;
  public ColorDataGradients cdg = ColorDataGradients.CLASSIC;

  @Override
  public Container getContainer() {
    Tensor polygon = unit(levels);
    Show show = HilbertLevelShow.of(polygon, resolution, cdg, 32);
    return ShowGridComponent.of(show);
  }

  /** @param n positive
   * @return hilbert polygon inside unit square [0, 1]^2 */
  public static Tensor unit(int n) {
    Tensor polygon = HilbertPolygon.of(n).multiply(Power.of(2.0, -n + 1));
    return polygon.maps(scalar -> scalar.subtract(RealScalar.of(1.0 + 1e-5)));
  }

  static void main() {
    new HilbertBenchmarkDemo().runStandalone();
  }
}
