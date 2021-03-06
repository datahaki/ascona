// code by jph
package ch.alpine.ascona.util.dat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Dimensions;
import ch.alpine.tensor.io.StringScalarQ;

class UzhSe3TxtFormatTest {
  @Test
  void testSimple() throws FileNotFoundException, IOException {
    File file = new File("/media/datahaki/media/resource/uzh/groundtruth", "outdoor_forward_5_davis.txt");
    if (file.isFile()) {
      Tensor tensor = UzhSe3TxtFormat.of(file);
      assertEquals(Dimensions.of(tensor), List.of(22294, 4, 4));
      assertFalse(StringScalarQ.any(tensor));
    }
  }
}
