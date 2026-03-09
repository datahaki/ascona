// code by jph
package ch.alpine.ascona.dat.gok;

import java.util.List;

import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSelectionCallback;
import ch.alpine.bridge.ref.ann.ReflectionMarker;

@ReflectionMarker
public final class GokartPosParam {
  @ReflectionMarker
  public static List<String> keys() {
    return GokartPos.INSTANCE.keys();
  }

  @FieldSelectionCallback("keys")
  public String string;
  @FieldSelectionArray({ "100", "250", "500", "1000", "2000", "5000" })
  public Integer limit = 1000;

  public GokartPosParam() {
    String name = "tpq/20Hz/20180827T170643_5.csv";
    string = name; // keys().getFirst();
  }

  public PosHz getPosHz() {
    return GokartPos.INSTANCE.get(string, limit);
  }
}
