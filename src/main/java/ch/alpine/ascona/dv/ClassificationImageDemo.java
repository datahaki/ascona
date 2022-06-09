// code by jph
package ch.alpine.ascona.dv;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JButton;

import ch.alpine.ascona.lev.LogWeightingDemo;
import ch.alpine.ascona.util.api.LogWeighting;
import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.ascona.util.arp.HsArrayPlot;
import ch.alpine.ascona.util.arp.HsArrayPlots;
import ch.alpine.ascona.util.cls.Classification;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.ImageRender;
import ch.alpine.ascona.util.ren.PointsRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.swing.SpinnerLabel;
import ch.alpine.sophus.hs.Biinvariant;
import ch.alpine.sophus.hs.Biinvariants;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.hs.MetricBiinvariant;
import ch.alpine.sophus.math.sample.RandomSample;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.io.ImageFormat;
import ch.alpine.tensor.mat.re.Inverse;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.d.DiscreteUniformDistribution;

public class ClassificationImageDemo extends LogWeightingDemo implements ActionListener {
  private static final int REFINEMENT = 160;
  private static final Random RANDOM = new Random();

  private static List<Biinvariant> distinct() {
    return Arrays.asList( //
        MetricBiinvariant.EUCLIDEAN, // FIXME ASCONA ALG should be retrieved from bitype
        Biinvariants.LEVERAGES, //
        Biinvariants.GARDEN);
  }

  // ---
  private final SpinnerLabel<ColorDataLists> spinnerColor = SpinnerLabel.of(ColorDataLists.class);
  private final SpinnerLabel<Integer> spinnerLabel = SpinnerLabel.of(2, 3, 4, 5);
  private final SpinnerLabel<Integer> spinnerCount = SpinnerLabel.of(5, 10, 15, 20, 25, 30, 40);
  private final SpinnerLabel<Integer> spinnerRes = SpinnerLabel.of(25, 40, 50, 75, 100, 150, 200, 250);
  private final JButton jButtonShuffle = new JButton("shuffle");
  private final SpinnerLabel<Labels> spinnerLabels = SpinnerLabel.of(Labels.class);
  private final SpinnerLabel<ClassificationImage> spinnerImage = SpinnerLabel.of(ClassificationImage.class);
  private final JButton jButtonExport = new JButton("export");
  // ---
  protected Tensor vector;

  public ClassificationImageDemo() {
    super(false, ManifoldDisplays.ARRAYS, LogWeightings.list());
    setMidpointIndicated(false);
    addSpinnerListener(v -> shuffle(spinnerCount.getValue()));
    {
      spinnerLogWeighting.addSpinnerListener(logWeighting -> {
        if (logWeighting.equals(LogWeightings.DISTANCES))
          spinnerLabels.setValue(Labels.ARG_MIN);
        else
          if ( //
          logWeighting.equals(LogWeightings.WEIGHTING) || //
              logWeighting.equals(LogWeightings.COORDINATE))
            spinnerLabels.setValue(Labels.ARG_MAX);
      });
      spinnerLogWeighting.addSpinnerListener(v -> recompute());
    }
    {
      spinnerColor.addSpinnerListener(v -> recompute());
      spinnerColor.addToComponentReduced(timerFrame.jToolBar, new Dimension(60, 28), "color data lists");
    }
    {
      // spinnerLabel.setList(Arrays.asList());
      spinnerLabel.setValue(3);
      spinnerLabel.addToComponentReduced(timerFrame.jToolBar, new Dimension(50, 28), "label count");
      spinnerLabel.addSpinnerListener(v -> shuffle(spinnerCount.getValue()));
    }
    {
      spinnerCount.setValue(20);
      spinnerCount.addToComponentReduced(timerFrame.jToolBar, new Dimension(60, 28), "landmark count");
      spinnerCount.addSpinnerListener(this::shuffle);
    }
    {
      spinnerRes.setValue(50);
      spinnerRes.addToComponentReduced(timerFrame.jToolBar, new Dimension(60, 28), "resolution");
      spinnerRes.addSpinnerListener(v -> recompute());
    }
    {
      jButtonShuffle.addActionListener(e -> shuffle(spinnerCount.getValue()));
      timerFrame.jToolBar.add(jButtonShuffle);
    }
    spinnerLabels.addSpinnerListener(v -> recompute());
    System.out.println("here");
    spinnerLogWeighting.setValue(LogWeightings.DISTANCES);
    shuffle(spinnerCount.getValue());
    spinnerLabels.addToComponentReduced(timerFrame.jToolBar, new Dimension(100, 28), "label");
    spinnerImage.addToComponentReduced(timerFrame.jToolBar, new Dimension(120, 28), "image");
    spinnerImage.addSpinnerListener(v -> recompute());
    {
      jButtonExport.addActionListener(this);
      timerFrame.jToolBar.add(jButtonExport);
    }
    {
      timerFrame.geometricComponent.jComponent.addMouseMotionListener(new MouseMotionListener() {
        @Override
        public void mouseMoved(MouseEvent e) {
          if (isPositioningOngoing())
            recompute();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
          // ---
        }
      });
    }
  }

