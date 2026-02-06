package ch.alpine.bridge.res;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.ext.ResourceData;
import ch.alpine.tensor.io.Import;

public abstract class ResourceMapper implements TensorUnaryOperator {
  private String parent;
  private List<String> lines;

  public ResourceMapper(String index) {
    parent = new File(index).getParent();
    lines = Collections.unmodifiableList(ResourceData.lines(index));
  }

  public final List<String> list() {
    return lines;
  }

  public final Tensor getData(String line) {
    return apply(Import.of(Path.of(parent, line).toString())); // toString() is required
  }
}
