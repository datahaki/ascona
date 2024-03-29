// code by jph
package ch.alpine.ascona.avg;

import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Objects;

import ch.alpine.ascona.avg.GeometricSymLinkRender.Link;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.sym.SymLink;
import ch.alpine.ascona.util.sym.SymLinkBuilder;
import ch.alpine.ascona.util.sym.SymLinkImage;
import ch.alpine.ascona.util.sym.SymScalar;
import ch.alpine.ascona.util.sym.SymSequence;
import ch.alpine.ascona.util.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.tensor.Tensor;

public abstract class AbstractSplitsDemo extends ControlPointsDemo {
  private static final Font FONT = new Font(Font.DIALOG, Font.PLAIN, 13);

  protected AbstractSplitsDemo(AsconaParam asconaParam) {
    super(asconaParam);
  }

  @Override // from RenderInterface
  public final synchronized void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
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
      origin = symLink.getPosition(manifoldDisplay.geodesicSpace());
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
