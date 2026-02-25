// adapted from chatgpt
package ch.alpine.ascona.usr;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

class SideTabsExample {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      JFrame frame = new JFrame("Side Tabs");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
      tabs.addTab("General", new JLabel("General Settings"));
      tabs.addTab("Advanced", new JLabel("Advanced Settings"));
      tabs.addTab("About", new JLabel("About Page"));
      frame.add(tabs);
      frame.setSize(400, 300);
      frame.setLocationRelativeTo(null);
      frame.setVisible(true);
    });
  }
}
