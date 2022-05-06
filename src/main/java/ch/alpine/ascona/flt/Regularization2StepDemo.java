// code by jph
package ch.alpine.ascona.flt;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.stream.IntStream;

import ch.alpine.ascona.util.api.BufferedImageSupplier;
import ch.alpine.ascona.util.dat.GokartPoseDataV2;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.sym.SymGeodesic;
import ch.alpine.ascona.util.sym.SymLinkImage;
import ch.alpine.ascona.util.sym.SymLinkImages;
import ch.alpine.ascona.util.sym.SymScalar;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.sophus.flt.ga.Regularization2Step;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.sca.N;

@ReflectionMarker
public final class Regularization2StepDemo extends AbstractSpectrogramDemo implements BufferedImageSupplier {
  /** regularization parameter in the interval [0, 1] */
  @FieldSlider
  @FieldClip(min = "0.0", max = "1.0")
  public Scalar ratio = RealScalar.of(0.6);

  public Regularization2StepDemo() {
    super(ManifoldDisplays.SE2_R2, GokartPoseDataV2.INSTANCE);
    ToolbarFieldsEditor.add(this, timerFrame.jToolBar);
    // ---
    updateState();
  }

  @Override // from AbstractDatasetFilterDemo
  public Tensor protected_render(GeometricLayer geometricLayer, Graphics2D graphics) {
    return Regularization2Step.string( //
        manifoldDisplay().geodesic(), //
        N.DOUBLE.apply(ratio)).apply(control());
  }

  @Override // from UniformDatasetFilterDemo
  protected String plotLabel() {
    return "Regularization2Step " + ratio;
  }

  @Override // from BufferedImageSupplier
  public BufferedImage bufferedImage() {
    Scalar factor = ratio;
    TensorUnaryOperator tensorUnaryOperator = Regularization2Step.string(SymGeodesic.INSTANCE, factor);
    Tensor vector = Tensor.of(IntStream.range(0, 3).mapToObj(SymScalar::leaf));
    Tensor tensor = tensorUnaryOperator.apply(vector);
    SymLinkImage symLinkImage = new SymLinkImage((SymScalar) tensor.get(1), SymLinkImages.FONT_SMALL);
    symLinkImage.title("Regularization2Step [" + factor + "]");
    return symLinkImage.bufferedImage();
  }

  public static void main(String[] args) {
    new Regularization2StepDemo().setVisible(1200, 600);
  }
}