// code by jph
package ch.alpine.ascona.ref.d1;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.swing.JButton;

import ch.alpine.ascona.crv.AbstractCurvatureDemo;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.ControlPointsStatic;
import ch.alpine.ascona.util.ren.Curvature2DRender;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.swing.SpinnerMenu;
import ch.alpine.sophus.crv.dub.DubinsGenerator;
import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.sophus.ref.d1.BSpline1CurveSubdivision;
import ch.alpine.sophus.ref.d1.CurveSubdivision;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.io.Import;
import ch.alpine.tensor.red.Mean;
import ch.alpine.tensor.red.Nest;
import ch.alpine.tensor.red.Times;

/** split interface and biinvariant mean based curve subdivision */
public class CurveSubdivisionDemo2 extends AbstractCurvatureDemo {
  private static final PathRender pathRender = new PathRender(new Color(0, 255, 0, 128));

  @ReflectionMarker
  public static class Param extends AbstractCurvatureParam {
    public Param() {
      super(ManifoldDisplays.ALL);
    }

    public CurveSubdivisionSchemes scheme = CurveSubdivisionSchemes.BSPLINE1;
    @FieldSelectionArray({ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" })
    public Integer refine = 5;
    public Boolean line = false;
    public Boolean cyclic = false;
    public Boolean symi = true;
    public Boolean comb = true;
    public final CurveSubdivisionParam csp = CurveSubdivisionParam.GLOBAL;
  }

  private final Param param;

  public CurveSubdivisionDemo2() {
    this(new Param());
  }

  public CurveSubdivisionDemo2(Param param) {
    super(param);
    this.param = param;
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
    {
      JButton jButton = new JButton("load");
      List<String> list = List.of("ducttape/20180514.csv", "tires/20190116.csv", "tires/20190117.csv");
      jButton.addActionListener(e -> {
        SpinnerMenu<String> spinnerMenu = new SpinnerMenu<>(list, null, Object::toString, null, false);
        spinnerMenu.addSpinnerListener(string -> {
          Tensor tensor = Import.of("/dubilab/controlpoints/" + string);
          tensor = Tensor.of(tensor.stream().map(Times.operator(Tensors.vector(0.5, 0.5, 1))));
          Tensor center = Mean.of(tensor);
          center.set(RealScalar.ZERO, 2);
          tensor = Tensor.of(tensor.stream().map(row -> row.subtract(center)));
          setManifoldDisplay(ManifoldDisplays.Se2);
          param.cyclic = true;
          setControlPointsSe2(tensor);
        });
        spinnerMenu.showSouth(jButton);
      });
      // Supplier<StandardMenu> supplier = () -> new StandardMenu() {
      // @Override
      // protected void design(JPopupMenu jPopupMenu) {
      // for (String string : list) {
      // JMenuItem jMenuItem = new JMenuItem(string);
      // jMenuItem.addActionListener(new ActionListener() {
      // @Override
      // public void actionPerformed(ActionEvent actionEvent) {
      // }
      // });
      // jPopupMenu.add(jMenuItem);
      // }
      // }
      // };
      // StandardMenu.bind(jButton, supplier);
      timerFrame.jToolBar.add(jButton);
    }
    // ---
    timerFrame.jToolBar.addSeparator();
    addButtonDubins();
    // ---
    controlPointsRender.setMidpointIndicated(true);
    // ---
    setManifoldDisplay(ManifoldDisplays.Se2);
    timerFrame.geometricComponent.setOffset(100, 600);
  }

  @Override
  public Tensor protected_render(GeometricLayer geometricLayer, Graphics2D graphics) {
    final CurveSubdivisionSchemes scheme = param.scheme;
    //
    if (scheme.equals(CurveSubdivisionSchemes.DODGSON_SABIN))
      setManifoldDisplay(ManifoldDisplays.R2);
    // ---
    if (param.symi) {
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
    final boolean cyclic = param.cyclic || !scheme.isStringSupported();
    Tensor control = getGeodesicControlPoints();
    int levels = param.refine;
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, control, null, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexP();
    }
    CurveSubdivision curveSubdivision = null;
    try {
      curveSubdivision = param.scheme.of(manifoldDisplay);
    } catch (Exception exception) {
      // ---
    }
    if (Objects.nonNull(curveSubdivision)) {
      GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
      Tensor refined = StaticHelper.refine( //
          control, levels, curveSubdivision, //
          scheme.isDual(), cyclic, geodesicSpace);
      if (param.line) {
        TensorUnaryOperator tensorUnaryOperator = StaticHelper.create(new BSpline1CurveSubdivision(geodesicSpace), cyclic);
        pathRender.setCurve(Nest.of(tensorUnaryOperator, control, 8), cyclic).render(geometricLayer, graphics);
      }
      Tensor render = Tensor.of(refined.stream().map(manifoldDisplay::point2xy));
      Curvature2DRender.of(render, cyclic, param.comb).render(geometricLayer, graphics);
      if (levels < 5)
        ControlPointsStatic.gray(manifoldDisplay, refined).render(geometricLayer, graphics);
      return refined;
    }
    return Tensors.empty();
  }

  public static void main(String[] args) {
    launch();
  }
}
