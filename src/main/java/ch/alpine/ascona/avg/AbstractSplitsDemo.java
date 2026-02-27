// code by jph
package ch.alpine.ascona.avg;

import java.awt.Font;
import java.awt.Graphics2D;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import ch.alpine.ascona.RandomPoints;
import ch.alpine.ascona.avg.GeometricSymLinkRender.Link;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.sym.SymLink;
import ch.alpine.ascony.sym.SymLinkBuilder;
import ch.alpine.ascony.sym.SymLinkImage;
import ch.alpine.ascony.sym.SymScalar;
import ch.alpine.ascony.sym.SymSequence;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointTypes;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.io.Get;
import ch.alpine.tensor.io.Put;

public abstract class AbstractSplitsDemo extends ControlPointsDemo {
  private static final Font FONT = new Font(Font.DIALOG, Font.PLAIN, 13);

  @ReflectionMarker
  static class SaveParam {
    @FieldFuse
    public transient Boolean save = false;
  }

  protected AbstractSplitsDemo(Object... objects) {
    super(objects);
    fieldsEditor(0).addUniversalListener(this::save);
    addChangeListener(this::loadOrShuffle);
    setManifoldDisplay(ManifoldDisplays.R2);
  }

  @Override
  protected final List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.ALL;
  }

  @Override
  protected final ControlPointType controlPointType() {
    return ControlPointTypes.CURVYCURV;
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
    Tensor tensor;
    try {
      tensor = Get.of(getClass().getResourceAsStream(getSelectedMD().toString().toLowerCase()));
    } catch (Exception exception) {
      System.err.println("cannot load");
      tensor = RandomPoints.on_line(manifoldDisplay, 3);
    }
    setControlPointsSe2(manifoldDisplay.point2xya().slash(tensor));
  }

  private Path getResPath() {
    Path user_dir = Path.of(System.getProperty("user.dir"));
    return user_dir.resolve("src/main/resources/ch/alpine/ascona/avg", getSelectedMD().toString().toLowerCase());
  }

  @Override // from RenderInterface
  public final synchronized void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    timerFrame.geometricComponent.renderGrid(graphics);
    Tensor control = getGeodesicControlPoints();
    // ---
    SymScalar symScalar = symScalar(SymSequence.of(control.length()));
    SymLink symLink = null;
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    if (Objects.nonNull(symScalar)) {
      graphics.drawImage(new SymLinkImage(symScalar, FONT).bufferedImage(), 0, 0, null);
      // ---
      symLink = SymLinkBuilder.of(control, symScalar);
      // ---
      GeometricSymLinkRender geometricSymLinkRender = new GeometricSymLinkRender(manifoldDisplay);
      geometricSymLinkRender.steps = 1;
      Link link = geometricSymLinkRender.new Link(symLink);
      // link.steps=1;
      link.render(geometricLayer, graphics);
    }
    Tensor origin = null;
    if (Objects.nonNull(symLink))
      origin = symLink.position(manifoldDisplay.geodesicSpace());
    LeversRender leversRender = LeversRender.of(manifoldDisplay, control, origin, geometricLayer, graphics);
    leversRender.renderSequence();
    leversRender.renderIndexP();
    leversRender.renderOrigin();
    leversRender.renderIndexX();
  }

  /** evaluates geodesic average on symbolic leaf sequence
   * 
   * @param vector of length at least 1
   * @return null if computation of geodesic average is not defined for given vector */
  abstract SymScalar symScalar(Tensor vector);
}
