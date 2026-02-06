// code by jph
package ch.alpine.ascona.misc;

import java.awt.GridLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import ch.alpine.ascona.dv.ClassificationDemo;
import ch.alpine.ascona.dv.ClassificationImageDemo;
import ch.alpine.ascona.dv.GrassmannDemo;
import ch.alpine.ascona.dv.OrderingDemo;
import ch.alpine.ascona.flt.GeodesicFiltersDatasetDemo;
import ch.alpine.ascona.gbc.d2.D2AveragingDemo;
import ch.alpine.ascona.gbc.d2.R2DeformationDemo;
import ch.alpine.ascona.gbc.d2.S2DeformationDemo;
import ch.alpine.ascona.lev.Se2AnimationDemo;
import ch.alpine.ascona.ref.d1.CurveSubdivisionDemo;
import ch.alpine.ascony.win.AbstractDemo;
import ch.alpine.bridge.awt.WindowBounds;
import ch.alpine.bridge.ref.util.ReflectionMarkers;
import ch.alpine.bridge.swing.LookAndFeels;

public enum Marrakesh {
  ;
  static void main() {
    ReflectionMarkers.INSTANCE.DEBUG_PRINT.set(true);
    LookAndFeels.LIGHT.updateComponentTreeUI();
    // ---
    List<Class<? extends AbstractDemo>> list = List.of( //
        BiinvariantMeanDemo.class, //
        GeodesicFiltersDatasetDemo.class, //
        CurveSubdivisionDemo.class, //
        D2AveragingDemo.class, //
        ClassificationImageDemo.class, //
        ClassificationDemo.class, //
        OrderingDemo.class, //
        GrassmannDemo.class, //
        Se2AnimationDemo.class, //
        R2DeformationDemo.class, //
        S2DeformationDemo.class //
    );
    JFrame jFrame = new JFrame();
    JPanel jPanel = new JPanel(new GridLayout(list.size(), 1));
    {
      for (Class<? extends AbstractDemo> cls : list) {
        JButton jButton = new JButton(cls.getSimpleName());
        jButton.addActionListener(_ -> AbstractDemo.run(cls));
        jPanel.add(jButton);
      }
    }
    jFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    jFrame.setContentPane(new JScrollPane(jPanel));
    WindowBounds.persistent(jFrame, AbstractDemo.WINDOW.properties(Marrakesh.class));
    jFrame.setVisible(true);
  }
}
