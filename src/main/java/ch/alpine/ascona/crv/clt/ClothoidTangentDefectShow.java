// code by jph
package ch.alpine.ascona.crv.clt;

import java.util.LinkedList;
import java.util.List;

import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.plt.ListPlot;
import ch.alpine.bridge.fig.plt.ReImPlot;
import ch.alpine.bridge.fig.plt.StringPlot;
import ch.alpine.bridge.fig.plt.StringPlot.StringItem;
import ch.alpine.bridge.pro.ShowProvider;
import ch.alpine.sophus.clt.ClothoidContext;
import ch.alpine.sophus.clt.ClothoidSolutions;
import ch.alpine.sophus.clt.ClothoidTangentDefect;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Round;

record ClothoidTangentDefectShow(Scalar s1, Scalar s2, Clip clip) implements ShowProvider {
  public static ClothoidTangentDefectShow of(ClothoidContext clothoidContext, Clip clip) {
    return new ClothoidTangentDefectShow(clothoidContext.s1(), clothoidContext.s2(), clip);
  }

  @Override
  public Show getShow() {
    ClothoidTangentDefect clothoidTangentDefect = ClothoidTangentDefect.of(s1, s2);
    ClothoidSolutions clothoidSolutions = new ClothoidSolutions(clothoidTangentDefect, clip);
    Show show = new Show();
    show.add(ReImPlot.of(clothoidTangentDefect, clip));
    Tensor zeros = Tensor.of(clothoidSolutions.lambdas().stream().map(l -> Tensors.of(l, l.maps(Scalar::zero))));
    show.add(ListPlot.of(zeros));
    List<StringItem> list = new LinkedList<>();
    for (Tensor _l : clothoidSolutions.lambdas()) {
      Scalar l = (Scalar) _l;
      list.add(StringItem.of(Tensors.of(l, l.zero()), "" + l.maps(Round._4)));
    }
    show.add(StringPlot.of(list));
    show.setShowLabel("Clothoid Tangent Defect");
    return show;
  }
}
