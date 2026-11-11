// code by jph
package ch.alpine.ascona.usr;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class WindowBoundsExample {
  private static final Preferences PREFS = Preferences.userNodeForPackage(WindowBoundsExample.class);
  private static final int DEFAULT_WIDTH = 900;
  private static final int DEFAULT_HEIGHT = 600;
  private static final String KEY_X = "window.x";
  private static final String KEY_Y = "window.y";
  private static final String KEY_WIDTH = "window.width";
  private static final String KEY_HEIGHT = "window.height";

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      JFrame frame = new JFrame("Window Bounds Example");
      frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      frame.setMinimumSize(new Dimension(300, 200));
      frame.add(new JLabel("Window bounds are saved and restored.", SwingConstants.CENTER));
      // Restore the previous position and size.
      Rectangle bounds = loadWindowBounds();
      // Ensure the window fits within a usable screen area.
      bounds = fitOnScreen(bounds);
      frame.setBounds(bounds);
      // Save normal (non-maximized) window bounds.
      frame.addComponentListener(new ComponentAdapter() {
        private void saveIfNormal() {
          if ((frame.getExtendedState() & Frame.MAXIMIZED_BOTH) == 0 && frame.isShowing()) {
            saveWindowBounds(frame.getBounds());
          }
        }

        @Override
        public void componentMoved(ComponentEvent e) {
          saveIfNormal();
        }

        @Override
        public void componentResized(ComponentEvent e) {
          saveIfNormal();
        }
      });
      frame.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
          if ((frame.getExtendedState() & Frame.MAXIMIZED_BOTH) == 0) {
            saveWindowBounds(frame.getBounds());
          }
          frame.dispose();
        }
      });
      frame.setVisible(true);
    });
  }

  private static Rectangle loadWindowBounds() {
    int x = PREFS.getInt(KEY_X, Integer.MIN_VALUE);
    int y = PREFS.getInt(KEY_Y, Integer.MIN_VALUE);
    int width = PREFS.getInt(KEY_WIDTH, DEFAULT_WIDTH);
    int height = PREFS.getInt(KEY_HEIGHT, DEFAULT_HEIGHT);
    // No saved position: use a default centered on the primary screen.
    if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE) {
      Rectangle usable = getUsableBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());
      width = Math.min(DEFAULT_WIDTH, usable.width);
      height = Math.min(DEFAULT_HEIGHT, usable.height);
      x = usable.x + (usable.width - width) / 2;
      y = usable.y + (usable.height - height) / 2;
      return new Rectangle(x, y, width, height);
    }
    // Reject invalid dimensions.
    if (width <= 0 || height <= 0) {
      width = DEFAULT_WIDTH;
      height = DEFAULT_HEIGHT;
    }
    return new Rectangle(x, y, width, height);
  }

  private static void saveWindowBounds(Rectangle bounds) {
    PREFS.putInt(KEY_X, bounds.x);
    PREFS.putInt(KEY_Y, bounds.y);
    PREFS.putInt(KEY_WIDTH, bounds.width);
    PREFS.putInt(KEY_HEIGHT, bounds.height);
  }

  private static Rectangle fitOnScreen(Rectangle saved) {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] devices = ge.getScreenDevices();
    Rectangle bestUsable = null;
    long bestIntersection = -1;
    // Prefer the screen containing the largest part of the old window.
    for (GraphicsDevice device : devices) {
      GraphicsConfiguration gc = device.getDefaultConfiguration();
      Rectangle usable = getUsableBounds(gc);
      Rectangle intersection = saved.intersection(usable);
      long area = intersection.isEmpty() ? 0 : (long) intersection.width * intersection.height;
      if (area > bestIntersection) {
        bestIntersection = area;
        bestUsable = usable;
      }
    }
    // Fallback if screen detection produces no usable bounds.
    if (bestUsable == null) {
      bestUsable = ge.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
    }
    // Shrink the window if the saved size exceeds the usable area.
    int width = Math.min(saved.width, bestUsable.width);
    int height = Math.min(saved.height, bestUsable.height);
    // Clamp the position so all four edges remain visible.
    int x = Math.max(bestUsable.x, Math.min(saved.x, bestUsable.x + bestUsable.width - width));
    int y = Math.max(bestUsable.y, Math.min(saved.y, bestUsable.y + bestUsable.height - height));
    return new Rectangle(x, y, width, height);
  }

  private static Rectangle getUsableBounds(GraphicsConfiguration gc) {
    Rectangle screen = gc.getBounds();
    Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
    return new Rectangle(screen.x + insets.left, screen.y + insets.top, Math.max(1, screen.width - insets.left - insets.right),
        Math.max(1, screen.height - insets.top - insets.bottom));
  }
}
