// code by jph
package ch.alpine.ascona.util.win;

import java.io.File;
import java.lang.reflect.Constructor;

import ch.alpine.bridge.awt.WindowBounds;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.FieldsEditor;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.bridge.swing.LookAndFeels;
import ch.alpine.tensor.ext.HomeDirectory;

@ReflectionMarker
public abstract class AbstractDemo implements RenderInterface {
  public static void launch() {
    StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
    StackTraceElement stackTraceElement = stackTraceElements[2];
    try {
      String clsName = stackTraceElement.getClassName();
      Class<?> cls = Class.forName(clsName);
      Constructor<?> constructor = cls.getConstructor();
      AbstractDemo abstractDemo = (AbstractDemo) constructor.newInstance();
      LookAndFeels.LIGHT.updateComponentTreeUI();
      File folder = HomeDirectory.file(".config", "ascona", "window");
      folder.mkdirs();
      File file = new File(folder, clsName + "_WindowBounds.properties");
      WindowBounds.persistent(abstractDemo.timerFrame.jFrame, file);
      abstractDemo.timerFrame.jFrame.setVisible(true);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  // ---
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
  @Deprecated
  public final void setVisible(int width, int height) {
    setVisible(100, 100, width, height);
  }

  @Deprecated
  public final void setVisible(int x, int y, int width, int height) {
    timerFrame.jFrame.setBounds(x, y, width, height);
    timerFrame.jFrame.setVisible(true);
  }

  public final void dispose() {
    timerFrame.jFrame.setVisible(false);
    timerFrame.jFrame.dispose();
  }
}
