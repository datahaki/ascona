package ch.alpine.ascona.analysis;

import java.io.IOException;

import ch.alpine.ascona.util.ren.HueColorData;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ImageResize;
import ch.alpine.tensor.io.Export;

public enum HueColorDataShow {
  ;
  public static void main(String[] args) throws IOException {
    ColorDataIndexed colorDataIndexed = HueColorData.of(10, 5);
    Tensor tensor = Range.of(0, colorDataIndexed.length()).map(Tensors::of).map(colorDataIndexed);
    tensor = ImageResize.nearest(tensor, 10);
    Export.of(HomeDirectory.Pictures("huecolordata.png"), tensor);
  }
}
