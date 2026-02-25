// adapted from chatgpt
package ch.alpine.ascona.usr;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

class TabSelectorOnly {
  public static JPanel empty() {
    JPanel jPanel = new JPanel();
    jPanel.setPreferredSize(new Dimension());
    return jPanel;
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      JFrame frame = new JFrame("Tab Selector");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      // --- shared content ---
      JPanel content = new JPanel();
      content.add(new JLabel("Mode A"));
      // --- tab strip ---
      JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
      tabs.addTab("A", empty()); // dummy
      tabs.addTab("B", new JPanel(null));
      tabs.addTab("C", empty());
      // tabs.addChangeListener(_ -> {
      // int index = tabs.getSelectedIndex();
      // content.removeAll();
      // switch (index) {
      // case 0 -> content.add(new JLabel("Mode A"));
      // case 1 -> content.add(new JLabel("Mode B"));
      // case 2 -> content.add(new JLabel("Mode C"));
      // }
      // content.revalidate();
      // content.repaint();
      // });
      tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
      frame.setLayout(new BorderLayout());
      frame.add(tabs, BorderLayout.WEST);
      frame.add(content, BorderLayout.CENTER);
      frame.setSize(400, 300);
      frame.setLocationRelativeTo(null);
      frame.setVisible(true);
    });
  }
}
