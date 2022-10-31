// code by jph
package ch.alpine.ascona.gbc.it;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Objects;

import ch.alpine.ascona.gbc.d2.AbstractScatteredSetWeightingDemo;
import ch.alpine.ascona.util.api.Box2D;
import ch.alpine.ascona.util.api.ImageTiling;
import ch.alpine.ascona.util.api.InsideConvexHullLogWeighting;
import ch.alpine.ascona.util.api.LogWeighting;
import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.ascona.util.arp.ArrayFunction;
import ch.alpine.ascona.util.arp.ArrayPlotImage;
import ch.alpine.ascona.util.arp.D2Raster;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.util.PanelFieldsEditor;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.ConstantArray;
import ch.alpine.tensor.alg.PadRight;
import ch.alpine.tensor.alg.Rescale;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.lie.r2.CirclePoints;

/** transfer weights from barycentric coordinates defined by set of control points
 * in the square domain (subset of R^2) to means in non-linear spaces */
// FIXME ASCONA SPIN
public class PlanarScatteredSetCoordinateDemo extends AbstractScatteredSetWeightingDemo {
  private final GenesisDequeProperties dequeGenesisProperties = new GenesisDequeProperties();

  // FIXME ASCONA the class structure is not correct, since log weighting is empty and not visible
  public PlanarScatteredSetCoordinateDemo() {
    super(ManifoldDisplays.d2Rasters(), List.of(LogWeightings.WEIGHTING));
    spinnerLogWeighting.setVisible(false);
    Container container = timerFrame.jFrame.getContentPane();
    PanelFieldsEditor fieldsPanel = new PanelFieldsEditor(dequeGenesisProperties);
    fieldsPanel.addUniversalListener(this::recompute);
    container.add(fieldsPanel.createJScrollPane(), BorderLayout.WEST);
    // ---
    TensorUnaryOperator tuo = PadRight.zeros(3);
    setControlPointsSe2(Tensor.of(CirclePoints.of(7).multiply(RealScalar.of(0.6)).stream().map(tuo)));
    // spun(ManifoldDisplays.R2);
    // addManifoldListener(this);
    addManifoldListener(l -> recompute());
    recompute();
    addMouseRecomputation();
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    graphics.setColor(Color.LIGHT_GRAY);
    graphics.draw(geometricLayer.toPath2D(Box2D.CORNERS, true));
    RenderQuality.setQuality(graphics);
    {
      LeversRender leversRender = //
          LeversRender.of(manifoldDisplay(), getGeodesicControlPoints(), null, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexX();
      leversRender.renderIndexP();
    }
    // ---
    if (Objects.isNull(arrayPlotImage))
      recompute();
    if (Objects.nonNull(arrayPlotImage)) {
      Dimension dimension = arrayPlotImage.getDimension();
      dimension.width *= 3;
      dimension.height *= 3;
      arrayPlotImage.draw(graphics, dimension);
    }
  }

  private ArrayPlotImage arrayPlotImage;

  @Override
  protected final void recompute() {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor sequence = getGeodesicControlPoints();
    arrayPlotImage = manifoldDisplay.dimensions() < sequence.length() //
        ? arrayPlotImage(manifoldDisplay, scatteredSetParam.spinnerColorData, sequence, scatteredSetParam.refine, operator(sequence)::sunder)
        : null;
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

  protected static ArrayPlotImage arrayPlotImage( //
      ManifoldDisplay manifoldDisplay, //
      ScalarTensorFunction colorDataGradient, //
      Tensor sequence, int refinement, TensorUnaryOperator tensorUnaryOperator) {
    D2Raster d2Raster = (D2Raster) manifoldDisplay;
    Tensor fallback = ConstantArray.of(DoubleScalar.INDETERMINATE, sequence.length());
    ArrayFunction<Tensor> arrayFunction = new ArrayFunction<>(tensorUnaryOperator, fallback);
    Tensor wgs = D2Raster.of(d2Raster, refinement, arrayFunction);
    Rescale rescale = new Rescale(ImageTiling.of(wgs));
    // logWeighting().equals(LogWeightings.DISTANCES)
    return ArrayPlotImage.of(rescale.result(), rescale.clip(), colorDataGradient);
  }

  public static void main(String[] args) {
    launch();
  }
}
