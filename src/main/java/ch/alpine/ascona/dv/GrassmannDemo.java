// code by jph
package ch.alpine.ascona.dv;

import java.awt.Graphics2D;
import java.util.Optional;

import ch.alpine.ascona.lev.LeversHud;
import ch.alpine.ascona.lev.PlaceWrap;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.pdf.RandomSample;

// FIXME
public final class GrassmannDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param0 extends AsconaParam {
    public Param0() {
      super(true, ManifoldDisplays.manifolds());
      manifoldDisplays = ManifoldDisplays.S2;
    }

    @FieldSelectionArray({ "4", "6", "8", "10" })
    public Integer size = 6;
    @FieldFuse
    public transient Boolean shuffle;
  }

  @ReflectionMarker
  public static class Param1 {
    public Biinvariants biinvariants = Biinvariants.LEVERAGES;
    public ColorDataGradients cdg = ColorDataGradients.TEMPERATURE;
  }

  private final Param0 param0;
  private final Param1 param1;

  public GrassmannDemo() {
    this(new Param0(), new Param1());
  }

  public GrassmannDemo(Param0 param0, Param1 param1) {
    super(param0, param1);
    this.param0 = param0;
    this.param1 = param1;
    controlPointsRender.setMidpointIndicated(false);
    fieldsEditor(0).addUniversalListener(this::shuffle);
    shuffle();
  }

  private void shuffle() {
    int n = param0.size;
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor tensor = Tensor.of(RandomSample.of(manifoldDisplay.randomSampleInterface(), n).stream() //
        .map(manifoldDisplay::point2xya));
    setControlPointsSe2(tensor);
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    PlaceWrap placeWrap = new PlaceWrap(getGeodesicControlPoints());
    Optional<Tensor> optional = placeWrap.getOrigin();
    Tensor sequence = placeWrap.getSequence();
    if (optional.isPresent()) {
      Tensor origin = optional.get();
      LeversRender leversRender = //
          LeversRender.of(manifoldDisplay, sequence, origin, geometricLayer, graphics);
      ColorDataGradient colorDataGradient = param1.cdg.deriveWithOpacity(RealScalar.of(0.5));
      LeversHud.render(param1.biinvariants, leversRender, colorDataGradient);
    }
  }

  static void main() {
    new GrassmannDemo().runStandalone();
  }
}
