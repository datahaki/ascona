// code by jph
package ch.alpine.ascona.dv;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.util.List;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.ColorPair;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.plt.MatrixPlot;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSelectionCallback;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.ts.Transition;
import ch.alpine.sophis.ts.TransitionSpace;
import ch.alpine.sophus.api.Manifold;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.opt.hun.BipartiteMatching;
import ch.alpine.tensor.pdf.RandomSample;
import ch.alpine.tensor.pdf.RandomSampleInterface;

class BipartiteMatchingDemo extends ControlPointsDemo {
  @ReflectionMarker
  static class Param {
    @FieldSelectionCallback("biinvariants")
    public Biinvariants biinvariants = Biinvariants.METRIC;

    @ReflectionMarker
    public static List<Biinvariants> biinvariants() {
      return Biinvariants.OKAY;
    }

    @FieldSelectionArray({ "5", "10", "20", "50" })
    public Integer n = 5;
    @FieldSelectionArray({ "0", "5", "10", "20", "50" })
    public Integer excess = 5;
    @FieldFuse
    public transient Boolean shuffle = true;
  }

  private final Param param;
  private Tensor ground;

  public BipartiteMatchingDemo() {
    super(param = new Param());
    fieldsEditor(param).addUniversalListener(this::shuffle);
    addChangeListener(this::shuffle);
    setManifoldDisplay(ManifoldDisplays.R2);
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.manifolds();
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.SCATTERED;
  }

  private synchronized void shuffle() {
    RandomSampleInterface rsi = manifoldDisplay().randomSampleInterface();
    ground = RandomSample.of(rsi, param.n);
    setGeodesicControlPoints(RandomSample.of(rsi, param.n + param.excess));
  }

  @Override // from RenderInterface
  public synchronized void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor control = getGeodesicControlPoints();
    if (0 < control.length()) {
      Manifold manifold = manifoldDisplay.manifold();
      Tensor matrix = param.biinvariants.ofSafe(manifold).relative_distances(ground).sunder().slash(control);
      BipartiteMatching bipartiteMatching = BipartiteMatching.of(matrix);
      int[] matching = bipartiteMatching.matching();
      graphics.setColor(Color.RED);
      TransitionSpace transitionSpace = manifoldDisplay.transitionSpace();
      for (int index = 0; index < matching.length; ++index)
        if (matching[index] != BipartiteMatching.UNASSIGNED) {
          Tensor head = control.get(index);
          Tensor tail = ground.get(matching[index]);
          Transition transition = transitionSpace.connect(head, tail);
          TensorUnaryOperator tuo = manifoldDisplay::point2xy;
          Path2D path2d = geometricLayer.toPath2D(tuo.slash(transition.linearized(RealScalar.of(0.1))));
          graphics.draw(path2d);
        }
      Dimension dimension = getSize();
      dimension.width /= 2;
      dimension.height /= 2;
      {
        Show show = new Show();
        show.setPlotLabel("Distance Matrix");
        show.add(MatrixPlot.of(matrix));
        show.render_autoIndent(graphics, new Rectangle(dimension.width, 0, dimension.width, dimension.height));
      }
    }
    manifoldDisplay.showPoints(ColorPair.REFERENCE, RealScalar.ONE, ground).render(geometricLayer, graphics);
  }

  static void main() {
    new BipartiteMatchingDemo().runStandalone();
  }
}
