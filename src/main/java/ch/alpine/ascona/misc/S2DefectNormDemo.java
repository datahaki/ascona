// code by jph
package ch.alpine.ascona.misc;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.List;

import ch.alpine.ascony.arp.ArrayFunction;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointTypes;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.fig.ImagePlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.Showable;
import ch.alpine.bridge.fig.VectorPlot;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.PvmBuilder;
import ch.alpine.bridge.ref.ann.FieldLabel;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.bm.MeanDefect;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.hs.s.STangentSpace;
import ch.alpine.sophus.hs.s.SnManifold;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Append;
import ch.alpine.tensor.alg.ConstantArray;
import ch.alpine.tensor.alg.Join;
import ch.alpine.tensor.alg.Rescale;
import ch.alpine.tensor.api.TensorScalarFunction;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.io.ImageFormat;
import ch.alpine.tensor.nrm.FrobeniusNorm;
import ch.alpine.tensor.nrm.NormalizeTotal;
import ch.alpine.tensor.nrm.Vector2NormSquared;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.N;
import ch.alpine.tensor.sca.Sign;
import ch.alpine.tensor.sca.pow.Sqrt;

class S2DefectNormDemo extends ControlPointsDemo {
  private static final Stroke STROKE = //
      new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0);
  private static final Tensor INITIAL = Tensors.fromString("{{-0.5, 0, 0}, {0.5, 0, 0}, {0, 0.5, 0}, {0, -0.5, 0}}").unmodifiable();

  @ReflectionMarker
  static class Param {
    @FieldSelectionArray({ "20", "30", "50", "75", "100", "150", "200", "250" })
    public Integer resolution = 20;
    @FieldLabel("color data gradient")
    public ColorDataGradients colorDataGradients = ColorDataGradients.PARULA;
    public ColorDataGradients cdg = ColorDataGradients.EMBER;
    public Boolean vector = true;
    @FieldLabel("weights")
    public Tensor user_weights = Tensors.vector(3, 2, -2);

    public Tensor weights(int n) {
      return n <= user_weights.length() //
          ? user_weights.extract(0, n)
          : Join.of(user_weights, ConstantArray.of(RealScalar.ONE, n - user_weights.length()));
    }
  }

  public final Param param;

  public S2DefectNormDemo() {
    super(param = new Param());
    geometricComponent().setRotatable(false);
    // ---
    setControlPointsSe2(INITIAL);
    // ---
    Tensor pvm = PvmBuilder.rhs().setOffset(400, 400).setPerPixel(400).digest();
    geometricComponent().setModel2Pixel(pvm);
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.S2_ONLY;
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointTypes.SCATTERED;
  }

  public class TSF implements TensorScalarFunction {
    final Tensor sequence;
    final Tensor weights;

    public TSF() {
      sequence = getGeodesicControlPoints();
      int n = sequence.length();
      weights = NormalizeTotal.FUNCTION.apply(param.weights(n).maps(N.DOUBLE));
    }

    @Override
    public Scalar apply(Tensor xyz) {
      MeanDefect meanDefect = MeanDefect.of(sequence, weights, new STangentSpace(xyz));
      return FrobeniusNorm.of(meanDefect.tangent());
    }
  }

  double rad() {
    return 1;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    TSF tsf = new TSF();
    int resolution = param.resolution;
    Show show = new Show();
    {
      ManifoldDisplay manifoldDisplay = manifoldDisplay();
      ArrayFunction<Scalar> arrayFunction = new ArrayFunction<>(new TSF(), DoubleScalar.INDETERMINATE);
      CoordinateBoundingBox cbb = manifoldDisplay.d2Raster_coordinateBoundingBox();
      Tensor raster = manifoldDisplay.d2Raster().of(arrayFunction, cbb, resolution);
      Showable showable = ImagePlot.of(ImageFormat.of(Rescale.of(raster).maps(param.colorDataGradients)), cbb);
      show.add(showable);
      if (param.vector)
        show.add(VectorPlot.of(p -> arrow(tsf, p), cbb, param.cdg));
      show.render(graphics, geometricLayer.toRectangle(cbb).orElseThrow());
    }
    graphics.setStroke(STROKE);
    // Tensor ms = Tensor.of(GEODESIC_DOMAIN.map(scalarTensorFunction).stream().map(manifoldDisplay::toPoint));
    graphics.setColor(new Color(192, 192, 192));
    // graphics.draw(geometricLayer.toPath2D(ms));
    graphics.setStroke(new BasicStroke());
    // ---
    Tensor mean = null;
    try {
      mean = SnManifold.INSTANCE.biinvariantMean().mean(tsf.sequence, tsf.weights);
    } catch (Exception e) {
      graphics.setColor(Color.RED);
      graphics.drawString("no mean avaiable", 0, 25);
      // ---
    }
    LeversRender leversRender = LeversRender.of(manifoldDisplay(), tsf.sequence, mean, geometricLayer, graphics);
    leversRender.renderOrigin();
    leversRender.renderSequence();
    leversRender.renderWeights(tsf.weights);
  }

  public static Tensor arrow(TSF tsf, Tensor p) {
    Scalar z2 = RealScalar.ONE.subtract(Vector2NormSquared.of(p));
    if (Sign.isPositive(z2)) {
      Scalar z = Sqrt.FUNCTION.apply(z2);
      Tensor xyz = Append.of(p, z);
      MeanDefect meanDefect = MeanDefect.of(tsf.sequence, tsf.weights, new STangentSpace(xyz));
      return meanDefect.tangent().extract(0, 2);
    }
    return DoubleScalar.INDETERMINATE;
  }

  public final Tensor iterationPath(Tensor sequence, Tensor weights, Tensor shifted, int iter) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    HomogeneousSpace homogeneousSpace = manifoldDisplay.homogeneousSpace();
    Tensor tensor = Tensors.empty();
    for (int count = 0; count < iter; ++count) {
      MeanDefect meanDefect = MeanDefect.of(sequence, weights, homogeneousSpace.tangentSpace(shifted));
      shifted = meanDefect.shifted();
      tensor.append(shifted);
    }
    return tensor;
  }

  static void main() {
    new S2DefectNormDemo().runStandalone();
  }
}
