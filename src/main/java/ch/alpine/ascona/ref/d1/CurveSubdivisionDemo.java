// code by jph
package ch.alpine.ascona.ref.d1;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;

import ch.alpine.ascona.curve.AbstractCurvatureDemo;
import ch.alpine.ascona.lev.LeversRender;
import ch.alpine.ascona.util.api.Curvature2DRender;
import ch.alpine.ascona.util.api.DubinsGenerator;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.dis.R2Display;
import ch.alpine.ascona.util.dis.Se2Display;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.ascona.util.win.LookAndFeels;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.bridge.swing.SpinnerLabel;
import ch.alpine.bridge.swing.StandardMenu;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.ref.d1.BSpline1CurveSubdivision;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.io.ResourceData;
import ch.alpine.tensor.red.Mean;
import ch.alpine.tensor.red.Nest;
import ch.alpine.tensor.red.Times;

/** split interface and biinvariant mean based curve subdivision */
@ReflectionMarker
public class CurveSubdivisionDemo extends AbstractCurvatureDemo {
  private final PathRender pathRender = new PathRender(new Color(0, 255, 0, 128));
  final SpinnerLabel<CurveSubdivisionSchemes> spinnerLabel = new SpinnerLabel<>();
  final SpinnerLabel<Integer> spinnerRefine = new SpinnerLabel<>();
  final SpinnerLabel<Scalar> spinnerMagicC = new SpinnerLabel<>();
  final JToggleButton jToggleLine = new JToggleButton("line");
  final JToggleButton jToggleCyclic = new JToggleButton("cyclic");
  final JToggleButton jToggleSymi = new JToggleButton("graph");
  final JToggleButton jToggleComb = new JToggleButton("comb");
  private final JToggleButton jToggleHelp = new JToggleButton("help");
  private static final Tensor OMEGA = Tensors.fromString("{-1/16, -1/36, 0, 1/72, 1/42, 1/36, 1/18, 1/16}");
  private final SpinnerLabel<Scalar> spinnerBeta = new SpinnerLabel<>();

