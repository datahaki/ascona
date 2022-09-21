// code by jph
package ch.alpine.ascona.util.dis;

import java.util.Arrays;
import java.util.List;

import ch.alpine.ascona.util.arp.D2Raster;
import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.sophus.hs.MetricManifold;

// TODO ASCONA rename to SpaceDisplay(s) etc.
public enum ManifoldDisplays {
  Se2ClA(Se2ClothoidDisplay.ANALYTIC),
  Se2ClL(Se2ClothoidDisplay.LEGENDRE),
  Se2CCl(Se2CoveringClothoidDisplay.INSTANCE),
  Se2C(Se2CoveringDisplay.INSTANCE),
  R2S1A(R2S1ADisplay.INSTANCE),
  R2S1B(R2S1BDisplay.INSTANCE),
  Se2(Se2Display.INSTANCE),
  Spd2(Spd2Display.INSTANCE),
  R2(R2Display.INSTANCE),
  S1(S1Display.INSTANCE),
  S2(S2Display.INSTANCE),
  Rp2(Rp2Display.INSTANCE),
  H1(H1Display.INSTANCE),
  H2(H2Display.INSTANCE),
  So3(So3Display.INSTANCE),
  He1(He1Display.INSTANCE),
  T1d(T1dDisplay.INSTANCE);

  private final ManifoldDisplay manifoldDisplay;

  ManifoldDisplays(ManifoldDisplay manifoldDisplay) {
    this.manifoldDisplay = manifoldDisplay;
  }

  public ManifoldDisplay manifoldDisplay() {
    return manifoldDisplay;
  }

  public GeodesicSpace geodesicSpace() {
    return manifoldDisplay().geodesicSpace();
  }

  public static final List<ManifoldDisplays> ALL = List.of(values());

  // ---
  /** requires biinvariant() */
  public static List<ManifoldDisplays> metricManifolds() {
    return Arrays.stream(values()) //
        .filter(md -> md.geodesicSpace() instanceof MetricManifold) //
        .toList();
  }

  // ---
  /** manifolds */
  public static List<ManifoldDisplays> manifolds() {
    return Arrays.stream(values()) //
        .filter(md -> md.geodesicSpace() instanceof Manifold) //
        .toList();
  }

  /** homogeneous spaces (have biinvariant mean) */
  public static List<ManifoldDisplays> homogeneousSpaces() {
    return Arrays.stream(values()) //
        .filter(md -> md.geodesicSpace() instanceof HomogeneousSpace) //
        .toList();
  }

  // ---
  /** implement {@link D2Raster} */
  public static List<ManifoldDisplays> d2Rasters() {
    return Arrays.stream(values()) //
        .filter(md -> md.manifoldDisplay() instanceof D2Raster) //
        .toList();
  }

  /** implement {@link D2Raster} */
  public static List<ManifoldDisplays> metricD2Rasters() {
    return Arrays.stream(values()) //
        .filter(md -> md.manifoldDisplay() instanceof D2Raster) //
        .filter(md -> md.geodesicSpace() instanceof MetricManifold) //
        .toList();
  }

  // ---
  public static final List<ManifoldDisplays> R2_ONLY = List.of( //
      R2);
  public static final List<ManifoldDisplays> R2_S2 = List.of( //
      R2, //
      S2);
  public static final List<ManifoldDisplays> R2_H2_S2_SE2C = List.of( //
      R2, //
      H2, //
      S2, //
      Se2C);
  // ---
  public static final List<ManifoldDisplays> SE2C_R2 = List.of( //
      Se2C, //
      R2);
  // ---
  public static final List<ManifoldDisplays> SE2_ONLY = List.of( //
      Se2);
  public static final List<ManifoldDisplays> SE2C_ONLY = List.of( //
      Se2C);
  // ---
  public static final List<ManifoldDisplays> SE2_R2 = List.of( //
      Se2, //
      R2);
  // ---
  public static final List<ManifoldDisplays> SE2C_SE2_R2 = List.of( //
      Se2C, //
      Se2, //
      R2);
  // ---
  public static final List<ManifoldDisplays> SE2C_SE2 = List.of( //
      Se2C, //
      Se2);
  // ---
  public static final List<ManifoldDisplays> S2_ONLY = List.of( //
      S2);
  public static final List<ManifoldDisplays> H2_ONLY = List.of( //
      H2);
  public static final List<ManifoldDisplays> CL_ONLY = List.of( //
      Se2ClA, //
      Se2ClL);
  public static final List<ManifoldDisplays> CLC_ONLY = List.of( //
      Se2C, //
      Se2ClA, //
      Se2ClL);
}
