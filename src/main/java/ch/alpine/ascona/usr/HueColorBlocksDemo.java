// code by jph
package ch.alpine.ascona.usr;

import ch.alpine.bridge.fig.ImagePlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.ShowDialog;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.io.ImageFormat;

public enum HueColorBlocksDemo {
  ;
  static void main() {
    ColorDataIndexed colorDataIndexed = HueColorBlocks.of(10, 5);
    Tensor tensor = Tensors.of(Range.of(0, colorDataIndexed.length())).map(colorDataIndexed);
    Show show = new Show();
    show.add(ImagePlot.of(ImageFormat.of(tensor)));
    ShowDialog.of(show);
  }
}
