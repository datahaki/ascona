// code by jph
package ch.alpine.ascona.dv;

import java.awt.Graphics2D;
import java.util.List;
import java.util.Optional;

import ch.alpine.ascona.ref.ShuffleFuse;
import ch.alpine.ascony.dat.PlaceWrap;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.LeversHud;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.pdf.RandomSample;

final class GrassmannDemo extends ControlPointsDemo {
  @ReflectionMarker
  static class Param0 {
    @FieldSelectionArray({ "4", "6", "8", "10" })
    public Integer size = 6;
    public final ShuffleFuse shuffleFuse = new ShuffleFuse();
  }

  @ReflectionMarker
  static class Param1 {
    public Biinvariants biinvariants = Biinvariants.USANCE;
    public ColorDataGradients cdg = ColorDataGradients.TEMPERATURE;
  }

  private final Param0 param0;
  private final Param1 param1;

  public GrassmannDemo() {
    super(param0 = new Param0(), param1 = new Param1());
    fieldsEditor(param0).addUniversalListener(this::shuffle);
    addChangeListener(this::shuffle);
    setManifoldDisplay(ManifoldDisplays.R2);
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.manifolds();
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.ADDREMOVE;
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
