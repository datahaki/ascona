// code by jph
package ch.alpine.ascona.crv.clt;

import java.awt.Container;

import ch.alpine.bridge.fig.ShowGridComponent;
import ch.alpine.bridge.pro.ManipulateProvider;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;

/** complex function along the real line of which the zeros are solutions
 * to the clothoid fit problem */
@ReflectionMarker
class ClothoidTangentDefectDemo implements ManipulateProvider {
  public Clip clip = Clips.absolute(30.0);
  @FieldSlider
  @FieldClip(min = "-10", max = "10")
  public Scalar s1 = RealScalar.of(0);
  @FieldSlider
  @FieldClip(min = "-10", max = "10")
  public Scalar s2 = RealScalar.of(0);

  @Override
  public Container getContainer() {
    return ShowGridComponent.of(new ClothoidTangentDefectShow(s1, s2, clip).getShow());
  }

  static void main() {
    new ClothoidTangentDefectDemo().runStandalone();
  }
}
