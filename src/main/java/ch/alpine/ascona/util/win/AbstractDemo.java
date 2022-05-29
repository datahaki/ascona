// code by jph
package ch.alpine.ascona.util.win;

import ch.alpine.bridge.ref.ann.ReflectionMarker;

@ReflectionMarker
public abstract class AbstractDemo implements RenderInterface {
  public final TimerFrame timerFrame = new TimerFrame();

  public AbstractDemo() {
    timerFrame.jFrame.setTitle(getClass().getSimpleName());
    timerFrame.geometricComponent.addRenderInterface(this);
  }

  /** @param width
   * @param height */
  public final void setVisible(int width, int height) {
    setVisible(100, 100, width, height);
  }

  public final void setVisible(int x, int y, int width, int height) {
    timerFrame.jFrame.setBounds(x, y, width, height);
    timerFrame.jFrame.setVisible(true);
  }

  public final void dispose() {
    timerFrame.jFrame.setVisible(false);
    timerFrame.jFrame.dispose();
  }
}
