// code by jph
package ch.alpine.ascona.dv;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ch.alpine.ascony.api.LogWeightings;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointTypes;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.ascony.win.PlaceWrap;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.Biinvariant;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophus.api.Manifold;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.ext.ArgMin;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.pdf.RandomSample;

public class WeightsDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param0 {
    @FieldSelectionArray({ "3", "5", "10" })
    public Integer size = 5;
    @FieldFuse
    public transient Boolean shuffle;
  }

  @ReflectionMarker
  public static class Param1 {
    public LogWeightings logWeightings = LogWeightings.DISTANCES;
    public ColorDataLists colorDataLists = ColorDataLists._097;
  }

  private final Param0 param0;
  private final Param1 param1;

  public WeightsDemo() {
    super(param0 = new Param0(), param1 = new Param1());
    // ---
    fieldsEditor(0).addUniversalListener(this::shuffle);
    addChangeListener(this::shuffle);
    setManifoldDisplay(ManifoldDisplays.Se2);
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.manifolds();
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointTypes.ADDREMOVE;
  }

  private void shuffle() {
    param0.shuffle = false;
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
    if (optional.isPresent()) {
      Tensor sequence = placeWrap.getSequence();
      Tensor origin = optional.get();
      LeversRender leversRender = //
          LeversRender.of(manifoldDisplay, sequence, origin, geometricLayer, graphics);
      // ---
      leversRender.renderSequence();
      leversRender.renderOrigin();
      leversRender.renderLevers();
      leversRender.renderIndexX();
      leversRender.renderIndexP();
      // ---
      if (manifoldDisplay.dimensions() < sequence.length()) {
        Manifold manifold = manifoldDisplay.manifold();
        Map<Biinvariants, Biinvariant> map2 = Biinvariants.all(manifold);
        Tensor matrix = Tensors.empty();
        int[] minIndex = new int[map2.size()];
        ColorDataIndexed colorDataIndexed = param1.colorDataLists.strict();
        {
          graphics.setFont(LeversRender.FONT_MATRIX);
          FontMetrics fontMetrics = graphics.getFontMetrics();
          int fheight = fontMetrics.getHeight();
          int index = 0;
          for (Biinvariant biinvariant : map2.values())
            try {
              Sedarim sedarim = param1.logWeightings.sedarim(biinvariant, s -> s, sequence);
              Tensor weights = sedarim.sunder(origin);
              minIndex[index] = ArgMin.of(weights);
              matrix.append(weights);
              graphics.setColor(colorDataIndexed.getColor(index));
              graphics.drawString(biinvariant.toString(), 2, (index + 1) * fheight);
              ++index;
            } catch (Exception exception) {
              // ---
            }
        }
        for (int index = 0; index < sequence.length(); ++index) {
          Tensor map = matrix.get(Tensor.ALL, index).maps(Tensors::of);
          leversRender.renderMatrix(sequence.get(index), map, colorDataIndexed);
        }
      }
    }
  }

  static void main() {
    new WeightsDemo().runStandalone();
  }
}
