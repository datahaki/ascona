// code by jph
package ch.alpine.ubongo.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import ch.alpine.ascona.util.api.Box2D;
import ch.alpine.ascona.util.ren.AxesRender;
import ch.alpine.ascona.util.ren.GridRender;
import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.alg.ArrayPad;
import ch.alpine.tensor.alg.Dimensions;
import ch.alpine.tensor.alg.Flatten;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.img.ImageCrop;
import ch.alpine.tensor.io.Export;
import ch.alpine.tensor.io.Import;
import ch.alpine.tensor.io.Pretty;
import ch.alpine.tensor.sca.Floor;
import ch.alpine.ubongo.Candidates;
import ch.alpine.ubongo.UbongoBoard;
import ch.alpine.ubongo.UbongoBoards;
import ch.alpine.ubongo.UbongoPiece;

public class UbongoDesigner extends AbstractDemo implements Runnable {
  private static final File FILE = RESOURCE_LOCATOR.file(UbongoDesigner.class.getSimpleName() + ".csv");
  public static final Scalar FREE = UbongoBoard.FREE;
  static final Collector<CharSequence, ?, String> EMBRACE = //
      Collectors.joining("", "\"", "\"");
  static final Collector<CharSequence, ?, String> EMBRACE2 = //
      Collectors.joining(", ", "", "");

  private static String rowToString(Tensor row) {
    return row.stream().map(s -> s.equals(FREE) ? "o" : " ").collect(EMBRACE);
  }

  @ReflectionMarker
  public static class Param {
    @FieldClip(min = "1", max = "12")
    public Integer num = 4;
    @FieldFuse
    public Boolean solve = false;
    @FieldFuse
    public Boolean reset = false;
  }

  private final Param param;
  private final GridRender gridRender;
  private Tensor template = Array.zeros(10, 11);
  private SolveThread solveThread = null;

  public UbongoDesigner() {
    this(new Param());
  }

  public UbongoDesigner(Param param) {
    super(param);
    this.param = param;
    fieldsEditor(0).addUniversalListener(this);
    {
      try {
        template = Import.of(FILE);
      } catch (Exception e) {
        System.err.println("does not exist: " + FILE);
      }
    }
    if (false) {
      Tensor temp = UbongoBoards.FINALBOS.board().mask();
      List<Integer> list = Dimensions.of(temp);
      template = ArrayPad.of(temp, List.of(0, 0), List.of(10 - list.get(0), 11 - list.get(1)));
    }
    // ---
    Tensor matrix = Tensors.fromString("{{30, 0, 100}, {0, -30, 500}, {0, 0, 1}}");
    matrix = matrix.dot(GfxMatrix.of(Tensors.vector(0, 0, -Math.PI / 2)));
    timerFrame.geometricComponent.setModel2Pixel(matrix);
    timerFrame.geometricComponent.setOffset(100, 100);
    int row_max = template.length();
    int col_max = Unprotect.dimension1(template);
    gridRender = new GridRender(Subdivide.of(0, row_max, row_max), Subdivide.of(0, col_max, col_max));
    timerFrame.geometricComponent.jComponent.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == 1) {
          Tensor xya = timerFrame.geometricComponent.getMouseSe2CState().map(Floor.FUNCTION);
          int row = xya.Get(0).number().intValue();
          int col = xya.Get(1).number().intValue();
          if (0 <= row && row < row_max)
            if (0 <= col && col < col_max) {
              boolean free = template.get(row, col).equals(FREE);
              template.set(free ? RealScalar.ZERO : FREE, row, col);
            }
        }
      }
    });
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    AxesRender.INSTANCE.render(geometricLayer, graphics);
    RenderQuality.setQuality(graphics);
    graphics.setColor(Color.DARK_GRAY);
    int dimension1 = Unprotect.dimension1(template);
    for (int row = 0; row < template.length(); ++row) {
      for (int col = 0; col < dimension1; ++col) {
        Scalar scalar = template.Get(row, col);
        if (!scalar.equals(FREE)) {
          geometricLayer.pushMatrix(GfxMatrix.translation(Tensors.vector(row, col)));
          Path2D path2d = geometricLayer.toPath2D(Box2D.SQUARE, true);
          graphics.fill(path2d);
          geometricLayer.popMatrix();
        }
      }
    }
    gridRender.render(geometricLayer, graphics);
    {
      int count = (int) Flatten.stream(template, -1).filter(FREE::equals).count();
      graphics.setColor(Color.DARK_GRAY);
      graphics.drawString("free=" + count, 0, 30);
      List<List<UbongoPiece>> candidates = Candidates.candidates(param.num, count);
      if (candidates.size() == 0) {
        graphics.setColor(Color.RED);
        graphics.drawString("CANDIDATE SET EMPTY", 0, 50);
      } else {
        graphics.drawString("comb=" + candidates.size(), 0, 50);
      }
    }
    SolveThread _solveThread = solveThread;
    if (Objects.nonNull(_solveThread)) {
      if (_solveThread.isAlive()) {
        graphics.drawString("computing: " + _solveThread.getMessage(), 0, 80);
      } else {
        // System.out.println("finished thread");
        solveThread = null;
      }
    }
  }

  @Override
  public void run() {
    if (param.reset) {
      param.reset = false;
      template.set(Scalar::zero, Tensor.ALL, Tensor.ALL);
    }
    if (param.solve) {
      param.solve = false;
      if (Objects.isNull(solveThread)) {
        Export.wrap(FILE, template);
        Tensor result = ImageCrop.eq(RealScalar.ZERO).apply(template);
        int use = param.num;
        String collect = result.stream().map(UbongoDesigner::rowToString).collect(EMBRACE2);
        System.out.println("=".repeat(32));
        System.out.printf("UNTITLED(%d, %s),\n", use, collect);
        System.out.println(Pretty.of(result));
        solveThread = new SolveThread(UbongoBoard.of(result), use);
      } else {
        solveThread.cancel();
        System.out.println("cancel issued");
      }
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
