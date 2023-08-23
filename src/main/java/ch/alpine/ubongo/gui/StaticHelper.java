// code by jph
package ch.alpine.ubongo.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Dimensions;
import ch.alpine.tensor.ext.ResourceData;
import ch.alpine.tensor.img.ImageRotate;
import ch.alpine.tensor.io.ImageFormat;
import ch.alpine.ubongo.UbongoBoard;
import ch.alpine.ubongo.UbongoEntry;
import ch.alpine.ubongo.UbongoLoader;
import ch.alpine.ubongo.UbongoPiece;
import ch.alpine.ubongo.UbongoPublish;
import ch.alpine.ubongo.UbongoSolution;

/* package */ enum StaticHelper {
  ;
  private static final int MARGIN_Y = 13;
  // 61.1465
  private static final int ZCALE = 7;
  private static final Color FILL = Color.LIGHT_GRAY;
  private static final String[] STARS = { "\u2729", "\u272a", "\u272b", "\u272c", "\u272d", "\u272e", "\u272f", "\u2730" };

  public static void draw(Graphics2D graphics, UbongoPublish ubongoPublish, int SCALE) {
    int piy = MARGIN_Y;
    {
      List<UbongoSolution> solutions = UbongoLoader.INSTANCE.load(ubongoPublish.ubongoBoards);
      int count = 0;
      for (int index : ubongoPublish.list) {
        BufferedImage bufferedImage = ResourceData.bufferedImage("/ch/alpine/ubongo/dice" + count + ".png");
        ++count;
        graphics.setColor(Color.DARK_GRAY);
        int pix = 60;
        String star = STARS[Math.floorMod(ubongoPublish.hashCode(), STARS.length)];
        {
          Graphics2D g = (Graphics2D) graphics.create();
          RenderQuality.setQuality(g);
          int piw = 5 * ZCALE;
          g.drawImage(bufferedImage, 2, piy - 5, piw, piw, null);
          g.dispose();
        }
        UbongoSolution ubongoSolution = solutions.get(index);
        for (UbongoEntry ubongoEntry : ubongoSolution.list()) {
          Tensor mask = ImageRotate.cw(ubongoEntry.ubongoPiece().mask());
          List<Integer> size = Dimensions.of(mask);
          int piw = size.get(1) * ZCALE;
          int scale = ZCALE;
          graphics.setColor(FILL);
          for (int row = 0; row < size.get(0); ++row)
            for (int col = 0; col < size.get(1); ++col) {
              Scalar scalar = mask.Get(row, col);
              if (Scalars.nonZero(scalar))
                graphics.fillRect(pix + col * scale, piy + row * scale, scale, scale);
            }
          pix += piw + 2 * ZCALE;
        }
        {
          Graphics2D g = (Graphics2D) graphics.create();
          RenderQuality.setQuality(g);
          int pjx = 60 + (ubongoSolution.list().size() * 4 + 2) * ZCALE;
          int size = 3 * ZCALE;
          int pjy = piy + size - (int) (0.5 * ZCALE);
          g.setFont(new Font(Font.DIALOG, Font.PLAIN, size));
          double log10 = Math.log10(ubongoSolution.search());
          FontMetrics fontMetrics = g.getFontMetrics();
          int width = fontMetrics.stringWidth(star);
          {
            int a = 192 + 32 + 16;
            g.setColor(new Color(a, a, a));
          }
          g.drawString(star.repeat(5), pjx, pjy);
          g.setClip(pjx, piy, (int) (log10 * width), 5 * ZCALE);
          {
            int a = 192 - 16;
            g.setColor(new Color(a, a, a));
          }
          g.drawString(star.repeat(5), pjx, pjy);
          g.dispose();
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

  public static void drawBoard(Graphics2D graphics, UbongoBoard ubongoBoard, List<UbongoEntry> solution) {
    {
      int scale = 30;
      List<Integer> size = Dimensions.of(ubongoBoard.mask());
      Tensor tensor = UbongoRender.of(size, solution);
      int pix = 50;
      int piy = 120;
      graphics.drawImage(ImageFormat.of(tensor), pix, piy, size.get(1) * scale, size.get(0) * scale, null);
    }
    int pix = 0;
    for (UbongoEntry ubongoEntry : solution) {
      UbongoPiece ubongoPiece = ubongoEntry.ubongoPiece();
      Tensor tensor = UbongoRender.of(ubongoPiece);
      List<Integer> size = Dimensions.of(tensor);
      int scale = 15;
      int piw = size.get(1) * scale;
      graphics.drawImage(ImageFormat.of(tensor), 30 + pix, 30, piw, size.get(0) * scale, null);
      pix += piw + 20;
    }
  }
}
