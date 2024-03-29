// code by jph
package ch.alpine.ubongo;

import java.awt.Color;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.alg.Flatten;
import ch.alpine.tensor.alg.NestList;
import ch.alpine.tensor.alg.Reverse;
import ch.alpine.tensor.img.ColorFormat;
import ch.alpine.tensor.img.ImageRotate;

/** 12 different pieces */
public enum UbongoPiece {
  A0(new Color(154, 68, 41), "xx"),
  A1(new Color(62, 121, 87), "xx", "x"),
  A2(new Color(219, 96, 28), "xx", "xx"),
  A3(new Color(46, 96, 98), "xx ", " xx"),
  A4(new Color(233, 197, 55), "xx ", " x", " xx"),
  B0(new Color(61, 150, 147), "xxx"),
  B1(new Color(195, 211, 87), "xxx", "x"),
  B2(new Color(225, 222, 47), "xxx", " x"),
  B3(new Color(56, 100, 39), "xxx", "xx"),
  C0(new Color(204, 148, 25), "xxxx"),
  C1(new Color(245, 137, 90), "xxxx", "x"),
  C2(new Color(247, 174, 59), "xxxx", " x");

  private final Color color;
  private final Tensor mask;
  private final int count;
  private final Set<UbongoStamp> set;

  private UbongoPiece(Color color, String... strings) {
    this.color = color;
    final int n = strings[0].length();
    Tensor prep = Tensors.empty();
    for (String string : strings) {
      Tensor row = Array.zeros(n);
      for (int index = 0; index < string.length(); ++index)
        if (string.charAt(index) == 'x')
          row.set(RealScalar.ONE, index);
      prep.append(row);
    }
    mask = prep.unmodifiable();
    count = (int) Flatten.stream(prep, -1).filter(RealScalar.ONE::equals).count();
    // ---
    Tensor rotated = NestList.of(ImageRotate::of, mask, 4);
    set = Stream.concat( //
        rotated.stream(), //
        rotated.stream().map(Reverse::of)) //
        .distinct() //
        .map(UbongoStamp::new) //
        .collect(Collectors.toSet());
  }

  public Tensor mask() {
    return mask;
  }

  public int count() {
    return count;
  }

  public Set<UbongoStamp> stamps() {
    return Collections.unmodifiableSet(set);
  }

  public Tensor colorVector() {
    return ColorFormat.toVector(color);
  }

  public int variationCount() {
    return set.size();
  }
}
