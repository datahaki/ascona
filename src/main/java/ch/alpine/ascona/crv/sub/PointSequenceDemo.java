// code by jph
package ch.alpine.ascona.crv.sub;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import ch.alpine.ascona.RandomPoints;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.io.Get;
import ch.alpine.tensor.io.Put;

abstract class PointSequenceDemo extends ControlPointsDemo {
  @ReflectionMarker
  static class SaveParam {
    @FieldFuse
    public transient Boolean save = false;
  }

  protected PointSequenceDemo(Object... objects) {
    super(objects);
    for (int index = 0; index < objects.length; ++index)
      if (objects[index] instanceof SaveParam)
        fieldsEditor(index).addUniversalListener(this::save);
    addChangeListener(this::loadOrShuffle);
    setManifoldDisplay(ManifoldDisplays.R2);
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.ALL;
  }

  @Override
  protected final ControlPointType controlPointType() {
    return ControlPointType.CURVYCURV;
  }

  private void save() {
    Path path = getResPath();
    if (Objects.nonNull(path))
      try {
        IO.println(path);
        Put.of(path, getGeodesicControlPoints());
      } catch (IOException e) {
        e.printStackTrace();
      }
  }

  private void loadOrShuffle() {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor tensor = RandomPoints.on_line(manifoldDisplay, initialCount());
    String name = getSelectedMD().toString().toLowerCase();
    try (InputStream inputStream = getClass().getResourceAsStream(name)) {
      if (Objects.nonNull(inputStream))
        tensor = Get.of(inputStream);
    } catch (Exception exception) {
      System.err.println("cannot load: " + name);
    }
    setGeodesicControlPoints(tensor);
  }

  protected abstract int initialCount();

  private Path getResPath() {
    Path user_dir = Path.of(System.getProperty("user.dir"));
    return user_dir.resolve("src/main/resources/ch/alpine/ascona/crv", getSelectedMD().toString().toLowerCase());
  }
}
