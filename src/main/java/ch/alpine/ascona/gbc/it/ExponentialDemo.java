// code by jph
package ch.alpine.ascona.gbc.it;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.util.Deque;
import java.util.Optional;

import ch.alpine.ascona.lev.PlaceWrap;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.ren.PointsRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.fig.ListLinePlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophis.crv.d2.alg.ConvexHull2D;
import ch.alpine.sophis.crv.d2.alg.OriginEnclosureQ;
import ch.alpine.sophis.gbc.it.GenesisDeque;
import ch.alpine.sophis.gbc.it.WeightsFactors;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.sophus.lie.se2.Se2Matrix;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.PadRight;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.lie.rot.CirclePoints;
import ch.alpine.tensor.red.Times;

public class ExponentialDemo extends ControlPointsDemo {
  private static final int WIDTH = 300;
  // ---
  private final GenesisDequeProperties genesisDequeProperties;

  public ExponentialDemo() {
    super(new AsconaParam(true, ManifoldDisplays.R2_ONLY), new GenesisDequeProperties());
    // ---
    genesisDequeProperties = (GenesisDequeProperties) objects()[1];
    // ---
    Tensor sequence = Tensor.of(CirclePoints.of(15).multiply(RealScalar.of(2)).stream().skip(5).map(PadRight.zeros(3)));
    sequence.set(Scalar::zero, 0, Tensor.ALL);
    setControlPointsSe2(sequence);
  }

  private static final PointsRender POINTS_RENDER = //
      new PointsRender(new Color(0, 128, 128, 64), new Color(0, 128, 128, 96));

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    PlaceWrap placeWrap = new PlaceWrap(getGeodesicControlPoints());
    Optional<Tensor> optional = placeWrap.getOrigin();
    if (optional.isPresent()) {
      Tensor origin = optional.get();
      HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
      Manifold manifold = homogeneousSpace;
      final Tensor sequence = placeWrap.getSequence();
      final Tensor levers2 = manifold.exponential(origin).log().slash(sequence);
      {
        Tensor hull = ConvexHull2D.of(sequence);
        PathRender pathRender = new PathRender(new Color(0, 0, 255, 128));
        pathRender.setCurve(hull, true);
        pathRender.render(geometricLayer, graphics);
      }
      if (OriginEnclosureQ.INSTANCE.isMember(levers2)) {
        GenesisDeque dequeGenesis = (GenesisDeque) genesisDequeProperties.genesis();
        Deque<WeightsFactors> deque = dequeGenesis.deque(levers2);
        {
          Tensor leversVirtual = Times.of(deque.peekLast().factors(), levers2);
          geometricLayer.pushMatrix(Se2Matrix.translation(origin));
          {
            graphics.setColor(Color.GRAY);
            for (int index = 0; index < levers2.length(); ++index) {
              Line2D line2d = geometricLayer.toLine2D(levers2.get(index), leversVirtual.get(index));
              graphics.draw(line2d);
            }
          }
          {
            LeversRender leversRender = LeversRender.of( //
                manifoldDisplay, leversVirtual, origin.map(Scalar::zero), geometricLayer, graphics);
            leversRender.renderSequence(POINTS_RENDER);
            // Tensor weights = iterativeAffineCoordinate.origin(deque, levers2);
            // leversRender.renderWeights(weights);
          }
          geometricLayer.popMatrix();
          {
            // FIXME BRIDGE this should not affect Show appearance!!!
            graphics.setFont(new Font(Font.DIALOG, Font.BOLD, 20));
            Show show = new Show();
            show.setPlotLabel("Weights");
            Tensor domain = Range.of(0, deque.size());
            for (int index = 0; index < levers2.length(); ++index) {
              int fi = index;
              show.add(ListLinePlot.of(domain, Tensor.of(deque.stream() //
                  .map(WeightsFactors::weights) //
                  .map(tensor -> tensor.Get(fi)))));
            }
            show.render_autoIndent(graphics, new Rectangle(0 * WIDTH, 0, WIDTH, 200));
          }
          {
            Show show = new Show();
            show.setPlotLabel("Factors");
            Tensor domain = Range.of(0, deque.size());
            for (int index = 0; index < levers2.length(); ++index) {
              int fi = index;
              show.add(ListLinePlot.of(domain, Tensor.of(deque.stream() //
                  .map(WeightsFactors::factors) //
                  .map(tensor -> tensor.Get(fi)))));
            }
            show.render_autoIndent(graphics, new Rectangle(1 * WIDTH, 0, WIDTH, 200));
          }
        }
      }
      {
        LeversRender leversRender = LeversRender.of( //
            manifoldDisplay, sequence, origin, geometricLayer, graphics);
        leversRender.renderSequence();
        leversRender.renderOrigin();
      }
    }
  }

  static void main() {
    launch();
  }
}
