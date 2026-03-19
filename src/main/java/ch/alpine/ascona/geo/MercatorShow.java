// code by jph
package ch.alpine.ascona.geo;

import java.awt.Container;

import ch.alpine.bridge.fig.PlotOption;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.ShowGridComponent;
import ch.alpine.bridge.fig.plt.Plot;
import ch.alpine.bridge.geo.Tile;
import ch.alpine.bridge.geo.TilePixel;
import ch.alpine.bridge.pro.ManipulateProvider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;
import ch.alpine.tensor.sca.Floor;

@ReflectionMarker
class MercatorShow implements ManipulateProvider {
  public Integer z = 3;

  @Override
  public Container getContainer() {
    Show show1 = new Show();
    {
      ScalarUnaryOperator suo = s -> RealScalar.of(TilePixel.from(z, s, RealScalar.ZERO).absY());
      show1.add(Plot.of(suo, TilePixel.LAT_DOMAIN, PlotOption.STRICT));
    }
    Show show2 = new Show();
    {
      Clip interval = Clips.interval(0, Tile.maxExclusive(z) * 256 - 1);
      ScalarUnaryOperator suo = s -> TilePixel.of(z, 0, Floor.intValueExact(s)).lat_lon().Get(0);
      show2.add(Plot.of(suo, interval, PlotOption.STRICT));
    }
    return ShowGridComponent.of(show1, show2);
  }

  static void main() {
    new MercatorShow().runStandalone();
  }
}
