// code by jph
package ch.alpine.ascona.util.win;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.io.AnimationWriter;
import ch.alpine.tensor.io.GifAnimationWriter;
import ch.alpine.tensor.io.ResourceData;

public class TimerFrame extends BaseFrame {
  protected static final String VIDEO_FORMAT = "gif";
  // ---
  protected final Timer timer = new Timer();
  AnimationWriter _animationWriter = null;

  /** frame with repaint rate of 20[Hz] */
  public TimerFrame() {
    this(50, TimeUnit.MILLISECONDS);
  }

  /** @param period between repaint invocations */
  public TimerFrame(int period, TimeUnit timeUnit) {
    {
      JToggleButton _jToggleButton = new JToggleButton("save2gif");
      try {
        _jToggleButton = new JToggleButton(new ImageIcon(ResourceData.bufferedImage("/ch/alpine/ascona/icon/camera.gif")));
      } catch (Exception exception) {
        System.err.println(exception);
      }
      JToggleButton jToggleButton = _jToggleButton;
      jToggleButton.setToolTipText("animation is stored in ~/Videos/...");
      jToggleButton.addActionListener(actionEvent -> {
        if (jToggleButton.isSelected() && _animationWriter == null) {
          try {
            File file = HomeDirectory.Videos(String.format("ascona_%d.%s", System.currentTimeMillis(), VIDEO_FORMAT));
            _animationWriter = new GifAnimationWriter(file, 100, TimeUnit.MILLISECONDS);
          } catch (IOException e) {
            e.printStackTrace();
            jToggleButton.setSelected(false);
          }
        }
        if (!jToggleButton.isSelected() && _animationWriter != null) {
          synchronized (_animationWriter) {
            try {
              _animationWriter.close();
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
          _animationWriter = null;
        }
      });
      jToolBar.add(jToggleButton);
    }
    jToolBar.addSeparator();
    { // periodic task for rendering
      TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
          geometricComponent.jComponent.repaint();
        }
      };
      timer.schedule(timerTask, 100, TimeUnit.MILLISECONDS.convert(period, timeUnit));
    }
    { // periodic task for rendering
      TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
          AnimationWriter animationWriter = _animationWriter;
          if (Objects.nonNull(animationWriter)) {
            synchronized (animationWriter) {
              try {
                System.out.println("record");
                animationWriter.write(offscreen());
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }
        }
      };
      timer.schedule(timerTask, 100, TimeUnit.MILLISECONDS.convert(period, timeUnit));
    }
    jFrame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent windowEvent) {
        timer.cancel();
      }
    });
  }
}
