// code by jph
package ch.alpine.ascona.misc;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

import ch.alpine.ascona.util.ren.RenderInterface;
import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.hs.sn.SnRandomSample;
import ch.alpine.sophus.math.sample.RandomSample;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.ConstantArray;
import ch.alpine.tensor.ext.BoundedLinkedList;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.img.ColorFormat;
import ch.alpine.tensor.lie.TensorWedge;
import ch.alpine.tensor.mat.ex.MatrixExp;
import ch.alpine.tensor.sca.Mod;

// TODO ASCONA REV add more parameters
public class SnRotationDemo extends AbstractDemo {
  private static class SnRotationChunk implements RenderInterface {
    private final ColorDataGradient colorDataGradient;
    private final BoundedLinkedList<Tensor> boundedLinkedList;
    private final Tensor rotation;
    private Tensor samples;

    public SnRotationChunk(int dimension, int numel, int max_size, double speed, ColorDataGradient colorDataGradient) {
      this.colorDataGradient = colorDataGradient;
      boundedLinkedList = new BoundedLinkedList<>(max_size);
      RandomSampleInterface randomSampleInterface = SnRandomSample.of(dimension);
      samples = RandomSample.of(randomSampleInterface, numel);
      Tensor angle = RandomSample.of(randomSampleInterface).multiply(RealScalar.of(speed));
      rotation = MatrixExp.of(TensorWedge.of(angle, ConstantArray.of(RealScalar.ONE, dimension + 1)));
    }

    public void integrate() {
      samples = samples.dot(rotation);
      boundedLinkedList.add(samples);
    }

    @Override
    public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
      for (int count = 0; count < samples.length(); ++count) {
        Tensor rgba = colorDataGradient.apply(Mod.function(1).apply(RationalScalar.of(count, 20)));
        Color color = ColorFormat.toColor(rgba);
        int fi = count;
        Tensor trace = Tensor.of(boundedLinkedList.stream().map(p -> p.get(fi)));
        Path2D path2d = geometricLayer.toPath2D(trace);
        graphics.setColor(color);
        graphics.draw(path2d);
      }
    }
  }

  @ReflectionMarker
  public static class Param {
    // TODO ASCONA shuffle not effective
    @FieldFuse
    public transient Boolean shuffle = false;
  }

  private final Param param;
  private final List<SnRotationChunk> list = new ArrayList<>();

  public SnRotationDemo() {
    this(new Param());
  }

  public SnRotationDemo(Param param) {
    super(param);
    this.param = param;
    list.add(new SnRotationChunk(3, 200, 3, 0.1, ColorDataGradients.PARULA.deriveWithOpacity(RealScalar.of(0.3))));
    // list.add(new SnRotationChunk(3, 50, 20, 0.02, ColorDataGradients.SOLAR.deriveWithOpacity(RealScalar.of(0.5))));
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    // graphics.setColor(new Color(128, 128, 128, 64));
    list.forEach(SnRotationChunk::integrate);
    graphics.setStroke(new BasicStroke(1.5f));
    list.get(0).render(geometricLayer, graphics);
    // graphics.setStroke(new BasicStroke(2.5f));
    // list.get(1).render(geometricLayer, graphics);
  }

  public static void main(String[] args) {
    launch();
  }
}
