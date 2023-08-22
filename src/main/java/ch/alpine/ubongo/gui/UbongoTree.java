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
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Dimensions;
import ch.alpine.tensor.io.ImageFormat;
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
      // System.err.println("TRUNC " + param.index);
      param.index = 0;
      fieldsEditor(0).updateJComponents();
    }
    List<UbongoEntry> solution = param.getSolution();
    {
      int scale = 30;
      List<Integer> size = Dimensions.of(param.ubongoBoards.board().mask());
      Tensor tensor = UbongoRender.of(size, solution);
      int pix = 50;
      int piy = 120;
      graphics.drawImage(ImageFormat.of(tensor), pix, piy, size.get(1) * scale, size.get(0) * scale, null);
    }
    int pix = 0;
    for (UbongoEntry ubongoEntry : solution) {
      Tensor tensor = UbongoRender.of(ubongoEntry.ubongoPiece());
      List<Integer> size = Dimensions.of(tensor);
      int scale = 15;
      int piw = size.get(1) * scale;
      graphics.drawImage(ImageFormat.of(tensor), 30 + pix, 30, piw, size.get(0) * scale, null);
      pix += piw + 20;
    }
  }

  @Override
  public void run() {
    param.update();
  }

  public static void main(String[] args) {
    launch();
  }
}
