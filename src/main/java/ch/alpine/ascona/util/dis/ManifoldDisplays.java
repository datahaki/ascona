// code by jph
package ch.alpine.ascona.util.dis;

import java.util.List;

// TODO ASCONA filter candidates dynamically for properties
public enum ManifoldDisplays {
  Se2ClA(Se2ClothoidDisplay.ANALYTIC), //
  Se2ClL(Se2ClothoidDisplay.LEGENDRE), //
  Se2CCl(Se2CoveringClothoidDisplay.INSTANCE), //
  Se2C(Se2CoveringDisplay.INSTANCE), //
  R2S1A(R2S1ADisplay.INSTANCE), //
  R2S1B(R2S1BDisplay.INSTANCE), //
  Se2(Se2Display.INSTANCE), //
  Spd2(Spd2Display.INSTANCE), //
  R2(R2Display.INSTANCE), //
  S1(S1Display.INSTANCE), //
  S2(S2Display.INSTANCE), //
  H1(H1Display.INSTANCE), //
  H2(H2Display.INSTANCE), //
  So3(So3Display.INSTANCE), //
  He1(He1Display.INSTANCE), //
  Dt1(Dt1Display.INSTANCE);

  private final ManifoldDisplay manifoldDisplay;

  private ManifoldDisplays(ManifoldDisplay manifoldDisplay) {
    this.manifoldDisplay = manifoldDisplay;
  }

  public ManifoldDisplay manifoldDisplay() {
    return manifoldDisplay;
  }

  public static final List<ManifoldDisplay> ALL = List.of( //
      Se2ClothoidDisplay.ANALYTIC, //
      Se2ClothoidDisplay.LEGENDRE, //
      Se2CoveringClothoidDisplay.INSTANCE, //
      Se2CoveringDisplay.INSTANCE, //
      R2S1ADisplay.INSTANCE, //
      R2S1BDisplay.INSTANCE, //
      Se2Display.INSTANCE, //
      Spd2Display.INSTANCE, //
      R2Display.INSTANCE, //
      S1Display.INSTANCE, //
      S2Display.INSTANCE, //
      H1Display.INSTANCE, //
      H2Display.INSTANCE, //
      So3Display.INSTANCE, //
      He1Display.INSTANCE, //
      Dt1Display.INSTANCE);
  // ---
  /** requires biinvariant() */
  public static final List<ManifoldDisplay> METRIC = List.of( //
      Spd2Display.INSTANCE, //
      R2Display.INSTANCE, //
      S1Display.INSTANCE, //
      S2Display.INSTANCE, //
      H1Display.INSTANCE, //
      H2Display.INSTANCE, //
      So3Display.INSTANCE);
  // ---
  /** homogeneous spaces (have biinvariant mean) */
  public static final List<ManifoldDisplay> MANIFOLDS = List.of( //
      Se2CoveringDisplay.INSTANCE, //
      Se2Display.INSTANCE, //
      Spd2Display.INSTANCE, //
      R2Display.INSTANCE, //
      S1Display.INSTANCE, //
      S2Display.INSTANCE, //
      H1Display.INSTANCE, //
      H2Display.INSTANCE, //
      So3Display.INSTANCE, //
      He1Display.INSTANCE, //
      Dt1Display.INSTANCE);
  // ---
  /** have array */
  public static final List<ManifoldDisplay> ARRAYS = List.of( //
      R2Display.INSTANCE, //
      H2Display.INSTANCE, //
      S2Display.INSTANCE, //
      Rp2Display.INSTANCE);
  // ---
  public static final List<ManifoldDisplay> R2_ONLY = List.of( //
      R2Display.INSTANCE);
  public static final List<ManifoldDisplays> l_R2_ONLY = List.of( //
      R2);
  public static final List<ManifoldDisplay> R2_S2 = List.of( //
      R2Display.INSTANCE, //
      S2Display.INSTANCE);
  // ---
  public static final List<ManifoldDisplay> SE2C_R2 = List.of( //
      Se2CoveringDisplay.INSTANCE, //
      R2Display.INSTANCE);
  // ---
  public static final List<ManifoldDisplay> SE2_ONLY = List.of( //
      Se2Display.INSTANCE);
  public static final List<ManifoldDisplays> l_SE2_ONLY = List.of( //
      Se2);
  // ---
  public static final List<ManifoldDisplay> SE2_R2 = List.of( //
      Se2Display.INSTANCE, //
      R2Display.INSTANCE);
  public static final List<ManifoldDisplays> l_SE2_R2 = List.of( //
      Se2, //
      R2);
  // ---
  public static final List<ManifoldDisplay> SE2C_SE2_R2 = List.of( //
      Se2CoveringDisplay.INSTANCE, //
      Se2Display.INSTANCE, //
      R2Display.INSTANCE);
  // ---
  public static final List<ManifoldDisplay> SE2C_SE2 = List.of( //
      Se2CoveringDisplay.INSTANCE, //
      Se2Display.INSTANCE);
  public static final List<ManifoldDisplays> l_SE2C_SE2 = List.of( //
      Se2C, //
      Se2);
  // ---
  public static final List<ManifoldDisplay> S2_ONLY = List.of( //
      S2Display.INSTANCE);
  public static final List<ManifoldDisplay> H2_ONLY = List.of( //
      H2Display.INSTANCE);
  public static final List<ManifoldDisplay> CL_ONLY = List.of( //
      Se2ClothoidDisplay.ANALYTIC, //
      Se2ClothoidDisplay.LEGENDRE //
  );
  public static final List<ManifoldDisplay> CLC_ONLY = List.of( //
      Se2CoveringClothoidDisplay.INSTANCE, //
      Se2ClothoidDisplay.ANALYTIC, //
      Se2ClothoidDisplay.LEGENDRE //
  );
}
