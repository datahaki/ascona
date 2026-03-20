// code by jph
package ch.alpine.ascona.def;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.util.Collection;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.msh.AveragedMovingDomain2D;
import ch.alpine.ascony.msh.Meshgrid;
import ch.alpine.ascony.msh.MovingDomain2D;
import ch.alpine.ascony.msh.Thinning;
import ch.alpine.ascony.ren.ColorPair;
import ch.alpine.ascony.ren.MeshRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.plt.ArrayPlot;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.api.Genesis;
import ch.alpine.sophis.crv.d2.ex.Box2D;
import ch.alpine.sophis.gbc.d2.InsidePolygonCoordinate;
import ch.alpine.sophis.gbc.d2.SPatch;
import ch.alpine.sophis.gbc.d2.ThreePointCoordinate;
import ch.alpine.sophis.gbc.d2.ThreePointScalings;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.Rational;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.PadRight;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;

class SPatchDemo extends ControlPointsDemo {
  @ReflectionMarker
  static class Param0 {
    @FieldSelectionArray({ "3", "4", "5", "6", "7" })
    public Integer n = 5;
    @FieldSelectionArray({ "20", "30", "50" })
    public Integer res = 34;
  }

  @ReflectionMarker
  static class Param1 {
    public ColorDataGradients cdg = ColorDataGradients.CLASSIC;
  }

  private final Param0 param0;
  private final Param1 param1;
  private SPatch sPatch;
  private MovingDomain2D movingDomain2D;

  public SPatchDemo() {
    super(param0 = new Param0(), param1 = new Param1());
    fieldsEditor(param0).addUniversalListener(this::shuffle);
    addChangeListener(this::shuffle);
    setManifoldDisplay(ManifoldDisplays.R2);
    shuffle();
  }

  @Override
  protected Collection<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.SE2C_R2;
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.HEAD_TAIL;
  }

  void shuffle() {
    Genesis genesis = new InsidePolygonCoordinate(ThreePointCoordinate.of(ThreePointScalings.MEAN_VALUE));
    sPatch = SPatch.of(param0.n, genesis, 2);
    Tensor embed = sPatch.getEmbed();
    setControlPointsSe2(Tensor.of(embed.stream() //
        .map(xy -> xy.multiply(RealScalar.of(3))).map(PadRight.zeros(3))));
    Clip clip = Clips.absolute(1);
    Tensor domain = new Meshgrid(Box2D.xy(clip), param0.res).image(sPatch::sunder);
    BiinvariantMean biinvariantMean = manifoldDisplay().homogeneousSpace().biinvariantMean();
    movingDomain2D = new AveragedMovingDomain2D(domain, biinvariantMean, manifoldDisplay().indetPoint());
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor sequence = getGeodesicControlPoints();
    HomogeneousSpace homogeneousSpace = manifoldDisplay.homogeneousSpace();
    {
      Tensor[][] forward = movingDomain2D.forward(sequence);
      Tensor points = Thinning.flatten(forward, 3, 3);
      manifoldDisplay.showPoints(ColorPair.INTERMEDIATE, RealScalar.of(0.4), points) //
          .render(geometricLayer, graphics);
      new MeshRender(forward, param1.cdg.deriveWithOpacity(Rational.HALF)) //
          .render(geometricLayer, graphics);
    }
    {
      Tensor weights = movingDomain2D.arrayReshape_weights();
      Show show = new Show();
      show.add(ArrayPlot.of(weights, param1.cdg));
      Dimension dimension = getSize();
      show.render_autoIndent(graphics, new Rectangle(0, 0, dimension.width - 100, 300));
    }
    {
      int n = sPatch.n();
      Tensor domain = Subdivide.of(0.0, 1.0, 15);
      graphics.setColor(new Color(0, 0, 0, 128));
      for (int i = 0; i < n; ++i)
        for (int j = 0; j < n; ++j) {
          Tensor r0 = Tensors.vector(i, j);
          Tensor r1 = Tensors.vector(i, (j + 1) % n);
          int i0 = sPatch.basis(r0);
          int i1 = sPatch.basis(r1);
          ScalarTensorFunction stf = homogeneousSpace.curve(sequence.get(i0), sequence.get(i1));
          Tensor points = Tensor.of(domain.maps(stf).stream().map(manifoldDisplay::point2xy));
          Path2D path2d = geometricLayer.toPath2D(points);
          graphics.draw(path2d);
        }
    }
  }

  static void main() {
    new SPatchDemo().runStandalone();
  }
}
