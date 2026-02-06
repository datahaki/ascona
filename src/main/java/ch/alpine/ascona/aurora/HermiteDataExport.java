// code by jph
package ch.alpine.ascona.aurora;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import ch.alpine.ascona.dat.gok.GokartPosVel;
import ch.alpine.ascona.dat.gok.PosVelHz;
import ch.alpine.ascony.api.HermiteSubdivisions;
import ch.alpine.sophis.crv.d2.Curvature2D;
import ch.alpine.sophis.ref.d1.BSpline1CurveSubdivision;
import ch.alpine.sophis.ref.d1.BSpline2CurveSubdivision;
import ch.alpine.sophis.ref.d1.CurveSubdivision;
import ch.alpine.sophis.ref.d1h.HermiteSubdivision;
import ch.alpine.sophis.ref.d1h.TensorIteration;
import ch.alpine.sophus.lie.LieGroup;
import ch.alpine.sophus.lie.rn.RGroup;
import ch.alpine.sophus.lie.se2.Se2CoveringGroup;
import ch.alpine.sophus.lie.so2.So2Lift;
import ch.alpine.sophus.math.Do;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.ext.Integers;
import ch.alpine.tensor.io.Export;
import ch.alpine.tensor.qty.Quantity;
import ch.alpine.tensor.qty.QuantityMagnitude;
import ch.alpine.tensor.red.Nest;

/* package */ class HermiteDataExport {
  private final int levels;
  private final Path folder;
  private final Tensor control = Tensors.empty();
  private final Scalar delta;
  private final Tensor domain;

  /** @param name "20190701T163225_01"
   * @param period 1/2[s]
   * @param levels 4
   * @throws IOException */
  public HermiteDataExport(String name, Scalar period, int levels) throws IOException {
    this.levels = Integers.requirePositive(levels);
    folder = HomeDirectory.Documents.resolve(name);
    Files.createDirectories(folder);
    PosVelHz posVelHz = GokartPosVel.get(name, 2000); // limit , 2_000);
    Tensor data = posVelHz.getPosVelSequence();
    data.set(new So2Lift(), Tensor.ALL, 0, 2);
    {
      Export.of(folder.resolve("gndtrth.mathematica"), data);
      Tensor domain1 = Range.of(0, data.length()).multiply(RealScalar.of(1 / 50.));
      Export.of(folder.resolve("gndtrth_domain.mathematica"), domain1);
    }
    Scalar rate = Quantity.of(50, "Hz");
    delta = QuantityMagnitude.SI().in("s").apply(period);
    int skip = Scalars.intValueExact(period.multiply(rate));
    for (int index = 0; index < data.length(); index += skip)
      control.append(data.get(index));
    Export.of(folder.resolve("control.mathematica"), control);
    domain = Range.of(0, control.length()).multiply(delta);
    Export.of(folder.resolve("control_domain.mathematica"), domain);
  }

  private void process(HermiteSubdivision hermiteSubdivision, CurveSubdivision curveSubdivision, String name) throws IOException {
    TensorIteration tensorIteration = //
        hermiteSubdivision.string(delta, control);
    Path dst = folder.resolve(name);
    Files.createDirectories(dst);
    {
      Tensor refined = Do.of(tensorIteration::iterate, levels);
      Export.of(dst.resolve("refined.mathematica"), refined);
      Tensor curvatu = Curvature2D.string(Tensor.of(refined.stream().map(point -> point.get(0).extract(0, 2))));
      Export.of(dst.resolve("curvatu.mathematica"), curvatu);
    }
    {
      Tensor tensor = Nest.of(curveSubdivision::string, domain, levels);
      Export.of(dst.resolve("refined_domain.mathematica"), tensor);
    }
  }

  private void processAll() throws IOException {
    LieGroup lieGroup = Se2CoveringGroup.INSTANCE;
    // BiinvariantMean biinvariantMean = Se2CoveringBiinvariantMean.INSTANCE;
    {
      HermiteSubdivision hermiteSubdivision = //
          HermiteSubdivisions.H1STANDARD.supply(lieGroup);
      CurveSubdivision curveSubdivision = new BSpline1CurveSubdivision(RGroup.INSTANCE);
      process(hermiteSubdivision, curveSubdivision, "h1standard");
    }
    {
      HermiteSubdivision hermiteSubdivision = //
          HermiteSubdivisions.H2STANDARD.supply(lieGroup);
      CurveSubdivision curveSubdivision = new BSpline2CurveSubdivision(RGroup.INSTANCE);
      process(hermiteSubdivision, curveSubdivision, "h2standard");
    }
    {
      HermiteSubdivision hermiteSubdivision = //
          HermiteSubdivisions.H2MANIFOLD.supply(lieGroup);
      CurveSubdivision curveSubdivision = new BSpline2CurveSubdivision(RGroup.INSTANCE);
      process(hermiteSubdivision, curveSubdivision, "h2manifold");
    }
    {
      HermiteSubdivision hermiteSubdivision = //
          HermiteSubdivisions.H3STANDARD.supply(lieGroup);
      CurveSubdivision curveSubdivision = new BSpline1CurveSubdivision(RGroup.INSTANCE);
      process(hermiteSubdivision, curveSubdivision, "h3standard");
    }
    {
      HermiteSubdivision hermiteSubdivision = //
          HermiteSubdivisions.H3A1.supply(lieGroup);
      CurveSubdivision curveSubdivision = new BSpline1CurveSubdivision(RGroup.INSTANCE);
      process(hermiteSubdivision, curveSubdivision, "h3a1");
    }
    {
      HermiteSubdivision hermiteSubdivision = //
          HermiteSubdivisions.H3A2.supply(lieGroup);
      CurveSubdivision curveSubdivision = new BSpline1CurveSubdivision(RGroup.INSTANCE);
      process(hermiteSubdivision, curveSubdivision, "h3a2");
    }
  }

  static void main() throws IOException {
    Scalar period = Quantity.of(RationalScalar.of(1, 1), "s");
    HermiteDataExport hermiteDataExport = new HermiteDataExport("20190701T163225_01", period, 6);
    hermiteDataExport.processAll();
  }
}
