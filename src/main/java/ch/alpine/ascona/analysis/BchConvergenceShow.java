// code by jph
package ch.alpine.ascona.analysis;

import ch.alpine.bridge.fig.ListLinePlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.Showable;
import ch.alpine.bridge.pro.ShowProvider;
import ch.alpine.sophus.lie.MatrixAlgebra;
import ch.alpine.sophus.lie.se.SeNGroup;
import ch.alpine.sophus.lie.sl.SlNGroup;
import ch.alpine.sophus.lie.so.SoNGroup;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Accumulate;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.lie.bch.BakerCampbellHausdorff;
import ch.alpine.tensor.mat.ex.MatrixExp;
import ch.alpine.tensor.mat.ex.MatrixLog;
import ch.alpine.tensor.nrm.FrobeniusNorm;
import ch.alpine.tensor.sca.N;
import ch.alpine.tensor.sca.exp.Log10;

class BchConvergenceShow implements ShowProvider {
  private final int depth;
  private final Show show = new Show();

  public BchConvergenceShow(int depth) {
    this.depth = depth;
    show.setPlotLabel("bch convergence");
  }

  void add(String name, MatrixAlgebra matrixAlgebra) {
    Tensor tensor = err(matrixAlgebra);
    Showable showable = show.add(ListLinePlot.of(Range.of(0, tensor.length()), tensor.map(Log10.FUNCTION)));
    showable.setLabel(name);
  }

  private Tensor err(MatrixAlgebra matrixAlgebra) {
    Tensor ad = matrixAlgebra.ad().map(N.DOUBLE);
    BakerCampbellHausdorff bakerCampbellHausdorff = //
        (BakerCampbellHausdorff) BakerCampbellHausdorff.of(ad, depth);
    Tensor x = Tensors.vector(+0.10, +0.12, +0.07);
    Tensor y = Tensors.vector(+0.05, -0.06, +0.11);
    Tensor series = bakerCampbellHausdorff.series(x, y);
    Tensor X = matrixAlgebra.toMatrix(x);
    Tensor Y = matrixAlgebra.toMatrix(y);
    Tensor Z = MatrixLog.of(MatrixExp.of(X).dot(MatrixExp.of(Y)));
    Tensor ref = matrixAlgebra.toVector(Z);
    return Tensor.of(Accumulate.of(series).stream().map(val -> val.subtract(ref)).map(FrobeniusNorm::of));
  }

  @Override
  public Show getShow() {
    add("se2", new SeNGroup(2).matrixAlgebra());
    add("so3", new SoNGroup(3).matrixAlgebra());
    add("sl2", new SlNGroup(2).matrixAlgebra());
    return show;
  }

  static void main() {
    new BchConvergenceShow(9).run();
  }
}
