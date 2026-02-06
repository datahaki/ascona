package ch.alpine.bridge.res;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.io.StringScalar;

public class ResourceIndexBuilder {
  public static final String FILENAME = "resource_index.vector";

  public static void of(File folder) {
    ResourceIndexBuilder resourceIndexBuilder = new ResourceIndexBuilder(folder);
  }

  private final int length;
  private final List<String> list = new LinkedList<>();

  private ResourceIndexBuilder(File root) {
    length = root.toString().length() + 1;
    check(root);
    Collections.sort(list);
    Tensor tensor = Tensor.of(list.stream().map(StringScalar::of));
    Unprotect.Export(new File(root, FILENAME), tensor);
  }

  void check(File folder) {
    for (final File file : folder.listFiles()) {
      if (file.isDirectory())
        check(file);
      else {
        if (file.getName().equals(FILENAME))
          System.err.println("skip " + file);
        else
          list.add(file.toString().substring(length));
      }
    }
  }

  static void main() {
    String path = "/home/datahaki/Projects/ascona/src/main/resources/ch/alpine/ascona/gokart/tpq/";
    File folder = new File(path);
    ResourceIndexBuilder.of(folder);
  }
}
