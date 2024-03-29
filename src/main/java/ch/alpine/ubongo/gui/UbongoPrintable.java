// code by jph
package ch.alpine.ubongo.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.List;

import ch.alpine.ubongo.UbongoPublish;

/* package */ class UbongoPrintable implements Printable {
  private final int scale;
  private final double factor;
  private final List<UbongoPublish> list;

  public UbongoPrintable(List<UbongoPublish> list, int scale, double factor) {
    this.scale = scale;
    this.factor = factor;
    this.list = list;
  }

  @Override
  public int print(Graphics _g, PageFormat pageFormat, int pageIndex) throws PrinterException {
    Graphics2D graphics = (Graphics2D) _g.create();
    graphics.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
    graphics.scale(factor, factor);
    boolean exists = pageIndex < Math.min(list.size(), 200);
    if (exists)
      StaticHelper.draw(graphics, list.get(pageIndex), scale);
    graphics.dispose();
    return exists //
        ? Printable.PAGE_EXISTS
        : Printable.NO_SUCH_PAGE;
  }

  @Override
  public String toString() {
    return String.format("ubongo%2d_%04d", scale, (int) (factor * 1000));
  }

  public static void main(String[] args) {
    List<UbongoPublish> list2 = List.of(UbongoPublish.KIRCH12);
    for (double factor : new double[] { 1.0 }) // , 0.995, 0.990943
      for (int scale : new int[] { 45 }) { // , 46, 47, 48
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        PageFormat pageFormat = printerJob.defaultPage();
        System.out.println(pageFormat);
        Paper paper = pageFormat.getPaper();
        System.out.println(paper);
        paper.setImageableArea(0.5 * 72, 0.5 * 72, 7 * 72, 10.5 * 72);
        // paper.setSize(11.7 * 72, 8.3 * 72);
        pageFormat.setPaper(paper);
        pageFormat.setOrientation(PageFormat.PORTRAIT);
        UbongoPrintable ubongoPrintable = new UbongoPrintable(list2, scale, factor);
        printerJob.setPrintable(ubongoPrintable, pageFormat);
        printerJob.setJobName(ubongoPrintable.toString());
        try {
          printerJob.print();
        } catch (PrinterException printerException) {
          printerException.printStackTrace();
        }
      }
  }
}
