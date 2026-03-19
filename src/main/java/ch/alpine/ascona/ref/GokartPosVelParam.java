// code by jph
package ch.alpine.ascona.ref;

import java.util.List;

import ch.alpine.ascona.dat.GokartPosVel;
import ch.alpine.ascony.dat.Se2PosVelHz;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSelectionCallback;
import ch.alpine.bridge.ref.ann.ReflectionMarker;

@ReflectionMarker
public final class GokartPosVelParam {
  @ReflectionMarker
  public static List<String> keys() {
    return GokartPosVel.INSTANCE.keys();
  }

  @FieldSelectionCallback("keys")
  public String string;
  @FieldSelectionArray({ "100", "250", "500", "1000", "2000", "5000" })
  public Integer limit = 1000;

  public GokartPosVelParam() {
    string = keys().getFirst();
  }

  public Se2PosVelHz getPosVelHz() {
    return GokartPosVel.INSTANCE.get(string, limit);
  }
}
