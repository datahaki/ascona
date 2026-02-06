package ch.alpine.bridge.res;

import java.io.File;
import java.util.Collections;
import java.util.List;

import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.ext.ResourceData;

public abstract class ResourceMapper implements TensorUnaryOperator {
  private String parent;
  private String name;
  private List<String> lines;

  public ResourceMapper(String index) {
    parent = new File(index).getParent();
    name = new File(index).getName();
    lines = Collections.unmodifiableList(ResourceData.lines(index));
  }

  public final List<String> list() {
    return lines;
  }

  public final Tensor getData(String line) {
    return apply(Unprotect.Import(new File(parent, line)));
  }
}