  public CurveSubdivisionDemo() {
    super(ManifoldDisplays.ALL);
    ToolbarFieldsEditor.add(this, timerFrame.jToolBar);
    Tensor control = null;
    {
      Tensor move = Tensors.fromString( //
          "{{1, 0, 0}, {1, 0, 0}, {2, 0, 2.5708}, {1, 0, 2.1}, {1.5, 0, 0}, {2.3, 0, -1.2}, {1.5, 0, 0}, {4, 0, 3.14159}, {2, 0, 3.14159}, {2, 0, 0}}");
      move = Tensor.of(move.stream().map(Times.operator(Tensors.vector(2, 1, 1))));
      Tensor init = Tensors.vector(0, 0, 2.1);
      control = DubinsGenerator.of(init, move);
      control = Tensors.fromString("{{0, 0, 0}, {1, 0, 0}, {2, 0, 0}, {3, 1, 0}, {4, 1, 0}, {5, 0, 0}, {6, 0, 0}, {7, 0, 0}}").multiply(RealScalar.of(2));
    }
    setControlPointsSe2(control);
    timerFrame.jToolBar.addSeparator();
    {
      JButton jButton = new JButton("load");
      List<String> list = Arrays.asList("ducttape/20180514.csv", "tires/20190116.csv", "tires/20190117.csv");
      Supplier<StandardMenu> supplier = () -> new StandardMenu() {
        @Override
        protected void design(JPopupMenu jPopupMenu) {
          for (String string : list) {
            JMenuItem jMenuItem = new JMenuItem(string);
            jMenuItem.addActionListener(new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent actionEvent) {
                Tensor tensor = ResourceData.of("/dubilab/controlpoints/" + string);
                tensor = Tensor.of(tensor.stream().map(Times.operator(Tensors.vector(0.5, 0.5, 1))));
                Tensor center = Mean.of(tensor);
                center.set(RealScalar.ZERO, 2);
                tensor = Tensor.of(tensor.stream().map(row -> row.subtract(center)));
                setGeodesicDisplay(Se2Display.INSTANCE);
                jToggleCyclic.setSelected(true);
                setControlPointsSe2(tensor);
              }
            });
            jPopupMenu.add(jMenuItem);
          }
        }
      };
      StandardMenu.bind(jButton, supplier);
      timerFrame.jToolBar.add(jButton);
    }
    // ---
    jToggleLine.setSelected(false);
    timerFrame.jToolBar.add(jToggleLine);
    // ---
    timerFrame.jToolBar.addSeparator();
    addButtonDubins();
    // ---
    // jToggleCyclic.setSelected(true);
    timerFrame.jToolBar.add(jToggleCyclic);
    // ---
    jToggleSymi.setSelected(true);
    timerFrame.jToolBar.add(jToggleSymi);
    // ---
    jToggleComb.setSelected(true);
    timerFrame.jToolBar.add(jToggleComb);
    // ---
    jToggleHelp.setToolTipText("indicate closest midpoint");
    jToggleHelp.setSelected(true);
    jToggleHelp.addActionListener(l -> setMidpointIndicated(jToggleHelp.isSelected()));
    timerFrame.jToolBar.addSeparator();
    timerFrame.jToolBar.add(jToggleHelp);
    // ---
    spinnerLabel.setArray(CurveSubdivisionSchemes.values());
    spinnerLabel.setIndex(0);
    spinnerLabel.addToComponentReduced(timerFrame.jToolBar, new Dimension(150, 28), "scheme");
    // ---
    spinnerRefine.setList(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
    spinnerRefine.setValue(6);
    spinnerRefine.addToComponentReduced(timerFrame.jToolBar, new Dimension(50, 28), "refinement");
    // ---
    spinnerMagicC.addSpinnerListener(value -> CurveSubdivisionHelper.MAGIC_C = value);
    spinnerMagicC.setList( //
        Tensors.fromString("{1/100, 1/10, 1/8, 1/6, 1/4, 1/3, 1/2, 2/3, 9/10, 99/100}").stream() //
            .map(Scalar.class::cast) //
            .collect(Collectors.toList()));
    spinnerMagicC.setValue(RationalScalar.HALF);
    spinnerMagicC.addToComponentReduced(timerFrame.jToolBar, new Dimension(50, 28), "refinement");
    {
      spinnerBeta.setList(OMEGA.stream().map(Scalar.class::cast).collect(Collectors.toList()));
      spinnerBeta.setValue(RationalScalar.of(1, 16));
      spinnerBeta.addSpinnerListener(l -> CurveSubdivisionHelper.OMEGA = spinnerBeta.getValue());
      spinnerBeta.addToComponentReduced(timerFrame.jToolBar, new Dimension(60, 28), "beta");
    }
    // ---
    // {
    // JSlider jSlider = new JSlider(1, 999, 500);
    // jSlider.setPreferredSize(new Dimension(360, 28));
    // jSlider.addChangeListener(changeEvent -> //
    // CurveSubdivisionHelper.MAGIC_C = RationalScalar.of(jSlider.getValue(), 1000));
    // timerFrame.jToolBar.add(jSlider);
    // }
    setGeodesicDisplay(Se2Display.INSTANCE);
    timerFrame.geometricComponent.setOffset(100, 600);
  }

  @Override
  public Tensor protected_render(GeometricLayer geometricLayer, Graphics2D graphics) {
    final CurveSubdivisionSchemes scheme = spinnerLabel.getValue();
    //
    if (scheme.equals(CurveSubdivisionSchemes.DODGSON_SABIN))
      setGeodesicDisplay(R2Display.INSTANCE);
    // ---
    if (jToggleSymi.isSelected()) {
      Optional<SymMaskImages> optional = SymMaskImages.get(scheme.name());
      if (optional.isPresent()) {
        BufferedImage image0 = optional.get().image0();
        graphics.drawImage(image0, 0, 0, null);
        BufferedImage image1 = optional.get().image1();
        graphics.drawImage(image1, image0.getWidth() + 1, 0, null);
      }
    }
    RenderQuality.setQuality(graphics);
    // ---
    final boolean cyclic = jToggleCyclic.isSelected() || !scheme.isStringSupported();
    Tensor control = getGeodesicControlPoints();
    int levels = spinnerRefine.getValue();
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, control, null, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexP();
    }
    GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
    Tensor refined = StaticHelper.refine( //
        control, levels, spinnerLabel.getValue().of(manifoldDisplay), //
        CurveSubdivisionHelper.isDual(scheme), cyclic, geodesicSpace);
    if (jToggleLine.isSelected()) {
      TensorUnaryOperator tensorUnaryOperator = StaticHelper.create(new BSpline1CurveSubdivision(manifoldDisplay.geodesicSpace()), cyclic);
      pathRender.setCurve(Nest.of(tensorUnaryOperator, control, 8), cyclic).render(geometricLayer, graphics);
    }
    Tensor render = Tensor.of(refined.stream().map(manifoldDisplay::toPoint));
    Curvature2DRender.of(render, cyclic, jToggleComb.isSelected(), geometricLayer, graphics);
    if (levels < 5)
      renderPoints(manifoldDisplay, refined, geometricLayer, graphics);
    return refined;
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.updateUI();
    new CurveSubdivisionDemo().setVisible(1200, 800);
  }
}
