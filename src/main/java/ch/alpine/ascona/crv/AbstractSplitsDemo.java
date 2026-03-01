// code by jph
package ch.alpine.ascona.crv;

import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Objects;

import ch.alpine.ascona.crv.GeometricSymLinkRender.Link;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.GridRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.sym.SymLink;
import ch.alpine.ascony.sym.SymLinkBuilder;
import ch.alpine.ascony.sym.SymLinkImage;
import ch.alpine.ascony.sym.SymScalar;
import ch.alpine.ascony.sym.SymSequence;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.Tensor;

public abstract class AbstractSplitsDemo extends PointSequenceDemo {
  private static final Font FONT = new Font(Font.DIALOG, Font.PLAIN, 13);

  @ReflectionMarker
  static class SaveParam {
    @FieldFuse
    public transient Boolean save = false;
  }

  protected AbstractSplitsDemo(Object... objects) {
    super(objects);
    setManifoldDisplay(ManifoldDisplays.R2);
    timerFrame.geometricComponent.addRenderInterfaceBackground( //
        new GridRender(timerFrame.geometricComponent.jComponent::getSize));
  }

  @Override // from RenderInterface
  public final synchronized void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    new GridRender(timerFrame.geometricComponent.jComponent::getSize).render(geometricLayer, graphics);
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
