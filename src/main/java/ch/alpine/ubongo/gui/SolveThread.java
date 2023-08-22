package ch.alpine.ubongo.gui;

import java.util.List;

import ch.alpine.ubongo.UbongoBoard;
import ch.alpine.ubongo.UbongoSolution;

class SolveThread extends Thread {
  public final UbongoBoard ubongoBoard;

  public SolveThread(UbongoBoard ubongoBoard, int use) {
    super(() -> {
      List<UbongoSolution> list = ubongoBoard.filter0(use);
      if (list.isEmpty()) {
        System.err.println("no solutions");
      } else {
        UbongoBrowser ubongoBrowser = new UbongoBrowser(ubongoBoard, list);
        ubongoBrowser.setVisible(800, 600);
      }
    });
    this.ubongoBoard = ubongoBoard;
    start();
  }

  public String getMessage() {
    return ubongoBoard.message;
  }

  public void cancel() {
    ubongoBoard.isRunning = false;
  }
}
