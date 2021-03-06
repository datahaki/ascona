// code by jph, ob
package ch.alpine.ascona.util.sym;

import java.awt.Font;

import ch.alpine.sophus.crv.GeodesicBSplineFunction;
import ch.alpine.sophus.flt.ga.GeodesicCenter;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.itp.DeBoor;

public enum SymLinkImages {
  ;
  public static final Font FONT_SMALL = new Font(Font.DIALOG, Font.PLAIN, 11);

  public static SymLinkImage deboor(Tensor knots, int length, Scalar scalar) {
    Tensor vector = SymSequence.of(length);
    ScalarTensorFunction scalarTensorFunction = DeBoor.of(SymGeodesic.INSTANCE, knots, vector);
    Tensor tensor = scalarTensorFunction.apply(scalar);
    SymLinkImage symLinkImage = new SymLinkImage(tensor, FONT_SMALL);
    symLinkImage.title("DeBoor at " + scalar);
    return symLinkImage;
  }

  public static SymLinkImage ofGC(ScalarUnaryOperator smoothingKernel, int radius) {
    TensorUnaryOperator tensorUnaryOperator = GeodesicCenter.of(SymGeodesic.INSTANCE, smoothingKernel);
    Tensor vector = SymSequence.of(2 * radius + 1);
    Tensor tensor = tensorUnaryOperator.apply(vector);
    SymLinkImage symLinkImage = new SymLinkImage(tensor, FONT_SMALL);
    symLinkImage.title(smoothingKernel + "[" + (2 * radius + 1) + "]");
    return symLinkImage;
  }

  /* package */ public static SymLinkImage symLinkImageGBSF(int degree, int length, Scalar scalar) {
    Tensor vector = SymSequence.of(length);
    ScalarTensorFunction scalarTensorFunction = GeodesicBSplineFunction.of(SymGeodesic.INSTANCE, degree, vector);
    Tensor tensor = scalarTensorFunction.apply(scalar);
    SymLinkImage symLinkImage = new SymLinkImage(tensor, FONT_SMALL);
    symLinkImage.title("DeBoor[" + degree + "] at " + scalar);
    return symLinkImage;
  }
}
