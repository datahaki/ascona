// code by jph
package ch.alpine.ascona.util.api;

import java.util.function.Supplier;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.win.GeometricComponent;

public enum ControlPointsRenders {
  ;
  public static ControlPointsRender create(AsconaParam asconaParam, Supplier<ManifoldDisplay> supplier, GeometricComponent geometricComponent) {
    ControlPointsRender controlPointsRender = new ControlPointsRender(asconaParam, supplier, //
        geometricComponent::getMouseSe2CState, //
        geometricComponent::getModel2Pixel);
    geometricComponent.jComponent.addMouseListener(controlPointsRender.mouseAdapter);
    geometricComponent.jComponent.addMouseMotionListener(controlPointsRender.mouseAdapter);
    geometricComponent.addRenderInterface(controlPointsRender);
    return controlPointsRender;
  }
}
