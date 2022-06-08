// code by jph
package ch.alpine.ascona.dv;

import java.awt.GridLayout;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import ch.alpine.ascona.flt.BiinvariantMeanDemo;
import ch.alpine.ascona.flt.GeodesicFiltersDatasetDemo;
import ch.alpine.ascona.gbc.d2.S2DeformationDemo;
import ch.alpine.ascona.ref.d1.CurveSubdivisionDemo;
import ch.alpine.ascona.ref.d2.SurfaceMeshDemo;
import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.ascona.util.win.LookAndFeels;

public class CsfWorkshop {
  private final JFrame jFrame = new JFrame();
  private final List<Class<? extends AbstractDemo>> list = Arrays.asList( //
      BiinvariantMeanDemo.class, //
      GeodesicFiltersDatasetDemo.class, //
      CurveSubdivisionDemo.class, //
      SurfaceMeshDemo.class, //
      ClassificationDemo.class, //
      S2DeformationDemo.class //
  );
  private final JPanel jPanel = new JPanel(new GridLayout(list.size(), 1));
  private AbstractDemo abstractDemo = null;

  public CsfWorkshop() {
    for (Class<? extends AbstractDemo> cls : list) {
      JButton jButton = new JButton(cls.getSimpleName());
      jButton.addActionListener(e -> {
        if (Objects.nonNull(abstractDemo))
          abstractDemo.dispose();
        try {
          abstractDemo = cls.getConstructor().newInstance();
          abstractDemo.setVisible(200, 40, 1000, 1000);
        } catch (Exception e1) {
          e1.printStackTrace();
        }
      });
      jPanel.add(jButton);
    }
    jFrame.setContentPane(new JScrollPane(jPanel));
    jFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    jFrame.setBounds(0, 40, 200, 600);
    jFrame.setVisible(true);
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.updateUI();
    new CsfWorkshop();
  }
}
