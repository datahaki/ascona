// code by jph
package ch.alpine.bridge.res;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.io.StringScalar;

public class ResourceIndexBuilder {
  public static final String FILENAME = "resource_index.vector";

  public static void of(Path folder) {
    ResourceIndexBuilder resourceIndexBuilder = new ResourceIndexBuilder(folder);
  }

  private final int length;
  private final List<String> list = new LinkedList<>();

  private ResourceIndexBuilder(Path root) {
    length = root.toString().length() + 1;
    check(root);
    Collections.sort(list);
    Tensor tensor = Tensor.of(list.stream().map(StringScalar::of));
    Unprotect.Export(root.resolve(FILENAME), tensor);
  }

  void check(Path folder) {
    try {
      for (final Path file : Files.list(folder).toList()) {
        if (Files.isDirectory(file))
          check(file);
        else {
          if (file.getFileName().toString().equals(FILENAME))
            System.err.println("skip " + file);
          else
            list.add(file.toString().substring(length));
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  static void main() {
    String path = "/home/datahaki/Projects/ascona/src/main/resources/ch/alpine/ascona/gokart/tpq/";
    ResourceIndexBuilder.of(Path.of(path));
  }
}
