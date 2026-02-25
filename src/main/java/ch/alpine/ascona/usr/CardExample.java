// adapted from chatgpt
package ch.alpine.ascona.usr;

import java.awt.BorderLayout;
import java.awt.CardLayout;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class CardExample {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      JFrame frame = new JFrame("CardLayout Demo");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      // --- Card panel ---
      JPanel cards = new JPanel(new CardLayout());
      cards.add(new JLabel("Mode A", SwingConstants.CENTER), "A");
      cards.add(new JLabel("Mode B", SwingConstants.CENTER), "B");
      cards.add(new JLabel("Mode C", SwingConstants.CENTER), "C");
      CardLayout layout = (CardLayout) cards.getLayout();
      // --- Selector buttons ---
      JRadioButton a = new JRadioButton("A");
      JRadioButton b = new JRadioButton("B");
      JRadioButton c = new JRadioButton("C");
      ButtonGroup group = new ButtonGroup();
      group.add(a);
      group.add(b);
      group.add(c);
      a.addActionListener(e -> layout.show(cards, "A"));
      b.addActionListener(e -> layout.show(cards, "B"));
      c.addActionListener(e -> layout.show(cards, "C"));
      a.setSelected(true);
      JPanel top = new JPanel();
      top.add(a);
      top.add(b);
      top.add(c);
      frame.add(top, BorderLayout.NORTH);
      frame.add(cards, BorderLayout.CENTER);
      frame.setSize(400, 300);
      frame.setLocationRelativeTo(null);
      frame.setVisible(true);
    });
  }
}
