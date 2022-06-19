// code by jph
package ch.alpine.ascona.gbc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JButton;

import ch.alpine.ascona.gbc.d2.AbstractScatteredSetWeightingDemo;
import ch.alpine.ascona.util.api.LogWeighting;
import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.ascona.util.arp.ArrayFunction;
import ch.alpine.ascona.util.arp.ArrayPlotRender;
import ch.alpine.ascona.util.arp.D2Raster;
import ch.alpine.ascona.util.arp.ImageTiling;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.sophus.dv.Biinvariant;
import ch.alpine.sophus.dv.Biinvariants;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.ConstantArray;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.ext.HomeDirectory;

public abstract class AbstractExportWeightingDemo extends AbstractScatteredSetWeightingDemo implements ActionListener {
  private static final int REFINEMENT = 120; // presentation 60
  private final JButton jButtonExport = new JButton("export");

  public AbstractExportWeightingDemo( //
      boolean addRemoveControlPoints, List<ManifoldDisplay> list, List<LogWeighting> array) {
    super(addRemoveControlPoints, list, array);
    {
      jButtonExport.addActionListener(this);
      timerFrame.jToolBar.add(jButtonExport);
    }
  }

  @Override
  public void actionPerformed(ActionEvent actionEvent) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    LogWeighting logWeighting = logWeighting();
    File root = HomeDirectory.Pictures( //
        getClass().getSimpleName(), //
        manifoldDisplay.toString(), //
        logWeighting.toString());
    root.mkdirs();
    HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
    Map<Biinvariants, Biinvariant> map = Biinvariants.all(homogeneousSpace);
    for (Biinvariant biinvariant : map.values()) {
      Tensor sequence = getGeodesicControlPoints();
      Sedarim sedarim = logWeighting.sedarim(biinvariant, variogram(), sequence);
      System.out.print("computing " + biinvariant);
      // ---
      ArrayPlotRender arrayPlotRender = arrayPlotRender(sequence, REFINEMENT, sedarim::sunder, 1);
      BufferedImage bufferedImage = arrayPlotRender.export();
      try {
        ImageIO.write(bufferedImage, "png", new File(root, biinvariant.toString() + ".png"));
      } catch (Exception exception) {
        exception.printStackTrace();
      }
      System.out.println(" done");
    }
    System.out.println("all done");
  }

  protected final ArrayPlotRender arrayPlotRender(Tensor sequence, int refinement, TensorUnaryOperator tensorUnaryOperator, int magnification) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    D2Raster hsArrayPlot = (D2Raster) manifoldDisplay;
    Tensor fallback = ConstantArray.of(DoubleScalar.INDETERMINATE, sequence.length());
    ArrayFunction<Tensor> arrayFunction = new ArrayFunction<>(tensorUnaryOperator, fallback);
    Tensor wgs = D2Raster.of(hsArrayPlot, refinement, arrayFunction);
    return ArrayPlotRender.rescale(ImageTiling.of(wgs), colorDataGradient(), magnification, logWeighting().equals(LogWeightings.DISTANCES));
  }
}
