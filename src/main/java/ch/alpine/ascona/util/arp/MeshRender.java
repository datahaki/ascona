// code by jph
package ch.alpine.ascona.util.arp;

import java.awt.Graphics2D;

import ch.alpine.ascona.util.win.RenderInterface;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.chq.FiniteTensorQ;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.img.ColorFormat;

public record MeshRender(Tensor[][] array, ColorDataGradient colorDataGradient) implements RenderInterface {
  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    for (int i0 = 1; i0 < array.length; ++i0)
      for (int i1 = 1; i1 < array[i0].length; ++i1) {
        Tensor po = array[i0][i1];
        Tensor p0 = array[i0 - 1][i1];
        Tensor p1 = array[i0][i1 - 1];
        Tensor pd = array[i0 - 1][i1 - 1];
        if (FiniteTensorQ.of(po) && //
            FiniteTensorQ.of(p0) && //
            FiniteTensorQ.of(p1) && //
            FiniteTensorQ.of(pd)) {
          Scalar shading = QuadShading.ANGLE.map(po, p0, p1, pd);
          graphics.setColor(ColorFormat.toColor(colorDataGradient.apply(shading)));
          graphics.fill(geometricLayer.toPath2D(Unprotect.byRef(po, p0, pd, p1)));
          graphics.draw(geometricLayer.toPath2D(Unprotect.byRef(p0, po)));
          graphics.draw(geometricLayer.toPath2D(Unprotect.byRef(p1, po)));
        }
      }
  }
}
