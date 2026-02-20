// code by jph
package ch.alpine.ascona.dv;

import java.awt.Graphics2D;

import ch.alpine.ascona.lev.LeversHud;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.lie.LieGroup;
import ch.alpine.sophus.lie.se2.Se2Matrix;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.mat.gr.InfluenceMatrix;

public class Se2InvarianceDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, ManifoldDisplays.SE2C_SE2);
      drawControlPoints = false;
    }

    @FieldSelectionArray({ "{0,0,0}", "{1,1,3}" })
    public Tensor xya = Tensors.vector(1, 1, 3);
  }

  private final Param param;

  public Se2InvarianceDemo() {
    this(new Param());
  }

  public Se2InvarianceDemo(Param param) {
    super(param);
    this.param = param;
    controlPointsRender.setMidpointIndicated(false);
    setControlPointsSe2(Tensors.fromString( //
        "{{0, 0, 0}, {3, -2, -1}, {4, 2, 1}, {-1, 3, 2}, {-2, -3, -2}, {-3, 0, 0}}"));
    setControlPointsSe2(Tensors.fromString( //
        "{{0.000, 0.000, 0.000}, {-0.950, 1.750, -2.618}, {0.833, 2.300, -1.047}, {2.667, 0.733, -2.618}, {2.033, -1.800, 2.356}, {-1.217, -0.633, -3.665}}"));
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    LieGroup lieGroup = (LieGroup) manifoldDisplay().geodesicSpace();
    Tensor controlPointsAll = getGeodesicControlPoints();
    if (0 < controlPointsAll.length()) {
      {
        Tensor sequence = controlPointsAll.extract(1, controlPointsAll.length());
        Tensor origin = controlPointsAll.get(0);
        Tensor matrix = lieGroup.exponential(origin).log().slash(sequence);
        Tensor weights = InfluenceMatrix.of(matrix).leverages_sqrt();
        LeversRender leversRender = //
            LeversRender.of(manifoldDisplay, sequence, origin, geometricLayer, graphics);
        leversRender.renderSequence();
        leversRender.renderLevers();
        leversRender.renderWeights(weights);
        leversRender.renderInfluenceX(LeversHud.COLOR_DATA_GRADIENT);
        leversRender.renderOrigin();
        leversRender.renderIndexX();
        leversRender.renderIndexP();
      }
      geometricLayer.pushMatrix(Se2Matrix.translation(Tensors.vector(10, 0)));
      try {
        TensorUnaryOperator lieGroupOR = lieGroup.actionR(param.xya);
        Tensor allR = Tensor.of(controlPointsAll.stream().map(lieGroupOR));
        TensorUnaryOperator lieGroupOp = lieGroup.actionL(lieGroup.invert(allR.get(0)));
        Tensor result = Tensor.of(allR.stream().map(lieGroupOp));
        Tensor sequence = result.extract(1, result.length());
        Tensor origin = result.get(0);
        Tensor matrix = lieGroup.exponential(origin).log().slash(sequence);
        Tensor weights = InfluenceMatrix.of(matrix).leverages_sqrt();
        LeversRender leversRender = //
            LeversRender.of(manifoldDisplay, sequence, origin, geometricLayer, graphics);
        leversRender.renderSequence();
        leversRender.renderLevers();
        leversRender.renderWeights(weights);
        leversRender.renderInfluenceX(LeversHud.COLOR_DATA_GRADIENT);
        leversRender.renderOrigin();
        leversRender.renderIndexX("x'");
        leversRender.renderIndexP("p'");
      } catch (Exception exception) {
        exception.printStackTrace();
      }
      geometricLayer.popMatrix();
    }
  }

  static void main() {
    new Se2InvarianceDemo().runStandalone();
  }
}
