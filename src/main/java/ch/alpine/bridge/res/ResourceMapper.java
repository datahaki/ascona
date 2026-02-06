package ch.alpine.bridge.res;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.ext.ResourceData;
import ch.alpine.tensor.io.Import;

public class ResourceMapper {
  public static final ResourceMapper of(String index) {
    return new ResourceMapper(index);
  }

  // ---
  private Path parent;
  private List<String> lines;

  private ResourceMapper(String index) {
    parent = Path.of(index).getParent();
    lines = Collections.unmodifiableList(ResourceData.lines(index));
  }

  public final List<String> list() {
    return lines;
  }

  public final Tensor importResource(String key) {
    return Import.of(parent.resolve(key).toString()); // toString() is required
  }
}
