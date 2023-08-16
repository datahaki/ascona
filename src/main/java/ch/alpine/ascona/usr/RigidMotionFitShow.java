// code by jph
package ch.alpine.ascona.usr;

import ch.alpine.bridge.fig.ArrayPlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.ShowDialog;
import ch.alpine.sophus.lie.se.RigidMotionFit;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.num.Pi;
import ch.alpine.tensor.pdf.Distribution;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.NormalDistribution;
import ch.alpine.tensor.red.Mean;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;
import ch.alpine.tensor.sca.tri.ArcTan;

/* package */ enum RigidMotionFitShow {
  ;
  private static Tensor shufflePoints(int n) {
    Distribution distribution = NormalDistribution.standard();
    Tensor random = RandomVariate.of(distribution, n, 2);
    Tensor mean = Mean.of(random).negate();
    return Tensor.of(random.stream().map(mean::add));
  }

  public static void main(String[] args) {
    Tensor target = Array.zeros(1, 2);
    Tensor shuffl = shufflePoints(2);
    shuffl.forEach(target::append);
    Tensor points = target.copy();
    int RES = 128;
    Tensor param = Subdivide.of(-10, 10, RES);
    Clip clip = Clips.absolute(Pi.VALUE);
    Scalar[][] array = new Scalar[RES][RES];
    for (int x = 0; x < RES; ++x)
      for (int y = 0; y < RES; ++y) {
        points.set(Tensors.of(param.get(x), param.get(y)), 0);
        RigidMotionFit rigidMotionFit = RigidMotionFit.of(target, points);
        Tensor rotation = rigidMotionFit.rotation(); // 2 x 2
        Scalar angle = ArcTan.of(rotation.Get(0, 0), rotation.Get(1, 0));
        array[x][y] = clip.rescale(angle);
      }
    // FIXME
    Show show = new Show();
    show.setAspectRatio(RealScalar.ONE);
    show.add(ArrayPlot.of(Tensors.matrix(array), ColorDataGradients.HUE));
    ShowDialog.of(show);
  }
}
