// code by jph
package ch.alpine.ascona.util.dat;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.alpine.bridge.lang.SI;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.ext.ResourceData;
import ch.alpine.tensor.io.Import;

/** Columns:
 * time
 * px
 * py
 * pangle
 * quality
 * vx
 * vy
 * vangle */
public class GokartPoseDataV2 implements GokartPoseData {
  public static final String PATH_FOLDER = "/dubilab/app/tpqv50";
  public static final String PATH_VECTOR = PATH_FOLDER + ".vector";
  private static final List<String> LIST = Collections.unmodifiableList(ResourceData.lines(PATH_VECTOR));
  // ---
  /** all available */
  public static final GokartPoseDataV2 INSTANCE = new GokartPoseDataV2(LIST);
  /** 20190701 */
  public static final GokartPoseDataV2 RACING_DAY = new GokartPoseDataV2(LIST.stream() //
      .filter(string -> string.startsWith("20190701")) //
      .toList());
  // ---
  private final List<String> list;

  private GokartPoseDataV2(List<String> list) {
    this.list = list;
  }

  @Override // from GokartPoseData
  public List<String> list() {
    return list;
  }

  @Override // from GokartPoseData
  public Tensor getPose(String name, int limit) {
    Objects.requireNonNull(name);
    // String string = PATH_FOLDER + "/" + name + ".csv";
    // System.out.println(string);
    return Tensor.of(Import.of(PATH_FOLDER + "/" + name + ".csv").stream() //
        .limit(limit) //
        .map(row -> row.extract(1, 4)));
  }

  @Override // from GokartPoseData
  public Scalar getSampleRate() {
    return SI.PER_SECOND.quantity(50);
  }

  /** @param name
   * @param limit
   * @return tensor with at most given limit entries of the form {{px, py, pangle}, {vx, vy, omega}} */
  public Tensor getPoseVel(String name, int limit) {
    return Tensor.of(Import.of(PATH_FOLDER + "/" + name + ".csv").stream() //
        .limit(limit) //
        .map(row -> Tensors.of(row.extract(1, 4), row.extract(5, 8))));
  }
}
