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
import java.util.Map;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.swing.JButton;

import ch.alpine.ascona.lev.LogWeightingDemo;
import ch.alpine.ascona.util.api.LogWeighting;
import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.ascona.util.arp.ArrayFunction;
import ch.alpine.ascona.util.arp.D2Raster;
import ch.alpine.ascona.util.cls.Classification;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.ImageRender;
import ch.alpine.ascona.util.ren.PointsRender;
import ch.alpine.ascona.util.win.LookAndFeels;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.swing.SpinnerLabel;
import ch.alpine.sophus.dv.Biinvariant;
import ch.alpine.sophus.dv.Biinvariants;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.hs.Sedarim;
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
    super(false, ManifoldDisplays.RASTERS, LogWeightings.list());
    addManifoldListener(v -> shuffle(spinnerCount.getValue()));
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
      spinnerColor.setValue(ColorDataLists._097);
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
    spinnerLabels.setValue(Labels.ARG_MIN);
    spinnerLabels.addSpinnerListener(v -> recompute());
    spinnerLogWeighting.setValue(LogWeightings.DISTANCES);
    spinnerLabels.addToComponentReduced(timerFrame.jToolBar, new Dimension(100, 28), "label");
    spinnerImage.setValue(ClassificationImage.BLENDED);
    spinnerImage.addToComponentReduced(timerFrame.jToolBar, new Dimension(120, 28), "image");
    spinnerImage.addSpinnerListener(v -> recompute());
    System.out.println("here");
    shuffle(spinnerCount.getValue());
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
        .map(manifoldDisplay::unproject));
    setControlPointsSe2(tensor);
    // assignment of random labels to points
    vector = RandomVariate.of(DiscreteUniformDistribution.of(0, spinnerLabel.getValue()), n);
    recompute();
  }

  @Override
  public void recompute() {
    System.out.println("recomp");
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    D2Raster hsArrayPlot = (D2Raster) manifoldDisplay;
    Labels labels = Objects.requireNonNull(spinnerLabels.getValue());
    Objects.requireNonNull(vector);
    Classification classification = labels.apply(vector);
    Sedarim sedarim = operator(getGeodesicControlPoints());
    ColorDataLists colorDataLists = spinnerColor.getValue();
    TensorUnaryOperator tensorUnaryOperator = //
        spinnerImage.getValue().operator(classification, sedarim::sunder, colorDataLists.cyclic());
    int resolution = spinnerRes.getValue();
    ArrayFunction<Tensor> arrayFunction = new ArrayFunction<>(tensorUnaryOperator, Array.zeros(4));
    Tensor raster = D2Raster.of(hsArrayPlot, resolution, arrayFunction);
    bufferedImage = ImageFormat.of(raster);
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    D2Raster hsArrayPlot = (D2Raster) manifoldDisplay;
    if (Objects.nonNull(bufferedImage)) {
      // Tensor pixel2model = HsArrayPlots.pixel2model( //
      // manifoldDisplay.coordinateBoundingBox(), //
      // new Dimension(bufferedImage.getWidth(), bufferedImage.getHeight()));
      // ImageRender.of(bufferedImage, pixel2model).render(geometricLayer, graphics);
      new ImageRender(bufferedImage, hsArrayPlot.coordinateBoundingBox()).render(geometricLayer, graphics);
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
    Map<Biinvariants, Biinvariant> map = Biinvariants.all(homogeneousSpace);
    for (Biinvariant biinvariant : map.values()) {
      Tensor sequence = getGeodesicControlPoints();
      Sedarim sedarim = logWeighting.sedarim( //
          biinvariant, //
          variogram(), //
          sequence);
      System.out.print("computing " + biinvariant);
      D2Raster hsArrayPlot = (D2Raster) manifoldDisplay;
      Classification classification = spinnerLabels.getValue().apply(vector);
      ColorDataLists colorDataLists = spinnerColor.getValue();
      ColorDataIndexed colorDataIndexed = colorDataLists.strict();
      TensorUnaryOperator tensorUnaryOperator = //
          spinnerImage.getValue().operator(classification, sedarim::sunder, colorDataIndexed);
      int resolution = REFINEMENT;
      ArrayFunction<Tensor> arrayFunction = new ArrayFunction<>(tensorUnaryOperator, Array.zeros(4));
      Tensor raster = D2Raster.of(hsArrayPlot, resolution, arrayFunction);
      BufferedImage bufferedImage = ImageFormat.of(raster);
      {
        Tensor matrix = ImageRender.pixel2model(hsArrayPlot.coordinateBoundingBox(), resolution, resolution);
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
    LookAndFeels.LIGHT.tryUpdateUI();
    new ClassificationImageDemo().setVisible(1300, 900);
  }
}
