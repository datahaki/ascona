// code by jph
package ch.alpine.ascona.misc;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophus.crv.d2.Arrowhead;
import ch.alpine.sophus.decim.HsLineDistance;
import ch.alpine.sophus.decim.HsLineDistanceLocal;
import ch.alpine.sophus.decim.HsLineProjection;
import ch.alpine.sophus.hs.Exponential;
import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.sophus.lie.LieGroup;
import ch.alpine.sophus.lie.se2c.Se2CoveringGroup;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.sca.Round;

// TODO ASCONA REV enhance with more explanations
public class Se2LineDistanceDemo extends ControlPointsDemo {
  private static final ColorDataIndexed COLOR_DATA_INDEXED_DRAW = ColorDataLists._097.cyclic().deriveWithAlpha(192);
  private static final ColorDataIndexed COLOR_DATA_INDEXED_FILL = ColorDataLists._097.cyclic().deriveWithAlpha(182);

  public Se2LineDistanceDemo() {
    super(new AsconaParam(false, ManifoldDisplays.SE2_ONLY));
    // ---
    Tensor tensor = Tensors.fromString("{{0, 0, 0}, {5, 0, 1}}");
    setControlPointsSe2(tensor);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    Tensor sequence = getControlPointsSe2();
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    LieGroup lieGroup = (LieGroup) manifoldDisplay().geodesicSpace();
    Exponential exponential = Se2CoveringGroup.INSTANCE; // TODO ASCONA check
    // ---
    GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
    Tensor beg = sequence.get(0);
    Tensor end = sequence.get(1);
    ScalarTensorFunction curve = geodesicSpace.curve(beg, end);
    {
      Tensor tensor = Subdivide.of(-0.5, 1.5, 55).map(curve);
      Path2D path2d = geometricLayer.toPath2D(Tensor.of(tensor.stream().map(manifoldDisplay::point2xy)));
      graphics.setColor(Color.BLUE);
      graphics.draw(path2d);
    }
    final Tensor mouse = timerFrame.geometricComponent.getMouseSe2CState();
    {
      HsLineDistance hsLineDistance = new HsLineDistance(lieGroup);
      HsLineDistanceLocal normImpl = hsLineDistance.tensorNorm(beg, end);
      {
        Tensor project = normImpl.project(mouse);
        Tensor exp = exponential.exp(project);
        Tensor glb = lieGroup.element(beg).combine(exp);
        {
          geometricLayer.pushMatrix(manifoldDisplay.matrixLift(glb));
          Path2D path2d = geometricLayer.toPath2D(Arrowhead.of(0.4));
          path2d.closePath();
          graphics.setColor(COLOR_DATA_INDEXED_FILL.getColor(2));
          graphics.fill(path2d);
          graphics.setColor(COLOR_DATA_INDEXED_DRAW.getColor(2));
          graphics.draw(path2d);
          geometricLayer.popMatrix();
        }
      }
      {
        Tensor orthogonal = normImpl.orthogonal(mouse);
        Tensor exp = exponential.exp(orthogonal);
        Tensor glb = lieGroup.element(beg).combine(exp);
        {
          graphics.setColor(Color.DARK_GRAY);
          graphics.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
          graphics.drawString("" + orthogonal.map(Round._3), 0, 40);
        }
        {
          geometricLayer.pushMatrix(manifoldDisplay.matrixLift(glb));
          Path2D path2d = geometricLayer.toPath2D(Arrowhead.of(0.4));
          path2d.closePath();
          graphics.setColor(COLOR_DATA_INDEXED_FILL.getColor(3));
          graphics.fill(path2d);
          graphics.setColor(COLOR_DATA_INDEXED_DRAW.getColor(3));
          graphics.draw(path2d);
          geometricLayer.popMatrix();
        }
      }
    }
    {
      HsLineProjection hsLineProjection = new HsLineProjection(lieGroup);
      Tensor onto = hsLineProjection.onto(beg, end, mouse);
      {
        geometricLayer.pushMatrix(manifoldDisplay.matrixLift(onto));
        Path2D path2d = geometricLayer.toPath2D(Arrowhead.of(0.4));
        path2d.closePath();
        graphics.setColor(COLOR_DATA_INDEXED_FILL.getColor(4));
        graphics.fill(path2d);
        graphics.setColor(COLOR_DATA_INDEXED_DRAW.getColor(4));
        graphics.draw(path2d);
        geometricLayer.popMatrix();
      }
    }
    // ---
    {
      geometricLayer.pushMatrix(manifoldDisplay.matrixLift(curve.apply(RationalScalar.HALF)));
      Path2D path2d = geometricLayer.toPath2D(Arrowhead.of(0.5));
      path2d.closePath();
      graphics.setColor(COLOR_DATA_INDEXED_FILL.getColor(0));
      graphics.fill(path2d);
      graphics.setColor(COLOR_DATA_INDEXED_DRAW.getColor(0));
      graphics.draw(path2d);
      geometricLayer.popMatrix();
    }
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, sequence, null, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexP();
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
