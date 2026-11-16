// code by jph
package ch.alpine.ascona.geo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import ch.alpine.bridge.geo.TileServers;
import ch.alpine.bridge.pro.VoidProvider;

class MonoColored implements VoidProvider {
  public TileServers tileServers = TileServers.OpenStreetMap;

  private void recur(Path dir) throws IOException {
    IO.println(dir);
    for (Path path : Files.list(dir).toList()) {
      if (Files.isDirectory(path))
        recur(path);
      else {
        try {
          // Tensor tensor = Import.of(path);
          // List<Integer> dims = Dimensions.of(tensor);
          // if (dims.equals(List.of(256, 256, 4))) {
          // List<Tensor> list = Flatten.stream(tensor, 1).distinct().limit(2).toList();
          // if (list.size() == 1)
          // IO.println(list);
          // } else
          // System.err.println(path);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  @Override
  public Void runStandalone() {
    try {
      recur(tileServers.path());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  static void main() {
    new MonoColored().runStandalone();
  }
}
