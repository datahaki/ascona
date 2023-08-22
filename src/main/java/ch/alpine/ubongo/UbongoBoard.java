// code by jph
package ch.alpine.ubongo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.alg.Dimensions;
import ch.alpine.tensor.alg.Flatten;
import ch.alpine.tensor.ext.Lists;
import ch.alpine.tensor.io.Primitives;
import ch.alpine.tensor.mat.MatrixQ;

public class UbongoBoard {
  public static final Scalar FREE = RealScalar.ONE.negate();
  public static final int free = -1;

  private static record Pnt(int i, int j) {
  }

  public static UbongoBoard of(String... strings) {
    // System.out.println("---");
    final int n = strings[0].length();
    Tensor prep = Tensors.empty();
    for (String string : strings) {
      Tensor row = Array.zeros(n);
      // if (n != string.length())
      // System.err.println(strings);
      for (int index = 0; index < string.length(); ++index)
        if (string.charAt(index) == 'o')
          row.set(FREE, index);
      prep.append(row);
    }
    return new UbongoBoard(prep);
  }

  public static UbongoBoard of(Tensor prep) {
    return new UbongoBoard(prep.copy());
  }

  private final Tensor mask;
  private final int dim1;
  private final int[] _mask;
  private final List<Integer> board_size;
  private final int count;
  private final Map<UbongoStamp, List<Pnt>> map = new HashMap<>();

  private UbongoBoard(Tensor prep) {
    mask = MatrixQ.require(prep).unmodifiable();
    board_size = Dimensions.of(prep);
    dim1 = board_size.get(1);
    count = (int) Flatten.stream(mask, -1).filter(FREE::equals).count();
    _mask = Primitives.toIntArray(Flatten.of(mask));
    // ---
    for (UbongoPiece ubongo : UbongoPiece.values())
      for (UbongoStamp ubongoStamp : ubongo.stamps()) {
        map.put(ubongoStamp, new ArrayList<>());
        Tensor stamp = ubongoStamp.stamp;
        List<Integer> size = Dimensions.of(stamp);
        for (int bi = 0; bi <= board_size.get(0) - size.get(0); ++bi)
          for (int bj = 0; bj <= board_size.get(1) - size.get(1); ++bj) {
            boolean status = true;
            for (int si = 0; si < size.get(0); ++si)
              for (int sj = 0; sj < size.get(1); ++sj) {
                boolean occupied = stamp.get(si, sj).equals(RealScalar.ONE);
                if (occupied)
                  status &= prep.get(bi + si, bj + sj).equals(FREE);
              }
            if (status)
              map.get(ubongoStamp).add(new Pnt(bi, bj));
          }
      }
  }

  public Tensor mask() {
    return mask;
  }

  public List<List<UbongoEntry>> filter0(int use) {
    List<List<UbongoPiece>> values = Candidates.candidates(use, count);
    List<List<UbongoEntry>> solutions = new LinkedList<>();
    for (List<UbongoPiece> list : values) {
      List<UbongoPiece> _list = new ArrayList<>(list);
      Collections.sort(_list, (u1, u2) -> Integer.compare(u2.count(), u1.count()));
      System.out.println(_list);
      Solve solve = new Solve(_list);
      if (solve.solutions.size() == 1) {
        List<UbongoEntry> list2 = solve.solutions.get(0);
        solutions.add(list2);
        System.out.println(list2);
      }
    }
    return solutions;
  }

  class Solve {
    private final List<List<UbongoEntry>> solutions = new LinkedList<>();

    public Solve(List<UbongoPiece> list) {
      solve(_mask.clone(), list, Collections.emptyList());
    }

    private void solve(int[] board, List<UbongoPiece> list, List<UbongoEntry> entries) {
      if (list.isEmpty()) {
        solutions.add(entries);
      } else {
        final UbongoPiece ubongo = list.get(0); // piece
        for (UbongoStamp ubongoStamp : ubongo.stamps()) {
          List<Pnt> points = map.get(ubongoStamp);
          Tensor stamp = ubongoStamp.stamp;
          for (Pnt point : points) {
            int bi = point.i();
            int bj = point.j();
            int[] nubrd = board.clone();
            boolean status = true;
            for (int si = 0; si < ubongoStamp.size0; ++si)
              for (int sj = 0; sj < ubongoStamp.size1; ++sj) {
                boolean occupied = stamp.get(si, sj).equals(RealScalar.ONE);
                if (occupied) {
                  int index = (bi + si) * dim1 + bj + sj;
                  if (nubrd[index] == free) {
                    nubrd[index] = 0;
                  } else {
                    status = false;
                    break;
                  }
                }
              }
            // ---
            if (status) {
              UbongoEntry ubongoEntry = new UbongoEntry(bi, bj, ubongo, stamp);
              List<UbongoEntry> arrayList = new ArrayList<>(entries);
              arrayList.add(ubongoEntry);
              solve(nubrd, Lists.rest(list), arrayList);
            }
          }
        }
      }
    }
  }
}
