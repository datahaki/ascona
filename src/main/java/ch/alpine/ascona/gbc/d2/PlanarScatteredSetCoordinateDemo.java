// code by jph
package ch.alpine.ascona.gbc.d2;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;

import ch.alpine.ascona.gbc.GenesisDequeParam;
import ch.alpine.ascony.api.LogWeightings;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.msh.ArrayFunction;
import ch.alpine.ascony.msh.ImageTiling;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.bridge.fig.ArrayPlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.Showable;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.util.FieldsEditor;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.sophis.crv.d2.ex.Box2D;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.ConstantArray;
import ch.alpine.tensor.alg.PadRight;
import ch.alpine.tensor.lie.rot.CirclePoints;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;

/** transfer weights from barycentric coordinates defined by set of control points
 * in the square domain (subset of R^2) to means in non-linear spaces */
// FIXME ASCONA SPIN
final class PlanarScatteredSetCoordinateDemo extends AbstractScatteredSetWeightingDemo {
  private final GenesisDequeParam dequeGenesisProperties = new GenesisDequeParam();

  // FIXME ASCONA the class structure is not correct, since log weighting is empty and not visible
  public PlanarScatteredSetCoordinateDemo() {
    super(List.of(LogWeightings.WEIGHTING));
    fieldsEditor(0).addUniversalListener(this::recompute);
    {
      FieldsEditor fieldsEditor = ToolbarFieldsEditor.addToComponent(dequeGenesisProperties, jToolBar());
      fieldsEditor.addUniversalListener(this::recompute);
    }
    addChangeListener(this::recompute);
    {
      MouseAdapter mouseAdapter = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent mouseEvent) {
          switch (mouseEvent.getButton()) {
          case MouseEvent.BUTTON1: // insert point
            if (!isPositioningOngoing())
              recompute();
            break;
          default:
          }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
          if (isPositioningOngoing())
            recompute();
        }
      };
      geometricComponent().addMouseListener(mouseAdapter);
      geometricComponent().addMouseMotionListener(mouseAdapter);
    } // ---
    setControlPointsSe2(PadRight.zeros(3).slash(CirclePoints.of(7)));
    recompute();
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.d2Rasters();
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    graphics.setColor(Color.LIGHT_GRAY);
    graphics.draw(geometricLayer.toPath2D(Box2D.ABSOLUTE_ONE, true));
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
      Dimension dimension = getSize();
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
      ArrayFunction<Tensor> arrayFunction = new ArrayFunction<>(weightingsParam.operator(manifoldDisplay.manifold(), sequence)::sunder, fallback);
      CoordinateBoundingBox cbb = manifoldDisplay.d2Raster_coordinateBoundingBox();
      Tensor wgs = manifoldDisplay.d2Raster().of(arrayFunction, cbb, scatteredSetParam.refine);
      showable = ArrayPlot.of(ImageTiling.of(wgs), scatteredSetParam.spinnerColorData);
    } else
      showable = null;
  }

  static void main() {
    new PlanarScatteredSetCoordinateDemo().runStandalone();
  }
}
