// adapted from chatgpt
package ch.alpine.ascona.usr;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

class CleanTabSelector {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      JFrame frame = new JFrame("Tab Selector");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setLayout(new BorderLayout());
      // --- real content panel ---
      JPanel content = new JPanel();
      content.add(new JLabel("Mode A"));
      // --- tab selector ---
      JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
      // dummy components (never shown)
      tabs.addTab("A", new JPanel());
      tabs.addTab("B", new JPanel());
      tabs.addTab("C", new JPanel());
      // remove content border & focus border
      tabs.setUI(new BasicTabbedPaneUI() {
        @Override
        protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
          // do nothing
        }

        @Override
        protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect,
            boolean isSelected) {
          // no focus rectangle
        }
      });
      // keep it narrow
      tabs.setPreferredSize(new Dimension(60, 0));
      // change content manually
      tabs.addChangeListener(e -> {
        int i = tabs.getSelectedIndex();
        content.removeAll();
        content.add(new JLabel("Mode " + tabs.getTitleAt(i)));
        content.revalidate();
        content.repaint();
      });
      frame.add(tabs, BorderLayout.WEST);
      frame.add(content, BorderLayout.CENTER);
      frame.setSize(500, 350);
      frame.setLocationRelativeTo(null);
      frame.setVisible(true);
    });
  }
}
