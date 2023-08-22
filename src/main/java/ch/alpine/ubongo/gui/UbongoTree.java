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
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.ubongo.UbongoBoards;
import ch.alpine.ubongo.UbongoEntry;
import ch.alpine.ubongo.UbongoLoader;

public class UbongoTree extends AbstractDemo implements Runnable {
  @ReflectionMarker
  public static class Param {
    public UbongoBoards ubongoBoards = UbongoBoards.AIRPLAN1;
    private List<List<UbongoEntry>> list;
    @FieldSelectionCallback("index")
    public Integer index = 0;

    public List<Scalar> index() {
      return Objects.isNull(list) //
          ? List.of()
          : IntStream.range(0, list.size()).mapToObj(RealScalar::of).toList();
    }

    public void update() {
      list = UbongoLoader.INSTANCE.load(ubongoBoards);
    }

    public List<UbongoEntry> getSolution() {
      return list.get(index);
    }
  }

  private final Param param;

  public UbongoTree() {
    this(new Param());
  }

  public UbongoTree(Param param) {
    super(param);
    this.param = param;
    param.update();
    fieldsEditor(0).addUniversalListener(this);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    if (param.list.size() <= param.index) {
      param.index = 0;
      fieldsEditor(0).updateJComponents();
    }
    StaticHelper.drawBoard(graphics, param.ubongoBoards.board(), param.getSolution());
  }

  @Override
  public void run() {
    param.update();
  }

  public static void main(String[] args) {
    launch();
  }
}
