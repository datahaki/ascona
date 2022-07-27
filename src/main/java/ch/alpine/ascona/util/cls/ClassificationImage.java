// code by jph
package ch.alpine.ascona.util.cls;

import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.img.ColorDataIndexed;

public enum ClassificationImage {
  BLENDED {
    @Override
    public TensorUnaryOperator operator(Classification classification, Sedarim sedarim, ColorDataIndexed colorDataIndexed) {
      return point -> {
        ClassificationResult classificationResult = classification.result(sedarim.sunder(point));
        Tensor rgba = colorDataIndexed.apply(RealScalar.of(classificationResult.label()));
        rgba.set(classificationResult.confidence().multiply(RealScalar.of(128 + 64)), 3);
        return rgba;
      };
    }
  }, //
  LABEL {
    @Override
    public TensorUnaryOperator operator(Classification classification, Sedarim sedarim, ColorDataIndexed colorDataIndexed) {
      ColorDataIndexed _colorDataIndexed = colorDataIndexed.deriveWithAlpha(128 + 64);
      return point -> _colorDataIndexed.apply(RealScalar.of(classification.result(sedarim.sunder(point)).label()));
    }
  }, //
  CONFIDENCE {
    @Override
    public TensorUnaryOperator operator(Classification classification, Sedarim sedarim, ColorDataIndexed colorDataIndexed) {
      return point -> ColorDataGradients.CLASSIC.apply(classification.result(sedarim.sunder(point)).confidence());
    }
  }, //
  ;

  /** @param classification
   * @param sedarim that maps a point to distances/weights/coordinates
   * @param colorDataIndexed
   * @return */
  public abstract TensorUnaryOperator operator( //
      Classification classification, Sedarim sedarim, ColorDataIndexed colorDataIndexed);
}
