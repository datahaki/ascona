// code by jph
package ch.alpine.ascona.dv;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;

import ch.alpine.ascony.api.LogWeightings;
import ch.alpine.ascony.arp.ArrayFunction;
import ch.alpine.ascony.arp.D2Raster;
import ch.alpine.ascony.cls.Classification;
import ch.alpine.ascony.cls.ClassificationImage;
import ch.alpine.ascony.cls.Labels;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.ImageRender;
import ch.alpine.ascony.ren.PointsRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSelectionCallback;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.io.ImageFormat;
import ch.alpine.tensor.pdf.RandomSample;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.d.DiscreteUniformDistribution;

public class ClassificationImageDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param0 extends AsconaParam {
    public Param0() {
      super(false, ManifoldDisplays.d2Rasters());
      manifoldDisplays = ManifoldDisplays.R2;
      drawControlPoints = false;
    }

    @FieldSelectionArray({ "10", "20", "50" })
    public Integer size = 20;
    @FieldSelectionArray({ "2", "3", "4", "5" })
    public Integer labels = 3;
    @FieldFuse
    public transient Boolean shuffle = false;
  }

  @ReflectionMarker
  public static class Param1 {
    @FieldSelectionCallback("biinvariants")
    public Biinvariants biinvariants = Biinvariants.LEVERAGES;
    public Labels labels = Labels.ARG_MIN;
    @FieldSelectionArray({ "50", "75", "100", "200" })
    public Integer res = 50;
    public ColorDataLists cdg = ColorDataLists._097;
    public ClassificationImage classificationImage = ClassificationImage.BLENDED;

    @ReflectionMarker
    public List<Biinvariants> biinvariants() {
      return Biinvariants.FAST;
    }
  }

  private final Param0 param0;
  private final Param1 param1;
  // ---
  protected Tensor vector;
  private BufferedImage bufferedImage;

  public ClassificationImageDemo() {
    this(new Param0(), new Param1());
  }

  public ClassificationImageDemo(Param0 param0, Param1 param1) {
    super(param0, param1);
    this.param0 = param0;
    this.param1 = param1;
    fieldsEditor(0).addUniversalListener(this::shuffle);
    fieldsEditor(1).addUniversalListener(this::recompute);
    {
      timerFrame.geometricComponent.jComponent.addMouseMotionListener(new MouseMotionListener() {
        @Override
        public void mouseMoved(MouseEvent e) {
          if (controlPointsRender.isPositioningOngoing())
            recompute();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
          // ---
        }
      });
    }
    shuffle();
  }

  private void shuffle() {
    int n = param0.size;
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor tensor = Tensor.of(RandomSample.of(manifoldDisplay.randomSampleInterface(), n).stream() //
        .map(manifoldDisplay::point2xya));
    setControlPointsSe2(tensor);
    // assignment of random labels to points
    int k = param0.labels;
    vector = RandomVariate.of(DiscreteUniformDistribution.forArray(k), n);
    recompute();
  }

  private void recompute() {
    // System.out.println("recomp");
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Manifold manifold = (Manifold) manifoldDisplay.geodesicSpace();
    D2Raster d2Raster = (D2Raster) manifoldDisplay;
    Objects.requireNonNull(vector);
    Classification classification = param1.labels.apply(vector);
    Sedarim sedarim = LogWeightings.DISTANCES.sedarim(param1.biinvariants.ofSafe(manifold), null, getGeodesicControlPoints());
    ColorDataLists colorDataLists = param1.cdg;
    TensorUnaryOperator tensorUnaryOperator = //
        param1.classificationImage.operator(classification, sedarim, colorDataLists.cyclic());
    int resolution = param1.res;
    ArrayFunction<Tensor> arrayFunction = new ArrayFunction<>(tensorUnaryOperator, Array.zeros(4));
    Tensor raster = D2Raster.of(d2Raster, resolution, arrayFunction);
    bufferedImage = ImageFormat.of(raster);
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    D2Raster hsArrayPlot = (D2Raster) manifoldDisplay;
    if (Objects.nonNull(bufferedImage))
      new ImageRender(bufferedImage, hsArrayPlot.coordinateBoundingBox()).render(geometricLayer, graphics);
    // ---
    render(geometricLayer, graphics, manifoldDisplay, getGeodesicControlPoints(), vector, param1.cdg.cyclic());
  }

  static void render(GeometricLayer geometricLayer, Graphics2D graphics, ManifoldDisplay manifoldDisplay, Tensor sequence, Tensor vector,
      ColorDataIndexed colorDataIndexedT) {
    Tensor shape = manifoldDisplay.shape().multiply(RealScalar.of(1.0));
    int index = 0;
    ColorDataIndexed colorDataIndexedO = colorDataIndexedT.deriveWithAlpha(128);
    for (Tensor point : sequence) {
      int label = vector.Get(index).number().intValue();
      PointsRender pointsRender = new PointsRender( //
          colorDataIndexedO.getColor(label), //
          colorDataIndexedT.getColor(label));
      pointsRender.show(manifoldDisplay::matrixLift, shape, Tensors.of(point)).render(geometricLayer, graphics);
      ++index;
    }
  }

  static void main() {
    launch();
  }
}
