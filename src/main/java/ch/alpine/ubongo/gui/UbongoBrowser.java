// code by jph
package ch.alpine.ubongo.gui;

import java.awt.Graphics2D;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldSelectionCallback;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.swing.LookAndFeels;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.ubongo.UbongoBoard;
import ch.alpine.ubongo.UbongoBoards;
import ch.alpine.ubongo.UbongoLoader;
import ch.alpine.ubongo.UbongoSolution;

public class UbongoBrowser extends AbstractDemo {
  private final UbongoBoard ubongoBoard;
  private final List<UbongoSolution> list;

  @ReflectionMarker
  public static class Param {
    private final int limit;

    public Param(int limit) {
      this.limit = limit;
    }

    @FieldSelectionCallback("index")
    public Integer index = 0;

    public List<Scalar> index() {
      return IntStream.range(0, limit).mapToObj(RealScalar::of).toList();
    }
  }

  private final Param param;

  public UbongoBrowser(UbongoBoard ubongoBoard, List<UbongoSolution> list) {
    this(new Param(list.size()), ubongoBoard, list);
  }

  public UbongoBrowser(Param param, UbongoBoard ubongoBoard, List<UbongoSolution> list) {
    super(param);
    this.param = param;
    this.ubongoBoard = Objects.requireNonNull(ubongoBoard);
    this.list = Objects.requireNonNull(list);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    StaticHelper.drawBoard(graphics, ubongoBoard, list.get(param.index).list());
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.updateComponentTreeUI();
    UbongoBoards ubongoBoards = UbongoBoards.LETTERH1;
    List<UbongoSolution> list = // ubongoBoards.solve();
        UbongoLoader.INSTANCE.load(ubongoBoards);
    if (list.isEmpty()) {
      System.err.println("no solutions");
    } else {
      UbongoBrowser ubongoBrowser = new UbongoBrowser(ubongoBoards.board(), list);
      ubongoBrowser.setVisible(800, 600);
    }
  }
}
