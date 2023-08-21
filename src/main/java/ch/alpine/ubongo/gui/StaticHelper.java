// code by jph
package ch.alpine.ubongo.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.io.ResourceLocator;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Dimensions;
import ch.alpine.tensor.ext.HomeDirectory;
import ch.alpine.tensor.ext.ResourceData;
import ch.alpine.tensor.img.ImageRotate;
import ch.alpine.ubongo.Ubongo;
import ch.alpine.ubongo.UbongoBoard;
import ch.alpine.ubongo.UbongoEntry;
import ch.alpine.ubongo.UbongoLoader;
import ch.alpine.ubongo.UbongoPublish;

/* package */ enum StaticHelper {
  ;
  public static final ResourceLocator RESOURCE_LOCATOR = //
      new ResourceLocator(HomeDirectory.Documents(Ubongo.class.getSimpleName()));
  private static final int MARGIN_Y = 13;
  // 61.1465
  private static final int ZCALE = 7;
  private static final Color FILL = Color.LIGHT_GRAY;

  public static void draw(Graphics2D graphics, UbongoPublish ubongoPublish, int SCALE) {
    int piy = MARGIN_Y;
    {
      List<List<UbongoEntry>> solutions = UbongoLoader.INSTANCE.load(ubongoPublish.ubongoBoards);
      int count = 0;
      for (int index : ubongoPublish.list) {
        BufferedImage bufferedImage = ResourceData.bufferedImage("/ch/alpine/ubongo/dice" + count + ".png");
        ++count;
        graphics.setColor(Color.DARK_GRAY);
        int pix = 60;
        {
          Graphics2D g = (Graphics2D) graphics.create();
          RenderQuality.setQuality(g);
          int piw = 5 * ZCALE;
          g.drawImage(bufferedImage, 2, piy - 5, piw, piw, null);
          g.dispose();
        }
        List<UbongoEntry> solution = solutions.get(index);
        for (UbongoEntry ubongoEntry : solution) {
          UbongoEntry ubongoPiece = new UbongoEntry(0, 0, ubongoEntry.ubongo(), ImageRotate.cw(ubongoEntry.ubongo().mask()));
          List<Integer> size = Dimensions.of(ubongoPiece.stamp());
          int piw = size.get(1) * ZCALE;
          int scale = ZCALE;
          Tensor mask = ubongoPiece.stamp();
          graphics.setColor(FILL);
          for (int row = 0; row < size.get(0); ++row)
            for (int col = 0; col < size.get(1); ++col) {
              Scalar scalar = mask.Get(row, col);
              if (Scalars.nonZero(scalar))
                graphics.fillRect(pix + col * scale, piy + row * scale, scale, scale);
            }
          pix += piw + 2 * ZCALE;
        }
        piy += 4 * ZCALE + 2 * ZCALE;
      }
    }
    {
      UbongoBoard ubongoBoard = ubongoPublish.ubongoBoards.board();
      Tensor mask = ubongoBoard.mask();
      int scale = SCALE;
      int marginX = 0;
      int marginY = piy + scale;
      graphics.setColor(FILL);
      List<Integer> size = Dimensions.of(mask);
      for (int row = 0; row < size.get(0); ++row)
        for (int col = 0; col < size.get(1); ++col) {
          Scalar scalar = mask.Get(row, col);
          if (Scalars.nonZero(scalar))
            graphics.fillRect(marginX + col * scale, marginY + row * scale, scale - 1, scale - 1);
        }
    }
  }
}
