// code by jph
package ch.alpine.ascona.gbc.d1;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import ch.alpine.ascony.api.LogWeightings;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointTypes;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophis.itp.CrossAveraging;
import ch.alpine.sophis.itp.RadialBasisFunctionInterpolation;
import ch.alpine.sophus.api.Manifold;
import ch.alpine.sophus.bm.LinearBiinvariantMean;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Sort;
import ch.alpine.tensor.alg.Transpose;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.sca.var.InversePowerVariogram;

/** TODO ASCONA ALG investigate, this produces some nice results for kriging+metric+power */
public class R1RadialBasisFunctionDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param {
    public LogWeightings logWeightings = LogWeightings.WEIGHTING;
    public Biinvariants biinvariants = Biinvariants.METRIC;
    public Boolean type = false;
    @FieldSelectionArray({ "30", "40", "50", "75", "100", "150", "200", "250" })
    public Integer resolution = 40;
    public ColorDataGradients cdg = ColorDataGradients.PARULA;
  }

  private final Param param;

  public R1RadialBasisFunctionDemo() {
    this(new Param());
  }

  public R1RadialBasisFunctionDemo(Param param) {
    super(param);
    this.param = param;
    // ---
    setControlPointsSe2(Tensors.fromString("{{0, 0, 0}, {1, 2, 0}, {2, -1, 0}}"));
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.R2_ONLY;
  }

  @Override
  public ControlPointType controlPointType() {
    return ControlPointTypes.SCATTERED;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor control = Sort.of(getControlPointsSe2());
    if (1 < control.length()) {
      Tensor support = control.get(Tensor.ALL, 0);
      Tensor funceva = control.get(Tensor.ALL, 1);
      // ---
      Tensor sequence = support.maps(Tensors::of);
      Tensor domain = StaticHelper.domain(getControlPointsSe2());
      Manifold manifold = manifoldDisplay().manifold();
      Sedarim sedarim = param.logWeightings.sedarim(param.biinvariants.ofSafe(manifold), InversePowerVariogram.of(2), sequence);
      try {
        TensorUnaryOperator tensorUnaryOperator = //
            RadialBasisFunctionInterpolation.of(sedarim, sequence, funceva);
        Tensor result = Tensor.of(domain.stream().map(Tensors::of).map(tensorUnaryOperator));
        new PathRender(Color.BLUE, 1.25f) //
            .setCurve(Transpose.of(Tensors.of(domain, result)), false) //
            .render(geometricLayer, graphics);
      } catch (Exception exception) {
        // ---
      }
      // if (!isDeterminate())
      try {
        TensorUnaryOperator operator = //
            new CrossAveraging(sedarim, LinearBiinvariantMean.INSTANCE, funceva);
        Tensor result = Tensor.of(domain.stream().map(Tensors::of).map(operator));
        new PathRender(Color.RED, 1.25f) //
            .setCurve(Transpose.of(Tensors.of(domain, result)), false) //
            .render(geometricLayer, graphics);
      } catch (Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  static void main() {
    new R1RadialBasisFunctionDemo().runStandalone();
  }
}
