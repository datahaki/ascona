// code by jph
package ch.alpine.ascona.gbc.it;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;
import java.util.Objects;

import ch.alpine.ascona.gbc.d2.AbstractScatteredSetWeightingDemo;
import ch.alpine.ascony.api.Box2D;
import ch.alpine.ascony.api.ImageTiling;
import ch.alpine.ascony.api.InsideConvexHullLogWeighting;
import ch.alpine.ascony.api.LogWeighting;
import ch.alpine.ascony.api.LogWeightings;
import ch.alpine.ascony.arp.ArrayFunction;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.bridge.fig.ArrayPlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.Showable;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.util.FieldsEditor;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.ConstantArray;
import ch.alpine.tensor.alg.PadRight;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.lie.rot.CirclePoints;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;

/** transfer weights from barycentric coordinates defined by set of control points
 * in the square domain (subset of R^2) to means in non-linear spaces */
// FIXME ASCONA SPIN
public class PlanarScatteredSetCoordinateDemo extends AbstractScatteredSetWeightingDemo {
  private final GenesisDequeProperties dequeGenesisProperties = new GenesisDequeProperties();

  // FIXME ASCONA the class structure is not correct, since log weighting is empty and not visible
  public PlanarScatteredSetCoordinateDemo() {
    super(List.of(LogWeightings.WEIGHTING));
    spinnerLogWeighting.setVisible(false);
    FieldsEditor fieldsEditor = ToolbarFieldsEditor.addToComponent(dequeGenesisProperties, timerFrame.jToolBar);
    fieldsEditor.addUniversalListener(this::recompute);
    // ---
    TensorUnaryOperator tuo = PadRight.zeros(3);
    setControlPointsSe2(Tensor.of(CirclePoints.of(7).multiply(RealScalar.of(0.6)).stream().map(tuo)));
    fieldsEditor(0).addUniversalListener(this::recompute);
    recompute();
    addMouseRecomputation();
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.d2Rasters();
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    graphics.setColor(Color.LIGHT_GRAY);
    graphics.draw(geometricLayer.toPath2D(Box2D.CORNERS, true));
    {
      LeversRender leversRender = //
          LeversRender.of(manifoldDisplay(), getGeodesicControlPoints(), null, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexX();
      leversRender.renderIndexP();
    }
    // ---
    if (Objects.isNull(showable))
      recompute();
    if (Objects.nonNull(showable)) {
      Dimension dimension = timerFrame.geometricComponent.jComponent.getSize();
      Show show = new Show();
      show.add(showable);
      show.render(graphics, new Rectangle(100, 10, dimension.width - 200, 400));
    }
  }

  private Showable showable;

  @Override
  protected final void recompute() {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor sequence = getGeodesicControlPoints();
    if (manifoldDisplay.dimensions() < sequence.length()) {
      Tensor fallback = ConstantArray.of(DoubleScalar.INDETERMINATE, sequence.length());
      ArrayFunction<Tensor> arrayFunction = new ArrayFunction<>(operator(sequence)::sunder, fallback);
      CoordinateBoundingBox cbb = manifoldDisplay.d2Raster_coordinateBoundingBox();
      Tensor wgs = manifoldDisplay.d2Raster().of(arrayFunction, cbb, scatteredSetParam.refine);
      showable = ArrayPlot.of(ImageTiling.of(wgs), scatteredSetParam.spinnerColorData);
    } else
      showable = null;
  }

  @Override
  protected LogWeighting logWeighting() {
    return new InsideConvexHullLogWeighting(dequeGenesisProperties.genesis());
  }
  // @Override
  // public void spun(ManifoldDisplays manifoldDisplay) {
  // if (manifoldDisplay.equals(ManifoldDisplays.R2)) {
  // setControlPointsSe2(Tensors.fromString( //
  // "{{-1.017, -0.953, 0.000}, {-0.991, 0.113, 0.000}, {-0.644, 0.967, 0.000}, {0.509, 0.840, 0.000}, {0.689, 0.513, 0.000}, {0.956, -0.627, 0.000}}"));
  // } else
  // if (manifoldDisplay.equals(ManifoldDisplays.S2)) {
  // setControlPointsSe2(Tensors.fromString( //
  // "{{0.300, 0.092, 0.000}, {-0.563, -0.658, 0.262}, {-0.854, -0.200, 0.000}, {-0.746, 0.663, -0.262}, {0.467, 0.758, 0.262}, {0.446, -0.554, 0.262}}"));
  // setControlPointsSe2(Tensors.fromString( //
  // "{{-0.521, 0.621, 0.262}, {-0.863, 0.258, 0.000}, {-0.725, 0.588, -0.785}, {0.392, 0.646, 0.000}, {-0.375, 0.021, 0.000}, {-0.525, -0.392, 0.000}}"));
  // setControlPointsSe2(Tensors.fromString( //
  // "{{-0.583, 0.338, 0.000}, {-0.904, -0.258, 0.262}, {-0.513, 0.804, 0.000}, {0.646, 0.667, 0.000}, {0.704, -0.100, 0.000}, {0.396, -0.688, 0.000}}"));
  // setControlPointsSe2(Tensors.fromString( //
  // "{{-0.363, 0.388, 0.000}, {-0.825, -0.271, 0.000}, {-0.513, 0.804, 0.000}, {0.646, 0.667, 0.000}, {0.704, -0.100, 0.000}, {-0.075, -0.733, 0.000}}"));
  // } else //
  // if (manifoldDisplay.equals(ManifoldDisplays.H2)) {
  // setControlPointsSe2(Tensors.fromString( //
  // "{{-1.900, 1.783, 0.000}, {-0.083, 2.517, 0.000}, {0.500, 1.400, 0.000}, {2.300, 2.117, 0.000}, {2.833, 0.217, 0.000}, {1.000, -1.550, 0.000}, {-0.283,
  // -0.667, 0.000}, {-1.450, -1.650, 0.000}}"));
  // }
  // }

  static void main() {
    new PlanarScatteredSetCoordinateDemo().runStandalone();
  }
}
