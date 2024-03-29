// code by jph
package ch.alpine.ascona.util.win;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.ext.ResourceData;

public class BaseFrame {
  private static final int SIZE = 800;
  protected static final String IMAGE_FORMAT = "png";
  // ---
  public final JFrame jFrame = new JFrame();
  private final JPanel jPanel = new JPanel(new BorderLayout());
  public final JToolBar jToolBar = new JToolBar();
  public final GeometricComponent geometricComponent = new GeometricComponent();
  protected final JLabel jStatusLabel = new JLabel();

  protected BaseFrame() {
    jFrame.setSize(SIZE, SIZE);
    jFrame.setLocationRelativeTo(null);
    jFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    jToolBar.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    jToolBar.setFloatable(false);
    {
      JButton jButton = new JButton("save2png");
      try {
        jButton = new JButton(new ImageIcon(ResourceData.bufferedImage("/ch/alpine/ascona/icon/camera.gif")));
      } catch (Exception exception) {
        System.err.println(exception);
      }
      jButton.setToolTipText("snapshot is stored in ~/Pictures/...");
      jButton.addActionListener(actionEvent -> {
        try {
          File file = HomeDirectory.Pictures(String.format("ascona_%d_%s.%s", //
              System.currentTimeMillis(), //
              jFrame.getTitle(), //
              IMAGE_FORMAT));
          ImageIO.write(offscreen(), IMAGE_FORMAT, file);
        } catch (Exception exception) {
          exception.printStackTrace();
        }
      });
      jToolBar.add(jButton);
    }
    jPanel.add(jToolBar, BorderLayout.NORTH);
    jPanel.add(geometricComponent.jComponent, BorderLayout.CENTER);
    jPanel.add(jStatusLabel, BorderLayout.SOUTH);
    jFrame.setContentPane(jPanel);
  }

  public final BufferedImage offscreen() {
    Dimension dimension = geometricComponent.jComponent.getSize();
    BufferedImage bufferedImage = //
        new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = bufferedImage.createGraphics();
    geometricComponent.render(graphics, dimension);
    graphics.dispose();
    return bufferedImage;
  }

  public final void close() {
    jFrame.setVisible(false);
    jFrame.dispose();
  }
}
