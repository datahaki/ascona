// code by jph
package ch.alpine.ascona.misc;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.util.Collection;

import ch.alpine.ascona.ref.ShuffleFuse;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.dis.S2Display;
import ch.alpine.ascony.win.ManifoldDisplayDemo;
import ch.alpine.bridge.gfx.GeometricComponent;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.PvmBuilder;
import ch.alpine.bridge.gfx.RenderInterface;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.hs.s.Sphere;
import ch.alpine.tensor.Rational;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.ConstantArray;
import ch.alpine.tensor.ext.BoundedLinkedList;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.img.ColorFormat;
import ch.alpine.tensor.lie.TensorWedge;
import ch.alpine.tensor.mat.ex.MatrixExp;
import ch.alpine.tensor.pdf.RandomSample;
import ch.alpine.tensor.pdf.RandomSampleInterface;

class SnRotationDemo extends ManifoldDisplayDemo implements RenderInterface {
  @ReflectionMarker
  static class Param {
    @FieldSelectionArray({ "2", "3", "4", "5", "6", "7", "8", "10", "15", "20" })
    public Integer dims = 3;
    @FieldSelectionArray({ "10", "100", "200", "300", "500" })
    public Integer numel = 200;
    @FieldSelectionArray({ "3", "4", "5", "10", "20", "50", "200" })
    public Integer max_size = 3;
    @FieldSelectionArray({ "0.005", "0.01", "0.02", "0.03" })
    public Scalar speed = RealScalar.of(0.01);
    public final ShuffleFuse shuffleFuse = new ShuffleFuse();

    public SnRotationChunk create(Paran paran) {
      return new SnRotationChunk(dims, numel, max_size, speed, paran);
    }
  }

  @ReflectionMarker
  static class Paran {
    public ColorDataGradients cdg = ColorDataGradients.PARULA;
  }

  private static class SnRotationChunk implements RenderInterface {
    private final BoundedLinkedList<Tensor> boundedLinkedList;
    private final Tensor rotation;
    private final Paran paran;
    private Tensor samples;

    public SnRotationChunk(int dimension, int numel, int max_size, Scalar speed, Paran paran) {
      boundedLinkedList = new BoundedLinkedList<>(max_size);
      RandomSampleInterface randomSampleInterface = new Sphere(dimension).randomSampleInterface();
      samples = RandomSample.of(randomSampleInterface, numel);
      Tensor angle = RandomSample.of(randomSampleInterface).multiply(speed);
      rotation = MatrixExp.of(TensorWedge.of(angle, ConstantArray.of(RealScalar.ONE, dimension + 1)));
      this.paran = paran;
    }

    public void integrate() {
      samples = samples.dot(rotation);
      boundedLinkedList.add(samples);
    }

    @Override
    public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
      for (int count = 0; count < samples.length(); ++count) {
        Tensor rgba = paran.cdg.apply(Rational.of(count, samples.length()));
        int fi = count;
        Tensor trace = Tensor.of(boundedLinkedList.stream().map(p -> p.get(fi)));
        graphics.setColor(ColorFormat.toColor(rgba));
        graphics.draw(geometricLayer.toPath2D(trace));
      }
    }
  }

  private final Param param;
  private final Paran paran;
  private SnRotationChunk snRotationChunk = null;

  public SnRotationDemo() {
    super(param = new Param(), paran = new Paran());
    fieldsEditor(param).addUniversalListener(this::update);
    update();
    GeometricComponent geometricComponent = geometricComponent();
    geometricComponent.addRenderInterfaceBackground(S2Display.INSTANCE.background());
    geometricComponent.addRenderInterface(this);
    Tensor pvm = PvmBuilder.rhs().setOffset(400, 400).setPerPixel(400).digest();
    geometricComponent.setModel2Pixel(pvm);
  }

  @Override
  protected Collection<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.S2_ONLY;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    snRotationChunk.integrate();
    graphics.setStroke(new BasicStroke(1.5f));
    snRotationChunk.render(geometricLayer, graphics);
  }

  private void update() {
    snRotationChunk = param.create(paran);
  }

  static void main() {
    new SnRotationDemo().runStandalone();
  }
}
