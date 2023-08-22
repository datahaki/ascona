// code by jph
package ch.alpine.ubongo;

import java.io.File;
import java.util.List;
import java.util.function.Function;

import ch.alpine.bridge.io.ResourceLocator;
import ch.alpine.tensor.ext.Cache;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.io.Export;
import ch.alpine.tensor.io.Import;

public enum UbongoLoader {
  INSTANCE;

  private final ResourceLocator resourceLocator = new ResourceLocator(HomeDirectory.Documents("ubongo"));
  private final Function<UbongoBoards, List<List<UbongoEntry>>> cache = Cache.of(this::of, 200);

  public List<List<UbongoEntry>> load(UbongoBoards ubongoBoards) {
    return cache.apply(ubongoBoards);
  }

  private List<List<UbongoEntry>> of(UbongoBoards ubongoBoards) {
    File file = resourceLocator.file(ubongoBoards.name());
    if (file.isFile())
      try {
        return Import.object(file);
      } catch (Exception exception) {
        exception.printStackTrace();
      }
    System.out.println("compute");
    List<List<UbongoEntry>> list = ubongoBoards.solve();
    try {
      if (!list.isEmpty())
        Export.object(file, list);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    return list;
  }
}