  private BufferedImage bufferedImage;

  final void shuffle(int n) {
    System.out.println("shuffle");
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor tensor = Tensor.of(RandomSample.of(manifoldDisplay.randomSampleInterface(), n).stream() //
        .map(manifoldDisplay::lift));
    setControlPointsSe2(tensor);
    // assignment of random labels to points
    vector = RandomVariate.of(DiscreteUniformDistribution.of(0, spinnerLabel.getValue()), RANDOM, n);
    recompute();
  }

  @Override
  public void recompute() {
    System.out.println("recomp");
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    HsArrayPlot geodesicArrayPlot = manifoldDisplay.arrayPlot();
    Labels labels = Objects.requireNonNull(spinnerLabels.getValue());
    Objects.requireNonNull(vector);
    Classification classification = labels.apply(vector);
    TensorUnaryOperator operator = operator(getGeodesicControlPoints());
    ColorDataLists colorDataLists = spinnerColor.getValue();
    TensorUnaryOperator tensorUnaryOperator = //
        spinnerImage.getValue().operator(classification, operator, colorDataLists.cyclic());
    int resolution = spinnerRes.getValue();
    bufferedImage = ImageFormat.of(geodesicArrayPlot.raster(resolution, tensorUnaryOperator, Array.zeros(4)));
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    if (Objects.nonNull(bufferedImage)) {
      // Tensor pixel2model = HsArrayPlots.pixel2model( //
      // manifoldDisplay.coordinateBoundingBox(), //
      // new Dimension(bufferedImage.getWidth(), bufferedImage.getHeight()));
      // ImageRender.of(bufferedImage, pixel2model).render(geometricLayer, graphics);
      new ImageRender(bufferedImage, manifoldDisplay.coordinateBoundingBox()).render(geometricLayer, graphics);
    }
    // ---
    render(geometricLayer, graphics, manifoldDisplay, getGeodesicControlPoints(), vector, spinnerColor.getValue().cyclic());
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

  @Override
  public final void actionPerformed(ActionEvent actionEvent) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    LogWeighting logWeighting = logWeighting();
    File root = HomeDirectory.Pictures( //
        getClass().getSimpleName(), //
        manifoldDisplay.toString());
    root.mkdirs();
    HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
    for (Biinvariant biinvariant : distinct()) {
      Tensor sequence = getGeodesicControlPoints();
      TensorUnaryOperator operator = logWeighting.operator( //
          biinvariant, //
          homogeneousSpace, //
          variogram(), //
          sequence);
      System.out.print("computing " + biinvariant);
      HsArrayPlot hsArrayPlot = manifoldDisplay.arrayPlot();
      Classification classification = spinnerLabels.getValue().apply(vector);
      ColorDataLists colorDataLists = spinnerColor.getValue();
      ColorDataIndexed colorDataIndexed = colorDataLists.strict();
      TensorUnaryOperator tensorUnaryOperator = //
          spinnerImage.getValue().operator(classification, operator, colorDataIndexed);
      int resolution = REFINEMENT;
      BufferedImage bufferedImage = //
          ImageFormat.of(hsArrayPlot.raster(resolution, tensorUnaryOperator, Array.zeros(4)));
      {
        Tensor matrix = HsArrayPlots.pixel2model( //
            manifoldDisplay.coordinateBoundingBox(), //
            new Dimension(resolution, resolution));
        GeometricLayer geometricLayer = new GeometricLayer(Inverse.of(matrix));
        Graphics2D graphics = bufferedImage.createGraphics();
        RenderQuality.setQuality(graphics);
        render(geometricLayer, graphics, manifoldDisplay, sequence, vector, colorDataIndexed);
      }
      // ---
      String format = String.format("%s_%s.png", logWeighting, biinvariant);
      try {
        ImageIO.write(bufferedImage, "png", new File(root, format));
      } catch (Exception exception) {
        exception.printStackTrace();
      }
      System.out.println(" done");
    }
    System.out.println("all done");
  }

  public static void main(String[] args) {
    new ClassificationImageDemo().setVisible(1300, 900);
  }
}
