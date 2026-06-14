package game;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class CarcassonneLauncher extends JFrame {
    private final JComboBox<String> modeBox;
    private final JComboBox<String> playerBox;
    private final JTextField urlField;

    public CarcassonneLauncher() {
        super("Carcassonne - Accueil");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 320);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(244, 238, 223));
        setLayout(new BorderLayout(12, 12));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(250, 247, 239));
        content.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(224, 216, 195)),
            BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));

        JLabel title = new JLabel("Carcassonne");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        title.setAlignmentX(CENTER_ALIGNMENT);
        title.setForeground(new Color(118, 78, 54));

        JLabel subtitle = new JLabel("Choisissez un mode de jeu");
        subtitle.setAlignmentX(CENTER_ALIGNMENT);
        subtitle.setForeground(new Color(99, 90, 78));

        modeBox = new JComboBox<>(new String[] {"Local partage", "Reseau"});
        playerBox = new JComboBox<>(new String[] {"Alice", "Bob"});
        urlField = new JTextField("ws://localhost:3001");

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(18, 0, 18, 0));
        form.add(new JLabel("Mode"));
        form.add(modeBox);
        form.add(new JLabel("Joueur (reseau)"));
        form.add(playerBox);
        form.add(new JLabel("URL reflecteur"));
        form.add(urlField);

        JButton launchButton = new JButton("Lancer");
        launchButton.setAlignmentX(CENTER_ALIGNMENT);
        launchButton.addActionListener(e -> launchSelectedMode());

        modeBox.addActionListener(e -> refreshModeFields());
        refreshModeFields();

        content.add(title);
        content.add(subtitle);
        content.add(form);
        content.add(launchButton);

        add(content, BorderLayout.CENTER);
    }

    private void refreshModeFields() {
        boolean reseau = "Reseau".equals(modeBox.getSelectedItem());
        playerBox.setEnabled(reseau);
        urlField.setEnabled(reseau);
    }

    private void launchSelectedMode() {
        String mode = (String) modeBox.getSelectedItem();
        if ("Local partage".equals(mode)) {
            dispose();
            CarcassonneGUI.launch();
            return;
        }

        String player = (String) playerBox.getSelectedItem();
        String url = urlField.getText().trim();
        if (url.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Entrez une URL de reflecteur valide.");
            return;
        }

        dispose();
        CarcassonneNetworkGUI.launch(url, player);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new CarcassonneLauncher().setVisible(true);
        });
    }
}
