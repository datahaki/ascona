// code by jph
package ch.alpine.ascona.util.ren;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.Optional;
import java.util.stream.IntStream;

import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophus.hs.r2.SignedCurvature2D;
import ch.alpine.sophus.srf.SurfaceMesh;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.sca.Sign;

public class SurfaceMeshRender implements RenderInterface {
  private final SurfaceMesh surfaceMesh;

  public SurfaceMeshRender(SurfaceMesh surfaceMesh) {
    this.surfaceMesh = surfaceMesh;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D _g) {
    Graphics2D graphics = (Graphics2D) _g.create();
    for (int[] face : surfaceMesh.faces()) {
      Tensor polygon = Tensor.of(IntStream.of(face).mapToObj(surfaceMesh.vrt::get));
      Optional<Scalar> optional = SignedCurvature2D.of( //
          polygon.get(0).extract(0, 2), //
          polygon.get(1).extract(0, 2), //
          polygon.get(2).extract(0, 2));
      boolean ccw = optional.isPresent() && Sign.isPositive(optional.orElseThrow());
      if (ccw) {
        Path2D path2d = geometricLayer.toPath2D(polygon);
        path2d.closePath();
        graphics.setColor(ccw ? new Color(0, 255, 0, 64) : new Color(0, 0, 255, 64));
        graphics.fill(path2d);
        graphics.setColor(Color.BLACK);
        graphics.draw(path2d);
      }
    }
    graphics.dispose();
  }
}
