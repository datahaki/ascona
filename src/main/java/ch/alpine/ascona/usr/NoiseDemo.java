package ch.alpine.ascona.usr;

import java.awt.Dimension;
import java.io.IOException;

import ch.alpine.bridge.fig.ArrayPlot;
import ch.alpine.bridge.fig.ChartUtils;
import ch.alpine.bridge.fig.JFreeChart;
import ch.alpine.sophus.math.noise.SimplexContinuousNoise;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.ext.HomeDirectory;

public enum NoiseDemo {
  ;
  public static void main(String[] args) throws IOException {
    Tensor x = Subdivide.of(-1, 1, 30);
    Tensor y = Subdivide.of(-1, 1, 30);
    {
      Tensor matrix = Tensors.matrix((i, j) -> //
      SimplexContinuousNoise.FUNCTION.apply(Tensors.of(x.get(i), y.get(j))), x.length(), y.length());
      JFreeChart jFreeChart = ArrayPlot.of(matrix);
      ChartUtils.saveChartAsPNG(HomeDirectory.Pictures(NoiseDemo.class.getSimpleName() + "2.png"), jFreeChart, new Dimension(600, 600));
    }
    {
      Tensor matrix = Tensors.matrix((i, j) -> //
      SimplexContinuousNoise.FUNCTION.apply(Tensors.of(x.get(i), y.get(j), RealScalar.ZERO)), x.length(), y.length());
      JFreeChart jFreeChart = ArrayPlot.of(matrix);
      ChartUtils.saveChartAsPNG(HomeDirectory.Pictures(NoiseDemo.class.getSimpleName() + "3.png"), jFreeChart, new Dimension(600, 600));
    }
  }
}
