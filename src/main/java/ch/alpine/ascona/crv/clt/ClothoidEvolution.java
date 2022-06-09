// code by jph
package ch.alpine.ascona.crv.clt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascona.lev.AbstractPlaceDemo;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.AxesRender;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.win.LookAndFeels;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.sophus.crv.clt.Clothoid;
import ch.alpine.sophus.crv.clt.ClothoidBuilder;
import ch.alpine.sophus.crv.clt.ClothoidComparators;
import ch.alpine.sophus.crv.clt.ClothoidTransition;
import ch.alpine.sophus.crv.clt.PriorityClothoid;
import ch.alpine.sophus.math.noise.SimplexContinuousNoise;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.ext.Timing;

public class ClothoidEvolution extends AbstractPlaceDemo {
  @ReflectionMarker
  public static class Param {
    @FieldSelectionArray({ "0.05", "0.1", "0.2", "0.3", "0.4", "0.5" })
    public Scalar beta = RealScalar.of(0.05);
    public Boolean animate = true;
  }

  private final Timing timing = Timing.started();
  public final Param param = new Param();

  public ClothoidEvolution() {
    super(true, ManifoldDisplays.CLC_ONLY);
    // ---
    ToolbarFieldsEditor.add(param, timerFrame.jToolBar);
    // ---
    timerFrame.geometricComponent.addRenderInterfaceBackground(AxesRender.INSTANCE);
    // ---
    Tensor ctrl = Tensors.fromString( //
        "{{0.017, 0.017, 0.000}, {1.733, 0.967, 4.712}, {3.933, -0.750, -3.665}, {5.567, 1.717, 3.927}, {7.983, 1.500, 4.451}}");
    setControlPointsSe2(ctrl);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor sequence = getGeodesicControlPoints();
    graphics.setColor(Color.BLUE);
    graphics.setStroke(new BasicStroke(2));
    // GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
    // ClothoidBuilder clothoidBuilder = (ClothoidBuilder) geodesicSpace;
    Tensor beg = sequence.get(0);
    ClothoidBuilder clothoidBuilder2 = PriorityClothoid.of(ClothoidComparators.CURVATURE_HEAD);
    double time = param.animate ? timing.seconds() * 0.2 : 0;
    for (int index = 1; index < sequence.length(); ++index) {
      Tensor end = sequence.get(index);
      Clothoid clothoid = clothoidBuilder2.curve(beg, end);
      ClothoidTransition clothoidTransition = ClothoidTransition.of(beg, end, clothoid);
      graphics.draw(geometricLayer.toPath2D(clothoidTransition.linearized(param.beta)));
      double split = 0.5 + 0.1 * SimplexContinuousNoise.FUNCTION.at(time, index);
      beg = clothoidTransition.clothoid().apply(RealScalar.of(split));
    }
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, sequence, null, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexP();
    }
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.updateUI();
    new ClothoidEvolution().setVisible(1000, 600);
  }
}
