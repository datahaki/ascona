// code by jph
package ch.alpine.ascona.util;

import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.bridge.lang.ClassDiscovery;
import ch.alpine.bridge.lang.ClassPaths;
import ch.alpine.bridge.lang.ClassVisitor;

class AbstractDemoTest {
  @Test
  public void testDiscovery() {
    List<Class<?>> list = new LinkedList<>();
    ClassDiscovery.execute(ClassPaths.getDefault(), new ClassVisitor() {
      @Override
      public void accept(String jarfile, Class<?> cls) {
        if (AbstractDemo.class.isAssignableFrom(cls) && !Modifier.isAbstract(cls.getModifiers())) {
          if (Modifier.isPublic(cls.getModifiers())) {
            Constructor<?> constructor = null;
            try {
              constructor = cls.getDeclaredConstructor();
            } catch (Exception e) {
              System.out.println("no default constructor: " + cls.getSimpleName());
            }
            if (Objects.nonNull(constructor)) {
              AbstractDemo abstractDemo = null;
              try {
                abstractDemo = (AbstractDemo) constructor.newInstance();
              } catch (Exception e) {
                System.out.println("new instance fail: " + cls.getSimpleName());
              }
              if (Objects.nonNull(abstractDemo))
                try {
                  AbstractDemoHelper.offscreen(abstractDemo);
                } catch (Exception e) {
                  System.out.println("offscreen fail: " + cls.getSimpleName());
                  list.add(cls);
                }
            }
          } else
            System.err.println("abstract demo non public: " + cls.getSimpleName());
          //
        }
      }
    });
    if (!list.isEmpty()) {
      System.out.println("List of failed AbstractDemos:");
      list.forEach(System.out::println);
      fail();
    }
  }
}
