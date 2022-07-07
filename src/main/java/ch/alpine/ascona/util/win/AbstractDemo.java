// code by jph
package ch.alpine.ascona.util.win;

import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.FieldsEditor;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;

@ReflectionMarker
public abstract class AbstractDemo implements RenderInterface {
  public final TimerFrame timerFrame = new TimerFrame();
  private final Object object;
  protected final FieldsEditor fieldsEditor;

  /** @param object may be null */
  public AbstractDemo(Object object) {
    this.object = object;
    timerFrame.jFrame.setTitle(getClass().getSimpleName());
    fieldsEditor = ToolbarFieldsEditor.add(object, timerFrame.jToolBar);
    timerFrame.geometricComponent.addRenderInterface(this);
  }

  @Deprecated
  public AbstractDemo() {
    this(null);
  }

  public Object object() {
    return object;
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
