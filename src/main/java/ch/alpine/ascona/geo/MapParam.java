// code by jph
package ch.alpine.ascona.geo;

import ch.alpine.bridge.geo.TilePixel;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;

@ReflectionMarker
class MapParam {
  public Tensor center = Tensors.fromString("{0,256,{0,0}}");

  public TilePixel prod() {
    return TilePixel.from( //
        center.Get(0).number().intValue(), //
        center.Get(1).number().intValue(), //
        center.get(2));
  }

  public void read(TilePixel tilePixel) {
    center = Tensors.of( //
        RealScalar.of(tilePixel.tile().z()), //
        RealScalar.of(tilePixel.wid()), //
        tilePixel.lon_lat());
  }
}
