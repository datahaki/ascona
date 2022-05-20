// code by jph
package ch.alpine.ascona.gbc.d2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Deque;
import java.util.Optional;

import org.jfree.chart.JFreeChart;

import ch.alpine.ascona.lev.AbstractPlaceDemo;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.ascona.util.ren.PointsRender;
import ch.alpine.bridge.fig.ListPlot;
import ch.alpine.bridge.fig.VisualSet;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.bridge.ref.util.PanelFieldsEditor;
import ch.alpine.sophus.crv.d2.OriginEnclosureQ;
import ch.alpine.sophus.gbc.it.Evaluation;
import ch.alpine.sophus.gbc.it.GenesisDeque;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.hs.HsDesign;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.PadRight;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.lie.r2.CirclePoints;
import ch.alpine.tensor.lie.r2.ConvexHull;
import ch.alpine.tensor.red.Times;

public class ExponentialDemo extends AbstractPlaceDemo {
  private static final int WIDTH = 300;
  // ---
  private final GenesisDequeProperties genesisDequeProperties = new GenesisDequeProperties();

  public ExponentialDemo() {
    super(true, ManifoldDisplays.R2_ONLY);
    // ---
    Container container = timerFrame.jFrame.getContentPane();
    PanelFieldsEditor fieldsPanel = new PanelFieldsEditor(genesisDequeProperties);
    container.add(BorderLayout.WEST, fieldsPanel.createJScrollPane());
    Tensor sequence = Tensor.of(CirclePoints.of(15).multiply(RealScalar.of(2)).stream().skip(5).map(PadRight.zeros(3)));
    sequence.set(Scalar::zero, 0, Tensor.ALL);
    setControlPointsSe2(sequence);
  }

  private static final PointsRender POINTS_RENDER = //
      new PointsRender(new Color(0, 128, 128, 64), new Color(0, 128, 128, 96));

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Optional<Tensor> optional = getOrigin();
    if (optional.isPresent()) {
      Tensor origin = optional.get();
      HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
      Manifold vectorLogManifold = homogeneousSpace;
      final Tensor sequence = getSequence();
      HsDesign hsDesign = new HsDesign(vectorLogManifold);
      final Tensor levers2 = hsDesign.matrix(sequence, origin);
      {
        Tensor hull = ConvexHull.of(sequence);
        PathRender pathRender = new PathRender(new Color(0, 0, 255, 128));
        pathRender.setCurve(hull, true);
        pathRender.render(geometricLayer, graphics);
      }
      if (OriginEnclosureQ.INSTANCE.test(levers2)) {
        GenesisDeque dequeGenesis = (GenesisDeque) genesisDequeProperties.genesis();
        Deque<Evaluation> deque = dequeGenesis.deque(levers2);
        {
          Tensor leversVirtual = Times.of(deque.peekLast().factors(), levers2);
          geometricLayer.pushMatrix(GfxMatrix.translation(origin));
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
            VisualSet visualSet = new VisualSet();
            visualSet.setPlotLabel("Weights");
            Tensor domain = Range.of(0, deque.size());
            for (int index = 0; index < levers2.length(); ++index) {
              int fi = index;
              visualSet.add(domain, Tensor.of(deque.stream() //
                  .map(Evaluation::weights) //
                  .map(tensor -> tensor.Get(fi))));
            }
            JFreeChart jFreeChart = ListPlot.of(visualSet);
            jFreeChart.draw(graphics, new Rectangle2D.Double(0 * WIDTH, 0, WIDTH, 200));
          }
          {
            VisualSet visualSet = new VisualSet();
            visualSet.setPlotLabel("Factors");
            Tensor domain = Range.of(0, deque.size());
            for (int index = 0; index < levers2.length(); ++index) {
              int fi = index;
              visualSet.add(domain, Tensor.of(deque.stream() //
                  .map(Evaluation::factors) //
                  .map(tensor -> tensor.Get(fi))));
            }
            JFreeChart jFreeChart = ListPlot.of(visualSet);
            jFreeChart.draw(graphics, new Rectangle2D.Double(1 * WIDTH, 0, WIDTH, 200));
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

  public static void main(String[] args) {
    new ExponentialDemo().setVisible(1300, 900);
  }
}
